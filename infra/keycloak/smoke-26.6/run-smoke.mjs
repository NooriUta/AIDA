#!/usr/bin/env node
// U0 SMOKE TEST — verify oidc-organization-group-membership-mapper in KC 26.6
//
// Setup:
//   docker compose -f infra/keycloak/smoke-26.6/docker-compose.smoke.yml up -d
//   node infra/keycloak/smoke-26.6/run-smoke.mjs
//
// Tests:
//   1. KC reachable on http://localhost:18181/kc
//   2. Realm "smoke" + org-enabled
//   3. Client "smoke-app" with Auth Code + PKCE + ROPC
//   4. Mapper "oidc-organization-group-membership-mapper" attached
//   5. User alice in org "acme", in org-group "editor"
//   6. ROPC token → check organization claim
//   7. Auth Code (PKCE) flow simulated via curl-equivalent → check organization.acme.groups

import crypto from 'node:crypto';

const KC = 'http://localhost:18181/kc';
const REALM = 'smoke';
const CLIENT_ID = 'smoke-app';
const CLIENT_SECRET = 'smoke-secret';
const REDIRECT_URI = 'http://localhost:9999/callback';
const USER = 'alice';
const USER_PASS = 'alice123';
const ORG_ALIAS = 'acme';
const ORG_NAME = 'Acme Corp';
const GROUP_NAME = 'editor';

const c = { reset:'\x1b[0m', cyan:'\x1b[36m', green:'\x1b[32m', red:'\x1b[31m', yellow:'\x1b[33m' };
const log  = m => console.error(`${c.cyan}[smoke]${c.reset} ${m}`);
const ok   = m => console.error(`${c.green}[ok]${c.reset} ${m}`);
const warn = m => console.error(`${c.yellow}[warn]${c.reset} ${m}`);
const fail = m => { console.error(`${c.red}[FAIL]${c.reset} ${m}`); process.exit(1); };

const sleep = ms => new Promise(r => setTimeout(r, ms));

async function http(method, url, { headers = {}, body = null, raw = false, allowFail = false, redirect = 'manual' } = {}) {
  const opts = { method, headers, redirect };
  if (body !== null) opts.body = body;
  let res;
  try { res = await fetch(url, opts); }
  catch (e) { if (allowFail) return { error: e.message }; throw e; }
  if (raw) return res;
  const text = await res.text();
  if (!res.ok && !allowFail) {
    throw new Error(`${method} ${url} → ${res.status}: ${text.slice(0, 400)}`);
  }
  return { status: res.status, text, json: text ? safeJson(text) : null, headers: res.headers };
}
function safeJson(s) { try { return JSON.parse(s); } catch { return null; } }

function decodeJwt(token) {
  const [, payload] = token.split('.');
  const padded = payload + '='.repeat((4 - payload.length % 4) % 4);
  const json = Buffer.from(padded.replace(/_/g, '/').replace(/-/g, '+'), 'base64').toString('utf8');
  return JSON.parse(json);
}

// ─── 0. Wait for KC ready ───────────────────────────────────────────
// Health endpoint is on management port 9000 (not exposed). Use openid config on main port.
log(`Waiting for KC at ${KC}...`);
for (let i = 0; i < 60; i++) {
  const r = await http('GET', `${KC}/realms/master/.well-known/openid-configuration`, { allowFail: true });
  if (r.status === 200) { ok('KC ready'); break; }
  if (i === 59) fail('KC never became ready');
  await sleep(2000);
}

// ─── 1. Get admin token ─────────────────────────────────────────────
log('Getting admin token...');
const adminTokenResp = await http('POST', `${KC}/realms/master/protocol/openid-connect/token`, {
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  body: new URLSearchParams({
    client_id: 'admin-cli',
    username: 'admin',
    password: 'admin',
    grant_type: 'password',
  }),
});
const ADMIN_TOKEN = adminTokenResp.json.access_token;
if (!ADMIN_TOKEN) fail('admin token empty');
ok('Admin token obtained');

const api = (method, path, body = null) =>
  http(method, `${KC}/admin/realms${path}`, {
    headers: {
      'Authorization': `Bearer ${ADMIN_TOKEN}`,
      'Content-Type': 'application/json',
    },
    body: body ? JSON.stringify(body) : null,
  });

// ─── 2. Realm "smoke" — idempotent (delete + recreate) ──────────────
log(`Resetting realm ${REALM}...`);
await http('DELETE', `${KC}/admin/realms/${REALM}`, {
  headers: { 'Authorization': `Bearer ${ADMIN_TOKEN}` },
  allowFail: true,
});
await sleep(500);
await api('POST', '', {
  realm: REALM,
  enabled: true,
  organizationsEnabled: true,
  sslRequired: 'none',
  accessTokenLifespan: 600,
  // Use legacy theme for smoke (server-rendered form, easier to script)
  loginTheme: 'keycloak',
});
ok(`Realm ${REALM} created`);

// ─── 3. Client smoke-app ────────────────────────────────────────────
log(`Creating client ${CLIENT_ID}...`);
await api('POST', `/${REALM}/clients`, {
  clientId: CLIENT_ID,
  secret: CLIENT_SECRET,
  enabled: true,
  protocol: 'openid-connect',
  clientAuthenticatorType: 'client-secret',
  standardFlowEnabled: true,
  directAccessGrantsEnabled: true,
  publicClient: false,
  redirectUris: [REDIRECT_URI],
  webOrigins: ['+'],
  attributes: { 'pkce.code.challenge.method': 'S256' },
});
const clientsResp = await api('GET', `/${REALM}/clients?clientId=${CLIENT_ID}`);
const CLIENT_UUID = clientsResp.json[0].id;
ok(`Client uuid=${CLIENT_UUID}`);

// ─── 4. Add mappers ──────────────────────────────────────────────────
log('Adding oidc-organization-group-membership-mapper...');
const orgGroupMapperResp = await api('POST', `/${REALM}/clients/${CLIENT_UUID}/protocol-mappers/models`, {
  name: 'org-group-mapper',
  protocol: 'openid-connect',
  protocolMapper: 'oidc-organization-group-membership-mapper',
  consentRequired: false,
  config: {
    'id.token.claim': 'true',
    'access.token.claim': 'true',
    'userinfo.token.claim': 'true',
    'introspection.token.claim': 'true',
  },
});
ok(`org-group mapper attached (status ${orgGroupMapperResp.status})`);

log('Adding oidc-organization-membership-mapper (parent org claim)...');
const orgMapperResp = await http('POST', `${KC}/admin/realms/${REALM}/clients/${CLIENT_UUID}/protocol-mappers/models`, {
  headers: { 'Authorization': `Bearer ${ADMIN_TOKEN}`, 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'org-mapper',
    protocol: 'openid-connect',
    protocolMapper: 'oidc-organization-membership-mapper',
    consentRequired: false,
    config: {
      'id.token.claim': 'true',
      'access.token.claim': 'true',
      'userinfo.token.claim': 'true',
    },
  }),
  allowFail: true,
});
if (orgMapperResp.status >= 200 && orgMapperResp.status < 300) ok('org mapper attached');
else warn(`org mapper attach status=${orgMapperResp.status} (may be auto-attached via scope in 26.6)`);

// ─── 5. User alice ──────────────────────────────────────────────────
log(`Creating user ${USER}...`);
await api('POST', `/${REALM}/users`, {
  username: USER,
  email: 'alice@acme.test',
  firstName: 'Alice',
  lastName: 'Test',
  enabled: true,
  emailVerified: true,
  credentials: [{ type: 'password', value: USER_PASS, temporary: false }],
});
const usersResp = await api('GET', `/${REALM}/users?username=${USER}`);
const USER_ID = usersResp.json[0].id;
ok(`User id=${USER_ID}`);

// ─── 6. Org acme + add alice ────────────────────────────────────────
log(`Creating organization ${ORG_ALIAS}...`);
await api('POST', `/${REALM}/organizations`, {
  name: ORG_NAME,
  alias: ORG_ALIAS,
  enabled: true,
  domains: [{ name: 'acme.test', verified: true }],
});
const orgsResp = await api('GET', `/${REALM}/organizations?search=${ORG_ALIAS}`);
const ORG_ID = orgsResp.json[0].id;
ok(`Org id=${ORG_ID}`);

log(`Adding ${USER} as org member...`);
await http('POST', `${KC}/admin/realms/${REALM}/organizations/${ORG_ID}/members`, {
  headers: { 'Authorization': `Bearer ${ADMIN_TOKEN}`, 'Content-Type': 'application/json' },
  body: JSON.stringify(USER_ID),
});
ok('Member added');

// ─── 7. Org-group editor + add alice ────────────────────────────────
log(`Creating org-group ${GROUP_NAME} inside ${ORG_ALIAS}...`);
const groupCreateResp = await http('POST', `${KC}/admin/realms/${REALM}/organizations/${ORG_ID}/groups`, {
  headers: { 'Authorization': `Bearer ${ADMIN_TOKEN}`, 'Content-Type': 'application/json' },
  body: JSON.stringify({ name: GROUP_NAME }),
  allowFail: true,
});
if (groupCreateResp.status < 200 || groupCreateResp.status >= 300) {
  warn(`Org-group create returned ${groupCreateResp.status}: ${groupCreateResp.text.slice(0, 200)}`);
  warn('Trying alternative endpoint /groups (realm-level)...');
  // KC may not have org-scoped /groups endpoint; try realm groups + parent
  const realmGroupResp = await api('POST', `/${REALM}/groups`, { name: GROUP_NAME, attributes: { 'kc.org': [ORG_ID] } });
  warn(`Realm group fallback status=${realmGroupResp.status}`);
}

const orgGroupsResp = await http('GET', `${KC}/admin/realms/${REALM}/organizations/${ORG_ID}/groups`, {
  headers: { 'Authorization': `Bearer ${ADMIN_TOKEN}` },
  allowFail: true,
});
let GROUP_ID = null;
if (orgGroupsResp.status === 200 && Array.isArray(orgGroupsResp.json)) {
  const g = orgGroupsResp.json.find(x => x.name === GROUP_NAME);
  if (g) GROUP_ID = g.id;
}
if (!GROUP_ID) {
  warn(`Org-group not found via /organizations/${ORG_ID}/groups. Status=${orgGroupsResp.status}`);
  warn('Trying realm-level groups search...');
  const allGroups = await api('GET', `/${REALM}/groups?search=${GROUP_NAME}`);
  if (allGroups.json && allGroups.json.length) {
    GROUP_ID = allGroups.json[0].id;
    warn(`Found via realm groups: id=${GROUP_ID}`);
  }
}
if (!GROUP_ID) fail('Could not find org-group anywhere');
ok(`Org-group id=${GROUP_ID}`);

log(`Adding ${USER} to org-group via Organization API...`);
// KC 26.6: org-groups must use org-scoped endpoint, not /users/{id}/groups
// PUT /organizations/{orgId}/groups/{groupId}/members/{userId}
await api('PUT', `/${REALM}/organizations/${ORG_ID}/groups/${GROUP_ID}/members/${USER_ID}`);
ok('User added to org-group');

// ─── 8. ROPC test ────────────────────────────────────────────────────
log('===== ROPC FLOW =====');
const ropcResp = await http('POST', `${KC}/realms/${REALM}/protocol/openid-connect/token`, {
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  body: new URLSearchParams({
    grant_type: 'password',
    client_id: CLIENT_ID,
    client_secret: CLIENT_SECRET,
    username: USER,
    password: USER_PASS,
    scope: 'openid organization',
  }),
});
const ropcToken = ropcResp.json.access_token;
if (!ropcToken) fail(`ROPC failed: ${ropcResp.text.slice(0, 300)}`);
const ropcClaims = decodeJwt(ropcToken);
console.log('--- ROPC JWT claims ---');
console.log(JSON.stringify(ropcClaims, null, 2));

// ─── 9. Auth Code + PKCE flow ────────────────────────────────────────
log('===== AUTH CODE + PKCE FLOW =====');

const verifier = crypto.randomBytes(32).toString('base64url');
const challenge = crypto.createHash('sha256').update(verifier).digest('base64url');
const state = crypto.randomBytes(8).toString('hex');

// Step 9a: GET /auth — capture login form
const authUrl = `${KC}/realms/${REALM}/protocol/openid-connect/auth?` + new URLSearchParams({
  response_type: 'code',
  client_id: CLIENT_ID,
  redirect_uri: REDIRECT_URI,
  scope: 'openid organization',
  state,
  code_challenge: challenge,
  code_challenge_method: 'S256',
});
const cookieJar = new Map(); // name -> value
const captureCookies = (headers) => {
  // Use getSetCookie() for proper multi-Set-Cookie handling (Node 18.14+/22+)
  const setCookies = typeof headers.getSetCookie === 'function'
    ? headers.getSetCookie()
    : [headers.get('set-cookie')].filter(Boolean);
  for (const sc of setCookies) {
    const [pair] = sc.split(';');
    const eq = pair.indexOf('=');
    if (eq > 0) {
      const name = pair.slice(0, eq).trim();
      const value = pair.slice(eq + 1).trim();
      cookieJar.set(name, value);
    }
  }
};
const cookiesAsHeader = () => Array.from(cookieJar.entries()).map(([k, v]) => `${k}=${v}`).join('; ');

log('GET /auth (login form)...');
const authPageResp = await fetch(authUrl, { redirect: 'manual' });
captureCookies(authPageResp.headers);
log(`Captured ${cookieJar.size} cookies: ${Array.from(cookieJar.keys()).join(', ')}`);
const authPageHtml = await authPageResp.text();
const formActionMatch = authPageHtml.match(/<form[^>]*id=["']kc-form-login["'][^>]*action="([^"]+)"/i)
  || authPageHtml.match(/<form[^>]*action="([^"]+)"[^>]*method=["']post["']/i);
if (!formActionMatch) {
  console.log('Auth page (first 2000 chars):');
  console.log(authPageHtml.slice(0, 2000));
  fail('Login form action not found');
}
const formAction = formActionMatch[1].replace(/&amp;/g, '&');
log(`Form action: ${formAction.slice(0, 120)}...`);

// Extract all hidden inputs from the login form (KC modern theme has tab_id, etc.)
const hiddenInputs = {};
const hiddenRe = /<input[^>]*type=["']hidden["'][^>]*>/gi;
const nameRe = /name=["']([^"']+)["']/i;
const valueRe = /value=["']([^"']*)["']/i;
let m;
while ((m = hiddenRe.exec(authPageHtml)) !== null) {
  const tag = m[0];
  const n = nameRe.exec(tag);
  const v = valueRe.exec(tag);
  if (n) hiddenInputs[n[1]] = v ? v[1] : '';
}
log(`Hidden inputs: ${JSON.stringify(Object.keys(hiddenInputs))}`);

// Step 9b: POST username (KC 26.6 has split user-then-password flow by default)
log('POST username to form...');
let formResp = await fetch(formAction, {
  method: 'POST',
  redirect: 'manual',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
    'Cookie': cookiesAsHeader(),
  },
  body: new URLSearchParams({ ...hiddenInputs, username: USER }),
});
captureCookies(formResp.headers);
let location = formResp.headers.get('location');
log(`username step status=${formResp.status}, location=${location ? location.slice(0,140) : '(none)'}`);

// If username step returns 200 with new form (password step), parse it and submit password
if (formResp.status === 200) {
  const passwordPageHtml = await formResp.text();
  const passwordFormMatch = passwordPageHtml.match(/<form[^>]*id=["']kc-form-login["'][^>]*action="([^"]+)"/i)
    || passwordPageHtml.match(/<form[^>]*action="([^"]+)"[^>]*method=["']post["']/i);
  if (!passwordFormMatch) fail('Password form not found after username step');
  const passwordFormAction = passwordFormMatch[1].replace(/&amp;/g, '&');
  log(`Password form action: ${passwordFormAction.slice(0, 120)}...`);
  log('POST password...');
  formResp = await fetch(passwordFormAction, {
    method: 'POST',
    redirect: 'manual',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Cookie': cookiesAsHeader(),
    },
    body: new URLSearchParams({ password: USER_PASS, credentialId: '' }),
  });
  captureCookies(formResp.headers);
  location = formResp.headers.get('location');
  log(`password step status=${formResp.status}, location=${location ? location.slice(0,140) : '(none)'}`);
}

if (![302, 303].includes(formResp.status) || !location) {
  const body = await formResp.text();
  // Look for error message in alert/feedback
  const errMatch = body.match(/<span[^>]*kc-feedback-text[^>]*>([^<]+)</)
    || body.match(/<div[^>]*alert-error[^>]*>([\s\S]*?)<\/div>/)
    || body.match(/id=["']input-error["'][^>]*>([^<]+)/);
  if (errMatch) console.log('KC error message:', errMatch[1].replace(/\s+/g, ' ').trim());
  // Find the form to see actual fields/action
  const formStart = body.search(/<form[^>]*kc-form-login/i);
  if (formStart >= 0) {
    console.log('Form section:');
    console.log(body.slice(formStart, formStart + 1500));
  } else {
    console.log('Body sample:');
    console.log(body.slice(0, 2000));
  }
  fail('Login did not redirect to callback');
}
const codeMatch = location.match(/[?&]code=([^&]+)/);
if (!codeMatch) fail(`No code in redirect: ${location}`);
const code = decodeURIComponent(codeMatch[1]);
ok('Got authorization code');

// Step 9c: exchange code
log('POST /token with code + verifier...');
const tokenResp = await http('POST', `${KC}/realms/${REALM}/protocol/openid-connect/token`, {
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  body: new URLSearchParams({
    grant_type: 'authorization_code',
    client_id: CLIENT_ID,
    client_secret: CLIENT_SECRET,
    code,
    redirect_uri: REDIRECT_URI,
    code_verifier: verifier,
  }),
});
const authcodeToken = tokenResp.json?.access_token;
if (!authcodeToken) fail(`Auth code exchange failed: ${tokenResp.text.slice(0, 400)}`);
const authcodeClaims = decodeJwt(authcodeToken);
console.log('--- AUTH CODE JWT claims ---');
console.log(JSON.stringify(authcodeClaims, null, 2));

// ─── 10. Verdict ─────────────────────────────────────────────────────
console.log('');
log('===== VERDICT =====');

const ropcHasOrg = !!ropcClaims.organization;
const authcodeHasOrg = !!authcodeClaims.organization;

console.log(`ROPC      → has organization claim: ${ropcHasOrg}`);
console.log(`Auth Code → has organization claim: ${authcodeHasOrg}`);

const extractGroups = c => {
  if (!c.organization) return null;
  if (Array.isArray(c.organization)) return c.organization;
  // Format: { "acme": { "id": "...", "groups": [...] } }
  const orgKey = Object.keys(c.organization)[0];
  return c.organization[orgKey]?.groups ?? null;
};

const ropcGroups = extractGroups(ropcClaims);
const authcodeGroups = extractGroups(authcodeClaims);
console.log(`ROPC      → org-groups: ${JSON.stringify(ropcGroups)}`);
console.log(`Auth Code → org-groups: ${JSON.stringify(authcodeGroups)}`);

if (authcodeHasOrg && authcodeGroups && authcodeGroups.length > 0) {
  ok('🟢 PIVOT GREEN: oidc-organization-group-membership-mapper triggers in Auth Code flow');
  console.log('');
  console.log('Decision: proceed with Phase 1-2 as planned (Option F core path)');
  process.exit(0);
} else if (authcodeHasOrg && !authcodeGroups) {
  warn('🟡 PARTIAL: organization claim present but no groups — check mapper config / org-group membership');
  process.exit(2);
} else {
  fail('🔴 PIVOT RED: org claim missing in Auth Code → fallback to Option E (manual realm groups)');
}
