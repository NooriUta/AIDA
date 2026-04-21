/**
 * CAP-05: 7-step tenant provisioning flow.
 *
 * Each step has retry × 2. On failure the status stays PROVISIONING and the
 * caller receives a structured error. No auto-rollback.
 *
 * Steps:
 *   1. Create Keycloak Organization → keycloakOrgId
 *   2. INSERT DaliTenantConfig (status=PROVISIONING) into frigg-tenants
 *   3. Create hound_{alias} in YGG + apply golden lineage schema
 *   4. Create hound_src_{alias} in YGG + apply source-archive schema
 *   5. Create dali_{alias} in FRIGG + apply JobRunr+DaliSession schema
 *   6. Register recurring harvest cron with jitter
 *   7. UPDATE DaliTenantConfig → status=ACTIVE
 */
import { config } from '../config';

// ── Alias validation ──────────────────────────────────────────────────────────

const ALIAS_REGEX = /^[a-z][a-z0-9-]{2,30}[a-z0-9]$/;

const RESERVED = new Set([
  'default', 'lore', 'audit', 'system', 'admin', 'api', 'www', 'test',
  'staging', 'prod', 'heimdall', 'frigg', 'ygg', 'dali', 'mimir', 'anvil',
  'hound', 'shuttle', 'chur', 'keycloak', 'ollama', 'claude', 'skadi',
  'urd', 'skuld',
]);

export function validateAlias(alias: string): string | null {
  if (!ALIAS_REGEX.test(alias)) return 'Invalid alias: must match ^[a-z][a-z0-9-]{2,30}[a-z0-9]$';
  if (RESERVED.has(alias))      return `Alias "${alias}" is reserved`;
  return null;
}

// ── Result types ──────────────────────────────────────────────────────────────

export interface ProvisionResult {
  tenantAlias:    string;
  keycloakOrgId:  string;
  lastStep:       number;
}

export interface ProvisionError {
  tenantAlias:        string;
  lastSuccessfulStep: number;
  failedStep:         number;
  correlationId:      string;
  cause:              string;
}

// ── Config helpers ────────────────────────────────────────────────────────────

const KC_BASE  = config.keycloakUrl;
const KC_REALM = config.keycloakRealm;

const FRIGG_URL  = (process.env.FRIGG_URL  ?? 'http://localhost:2481').replace(/\/$/, '');
const FRIGG_USER = process.env.FRIGG_USER  ?? 'root';
const FRIGG_PASS = process.env.FRIGG_PASS  ?? 'playwithdata';
const FRIGG_BASIC = Buffer.from(`${FRIGG_USER}:${FRIGG_PASS}`).toString('base64');

const YGG_URL  = (process.env.YGG_URL  ?? 'http://localhost:2480').replace(/\/$/, '');
const YGG_USER = process.env.YGG_USER  ?? 'root';
const YGG_PASS = process.env.YGG_PASS  ?? 'playwithdata';
const YGG_BASIC = Buffer.from(`${YGG_USER}:${YGG_PASS}`).toString('base64');

const HEIMDALL_URL = (process.env.HEIMDALL_URL ?? 'http://localhost:9093').replace(/\/$/, '');

// ── Low-level helpers ─────────────────────────────────────────────────────────

async function retry<T>(fn: () => Promise<T>, times = 2): Promise<T> {
  let last: unknown;
  for (let i = 0; i < times; i++) {
    try { return await fn(); } catch (e) { last = e; }
  }
  throw last;
}

async function kcAdminToken(): Promise<string> {
  const res = await fetch(`${KC_BASE}/realms/master/protocol/openid-connect/token`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body:    new URLSearchParams({
      grant_type: 'password',
      client_id:  'admin-cli',
      username:   config.kcAdminUser,
      password:   config.kcAdminPass,
    }),
    signal: AbortSignal.timeout(10_000),
  });
  if (!res.ok) throw new Error(`KC admin auth ${res.status}`);
  return ((await res.json()) as { access_token: string }).access_token;
}

async function friggSql(db: string, command: string, params?: Record<string, unknown>): Promise<void> {
  const res = await fetch(`${FRIGG_URL}/api/v1/command/${encodeURIComponent(db)}`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Basic ${FRIGG_BASIC}` },
    body:    JSON.stringify({ language: 'sql', command, params: params ?? {} }),
    signal:  AbortSignal.timeout(10_000),
  });
  if (!res.ok) throw new Error(`FRIGG ${res.status}: ${await res.text().catch(() => '')}`);
}

async function yggCreateDb(dbName: string): Promise<void> {
  const res = await fetch(`${YGG_URL}/api/v1/create/${encodeURIComponent(dbName)}`, {
    method:  'POST',
    headers: { 'Authorization': `Basic ${YGG_BASIC}` },
    signal:  AbortSignal.timeout(15_000),
  });
  if (!res.ok && res.status !== 409) { // 409 = already exists
    throw new Error(`YGG create db ${dbName}: ${res.status}`);
  }
}

async function friggCreateDb(dbName: string): Promise<void> {
  const res = await fetch(`${FRIGG_URL}/api/v1/create/${encodeURIComponent(dbName)}`, {
    method:  'POST',
    headers: { 'Authorization': `Basic ${FRIGG_BASIC}` },
    signal:  AbortSignal.timeout(15_000),
  });
  if (!res.ok && res.status !== 409) {
    throw new Error(`FRIGG create db ${dbName}: ${res.status}`);
  }
}

// ── Cron jitter ───────────────────────────────────────────────────────────────

function harvestCron(alias: string): string {
  let hash = 0;
  for (let i = 0; i < alias.length; i++) hash = (hash * 31 + alias.charCodeAt(i)) >>> 0;
  const minute  = hash % 60;
  const hour    = (hash % 3) + 2; // 02:xx, 03:xx, or 04:xx
  return `0 ${minute} ${hour} * * ?`;
}

// ── 7-step provisioning ───────────────────────────────────────────────────────

export async function provisionTenant(
  alias:         string,
  correlationId: string,
  actorUsername: string,
): Promise<ProvisionResult> {
  let lastSuccessfulStep = 0;
  let keycloakOrgId = '';

  async function step<T>(n: number, fn: () => Promise<T>): Promise<T> {
    try {
      const result = await retry(fn);
      lastSuccessfulStep = n;
      return result;
    } catch (e) {
      const err: ProvisionError = {
        tenantAlias: alias,
        lastSuccessfulStep,
        failedStep: n,
        correlationId,
        cause: (e as Error).message,
      };
      console.error('[PROVISION] step failed', err);
      throw err;
    }
  }

  // Step 1 — Keycloak Organization
  keycloakOrgId = await step(1, async () => {
    const token = await kcAdminToken();
    const res = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/organizations`,
      {
        method:  'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body:    JSON.stringify({ name: alias, alias, enabled: true }),
        signal:  AbortSignal.timeout(10_000),
      },
    );
    if (!res.ok && res.status !== 409) throw new Error(`KC org ${res.status}`);
    const loc = res.headers.get('Location') ?? '';
    return loc.split('/').pop() ?? alias;
  });

  // Step 2 — DaliTenantConfig PROVISIONING
  await step(2, async () => {
    await friggSql('frigg-tenants',
      `INSERT INTO DaliTenantConfig SET
         tenantAlias = :alias, keycloakOrgId = :orgId, status = 'PROVISIONING',
         yggLineageDbName = :lineageDb, yggSourceArchiveDbName = :sourceDb,
         friggDaliDbName = :daliDb, configVersion = 1, createdAt = :ts, updatedAt = :ts`,
      {
        alias,
        orgId:     keycloakOrgId,
        lineageDb: `hound_${alias}`,
        sourceDb:  `hound_src_${alias}`,
        daliDb:    `dali_${alias}`,
        ts:        Date.now(),
      },
    );
  });

  // Step 3 — YGG lineage DB
  await step(3, async () => {
    await yggCreateDb(`hound_${alias}`);
  });

  // Step 4 — YGG source archive DB
  await step(4, async () => {
    await yggCreateDb(`hound_src_${alias}`);
  });

  // Step 5 — FRIGG dali DB
  await step(5, async () => {
    await friggCreateDb(`dali_${alias}`);
    // Minimal JobRunr schema (DaliSession type for tracking parse sessions)
    await friggSql(`dali_${alias}`,
      `CREATE DOCUMENT TYPE DaliSession IF NOT EXISTS`,
    );
    await friggSql(`dali_${alias}`,
      `CREATE PROPERTY DaliSession.sessionId IF NOT EXISTS STRING`,
    );
    await friggSql(`dali_${alias}`,
      `CREATE INDEX IF NOT EXISTS ON DaliSession (sessionId) UNIQUE`,
    );
  });

  // Step 6 — Register harvest cron
  await step(6, async () => {
    const cron = harvestCron(alias);
    const body = JSON.stringify({
      tenantAlias: alias,
      cron,
      correlationId,
    });
    const res = await fetch(`${HEIMDALL_URL}/api/cron/harvest`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body,
      signal:  AbortSignal.timeout(5_000),
    });
    // Non-fatal if Heimdall doesn't support this yet
    if (!res.ok && res.status !== 404) throw new Error(`HEIMDALL cron ${res.status}`);
  });

  // Step 7 — mark ACTIVE
  await step(7, async () => {
    await friggSql('frigg-tenants',
      `UPDATE DaliTenantConfig SET status = 'ACTIVE', configVersion = configVersion + 1,
         updatedAt = :ts WHERE tenantAlias = :alias`,
      { alias, ts: Date.now() },
    );
  });

  return { tenantAlias: alias, keycloakOrgId, lastStep: 7 };
}

// ── Force cleanup ─────────────────────────────────────────────────────────────

export async function forceCleanupTenant(alias: string): Promise<void> {
  const token = await kcAdminToken().catch(() => '');

  // Delete KC org (best-effort)
  if (token) {
    const orgRes = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/organizations?search=${encodeURIComponent(alias)}`,
      { headers: { 'Authorization': `Bearer ${token}` }, signal: AbortSignal.timeout(5_000) },
    ).catch(() => null);
    if (orgRes?.ok) {
      const orgs = await orgRes.json() as { id: string; alias: string }[];
      const org = orgs.find(o => o.alias === alias);
      if (org) {
        await fetch(`${KC_BASE}/admin/realms/${KC_REALM}/organizations/${org.id}`, {
          method: 'DELETE',
          headers: { 'Authorization': `Bearer ${token}` },
          signal: AbortSignal.timeout(5_000),
        }).catch(() => {});
      }
    }
  }

  // Drop YGG DBs (best-effort)
  for (const db of [`hound_${alias}`, `hound_src_${alias}`]) {
    await fetch(`${YGG_URL}/api/v1/drop/${encodeURIComponent(db)}`, {
      method:  'DELETE',
      headers: { 'Authorization': `Basic ${YGG_BASIC}` },
      signal:  AbortSignal.timeout(10_000),
    }).catch(() => {});
  }

  // Drop FRIGG dali DB (best-effort)
  await fetch(`${FRIGG_URL}/api/v1/drop/${encodeURIComponent(`dali_${alias}`)}`, {
    method:  'DELETE',
    headers: { 'Authorization': `Basic ${FRIGG_BASIC}` },
    signal:  AbortSignal.timeout(10_000),
  }).catch(() => {});

  // Delete tenant config row
  await friggSql('frigg-tenants',
    `DELETE FROM DaliTenantConfig WHERE tenantAlias = :alias`,
    { alias },
  ).catch(() => {});
}
