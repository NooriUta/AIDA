/**
 * MTN-27 — Generic CAS helper for DaliTenantConfig mutations.
 *
 * Wraps the UPDATE + re-SELECT pattern from `PUT /retention` so status-change
 * endpoints (suspend / unsuspend / archive-now / restore) can use the same
 * optimistic-lock semantics without duplicating SQL.
 *
 * Returns one of:
 *   - { ok: true, configVersion }  — CAS succeeded, version bumped
 *   - { ok: false, conflict: true, current } — caller's expectedConfigVersion
 *     was stale; tell the client to reload and retry
 *
 * Caller supplies just the set-clause fragment and a params map. Example:
 *
 *   const r = await casUpdateTenant('frigg-tenants', 'acme', 1, {
 *     setClause: `status = 'SUSPENDED', updatedAt = :ts`,
 *     params:    { ts: Date.now() },
 *   });
 */

const FRIGG_URL  = (process.env.FRIGG_URL  ?? 'http://127.0.0.1:2481').replace(/\/$/, '');
const FRIGG_USER = process.env.FRIGG_USER  ?? 'root';
const FRIGG_PASS = process.env.FRIGG_PASS  ?? 'playwithdata';
const FRIGG_BASIC = Buffer.from(`${FRIGG_USER}:${FRIGG_PASS}`).toString('base64');

async function friggSql(db: string, command: string, params?: Record<string, unknown>): Promise<unknown[]> {
  const res = await fetch(`${FRIGG_URL}/api/v1/command/${encodeURIComponent(db)}`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Basic ${FRIGG_BASIC}` },
    body:    JSON.stringify({ language: 'sql', command, params: params ?? {} }),
    signal:  AbortSignal.timeout(10_000),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`FRIGG ${res.status}: ${text}`);
  }
  const data = (await res.json()) as { result?: unknown[] };
  return data.result ?? [];
}

export interface CasUpdateInput {
  /** Everything after `SET ` — does NOT include `configVersion = configVersion + 1` (helper adds it). */
  setClause: string;
  /** Bound params for {@link setClause}; do NOT include `alias` or `expected`. */
  params?: Record<string, unknown>;
}

export type CasUpdateResult =
  | { ok: true; configVersion: number }
  | { ok: false; conflict: true; current: number };

export async function casUpdateTenant(
  db: string,
  tenantAlias: string,
  expectedConfigVersion: number,
  input: CasUpdateInput,
): Promise<CasUpdateResult> {
  if (typeof expectedConfigVersion !== 'number' || expectedConfigVersion < 1) {
    throw new Error('expectedConfigVersion must be a positive integer');
  }
  const params: Record<string, unknown> = {
    ...(input.params ?? {}),
    alias:    tenantAlias,
    expected: expectedConfigVersion,
  };
  await friggSql(db,
    `UPDATE DaliTenantConfig SET ${input.setClause}, configVersion = configVersion + 1
     WHERE tenantAlias = :alias AND configVersion = :expected`,
    params,
  );
  // ArcadeDB HTTP does not return affected-rows — re-SELECT to confirm CAS success.
  const after = await friggSql(db,
    `SELECT configVersion FROM DaliTenantConfig WHERE tenantAlias = :alias LIMIT 1`,
    { alias: tenantAlias },
  );
  const current = Number((after[0] as { configVersion?: number })?.configVersion ?? 0);
  if (current !== expectedConfigVersion + 1) {
    return { ok: false, conflict: true, current };
  }
  return { ok: true, configVersion: current };
}

/** Format a 409 response body matching the contract from PUT /retention. */
export function casConflictBody(
  tenantAlias: string,
  expectedConfigVersion: number,
  currentConfigVersion: number,
): Record<string, unknown> {
  return {
    error:                'config_version_conflict',
    tenantAlias,
    expectedConfigVersion,
    currentConfigVersion,
    hint: 'GET /api/admin/tenants/:alias to read current configVersion, then retry with the new value',
  };
}
