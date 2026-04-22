/**
 * CAP-04: Session store — replaced in-memory Map with CachedSessionStore.
 * Writes through to ArcadeDB (FRIGG frigg-sessions); LRU cache for hot paths.
 *
 * Sessions survive Chur restarts. Horizontal scaling works because both replicas
 * read from / write to the same ArcadeDB backend.
 */
import { randomUUID } from 'node:crypto';
import { refreshAccessToken, extractUserInfo } from './keycloak';
import type { UserRole } from './types';
import { ArcadeDbSessionStore } from './store/ArcadeDbSessionStore';
import { CachedSessionStore }   from './store/CachedSessionStore';
import type { SessionStore }    from './store/SessionStore';

// ── Types ────────────────────────────────────────────────────────────────────

export interface Session {
  accessToken:        string;
  refreshToken:       string;
  accessExpiresAt:    number;   // Date.now() + expires_in * 1000
  sub:                string;
  username:           string;
  role:               UserRole;
  scopes:             string[];
  activeTenantAlias?: string;   // MTN-13: last tenant switch; undefined = 'default'
}

export interface SessionUser {
  sub:      string;
  username: string;
  role:     UserRole;
  scopes:   string[];
}

// ── Store ────────────────────────────────────────────────────────────────────

let store: SessionStore = new CachedSessionStore(new ArcadeDbSessionStore());

/** Replace the store — for unit tests only. */
export function _setStoreForTesting(s: SessionStore): void {
  store = s;
}

/** Mutex map: prevents concurrent refresh for the same session. */
const refreshLocks = new Map<string, Promise<Session>>();

/** 30-second buffer before expiry to avoid edge-case 401s. */
const EXPIRY_BUFFER_MS = 30_000;

// ── Public API ───────────────────────────────────────────────────────────────

/**
 * Create a new session from a Keycloak token response.
 * Returns the session ID (to be stored in the cookie).
 */
export async function createSession(
  accessToken:  string,
  refreshToken: string,
  expiresIn:    number,
  sub:          string,
  username:     string,
  role:         UserRole,
  scopes:       string[] = [],
): Promise<string> {
  const sid = randomUUID();
  const session: Session = {
    accessToken,
    refreshToken,
    accessExpiresAt: Date.now() + expiresIn * 1000,
    sub,
    username,
    role,
    scopes,
  };
  await store.create(sid, session);
  return sid;
}

/** Retrieve a session by ID. Returns undefined if not found. */
export async function getSession(sid: string): Promise<Session | undefined> {
  return store.get(sid);
}

/** Delete a session (logout). Returns the deleted session or undefined. */
export async function deleteSession(sid: string): Promise<Session | undefined> {
  const session = await store.delete(sid);
  refreshLocks.delete(sid);
  return session;
}

/** Partially update a session (e.g. activeTenantAlias switch). */
export async function updateSession(sid: string, patch: Partial<Session>): Promise<void> {
  const current = await store.get(sid);
  if (!current) throw new Error('session_not_found');
  await store.update(sid, { ...current, ...patch });
}

/** Check if the access token is still valid (with buffer). */
export function isAccessValid(session: Session): boolean {
  return session.accessExpiresAt > Date.now() + EXPIRY_BUFFER_MS;
}

/**
 * Ensure the session has a valid access token.
 * If expired, refresh via Keycloak. Uses a mutex to prevent concurrent refreshes.
 *
 * Returns the (possibly refreshed) session, or throws if refresh fails.
 */
export async function ensureValidSession(sid: string): Promise<Session> {
  const session = await store.get(sid);
  if (!session) throw new Error('Session not found');

  if (isAccessValid(session)) return session;

  // Mutex: if a refresh is already in flight for this sid, wait for it
  const existing = refreshLocks.get(sid);
  if (existing) return existing;

  const promise = doRefresh(sid, session);
  refreshLocks.set(sid, promise);

  try {
    return await promise;
  } finally {
    refreshLocks.delete(sid);
  }
}

// ── Internal ─────────────────────────────────────────────────────────────────

async function doRefresh(sid: string, session: Session): Promise<Session> {
  const tokens = await refreshAccessToken(session.refreshToken);
  const userInfo = extractUserInfo(
    JSON.parse(Buffer.from(tokens.access_token.split('.')[1], 'base64url').toString()),
  );

  const updated: Session = {
    accessToken:     tokens.access_token,
    refreshToken:    tokens.refresh_token,
    accessExpiresAt: Date.now() + tokens.expires_in * 1000,
    sub:             userInfo.sub,
    username:        userInfo.username,
    role:            userInfo.role,
    scopes:          userInfo.scopes,
  };

  await store.update(sid, updated);
  return updated;
}

// ── Cleanup ──────────────────────────────────────────────────────────────────

/** CAP-03: Sweep expired sessions every 5 minutes (fallback until HEIMDALL scheduler). */
setInterval(async () => {
  await store.sweep().catch(() => {});
}, 5 * 60 * 1000).unref();
