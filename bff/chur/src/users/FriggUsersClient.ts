/**
 * MTN-65 — Shared HTTP client for the `frigg-users` ArcadeDB database.
 *
 * One-time schema bootstrap is performed by `scripts/init-arcadedb.sh`. This
 * module only performs SQL commands; it re-asserts the schema in dev fallback
 * mode so the chur test harness works without the init script.
 *
 * Pattern mirrors {@link ../store/ArcadeDbSessionStore} for consistency.
 */
import { config } from '../config';

const BASIC = Buffer.from(`${config.friggUser}:${config.friggPass}`).toString('base64');
const HEADERS = {
  'Content-Type':  'application/json',
  'Authorization': `Basic ${BASIC}`,
} as const;

const DB = config.friggUsersDb;

const VERTEX_TYPES = [
  'UserProfile',
  'UserPreferences',
  'UserNotifications',
  'UserConsents',
  'UserSourceBindings',
  'UserApplicationState',
  'UserLifecycle',
  'UserSessionEvents',
] as const;
export type UserVertexType = (typeof VERTEX_TYPES)[number];

const UNIQUE_PER_USER: readonly UserVertexType[] = [
  'UserProfile',
  'UserPreferences',
  'UserNotifications',
  'UserLifecycle',
  'UserApplicationState',
];

let dbBootstrapped = false;
async function ensureDatabase(): Promise<void> {
  if (dbBootstrapped) return;
  try {
    const res = await fetch(`${config.friggUrl}/api/v1/databases`, {
      headers: { Authorization: HEADERS.Authorization },
      signal:  AbortSignal.timeout(5_000),
    });
    if (res.ok) {
      const data = (await res.json()) as { result?: string[] };
      if (!data.result?.includes(DB)) {
        await fetch(`${config.friggUrl}/api/v1/server`, {
          method:  'POST',
          headers: HEADERS,
          body:    JSON.stringify({ command: `create database ${DB}` }),
          signal:  AbortSignal.timeout(5_000),
        });
      }
    }
    dbBootstrapped = true;
  } catch {
    // init-arcadedb.sh is authoritative; tolerate miss in tests
  }
}

let schemaBootstrapped = false;
async function ensureSchema(): Promise<void> {
  if (schemaBootstrapped) return;
  await ensureDatabase();
  try {
    for (const t of VERTEX_TYPES) {
      await friggUsersSql(`CREATE VERTEX TYPE ${t} IF NOT EXISTS`);
      await friggUsersSql(`CREATE PROPERTY ${t}.userId IF NOT EXISTS STRING`);
      await friggUsersSql(`CREATE PROPERTY ${t}.configVersion IF NOT EXISTS INTEGER`);
      await friggUsersSql(`CREATE PROPERTY ${t}.updatedAt IF NOT EXISTS LONG`);
      await friggUsersSql(`CREATE PROPERTY ${t}.reserved_acl_v2 IF NOT EXISTS STRING`);
    }
    for (const t of UNIQUE_PER_USER) {
      await friggUsersSql(`CREATE INDEX IF NOT EXISTS ON ${t} (userId) UNIQUE`);
    }
    await friggUsersSql(`CREATE INDEX IF NOT EXISTS ON UserConsents (userId) NOTUNIQUE`);
    await friggUsersSql(`CREATE INDEX IF NOT EXISTS ON UserSessionEvents (userId) NOTUNIQUE`);
    await friggUsersSql(`CREATE PROPERTY UserSourceBindings.tenantAlias IF NOT EXISTS STRING`);
    await friggUsersSql(`CREATE INDEX IF NOT EXISTS ON UserSourceBindings (userId, tenantAlias) UNIQUE`);
    await friggUsersSql(`CREATE PROPERTY UserSessionEvents.ts IF NOT EXISTS LONG`);
    await friggUsersSql(`CREATE PROPERTY UserSessionEvents.tenantAlias IF NOT EXISTS STRING`);
    await friggUsersSql(`CREATE INDEX IF NOT EXISTS ON UserSessionEvents (ts) NOTUNIQUE`);
    await friggUsersSql(`CREATE PROPERTY UserLifecycle.dataRetentionUntil IF NOT EXISTS LONG`);
    await friggUsersSql(`CREATE INDEX IF NOT EXISTS ON UserLifecycle (dataRetentionUntil) NOTUNIQUE`);
    schemaBootstrapped = true;
  } catch {
    // Schema already exists — safe to ignore in tests
    schemaBootstrapped = true;
  }
}

export async function friggUsersSql(
  command: string,
  params?: Record<string, unknown>,
): Promise<unknown[]> {
  const url = `${config.friggUrl}/api/v1/command/${encodeURIComponent(DB)}`;
  const res = await fetch(url, {
    method:  'POST',
    headers: HEADERS,
    body:    JSON.stringify({ language: 'sql', command, params: params ?? {} }),
    signal:  AbortSignal.timeout(5_000),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`FRIGG users ${res.status}: ${text}`);
  }
  const data = (await res.json()) as { result?: unknown[] };
  return data.result ?? [];
}

export async function friggUsersQuery(
  command: string,
  params?: Record<string, unknown>,
): Promise<unknown[]> {
  const url = `${config.friggUrl}/api/v1/query/${encodeURIComponent(DB)}`;
  const res = await fetch(url, {
    method:  'POST',
    headers: HEADERS,
    body:    JSON.stringify({ language: 'sql', command, params: params ?? {} }),
    signal:  AbortSignal.timeout(5_000),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`FRIGG users ${res.status}: ${text}`);
  }
  const data = (await res.json()) as { result?: unknown[] };
  return data.result ?? [];
}

export async function withSchema<T>(fn: () => Promise<T>): Promise<T> {
  await ensureSchema();
  return fn();
}

/** @internal test helper — resets the memoised bootstrap flags. */
export function __resetBootstrapFlagsForTests(): void {
  dbBootstrapped = false;
  schemaBootstrapped = false;
}
