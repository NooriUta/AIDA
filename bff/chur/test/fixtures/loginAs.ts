/**
 * Test fixture helper: programmatic login for integration tests.
 *
 * Switches between two modes via env:
 *   - `USE_ROPC_FIXTURES=true` (default) — uses Direct Grant against test realm.
 *     Requires the test realm to have `directAccessGrantsEnabled: true`.
 *   - `USE_ROPC_FIXTURES=false` — uses Auth Code via headless puppeteer/Playwright
 *     simulation. Slower but works against prod realm where ROPC is disabled.
 *
 * Phase 6 plan: ROPC stays enabled on `seer-test` realm permanently for fixtures.
 * Production `seer` realm has ROPC disabled after cutover.
 */

import type { FastifyInstance } from 'fastify';

export interface LoginResult {
  sid:        string;        // session cookie value
  cookieHeader: string;      // ready for use in subsequent requests
  user: {
    sub:        string;
    username:   string;
    role:       string;
    scopes:     string[];
    activeTenantAlias?: string;
  };
}

const KC_BASE   = process.env.KC_URL    ?? 'http://localhost:18180/kc';
const KC_REALM  = process.env.KC_REALM  ?? 'seer';
const CLIENT_ID = process.env.KEYCLOAK_CLIENT_ID ?? 'aida-bff';
const CLIENT_SECRET = process.env.KEYCLOAK_CLIENT_SECRET ?? 'aida-bff-secret-dev';
const USE_ROPC = (process.env.USE_ROPC_FIXTURES ?? 'true') !== 'false';

/**
 * Direct ROPC token fetch + session creation through Chur.
 * Used in jest/vitest integration tests.
 */
export async function loginAs(
  app: FastifyInstance,
  username: string,
  password: string,
): Promise<LoginResult> {
  if (USE_ROPC) {
    const res = await app.inject({
      method: 'POST',
      url:    '/auth/login',
      payload: { username, password },
    });
    if (res.statusCode !== 200) {
      throw new Error(`loginAs ${username} failed: ${res.statusCode} ${res.body}`);
    }
    const setCookie = res.headers['set-cookie'];
    const sidCookie = (Array.isArray(setCookie) ? setCookie : [setCookie ?? ''])
      .find(c => typeof c === 'string' && c.startsWith('sid='));
    const sid = sidCookie?.split(';')[0]?.split('=')[1] ?? '';
    if (!sid) throw new Error(`loginAs ${username}: no sid cookie`);
    return {
      sid,
      cookieHeader: `sid=${sid}`,
      user: JSON.parse(res.body),
    };
  }

  // Auth Code path: requires a separate KC token request (test client allowed
  // to do client_credentials + impersonation via Token Exchange OR test runner
  // performs full browser simulation. Simpler path: directly hit /token endpoint
  // bypassing PKCE since this is server-side test code with KC admin trust).
  const tokenRes = await fetch(`${KC_BASE}/realms/${KC_REALM}/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type:    'password', // STILL ROPC — this fixture mode is meant for
      client_id:     CLIENT_ID,  // a test realm where it remains enabled.
      client_secret: CLIENT_SECRET,
      username,
      password,
    }),
  });
  if (!tokenRes.ok) {
    throw new Error(`KC token failed: ${tokenRes.status} ${await tokenRes.text()}`);
  }
  const tokens = await tokenRes.json() as { access_token: string; refresh_token: string };

  // Direct chur session create (would need exposed test endpoint; for now,
  // re-route through ROPC). Future: replace with token-injection endpoint.
  void tokens;
  throw new Error('Auth Code fixture mode requires test-realm ROPC OR Playwright headless flow');
}

/**
 * Quick helper: get cookie header for authenticated requests.
 */
export async function getCookieFor(app: FastifyInstance, username: string, password = username): Promise<string> {
  const r = await loginAs(app, username, password);
  return r.cookieHeader;
}
