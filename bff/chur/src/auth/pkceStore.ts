/**
 * In-memory PKCE state store for Authorization Code flow.
 *
 * Lifecycle: state created on /auth/login (server-generated), consumed once on
 * /auth/callback. TTL 10 minutes (login flow window).
 *
 * Production note: for multi-instance deployment, swap to Redis/FRIGG-backed
 * store. Single-instance dev/staging works fine in-memory.
 */

import crypto from 'node:crypto';

export interface PkceEntry {
  codeVerifier: string;
  state:        string;
  redirectUri:  string;
  /** Original URL the user wanted (for post-login redirect). */
  returnTo?:    string;
  /** KC client_id used for this auth request — needed for token exchange. */
  clientId?:    string;
  createdAt:    number; // epoch ms
}

const STORE = new Map<string, PkceEntry>();
const TTL_MS = 10 * 60 * 1000; // 10 minutes

// Periodic cleanup of expired entries
setInterval(() => {
  const now = Date.now();
  for (const [state, entry] of STORE) {
    if (now - entry.createdAt > TTL_MS) STORE.delete(state);
  }
}, 60_000).unref();

/** Generate fresh PKCE pair + state. */
export function createPkce(redirectUri: string, returnTo?: string, clientId?: string): {
  state:         string;
  codeVerifier:  string;
  codeChallenge: string;
} {
  const codeVerifier = crypto.randomBytes(32).toString('base64url');
  const codeChallenge = crypto.createHash('sha256').update(codeVerifier).digest('base64url');
  const state = crypto.randomBytes(16).toString('hex');

  STORE.set(state, { codeVerifier, state, redirectUri, returnTo, clientId, createdAt: Date.now() });
  return { state, codeVerifier, codeChallenge };
}

/** Consume entry by state — single use. Returns null if not found or expired. */
export function consumePkce(state: string): PkceEntry | null {
  const entry = STORE.get(state);
  if (!entry) return null;
  STORE.delete(state);
  if (Date.now() - entry.createdAt > TTL_MS) return null;
  return entry;
}

/** Test-only: clear store. */
export function __resetPkceStore(): void {
  STORE.clear();
}
