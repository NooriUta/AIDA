/**
 * MTN-65 — Tests for the generic frigg-users repository (upsert / get / delete
 * on singleton vertex types: UserProfile, UserPreferences, UserNotifications,
 * UserLifecycle, UserApplicationState).
 *
 * Fetch is stubbed per-request; the test inspects URL + body to route responses.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  upsertUserVertex,
  getUserVertex,
  deleteUserVertex,
} from './FriggUserRepository';
import { __resetBootstrapFlagsForTests } from './FriggUsersClient';

type Handler = (url: string, init?: { body?: string }) => Promise<unknown> | unknown;
let handler: Handler = () => ({});

function ok(body: unknown = {}): unknown {
  return {
    ok: true, status: 200,
    text: async () => '',
    json: async () => body,
    headers: { get: () => null },
  };
}

beforeEach(() => {
  __resetBootstrapFlagsForTests();
  handler = () => ok();
  vi.stubGlobal('fetch', vi.fn((url: string, init?: { body?: string }) =>
    Promise.resolve(handler(String(url), init)),
  ));
});

describe('upsertUserVertex — singleton types', () => {
  it('inserts a new row with configVersion=1 when no row exists', async () => {
    const seen: string[] = [];
    handler = (url, init) => {
      seen.push(url);
      if (url.includes('/api/v1/databases')) return ok({ result: ['frigg-users'] });
      if (url.includes('/api/v1/query/')) {
        // No existing row
        return ok({ result: [] });
      }
      if (url.includes('/api/v1/command/') && init?.body?.includes('INSERT INTO UserProfile')) {
        return ok({ result: [{ '@rid': '#17:0' }] });
      }
      return ok({ result: [] });
    };

    const r = await upsertUserVertex('UserProfile', 'user-abc', { firstName: 'Ada' });
    expect(r).toEqual({ ok: true, configVersion: 1 });
    expect(seen.some(u => u.includes('/api/v1/command/'))).toBe(true);
  });

  it('increments configVersion on existing row', async () => {
    handler = (url, init) => {
      if (url.includes('/api/v1/databases')) return ok({ result: ['frigg-users'] });
      if (url.includes('/api/v1/query/')) return ok({ result: [{ configVersion: 3 }] });
      if (init?.body?.includes('UPDATE UserProfile')) return ok({ result: [{ count: 1 }] });
      return ok({ result: [] });
    };

    const r = await upsertUserVertex('UserProfile', 'user-abc', { firstName: 'Ada' });
    expect(r).toEqual({ ok: true, configVersion: 4 });
  });

  it('returns conflict when expectedConfigVersion mismatches current', async () => {
    handler = (url) => {
      if (url.includes('/api/v1/databases')) return ok({ result: ['frigg-users'] });
      if (url.includes('/api/v1/query/')) return ok({ result: [{ configVersion: 5 }] });
      return ok({ result: [] });
    };

    const r = await upsertUserVertex('UserProfile', 'user-abc', {}, 3);
    expect(r).toEqual({ ok: false, conflict: true, current: 5 });
  });

  it('rejects expectedConfigVersion>0 when no row exists', async () => {
    handler = (url) => {
      if (url.includes('/api/v1/databases')) return ok({ result: ['frigg-users'] });
      if (url.includes('/api/v1/query/')) return ok({ result: [] });
      return ok({ result: [] });
    };

    const r = await upsertUserVertex('UserPreferences', 'user-xyz', {}, 2);
    expect(r).toEqual({ ok: false, conflict: true, current: 0 });
  });

  it('rejects non-singleton types', async () => {
    await expect(upsertUserVertex('UserConsents', 'user-x', {})).rejects.toThrow(/not a singleton/);
    await expect(upsertUserVertex('UserSessionEvents', 'user-x', {})).rejects.toThrow(/not a singleton/);
    await expect(upsertUserVertex('UserSourceBindings', 'user-x', {})).rejects.toThrow(/not a singleton/);
  });

  it('rejects invalid userId', async () => {
    await expect(upsertUserVertex('UserProfile', '', {})).rejects.toThrow(/userId/);
    await expect(upsertUserVertex('UserProfile', 'x'.repeat(200), {})).rejects.toThrow(/userId/);
  });
});

describe('getUserVertex', () => {
  it('returns null when no row exists', async () => {
    handler = (url) => {
      if (url.includes('/api/v1/databases')) return ok({ result: ['frigg-users'] });
      return ok({ result: [] });
    };
    expect(await getUserVertex('UserProfile', 'nobody')).toBeNull();
  });

  it('parses payload JSON + meta fields', async () => {
    handler = (url) => {
      if (url.includes('/api/v1/databases')) return ok({ result: ['frigg-users'] });
      return ok({ result: [{
        userId: 'user-1',
        configVersion: 7,
        updatedAt: 1700000000000,
        reserved_acl_v2: null,
        payload: JSON.stringify({ firstName: 'Ada', title: 'Engineer' }),
      }] });
    };
    const r = await getUserVertex<{ firstName: string; title: string }>('UserProfile', 'user-1');
    expect(r).toEqual({
      userId: 'user-1',
      configVersion: 7,
      updatedAt: 1700000000000,
      reserved_acl_v2: null,
      data: { firstName: 'Ada', title: 'Engineer' },
    });
  });
});

describe('deleteUserVertex', () => {
  it('returns count from ArcadeDB delete response', async () => {
    handler = (url, init) => {
      if (url.includes('/api/v1/databases')) return ok({ result: ['frigg-users'] });
      if (init?.body?.includes('DELETE FROM UserProfile')) return ok({ result: [{ count: 1 }] });
      return ok({ result: [] });
    };
    const r = await deleteUserVertex('UserProfile', 'user-1');
    expect(r).toEqual({ deleted: 1 });
  });
});
