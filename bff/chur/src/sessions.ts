/**
 * In-memory session store for Keycloak BFF pattern.
 *
 * Each browser session is identified by a random UUID ("sid") stored in an
 * httpOnly cookie. The server-side Session holds the Keycloak access + refresh
 * tokens so they never leave the backend.
 *
 * Trade-off: sessions are lost on Chur restart (users must re-login).
 * For horizontal scaling, replace with Redis via the SessionStore interface.
 */
import { randomUUID } from 'node:crypto';
import { refreshAccessToken, extractUserInfo } from './keycloak';
import type { UserRole } from './types';

// ── Types ────────────────────────────────────────────────────────────────────

export interface Session {
  accessToken:      string;
  refreshToken:     string;
  accessExpiresAt:  number;   // Date.now() + expires_in * 1000
  sub:              string;
  username:         string;
  role:             UserRole;
  scopes:           string[];
}

export interface SessionUser {
  sub:      string;
  username: string;
  role:     UserRole;
  scopes:   string[];
}

// ── Store ────────────────────────────────────────────────────────────────────

const sessions = new Map<string, Session>();

/** Mutex map: prevents concurrent refresh for the same session. */
const refreshLocks = new Map<string, Promise<Session>>();

/** 30-second buffer before expiry to avoid edge-case 401s. */
const EXPIRY_BUFFER_MS = 30_000;

// ── Public API ───────────────────────────────────────────────────────────────

/**
 * Create a new session from a Keycloak token response.
 * Returns the session ID (to be stored in the cookie).
 */
export function createSession(
  accessToken:  string,
  refreshToken: string,
  expiresIn:    number,
  sub:          string,
  username:     string,
  role:         UserRole,
  scopes:       string[] = [],
): string {
  const sid = randomUUID();
  sessions.set(sid, {
    accessToken,
    refreshToken,
    accessExpiresAt: Date.now() + expiresIn * 1000,
    sub,
    username,
    role,
    scopes,
  });
  return sid;
}

/** Retrieve a session by ID. Returns undefined if not found. */
export function getSession(sid: string): Session | undefined {
  return sessions.get(sid);
}

/** Delete a session (logout). Returns the deleted session or undefined. */
export function deleteSession(sid: string): Session | undefined {
  const session = sessions.get(sid);
  sessions.delete(sid);
  refreshLocks.delete(sid);
  return session;
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
  const session = sessions.get(sid);
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

  sessions.set(sid, updated);
  return updated;
}

// ── Cleanup ──────────────────────────────────────────────────────────────────

/** Sweep expired sessions every 5 minutes. */
setInterval(() => {
  const now = Date.now();
  for (const [sid, session] of sessions) {
    // Remove sessions where even the refresh token is likely expired.
    // Keycloak default SSO idle = 30 min. We use a generous 1-hour cutoff.
    if (session.accessExpiresAt + 60 * 60 * 1000 < now) {
      sessions.delete(sid);
      refreshLocks.delete(sid);
    }
  }
}, 5 * 60 * 1000).unref();
