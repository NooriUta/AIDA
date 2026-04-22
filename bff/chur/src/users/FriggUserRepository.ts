/**
 * MTN-65 — Generic CRUD repository for single-row-per-user vertex types in
 * `frigg-users`. Handles:
 *   - upsert (UPDATE …, falling through to INSERT on zero rows affected)
 *   - get (by userId)
 *   - delete
 *
 * Append-only vertices (UserConsents, UserSessionEvents) and composite-key
 * UserSourceBindings have their own dedicated modules.
 */
import { friggUsersQuery, friggUsersSql, withSchema, type UserVertexType } from './FriggUsersClient';

const SINGLETON_TYPES: ReadonlySet<UserVertexType> = new Set([
  'UserProfile',
  'UserPreferences',
  'UserNotifications',
  'UserLifecycle',
  'UserApplicationState',
]);

export interface UserVertexRow<T extends Record<string, unknown> = Record<string, unknown>> {
  userId:         string;
  configVersion:  number;
  updatedAt:      number;
  reserved_acl_v2: string | null;
  data:           T;
}

function assertSingleton(type: UserVertexType): void {
  if (!SINGLETON_TYPES.has(type)) {
    throw new Error(
      `FriggUserRepository.upsert/get/delete: '${type}' is not a singleton vertex — use the dedicated module`,
    );
  }
}

function validateUserId(userId: string): void {
  if (typeof userId !== 'string' || userId.length === 0 || userId.length > 128) {
    throw new Error('userId must be a non-empty string (≤128 chars)');
  }
}

/**
 * Upsert a single row for `userId` in `type`, with optimistic-locking on
 * `configVersion`. If `expectedConfigVersion` is provided and does not match
 * the stored value, returns `{ conflict: true, current }` without writing.
 *
 * On first write (no row) the record is inserted with `configVersion = 1`.
 */
export async function upsertUserVertex<T extends Record<string, unknown>>(
  type: UserVertexType,
  userId: string,
  data: T,
  expectedConfigVersion?: number,
): Promise<
  | { ok: true; configVersion: number }
  | { ok: false; conflict: true; current: number }
> {
  assertSingleton(type);
  validateUserId(userId);

  return withSchema(async () => {
    const now = Date.now();
    const existing = (await friggUsersQuery(
      `SELECT configVersion FROM ${type} WHERE userId = :userId LIMIT 1`,
      { userId },
    )) as Array<{ configVersion?: number }>;

    if (existing.length === 0) {
      if (expectedConfigVersion !== undefined && expectedConfigVersion !== 0) {
        return { ok: false, conflict: true, current: 0 };
      }
      await friggUsersSql(
        `INSERT INTO ${type} SET userId = :userId, configVersion = 1, updatedAt = :now, payload = :payload`,
        { userId, now, payload: JSON.stringify(data) },
      );
      return { ok: true, configVersion: 1 };
    }

    const current = Number(existing[0]?.configVersion ?? 0);
    if (expectedConfigVersion !== undefined && expectedConfigVersion !== current) {
      return { ok: false, conflict: true, current };
    }
    const next = current + 1;
    await friggUsersSql(
      `UPDATE ${type} SET configVersion = :next, updatedAt = :now, payload = :payload WHERE userId = :userId`,
      { next, now, payload: JSON.stringify(data), userId },
    );
    return { ok: true, configVersion: next };
  });
}

export async function getUserVertex<T extends Record<string, unknown>>(
  type: UserVertexType,
  userId: string,
): Promise<UserVertexRow<T> | null> {
  assertSingleton(type);
  validateUserId(userId);

  return withSchema(async () => {
    const rows = (await friggUsersQuery(
      `SELECT userId, configVersion, updatedAt, reserved_acl_v2, payload FROM ${type} WHERE userId = :userId LIMIT 1`,
      { userId },
    )) as Array<Record<string, unknown>>;
    if (rows.length === 0) return null;
    const row = rows[0];
    const payload = typeof row.payload === 'string' ? JSON.parse(row.payload) : row.payload;
    return {
      userId:          String(row.userId),
      configVersion:   Number(row.configVersion ?? 0),
      updatedAt:       Number(row.updatedAt ?? 0),
      reserved_acl_v2: (row.reserved_acl_v2 ?? null) as string | null,
      data:            (payload ?? {}) as T,
    };
  });
}

export async function deleteUserVertex(
  type: UserVertexType,
  userId: string,
): Promise<{ deleted: number }> {
  assertSingleton(type);
  validateUserId(userId);

  return withSchema(async () => {
    const result = (await friggUsersSql(
      `DELETE FROM ${type} WHERE userId = :userId`,
      { userId },
    )) as Array<{ count?: number }>;
    const deleted = Number(result[0]?.count ?? 0);
    return { deleted };
  });
}
