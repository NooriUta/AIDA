/**
 * Migration: legacy `organization.role` user attribute → KC 26.6 Organization Groups.
 *
 * For each user that has `organization.alias` + `organization.role` attributes:
 *   1. Resolve KC org by alias
 *   2. Ensure the org-group exists (create if needed)
 *   3. PUT user as member of that org-group
 *   4. (Phase 6) — separately delete the legacy attributes
 *
 * Idempotent + dry-run mode (DRY_RUN=true).
 *
 * Run:
 *   DRY_RUN=true  npx tsx scripts/migrations/2026-04-attr-to-org-groups.ts
 *   DRY_RUN=false npx tsx scripts/migrations/2026-04-attr-to-org-groups.ts
 */

const KC_BASE   = process.env.KC_URL    ?? 'http://localhost:18180/kc';
const KC_REALM  = process.env.KC_REALM  ?? 'seer';
const KC_USER   = process.env.KC_ADMIN_USER ?? 'admin';
const KC_PASS   = process.env.KC_ADMIN_PASS ?? 'admin';
const DRY_RUN   = process.env.DRY_RUN !== 'false'; // default safe = dry

const STANDARD_ROLES = ['viewer', 'editor', 'operator', 'auditor', 'local-admin', 'tenant-owner'];

interface KcUser {
  id:         string;
  username:   string;
  attributes?: Record<string, string[]>;
}
interface KcOrg {
  id:    string;
  alias: string;
}
interface KcGroup {
  id:   string;
  name: string;
}

async function adminToken(): Promise<string> {
  const res = await fetch(`${KC_BASE}/realms/master/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      client_id: 'admin-cli',
      grant_type: 'password',
      username: KC_USER,
      password: KC_PASS,
    }),
  });
  if (!res.ok) throw new Error(`admin token failed: ${res.status}`);
  const body = await res.json() as { access_token: string };
  return body.access_token;
}

function makeApi(token: string) {
  return async function api<T = unknown>(method: string, path: string, body?: unknown): Promise<T> {
    const res = await fetch(`${KC_BASE}/admin/realms${path}`, {
      method,
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: body ? JSON.stringify(body) : null,
    });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(`${method} ${path} → ${res.status}: ${text.slice(0, 300)}`);
    }
    if (res.status === 204) return undefined as T;
    const text = await res.text();
    return text ? JSON.parse(text) as T : undefined as T;
  };
}

async function main() {
  console.log(`[migrate] ${DRY_RUN ? 'DRY-RUN' : 'LIVE'} mode`);
  const token = await adminToken();
  const api   = makeApi(token);

  // Cache org + group lookups
  const orgByAlias = new Map<string, KcOrg>();
  const groupByOrgAndName = new Map<string, KcGroup>(); // key: `${orgId}/${name}`

  async function getOrCreateOrgGroup(org: KcOrg, groupName: string): Promise<KcGroup> {
    const cacheKey = `${org.id}/${groupName}`;
    if (groupByOrgAndName.has(cacheKey)) return groupByOrgAndName.get(cacheKey)!;
    // Try to find existing
    const existing = await api<KcGroup[]>('GET', `/${KC_REALM}/organizations/${org.id}/groups`);
    let group = existing.find(g => g.name === groupName);
    if (!group) {
      console.log(`[migrate]   creating group "${groupName}" in org ${org.alias}`);
      if (!DRY_RUN) {
        await api('POST', `/${KC_REALM}/organizations/${org.id}/groups`, { name: groupName });
        const reread = await api<KcGroup[]>('GET', `/${KC_REALM}/organizations/${org.id}/groups`);
        group = reread.find(g => g.name === groupName);
        if (!group) throw new Error(`group ${groupName} not found after create`);
      } else {
        group = { id: '<dry-run>', name: groupName };
      }
    }
    groupByOrgAndName.set(cacheKey, group);
    return group;
  }

  // 1. List all users (paginated)
  const allUsers: KcUser[] = [];
  let first = 0;
  const pageSize = 100;
  while (true) {
    const page = await api<KcUser[]>('GET', `/${KC_REALM}/users?first=${first}&max=${pageSize}&briefRepresentation=false`);
    allUsers.push(...page);
    if (page.length < pageSize) break;
    first += pageSize;
  }
  console.log(`[migrate] total users: ${allUsers.length}`);

  // 2. Cache organizations
  const orgs = await api<KcOrg[]>('GET', `/${KC_REALM}/organizations?max=1000`);
  for (const o of orgs) orgByAlias.set(o.alias, o);
  console.log(`[migrate] organizations: ${orgs.length}`);

  // 3. Process each user
  let processed = 0, skipped = 0, errors = 0;
  for (const u of allUsers) {
    const aliasArr = u.attributes?.['organization.alias'];
    const roleArr  = u.attributes?.['organization.role'];
    if (!aliasArr?.[0] || !roleArr?.[0]) { skipped++; continue; }
    const alias = aliasArr[0];
    const role  = roleArr[0];
    if (!STANDARD_ROLES.includes(role)) {
      console.warn(`[migrate]   user ${u.username}: unknown role "${role}", skipping`);
      skipped++;
      continue;
    }
    const org = orgByAlias.get(alias);
    if (!org) {
      console.warn(`[migrate]   user ${u.username}: org "${alias}" not found, skipping`);
      skipped++;
      continue;
    }
    try {
      const group = await getOrCreateOrgGroup(org, role);
      console.log(`[migrate] ${u.username} → ${alias}/${role} (group=${group.id})`);
      if (!DRY_RUN) {
        await api('PUT', `/${KC_REALM}/organizations/${org.id}/groups/${group.id}/members/${u.id}`);
      }
      processed++;
    } catch (e) {
      console.error(`[migrate] error for ${u.username}: ${(e as Error).message}`);
      errors++;
    }
  }

  console.log(`\n[migrate] DONE — processed=${processed} skipped=${skipped} errors=${errors}`);
  if (DRY_RUN) console.log('[migrate] (dry-run; rerun with DRY_RUN=false to apply)');
}

main().catch(e => {
  console.error('[migrate] FATAL:', e);
  process.exit(1);
});
