/**
 * MTN-09 — Tests for the tenant quota enforcement middleware.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  checkQuota,
  bumpCachedCount,
  __resetQuotaCacheForTests,
} from './quotaCheck';

type Handler = (url: string, init?: { body?: string }) => unknown;
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
  __resetQuotaCacheForTests();
  handler = () => ok({ result: [] });
  vi.stubGlobal('fetch', vi.fn((url: string, init?: { body?: string }) =>
    Promise.resolve(handler(String(url), init)),
  ));
});

describe('checkQuota', () => {
  it('allows when cap is absent (Infinity)', async () => {
    handler = (url, init) => {
      if (init?.body?.includes('SELECT maxParseSessions')) return ok({ result: [{ cap: null }] });
      return ok({ result: [{ c: 999 }] });
    };
    const r = await checkQuota('acme', 'parseSessions');
    expect(r.allowed).toBe(true);
    expect(r.status).toBe(200);
    expect(r.cap).toBe(Infinity);
  });

  it('allows when current < cap', async () => {
    handler = (url, init) => {
      if (init?.body?.includes('SELECT maxParseSessions')) return ok({ result: [{ cap: 10 }] });
      return ok({ result: [{ c: 5 }] });
    };
    const r = await checkQuota('acme', 'parseSessions');
    expect(r).toMatchObject({ allowed: true, status: 200, count: 5, cap: 10 });
  });

  it('returns 429 when current == cap (soft cap)', async () => {
    handler = (url, init) => {
      if (init?.body?.includes('SELECT maxParseSessions')) return ok({ result: [{ cap: 3 }] });
      return ok({ result: [{ c: 3 }] });
    };
    const r = await checkQuota('acme', 'parseSessions');
    expect(r.allowed).toBe(false);
    expect(r.status).toBe(429);
    expect(r.reason).toMatch(/soft_cap/);
  });

  it('returns 403 when current > cap (hard cap, config lowered)', async () => {
    handler = (url, init) => {
      if (init?.body?.includes('SELECT maxSources')) return ok({ result: [{ cap: 2 }] });
      return ok({ result: [{ c: 5 }] });
    };
    const r = await checkQuota('acme', 'sources');
    expect(r.allowed).toBe(false);
    expect(r.status).toBe(403);
    expect(r.reason).toMatch(/hard_cap/);
  });

  it('atoms cap currently not enforced (returns 0 count)', async () => {
    handler = (url, init) => {
      if (init?.body?.includes('SELECT maxAtoms')) return ok({ result: [{ cap: 1_000_000 }] });
      return ok({ result: [{ c: 42 }] }); // would have been ignored
    };
    const r = await checkQuota('acme', 'atoms');
    expect(r.count).toBe(0);
    expect(r.allowed).toBe(true);
  });

  it('caches results within TTL (single FRIGG fetch pair)', async () => {
    const calls: string[] = [];
    handler = (url, init) => {
      calls.push(init?.body?.includes('SELECT max') ? 'CAP' : 'COUNT');
      if (init?.body?.includes('SELECT maxParseSessions')) return ok({ result: [{ cap: 10 }] });
      return ok({ result: [{ c: 5 }] });
    };

    await checkQuota('acme', 'parseSessions');
    await checkQuota('acme', 'parseSessions');
    await checkQuota('acme', 'parseSessions');

    expect(calls.filter(c => c === 'CAP').length).toBe(1);
    expect(calls.filter(c => c === 'COUNT').length).toBe(1);
  });

  it('fails open on FRIGG error (count = 0)', async () => {
    handler = (url, init) => {
      if (init?.body?.includes('SELECT max')) return ok({ result: [{ cap: 10 }] });
      return { ok: false, status: 500, text: async () => 'boom', json: async () => ({}), headers: { get: () => null } };
    };
    const r = await checkQuota('acme', 'parseSessions');
    expect(r.allowed).toBe(true);  // fail-open
    expect(r.count).toBe(0);
  });
});

describe('bumpCachedCount', () => {
  it('increments the cached count for a tenant/resource', async () => {
    handler = (url, init) => {
      if (init?.body?.includes('SELECT maxParseSessions')) return ok({ result: [{ cap: 10 }] });
      return ok({ result: [{ c: 5 }] });
    };
    const first = await checkQuota('acme', 'parseSessions');
    expect(first.count).toBe(5);

    bumpCachedCount('acme', 'parseSessions');
    bumpCachedCount('acme', 'parseSessions');

    const second = await checkQuota('acme', 'parseSessions');
    expect(second.count).toBe(7);
  });

  it('is a no-op when no cache entry exists', () => {
    // Does not throw; cache stays empty.
    bumpCachedCount('nobody', 'sources', 3);
  });
});
