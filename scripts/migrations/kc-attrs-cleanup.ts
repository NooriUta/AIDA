#!/usr/bin/env -S tsx
/**
 * MTN-67 — Post-migration Keycloak attributes{} cleanup.
 *
 * After MTN-66 (kc-attrs-to-frigg-users.ts) has migrated all profile /
 * preferences / notifications / quotas / source_bindings data to
 * FRIGG frigg-users, this script clears the same keys out of each KC user's
 * attributes{}. Reserved KC-internal attributes are preserved:
 *   - organization.alias   (KC-ORG-03 identity binding)
 *   - locale                (KC UI locale)
 *
 * Run sequence:
 *   1. scripts/migrations/kc-attrs-to-frigg-users.ts  (MTN-66)
 *   2. chur reader paths switched to FriggUserRepository (code change)
 *   3. THIS script  (MTN-67)
 *
 * Idempotent: re-running on already-clean users is a no-op.
 *
 * Usage:
 *   DRY_RUN=1 KC_URL=... npx tsx scripts/migrations/kc-attrs-cleanup.ts
 *   KC_URL=... npx tsx scripts/migrations/kc-attrs-cleanup.ts
 *
 * Output: lists per-user count of keys removed.
 */

import * as process from 'node:process';

const KC_URL        = process.env.KC_URL        ?? 'http://localhost:18180/kc';
const KC_REALM      = process.env.KC_REALM      ?? 'seer';
const KC_ADMIN_USER = process.env.KC_ADMIN_USER ?? 'admin';
const KC_ADMIN_PASS = process.env.KC_ADMIN_PASS ?? 'admin';

const DRY_RUN = process.env.DRY_RUN === '1' || process.env.DRY_RUN === 'true';
const PAGE    = 1000;

// Attribute keys that migrated to FRIGG and must now be stripped from KC.
// Everything else (organization.alias, locale, emailVerified, etc.) stays.
const KEYS_TO_REMOVE_EXACT = new Set([
  'title', 'dept', 'phone',
  'avatarColor', 'pref.lang', 'tz', 'dateFmt', 'startPage',
  'quota_mimir', 'quota_sessions', 'quota_atoms', 'quota_workers', 'quota_anvil',
  'source_bindings',
]);
const KEYS_TO_REMOVE_PREFIX = [
  'profile.',
  'prefs.',
];

interface KcUser {
  id: string;
  username: string;
  attributes?: Record<string, string[]>;
}

async function kcAdminToken(): Promise<string> {
  const body = new URLSearchParams({
    grant_type: 'password', client_id: 'admin-cli',
    username: KC_ADMIN_USER, password: KC_ADMIN_PASS,
  });
  const res = await fetch(`${KC_URL}/realms/master/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
    signal: AbortSignal.timeout(10_000),
  });
  if (!res.ok) throw new Error(`KC token failed: ${res.status}`);
  return (await res.json() as { access_token: string }).access_token;
}

async function kcListUsers(token: string): Promise<KcUser[]> {
  const out: KcUser[] = [];
  let first = 0;
  for (;;) {
    const res = await fetch(
      `${KC_URL}/admin/realms/${KC_REALM}/users?first=${first}&max=${PAGE}&briefRepresentation=false`,
      { headers: { Authorization: `Bearer ${token}` }, signal: AbortSignal.timeout(30_000) },
    );
    if (!res.ok) throw new Error(`KC listUsers ${res.status}`);
    const batch = await res.json() as KcUser[];
    out.push(...batch);
    if (batch.length < PAGE) break;
    first += PAGE;
  }
  return out;
}

async function kcUpdateUser(
  token: string,
  userId: string,
  newAttributes: Record<string, string[]>,
): Promise<void> {
  if (DRY_RUN) return;
  // KC PUT replaces attributes wholesale when present. We fetch fresh single
  // user and PUT with filtered attributes + all other fields preserved.
  const getRes = await fetch(`${KC_URL}/admin/realms/${KC_REALM}/users/${encodeURIComponent(userId)}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!getRes.ok) throw new Error(`KC get user ${userId}: ${getRes.status}`);
  const user = await getRes.json() as Record<string, unknown>;
  user.attributes = newAttributes;
  const putRes = await fetch(`${KC_URL}/admin/realms/${KC_REALM}/users/${encodeURIComponent(userId)}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    body:   JSON.stringify(user),
    signal: AbortSignal.timeout(15_000),
  });
  if (!putRes.ok) throw new Error(`KC update ${userId}: ${putRes.status} ${await putRes.text()}`);
}

function strip(attrs: Record<string, string[]> | undefined): {
  kept: Record<string, string[]>;
  removed: string[];
} {
  const kept: Record<string, string[]> = {};
  const removed: string[] = [];
  if (!attrs) return { kept, removed };
  for (const [k, v] of Object.entries(attrs)) {
    if (KEYS_TO_REMOVE_EXACT.has(k)) { removed.push(k); continue; }
    if (KEYS_TO_REMOVE_PREFIX.some(p => k.startsWith(p))) { removed.push(k); continue; }
    kept[k] = v;
  }
  return { kept, removed };
}

async function main(): Promise<void> {
  console.log(`[MTN-67] ${DRY_RUN ? 'DRY-RUN' : 'LIVE'} cleanup`);
  console.log(`  KC: ${KC_URL} realm=${KC_REALM}`);

  const token = await kcAdminToken();
  const users = await kcListUsers(token);
  console.log(`[MTN-67] KC users: ${users.length}`);

  let touched = 0;
  let totalRemoved = 0;
  for (const u of users) {
    const { kept, removed } = strip(u.attributes);
    if (removed.length === 0) continue;
    totalRemoved += removed.length;
    touched++;
    console.log(`[MTN-67] ${u.username} (${u.id}): remove [${removed.join(', ')}]`);
    await kcUpdateUser(token, u.id, kept);
  }
  console.log(`[MTN-67] ${DRY_RUN ? 'would clean' : 'cleaned'} ${touched} users, ${totalRemoved} keys total`);
}

main().catch(err => {
  console.error('[MTN-67] FATAL:', err);
  process.exit(1);
});
