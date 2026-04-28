/**
 * Phase 6 cutover cleanup: remove legacy `organization.role` user attribute
 * after staging burn-in confirms org-groups model works.
 *
 * Keeps `organization.alias` (used by chur to determine primary tenant claim).
 *
 * Idempotent + dry-run mode.
 *
 * Run after:
 *   1. Migration 2026-04-attr-to-org-groups.ts completed (LIVE mode)
 *   2. Staging burn-in 24-48h with feature flag AUTH_GROUPS_PRIMARY=true
 *   3. Login success rate green, no fallback warnings in logs
 *
 * Then:
 *   DRY_RUN=true  npx tsx scripts/migrations/2026-04-cleanup-org-attrs.ts
 *   DRY_RUN=false npx tsx scripts/migrations/2026-04-cleanup-org-attrs.ts
 */

const KC_BASE  = process.env.KC_URL    ?? 'http://localhost:18180/kc';
const KC_REALM = process.env.KC_REALM  ?? 'seer';
const KC_USER  = process.env.KC_ADMIN_USER ?? 'admin';
const KC_PASS  = process.env.KC_ADMIN_PASS ?? 'admin';
const DRY_RUN  = process.env.DRY_RUN !== 'false';

interface KcUser {
  id:          string;
  username:    string;
  attributes?: Record<string, string[]>;
  [k: string]: unknown;
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
  return (await res.json() as { access_token: string }).access_token;
}

async function main() {
  console.log(`[cleanup] ${DRY_RUN ? 'DRY-RUN' : 'LIVE'} mode`);
  const token = await adminToken();

  let first = 0;
  const pageSize = 100;
  let removed = 0, untouched = 0;
  while (true) {
    const r = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/users?first=${first}&max=${pageSize}&briefRepresentation=false`,
      { headers: { Authorization: `Bearer ${token}` } },
    );
    if (!r.ok) throw new Error(`list users failed: ${r.status}`);
    const page = await r.json() as KcUser[];
    for (const u of page) {
      if (!u.attributes?.['organization.role']) { untouched++; continue; }
      console.log(`[cleanup] ${u.username}: removing organization.role attribute`);
      if (!DRY_RUN) {
        const updated = { ...u, attributes: { ...u.attributes } };
        delete updated.attributes!['organization.role'];
        const upd = await fetch(`${KC_BASE}/admin/realms/${KC_REALM}/users/${u.id}`, {
          method: 'PUT',
          headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
          body: JSON.stringify(updated),
        });
        if (!upd.ok) {
          console.error(`[cleanup] failed for ${u.username}: ${upd.status}`);
          continue;
        }
      }
      removed++;
    }
    if (page.length < pageSize) break;
    first += pageSize;
  }
  console.log(`\n[cleanup] DONE — removed=${removed} untouched=${untouched}`);
}

main().catch(e => { console.error('[cleanup] FATAL:', e); process.exit(1); });
