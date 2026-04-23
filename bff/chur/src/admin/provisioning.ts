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

const FRIGG_URL  = (process.env.FRIGG_URL  ?? 'http://127.0.0.1:2481').replace(/\/$/, '');
const FRIGG_USER = process.env.FRIGG_USER  ?? 'root';
const FRIGG_PASS = process.env.FRIGG_PASS  ?? 'playwithdata';
const FRIGG_BASIC = Buffer.from(`${FRIGG_USER}:${FRIGG_PASS}`).toString('base64');

const YGG_URL  = (process.env.YGG_URL  ?? 'http://127.0.0.1:2480').replace(/\/$/, '');
const YGG_USER = process.env.YGG_USER  ?? 'root';
const YGG_PASS = process.env.YGG_PASS  ?? 'playwithdata';
const YGG_BASIC = Buffer.from(`${YGG_USER}:${YGG_PASS}`).toString('base64');

const HEIMDALL_URL = (process.env.HEIMDALL_URL ?? 'http://127.0.0.1:9093').replace(/\/$/, '');

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
  if (!res.ok && res.status !== 409) {
    const body = await res.text().catch(() => '');
    if (/already exists/i.test(body)) return;
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
    const body = await res.text().catch(() => '');
    if (/already exists/i.test(body)) return;
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

// ── MTN-25: 7-step provisioning saga ─────────────────────────────────────────
//
// Each step has a matching compensating action. On failure the saga walks the
// stack of completed steps in reverse calling each compensate() (best-effort —
// logged warns, never thrown). If compensation itself fails, the tenant row
// status transitions to PROVISIONING_FAILED so an operator can pick it up via
// `/resume-provisioning` or `/force-cleanup`.
//
// Pre-flight (step 0) verifies reachability of KC / YGG / FRIGG BEFORE side-
// effects, so a known-down dependency short-circuits without half-state.

type Compensation = () => Promise<void>;

async function preflight(alias: string): Promise<void> {
  // KC admin-token reachability
  try {
    await kcAdminToken();
  } catch (e) {
    throw new Error(`Pre-flight: Keycloak unreachable (${(e as Error).message})`);
  }
  // YGG ping
  const yggRes = await fetch(`${YGG_URL}/api/v1/server`, {
    headers: { 'Authorization': `Basic ${YGG_BASIC}` },
    signal:  AbortSignal.timeout(3_000),
  }).catch(() => null);
  if (!yggRes?.ok) throw new Error('Pre-flight: YGG unreachable');
  // FRIGG ping
  const friggRes = await fetch(`${FRIGG_URL}/api/v1/server`, {
    headers: { 'Authorization': `Basic ${FRIGG_BASIC}` },
    signal:  AbortSignal.timeout(3_000),
  }).catch(() => null);
  if (!friggRes?.ok) throw new Error('Pre-flight: FRIGG unreachable');

  // Conflict check: if KC org with same alias already exists — abort with 409
  try {
    const token = await kcAdminToken();
    const res = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/organizations?search=${encodeURIComponent(alias)}`,
      { headers: { 'Authorization': `Bearer ${token}` }, signal: AbortSignal.timeout(3_000) },
    );
    if (res.ok) {
      const orgs = await res.json() as Array<{ alias: string }>;
      if (orgs.find(o => o.alias === alias)) {
        throw new Error(`Pre-flight: Keycloak org "${alias}" already exists (409 conflict)`);
      }
    }
  } catch (e) {
    // Re-throw the 409 conflict message; swallow other errors (KC flakiness during check)
    if ((e as Error).message.includes('already exists')) throw e;
  }
}

export async function provisionTenant(
  alias:         string,
  correlationId: string,
  actorUsername: string,
): Promise<ProvisionResult> {
  const compensations: Array<{ step: number; action: Compensation }> = [];
  let lastSuccessfulStep = 0;
  let keycloakOrgId = '';

  const runStep = async <T>(n: number, forward: () => Promise<T>, undo?: Compensation): Promise<T> => {
    try {
      const result = await retry(forward);
      lastSuccessfulStep = n;
      if (undo) compensations.push({ step: n, action: undo });
      return result;
    } catch (e) {
      const msg = (e as Error).message;
      console.error(`[PROVISION] step ${n} failed (correlationId=${correlationId}): ${msg}`);
      await rollback(compensations, alias);
      await markFailed(alias, n, msg).catch(() => { /* FRIGG may also be down */ });
      const err: ProvisionError = {
        tenantAlias: alias,
        lastSuccessfulStep,
        failedStep:   n,
        correlationId,
        cause: msg,
      };
      throw err;
    }
  };

  // Step 0 — pre-flight (no state changes)
  await runStep(0, async () => {
    await preflight(alias);
  });

  // Step 1 — Keycloak Organization (compensation: delete org)
  keycloakOrgId = await runStep(1, async () => {
    const token = await kcAdminToken();
    // KC 26+ requires at least one domain on org create.
    const orgDomain = `${alias}.aida.local`;
    const res = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/organizations`,
      {
        method:  'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body:    JSON.stringify({
          name: alias,
          alias,
          enabled: true,
          domains: [{ name: orgDomain, verified: false }],
        }),
        signal:  AbortSignal.timeout(10_000),
      },
    );
    if (!res.ok && res.status !== 409) {
      const body = await res.text().catch(() => '');
      throw new Error(`KC org ${res.status}${body ? `: ${body.slice(0, 300)}` : ''}`);
    }
    const loc = res.headers.get('Location') ?? '';
    return loc.split('/').pop() ?? alias;
  }, async () => {
    const token = await kcAdminToken().catch(() => '');
    if (!token || !keycloakOrgId) return;
    await fetch(`${KC_BASE}/admin/realms/${KC_REALM}/organizations/${encodeURIComponent(keycloakOrgId)}`, {
      method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` },
      signal: AbortSignal.timeout(5_000),
    }).catch(() => {});
  });

  // Step 2 — DaliTenantConfig PROVISIONING (compensation: delete row)
  // Defaults mirror the `default` tenant row so UI/quota logic sees identical shape.
  await runStep(2, async () => {
    await friggSql('frigg-tenants',
      `INSERT INTO DaliTenantConfig SET
         tenantAlias = :alias, keycloakOrgId = :orgId, status = 'PROVISIONING',
         yggLineageDbName = :lineageDb, yggSourceArchiveDbName = :sourceDb,
         friggDaliDbName = :daliDb, configVersion = 1,
         harvestCron = :harvestCron, llmMode = 'off',
         dataRetentionDays = 30, maxParseSessions = 10, maxAtoms = 10000,
         maxSources = 5, maxConcurrentJobs = 2,
         createdAt = :ts, updatedAt = :ts`,
      {
        alias,
        orgId:       keycloakOrgId,
        lineageDb:   `hound_${alias}`,
        sourceDb:    `hound_src_${alias}`,
        daliDb:      `dali_${alias}`,
        harvestCron: harvestCron(alias),
        ts:          Date.now(),
      },
    );
  }, async () => {
    await friggSql('frigg-tenants',
      `DELETE FROM DaliTenantConfig WHERE tenantAlias = :alias`,
      { alias },
    ).catch(() => {});
  });

  // Step 3 — YGG lineage DB (compensation: drop)
  await runStep(3, async () => {
    await yggCreateDb(`hound_${alias}`);
  }, async () => {
    await yggDropDb(`hound_${alias}`).catch(() => {});
  });

  // Step 4 — YGG source archive DB (compensation: drop)
  await runStep(4, async () => {
    await yggCreateDb(`hound_src_${alias}`);
  }, async () => {
    await yggDropDb(`hound_src_${alias}`).catch(() => {});
  });

  // Step 5 — FRIGG dali DB (compensation: drop)
  await runStep(5, async () => {
    await friggCreateDb(`dali_${alias}`);
    await friggSql(`dali_${alias}`, `CREATE DOCUMENT TYPE DaliSession IF NOT EXISTS`);
    await friggSql(`dali_${alias}`, `CREATE PROPERTY DaliSession.sessionId IF NOT EXISTS STRING`);
    await friggSql(`dali_${alias}`, `CREATE INDEX IF NOT EXISTS ON DaliSession (sessionId) UNIQUE`);
  }, async () => {
    await friggDropDb(`dali_${alias}`).catch(() => {});
  });

  // Step 6 — Register harvest cron (best-effort; HEIMDALL may not be up yet in dev)
  await runStep(6, async () => {
    const cron = harvestCron(alias);
    const res = await fetch(`${HEIMDALL_URL}/api/cron/harvest`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify({ tenantAlias: alias, cron, correlationId }),
      signal:  AbortSignal.timeout(5_000),
    }).catch(e => {
      console.warn(`[PROVISION] step 6 cron registration skipped (HEIMDALL unreachable): ${(e as Error).message}`);
      return null;
    });
    if (res && !res.ok && res.status !== 404 && res.status !== 503) {
      throw new Error(`HEIMDALL cron ${res.status}`);
    }
  }, async () => {
    await fetch(`${HEIMDALL_URL}/api/cron/harvest/${encodeURIComponent(alias)}`, {
      method: 'DELETE', signal: AbortSignal.timeout(3_000),
    }).catch(() => {});
  });

  // Step 7 — mark ACTIVE (no compensation: terminal success)
  await runStep(7, async () => {
    await friggSql('frigg-tenants',
      `UPDATE DaliTenantConfig SET status = 'ACTIVE', configVersion = configVersion + 1,
         updatedAt = :ts WHERE tenantAlias = :alias`,
      { alias, ts: Date.now() },
    );
  });

  return { tenantAlias: alias, keycloakOrgId, lastStep: 7 };
}

// ── MTN-25: Compensation + recovery helpers ──────────────────────────────────

async function rollback(
  stack: Array<{ step: number; action: Compensation }>,
  alias: string,
): Promise<void> {
  console.warn(`[PROVISION] rolling back ${stack.length} step(s) for tenant=${alias}`);
  for (const { step: n, action } of [...stack].reverse()) {
    try {
      await action();
      console.warn(`[PROVISION] compensation step ${n} OK`);
    } catch (e) {
      console.error(`[PROVISION] compensation step ${n} FAILED: ${(e as Error).message}`);
    }
  }
}

async function markFailed(alias: string, failedStep: number, cause: string): Promise<void> {
  const ts = Date.now();
  // Upsert: INSERT if not present, UPDATE with PROVISIONING_FAILED otherwise.
  await friggSql('frigg-tenants',
    `UPDATE DaliTenantConfig SET status = 'PROVISIONING_FAILED',
       lastFailedStep = :step, lastFailedCause = :cause, updatedAt = :ts
     WHERE tenantAlias = :alias`,
    { alias, step: failedStep, cause: cause.slice(0, 500), ts },
  ).catch(() => {
    // Row may not exist if step 2 was the one that failed before insert.
    return friggSql('frigg-tenants',
      `INSERT INTO DaliTenantConfig SET tenantAlias = :alias,
         status = 'PROVISIONING_FAILED', configVersion = 0,
         lastFailedStep = :step, lastFailedCause = :cause,
         createdAt = :ts, updatedAt = :ts`,
      { alias, step: failedStep, cause: cause.slice(0, 500), ts },
    ).catch(() => { /* swallow */ });
  });
}

async function yggDropDb(dbName: string): Promise<void> {
  await fetch(`${YGG_URL}/api/v1/drop/${encodeURIComponent(dbName)}`, {
    method: 'POST', headers: { 'Authorization': `Basic ${YGG_BASIC}` },
    signal: AbortSignal.timeout(10_000),
  }).catch(() => {});
}
async function friggDropDb(dbName: string): Promise<void> {
  await fetch(`${FRIGG_URL}/api/v1/drop/${encodeURIComponent(dbName)}`, {
    method: 'POST', headers: { 'Authorization': `Basic ${FRIGG_BASIC}` },
    signal: AbortSignal.timeout(10_000),
  }).catch(() => {});
}

/**
 * MTN-25: Resume provisioning for a tenant stuck in PROVISIONING or
 * PROVISIONING_FAILED. Reads `lastFailedStep` from DaliTenantConfig and
 * re-runs the saga from step N+1 by delegating to a trimmed `provisionTenant`.
 *
 * <p>Simplest implementation: force-cleanup + provision again. Production can
 * evolve to truly incremental resume (skip already-succeeded steps).
 */
export async function resumeProvisioning(
  alias:         string,
  correlationId: string,
  actorUsername: string,
): Promise<ProvisionResult> {
  console.warn(`[PROVISION] resume-provisioning alias=${alias} corr=${correlationId}`);
  await forceCleanupTenant(alias).catch(e =>
    console.warn(`[PROVISION] resume: force-cleanup warning — ${(e as Error).message}`),
  );
  return provisionTenant(alias, correlationId, actorUsername);
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
    await yggDropDb(db);
  }

  // Drop FRIGG dali DB (best-effort)
  await friggDropDb(`dali_${alias}`);

  // Delete tenant config row
  await friggSql('frigg-tenants',
    `DELETE FROM DaliTenantConfig WHERE tenantAlias = :alias`,
    { alias },
  ).catch(() => {});
}
