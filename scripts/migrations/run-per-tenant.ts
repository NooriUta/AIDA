#!/usr/bin/env tsx
/**
 * MTN-33 — Per-tenant schema migration coordinator.
 *
 * Iterates all ACTIVE tenants from DaliTenantConfig, runs a migration
 * callback (supplied as --cb=<path>) for each, then bumps configVersion
 * atomically via CAS.
 *
 * Usage:
 *   tsx scripts/migrations/run-per-tenant.ts --cb=./my-migration.ts [--dry-run]
 *
 * The callback module must export a default async function:
 *   export default async function migrate(ctx: MigrationContext): Promise<void>
 *
 * Exit codes: 0 = all OK, 1 = one or more tenants failed.
 */

import { parseArgs } from 'node:util';

const FRIGG_URL  = (process.env.FRIGG_URL  ?? 'http://localhost:2481').replace(/\/$/, '');
const FRIGG_USER = process.env.FRIGG_USER  ?? 'root';
const FRIGG_PASS = process.env.FRIGG_PASS  ?? '';
const FRIGG_BASIC = Buffer.from(`${FRIGG_USER}:${FRIGG_PASS}`).toString('base64');

export interface MigrationContext {
  tenantAlias: string;
  configVersion: number;
  /** Execute SQL against any FRIGG database. */
  sql: (db: string, command: string, params?: Record<string, unknown>) => Promise<unknown[]>;
  dryRun: boolean;
}

async function arcadeSql(db: string, command: string, params: Record<string, unknown> = {}): Promise<unknown[]> {
  const res = await fetch(`${FRIGG_URL}/api/v1/command/${encodeURIComponent(db)}`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Basic ${FRIGG_BASIC}` },
    body:    JSON.stringify({ language: 'sql', command, params }),
    signal:  AbortSignal.timeout(15_000),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`FRIGG [${db}] ${res.status}: ${text}`);
  }
  const data = (await res.json()) as { result?: unknown[] };
  return data.result ?? [];
}

interface TenantRow {
  tenantAlias: string;
  configVersion: number;
}

async function listActiveTenants(): Promise<TenantRow[]> {
  const rows = await arcadeSql(
    'frigg-tenants',
    `SELECT tenantAlias, configVersion FROM DaliTenantConfig WHERE status = 'ACTIVE' ORDER BY tenantAlias`,
  );
  return rows as TenantRow[];
}

async function casVersionBump(tenantAlias: string, expectedVersion: number): Promise<boolean> {
  await arcadeSql(
    'frigg-tenants',
    `UPDATE DaliTenantConfig SET configVersion = configVersion + 1
     WHERE tenantAlias = :alias AND configVersion = :expected`,
    { alias: tenantAlias, expected: expectedVersion },
  );
  const after = await arcadeSql(
    'frigg-tenants',
    `SELECT configVersion FROM DaliTenantConfig WHERE tenantAlias = :alias LIMIT 1`,
    { alias: tenantAlias },
  );
  const current = Number((after[0] as TenantRow | undefined)?.configVersion ?? 0);
  return current === expectedVersion + 1;
}

async function main() {
  const { values } = parseArgs({
    args: process.argv.slice(2),
    options: {
      cb:      { type: 'string' },
      'dry-run': { type: 'boolean', default: false },
    },
    strict: true,
  });

  const cbPath  = values.cb;
  const dryRun  = values['dry-run'] as boolean;

  if (!cbPath) {
    console.error('Error: --cb=<path> is required');
    process.exit(1);
  }

  const cbUrl = new URL(cbPath, `file://${process.cwd()}/`).href;
  const mod   = await import(cbUrl) as { default: (ctx: MigrationContext) => Promise<void> };
  if (typeof mod.default !== 'function') {
    console.error(`Error: ${cbPath} must export a default function`);
    process.exit(1);
  }

  const tenants = await listActiveTenants();
  console.log(`Found ${tenants.length} active tenant(s)${dryRun ? ' [DRY RUN]' : ''}`);

  const errors: string[] = [];

  for (const tenant of tenants) {
    const { tenantAlias, configVersion } = tenant;
    process.stdout.write(`  ${tenantAlias} (v${configVersion}) ... `);

    try {
      await mod.default({
        tenantAlias,
        configVersion,
        sql: arcadeSql,
        dryRun,
      });

      if (!dryRun) {
        const bumped = await casVersionBump(tenantAlias, configVersion);
        if (!bumped) {
          console.error(`CONFLICT (configVersion changed during migration — retry)`);
          errors.push(`${tenantAlias}: CAS conflict`);
          continue;
        }
      }

      console.log(`OK${dryRun ? ' (dry)' : ` → v${configVersion + 1}`}`);
    } catch (err) {
      const msg = err instanceof Error ? err.message : String(err);
      console.error(`FAILED: ${msg}`);
      errors.push(`${tenantAlias}: ${msg}`);
    }
  }

  if (errors.length) {
    console.error(`\n${errors.length} tenant(s) failed:`);
    errors.forEach(e => console.error(`  - ${e}`));
    process.exit(1);
  }

  console.log('\nAll tenants migrated successfully.');
}

main().catch(err => {
  console.error('Fatal:', err);
  process.exit(1);
});
