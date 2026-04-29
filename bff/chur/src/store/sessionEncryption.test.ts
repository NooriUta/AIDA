/**
 * TC-CHUR-21 — MTN-59: ArcadeDbSessionStore MUST store accessToken and
 * refreshToken as AES-256-GCM ciphertext, never as plaintext.
 *
 * Tests the integration between ArcadeDbSessionStore (store layer) and
 * encryption.ts (crypto layer). Verifies that:
 *   1. The INSERT payload sent to FRIGG contains ciphertext, not the original token.
 *   2. isCiphertext() recognises the stored value.
 *   3. A round-trip create → get decrypts back to the original token.
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { isCiphertext, __resetKeyCacheForTests } from '../session/encryption';

// 32 bytes encoded as base64 — deterministic test key
const TEST_DEK = Buffer.alloc(32, 0x41).toString('base64'); // "AAAA…" × 32

// ── Helpers ──────────────────────────────────────────────────────────────────

import type { Session } from '../sessions';

function makeSession(token = 'eyJhbGciOiJSUzI1NiJ9.payload.sig'): Session {
  return {
    accessToken:     token,
    refreshToken:    `rt-${token}`,
    accessExpiresAt: Date.now() + 300_000,
    sub:             'sub-test-001',
    username:        'alice',
    role:            'viewer',
    scopes:          ['openid', 'profile'],
  };
}

// ── Fetch mock factory ────────────────────────────────────────────────────────

type CapturedInsert = {
  command: string;
  params:  Record<string, unknown>;
};

/**
 * Build a fetch stub that:
 *   - Handles GET /api/v1/databases → returns the session DB as existing
 *   - Handles POST /api/v1/command/* with CREATE commands → ok
 *   - Handles POST /api/v1/command/* with SELECT → returns provided rows
 *   - Handles POST /api/v1/command/* with INSERT/UPDATE/DELETE → captures params
 */
function makeFetchStub(rows: Record<string, unknown>[] = []) {
  const captured: CapturedInsert[] = [];

  const stub = vi.fn(async (url: string, init?: RequestInit) => {
    const urlStr = String(url);
    const body   = init?.body ? JSON.parse(init.body as string) as {
      language?: string; command?: string; params?: Record<string, unknown>;
    } : {};

    // Database list
    if (urlStr.endsWith('/api/v1/databases')) {
      return new Response(JSON.stringify({ result: ['frigg-sessions'] }), { status: 200 });
    }

    const cmd = body.command ?? '';

    // Schema bootstrap (CREATE TYPE / CREATE PROPERTY / CREATE INDEX) — silently succeed
    if (/^CREATE|^ALTER/i.test(cmd)) {
      return new Response(JSON.stringify({ result: [] }), { status: 200 });
    }

    // SELECT — return provided rows
    if (/^SELECT/i.test(cmd)) {
      return new Response(JSON.stringify({ result: rows }), { status: 200 });
    }

    // INSERT / UPDATE / DELETE — capture params
    if (/^INSERT|^UPDATE|^DELETE/i.test(cmd)) {
      captured.push({ command: cmd, params: body.params ?? {} });
      return new Response(JSON.stringify({ result: [] }), { status: 200 });
    }

    return new Response(JSON.stringify({ result: [] }), { status: 200 });
  });

  return { stub, captured };
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('ArcadeDbSessionStore — MTN-59 token encryption at rest', () => {

  beforeEach(() => {
    process.env.AIDA_SESSION_DEK_KEY = TEST_DEK;
    __resetKeyCacheForTests();

    // Reset module-level bootstrap flags so each test starts clean.
    // We do this by re-importing the module in an isolated context.
    vi.resetModules();
  });

  afterEach(() => {
    delete process.env.AIDA_SESSION_DEK_KEY;
    __resetKeyCacheForTests();
    vi.restoreAllMocks();
  });

  it('TC-CHUR-21: accessToken stored as AES-256-GCM ciphertext (not plaintext)', async () => {
    const { stub, captured } = makeFetchStub();
    vi.stubGlobal('fetch', stub);

    // Re-import after vi.resetModules() so bootstrap flags are fresh
    const { ArcadeDbSessionStore } = await import('./ArcadeDbSessionStore');
    // Re-import encryption to pick up TEST_DEK from env
    const { isCiphertext: isCT } = await import('../session/encryption');

    const store   = new ArcadeDbSessionStore();
    const session = makeSession('eyJhbGciOiJSUzI1NiJ9.secret-payload');

    await store.create('sid-tc-chur-21', session);

    const insert = captured.find(c => /INSERT/i.test(c.command));
    expect(insert, 'No INSERT call captured').toBeTruthy();

    const stored = String(insert!.params['accessToken'] ?? '');

    // MTN-59: MUST be ciphertext — not the original token
    expect(stored).not.toEqual(session.accessToken);
    expect(isCT(stored)).toBe(true);
  });

  it('TC-CHUR-21: refreshToken also stored as ciphertext', async () => {
    const { stub, captured } = makeFetchStub();
    vi.stubGlobal('fetch', stub);

    const { ArcadeDbSessionStore } = await import('./ArcadeDbSessionStore');
    const { isCiphertext: isCT }   = await import('../session/encryption');

    const store   = new ArcadeDbSessionStore();
    const session = makeSession('some-access-token');

    await store.create('sid-rt', session);

    const insert = captured.find(c => /INSERT/i.test(c.command));
    const stored = String(insert!.params['refreshToken'] ?? '');

    expect(stored).not.toEqual(session.refreshToken);
    expect(isCT(stored)).toBe(true);
  });

  it('TC-CHUR-21: different sessions produce different ciphertexts for same token', async () => {
    const { stub, captured } = makeFetchStub();
    vi.stubGlobal('fetch', stub);

    const { ArcadeDbSessionStore } = await import('./ArcadeDbSessionStore');

    const store   = new ArcadeDbSessionStore();
    const session = makeSession('identical-token');

    await store.create('sid-a', session);
    await store.create('sid-b', session);

    const inserts = captured.filter(c => /INSERT/i.test(c.command));
    expect(inserts).toHaveLength(2);

    const ct1 = String(inserts[0].params['accessToken'] ?? '');
    const ct2 = String(inserts[1].params['accessToken'] ?? '');

    // AES-256-GCM uses random IV — same plaintext → different ciphertext each time
    expect(ct1).not.toEqual(ct2);
  });

  it('TC-CHUR-21: round-trip create→get decrypts back to original token', async () => {
    const session      = makeSession('round-trip-token');
    const storedParams: Record<string, unknown> = {};

    // Capture what gets inserted, then feed it back as the SELECT result
    let insertParams: Record<string, unknown> | null = null;

    const stub = vi.fn(async (url: string, init?: RequestInit) => {
      const urlStr = String(url);
      const body   = init?.body
        ? JSON.parse(init.body as string) as { command?: string; params?: Record<string, unknown> }
        : {};
      const cmd = body.command ?? '';

      if (urlStr.endsWith('/api/v1/databases')) {
        return new Response(JSON.stringify({ result: ['frigg-sessions'] }), { status: 200 });
      }
      if (/^CREATE|^ALTER/i.test(cmd)) {
        return new Response(JSON.stringify({ result: [] }), { status: 200 });
      }
      if (/^INSERT/i.test(cmd)) {
        insertParams = { ...body.params };
        return new Response(JSON.stringify({ result: [] }), { status: 200 });
      }
      if (/^SELECT/i.test(cmd)) {
        // Return the previously-inserted doc (with encrypted tokens)
        const doc = insertParams ? {
          ...insertParams,
          expiresAt: Date.now() + 600_000,  // not expired
        } : {};
        return new Response(JSON.stringify({ result: [doc] }), { status: 200 });
      }
      return new Response(JSON.stringify({ result: [] }), { status: 200 });
    });

    vi.stubGlobal('fetch', stub);

    const { ArcadeDbSessionStore } = await import('./ArcadeDbSessionStore');

    const store = new ArcadeDbSessionStore();
    await store.create('sid-rt', session);

    const retrieved = await store.get('sid-rt');

    expect(retrieved).not.toBeUndefined();
    // After decryption, the original plaintext token must be restored
    expect(retrieved!.accessToken).toBe(session.accessToken);
    expect(retrieved!.refreshToken).toBe(session.refreshToken);
  });

  it('TC-CHUR-21: without DEK key the token is stored as plaintext (dev fallback)', async () => {
    // Remove DEK so encryptToken() falls back to identity
    delete process.env.AIDA_SESSION_DEK_KEY;
    __resetKeyCacheForTests();

    const { stub, captured } = makeFetchStub();
    vi.stubGlobal('fetch', stub);

    const { ArcadeDbSessionStore } = await import('./ArcadeDbSessionStore');
    const { isCiphertext: isCT }   = await import('../session/encryption');

    const store   = new ArcadeDbSessionStore();
    const session = makeSession('plain-dev-token');

    await store.create('sid-dev', session);

    const insert = captured.find(c => /INSERT/i.test(c.command));
    const stored = String(insert!.params['accessToken'] ?? '');

    expect(isCT(stored)).toBe(false);
    expect(stored).toBe(session.accessToken);
  });
});
