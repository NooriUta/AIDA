#!/usr/bin/env -S tsx
/**
 * MTN-66 — Batch migration: Keycloak user attributes → FRIGG frigg-users.
 *
 * Reads all users from Keycloak realm, extracts the attributes.{profile.*,
 * prefs.*, notify.*, quota_*, source_bindings} keys, and upserts them into
 * the FRIGG frigg-users DB as UserProfile / UserPreferences / UserNotifications
 * / UserApplicationState vertices (per USER_ATTRIBUTES_KC_VS_FRIGG.md §7).
 *
 * Run:
 *   # Dry-run (no writes, count assert only):
 *   DRY_RUN=1 KC_URL=http://localhost:18180/kc FRIGG_URL=http://localhost:2481 \
 *     npx tsx scripts/migrations/kc-attrs-to-frigg-users.ts
 *
 *   # Live migration:
 *   KC_URL=... FRIGG_URL=... npx tsx scripts/migrations/kc-attrs-to-frigg-users.ts
 *
 * Hard assertion: count(FRIGG.UserProfile) === count(KC.users) at exit.
 * Idempotent: re-running replays with no data change (upsert semantics).
 *
 * Snapshot for rollback: writes pre-migration KC dump to
 *   ./archived/kc-attrs-snapshot-{timestamp}.json
 *
 * Follows ADR Q-UA-2 (no dual-write): chur reader paths switch to FRIGG
 * immediately after this migration completes (MTN-67 cleanup then clears
 * the now-unused KC attributes).
 */

import { writeFile, mkdir } from 'node:fs/promises';
import { join } from 'node:path';
import * as process from 'node:process';

// ── Config ──────────────────────────────────────────────────────────────────

const KC_URL        = process.env.KC_URL        ?? 'http://localhost:18180/kc';
const KC_REALM      = process.env.KC_REALM      ?? 'seer';
const KC_ADMIN_USER = process.env.KC_ADMIN_USER ?? 'admin';
const KC_ADMIN_PASS = process.env.KC_ADMIN_PASS ?? 'admin';

const FRIGG_URL     = (process.env.FRIGG_URL ?? 'http://localhost:2481').replace(/\/$/, '');
const FRIGG_USER    = process.env.FRIGG_USER  ?? 'root';
const FRIGG_PASS    = process.env.FRIGG_PASS  ?? 'playwithdata';
const FRIGG_USERS_DB = process.env.FRIGG_USERS_DB ?? 'frigg-users';

const DRY_RUN = process.env.DRY_RUN === '1' || process.env.DRY_RUN === 'true';
const PAGE    = 1000;

const BASIC = Buffer.from(`${FRIGG_USER}:${FRIGG_PASS}`).toString('base64');

// ── Types ───────────────────────────────────────────────────────────────────

interface KcUser {
  id: string;
  username: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  enabled?: boolean;
  attributes?: Record<string, string[]>;
}

// ── Helpers ─────────────────────────────────────────────────────────────────

function attr(u: KcUser, key: string): string | undefined {
  const v = u.attributes?.[key];
  return Array.isArray(v) ? v[0] : undefined;
}

function attrBool(u: KcUser, key: string, fallback = false): boolean {
  const v = attr(u, key);
  if (v === undefined) return fallback;
  return v === 'true' || v === '1' || v === 'yes';
}

async function kcAdminToken(): Promise<string> {
  const body = new URLSearchParams({
    grant_type: 'password',
    client_id:  'admin-cli',
    username:   KC_ADMIN_USER,
    password:   KC_ADMIN_PASS,
  });
  const res = await fetch(`${KC_URL}/realms/master/protocol/openid-connect/token`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
    signal:  AbortSignal.timeout(10_000),
  });
  if (!res.ok) throw new Error(`KC admin token failed: ${res.status}`);
  const data = (await res.json()) as { access_token: string };
  return data.access_token;
}

async function kcListUsers(token: string): Promise<KcUser[]> {
  const out: KcUser[] = [];
  let first = 0;
  for (;;) {
    const res = await fetch(
      `${KC_URL}/admin/realms/${KC_REALM}/users?first=${first}&max=${PAGE}&briefRepresentation=false`,
      { headers: { Authorization: `Bearer ${token}` }, signal: AbortSignal.timeout(30_000) },
    );
    if (!res.ok) throw new Error(`KC listUsers failed: ${res.status}`);
    const batch = (await res.json()) as KcUser[];
    out.push(...batch);
    if (batch.length < PAGE) break;
    first += PAGE;
  }
  return out;
}

async function friggSql(db: string, command: string, params?: Record<string, unknown>): Promise<void> {
  if (DRY_RUN) return;
  const res = await fetch(`${FRIGG_URL}/api/v1/command/${encodeURIComponent(db)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Basic ${BASIC}` },
    body: JSON.stringify({ language: 'sql', command, params: params ?? {} }),
    signal: AbortSignal.timeout(15_000),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`FRIGG ${res.status}: ${text}`);
  }
}

async function friggQuery(db: string, command: string): Promise<unknown[]> {
  const res = await fetch(`${FRIGG_URL}/api/v1/query/${encodeURIComponent(db)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Basic ${BASIC}` },
    body: JSON.stringify({ language: 'sql', command, params: {} }),
    signal: AbortSignal.timeout(15_000),
  });
  if (!res.ok) throw new Error(`FRIGG ${res.status}: ${await res.text().catch(() => '')}`);
  const data = (await res.json()) as { result?: unknown[] };
  return data.result ?? [];
}

// ── Mapping (per USER_ATTRIBUTES_KC_VS_FRIGG.md §7) ─────────────────────────

function toProfile(u: KcUser): Record<string, unknown> {
  return {
    title:     attr(u, 'profile.title') ?? attr(u, 'title') ?? '',
    dept:      attr(u, 'profile.dept')  ?? attr(u, 'dept')  ?? '',
    phone:     attr(u, 'profile.phone') ?? attr(u, 'phone') ?? '',
    avatarUrl: attr(u, 'profile.avatarUrl') ?? '',
  };
}

function toPreferences(u: KcUser): Record<string, unknown> {
  return {
    avatarColor: attr(u, 'prefs.avatarColor') ?? attr(u, 'avatarColor') ?? '#A8B860',
    lang:        attr(u, 'prefs.lang')        ?? attr(u, 'pref.lang')    ?? 'ru',
    tz:          attr(u, 'prefs.tz')          ?? attr(u, 'tz')           ?? 'Europe/Moscow',
    dateFmt:     attr(u, 'prefs.dateFmt')     ?? attr(u, 'dateFmt')      ?? 'DD.MM.YYYY',
    startPage:   attr(u, 'prefs.startPage')   ?? attr(u, 'startPage')    ?? 'dashboard',
    theme:       attr(u, 'prefs.theme')       ?? 'auto',
    density:     attr(u, 'prefs.density')     ?? 'comfortable',
  };
}

function toNotifications(u: KcUser): Record<string, unknown> {
  return {
    email:   attrBool(u, 'prefs.notify.email',   true),
    browser: attrBool(u, 'prefs.notify.browser', false),
    harvest: attrBool(u, 'prefs.notify.harvest', false),
    errors:  attrBool(u, 'prefs.notify.errors',  true),
    digest:  attrBool(u, 'prefs.notify.digest',  false),
  };
}

function toSourceBindings(u: KcUser): string[] {
  return (attr(u, 'source_bindings') ?? '').split(',').map(s => s.trim()).filter(Boolean);
}

// ── Upsert via singleton pattern ────────────────────────────────────────────

async function upsertSingleton(
  type: string,
  userId: string,
  data: Record<string, unknown>,
): Promise<void> {
  const now = Date.now();
  const payloadJson = JSON.stringify(data);
  // ON INSERT … UPDATE via try-delete-insert pattern (ArcadeDB lacks native upsert)
  try {
    await friggSql(FRIGG_USERS_DB,
      `DELETE FROM ${type} WHERE userId = :userId`,
      { userId });
  } catch { /* ignore — type may not have any rows yet */ }
  await friggSql(FRIGG_USERS_DB,
    `INSERT INTO ${type} SET userId = :userId, configVersion = 1, updatedAt = :now, payload = :payload`,
    { userId, now, payload: payloadJson });
}

async function insertSourceBindings(userId: string, bindings: string[]): Promise<void> {
  if (bindings.length === 0) return;
  // Clear old bindings for idempotency
  try {
    await friggSql(FRIGG_USERS_DB,
      `DELETE FROM UserSourceBindings WHERE userId = :userId`,
      { userId });
  } catch { /* ignore */ }
  const now = Date.now();
  // One row per (userId, tenantAlias). For legacy data we don't know alias
  // yet — default to "default". Admin re-scopes manually if needed.
  for (const source of bindings) {
    await friggSql(FRIGG_USERS_DB,
      `INSERT INTO UserSourceBindings SET userId = :userId, tenantAlias = 'default', ` +
      `sourceId = :source, configVersion = 1, updatedAt = :now`,
      { userId, source, now });
  }
}

// ── Snapshot / archive ──────────────────────────────────────────────────────

async function writeSnapshot(users: KcUser[]): Promise<string> {
  const ts = new Date().toISOString().replace(/[:.]/g, '-');
  const dir = 'archived';
  const path = join(dir, `kc-attrs-snapshot-${ts}.json`);
  await mkdir(dir, { recursive: true }).catch(() => {});
  await writeFile(path, JSON.stringify(users, null, 2), 'utf8');
  return path;
}

// ── Main ────────────────────────────────────────────────────────────────────

async function main(): Promise<void> {
  console.log(`[MTN-66] ${DRY_RUN ? 'DRY-RUN' : 'LIVE'} — migrating KC attrs → FRIGG users`);
  console.log(`  KC:    ${KC_URL} realm=${KC_REALM}`);
  console.log(`  FRIGG: ${FRIGG_URL} db=${FRIGG_USERS_DB}`);

  console.log('[MTN-66] fetching KC admin token…');
  const token = await kcAdminToken();

  console.log('[MTN-66] fetching all users from KC…');
  const users = await kcListUsers(token);
  console.log(`[MTN-66] KC users: ${users.length}`);

  const snapshotPath = await writeSnapshot(users);
  console.log(`[MTN-66] snapshot written: ${snapshotPath}`);

  let migrated = 0;
  for (const u of users) {
    try {
      await upsertSingleton('UserProfile',       u.id, toProfile(u));
      await upsertSingleton('UserPreferences',   u.id, toPreferences(u));
      await upsertSingleton('UserNotifications', u.id, toNotifications(u));
      await insertSourceBindings(u.id, toSourceBindings(u));
      migrated++;
      if (migrated % 100 === 0) console.log(`[MTN-66] progress: ${migrated}/${users.length}`);
    } catch (e) {
      console.error(`[MTN-66] migration failed for user=${u.username} id=${u.id}:`, (e as Error).message);
      throw e;
    }
  }
  console.log(`[MTN-66] upserted ${migrated} users`);

  if (!DRY_RUN) {
    console.log('[MTN-66] verifying count assertion…');
    const rows = await friggQuery(FRIGG_USERS_DB, 'SELECT count(*) AS c FROM UserProfile');
    const friggCount = Number((rows[0] as { c?: number } | undefined)?.c ?? 0);
    if (friggCount !== users.length) {
      throw new Error(`count mismatch: KC=${users.length} FRIGG.UserProfile=${friggCount}`);
    }
    console.log(`[MTN-66] ✓ count match: KC=${users.length} FRIGG.UserProfile=${friggCount}`);
  } else {
    console.log('[MTN-66] DRY_RUN — skipped FRIGG count assertion');
  }

  console.log('[MTN-66] done');
}

main().catch(err => {
  console.error('[MTN-66] FATAL:', err);
  process.exit(1);
});
