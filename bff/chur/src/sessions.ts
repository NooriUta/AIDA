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
import { emitSessionEvent }     from './users/UserSessionEventsEmitter';
import { config }               from './config';

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
  createdAt?:         number;   // MTN-39: epoch ms when session was created (for role-change invalidation)
  email?:             string;
  firstName?:         string;
  lastName?:          string;
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
    createdAt: Date.now(),
  }; // email/firstName/lastName populated in createSession caller via updateSession if available
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

async function fetchLastRoleChangeAt(tenantAlias: string): Promise<number | null> {
  const FRIGG_BASIC = Buffer.from(`${config.friggUser}:${config.friggPass}`).toString('base64');
  try {
    const res = await fetch(
      `${config.friggUrl}/api/v1/query/${encodeURIComponent(config.friggTenantsDb)}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Basic ${FRIGG_BASIC}` },
        body: JSON.stringify({
          language: 'sql',
          command: 'SELECT lastRoleChangeAt FROM DaliTenantConfig WHERE tenantAlias = :alias LIMIT 1',
          params: { alias: tenantAlias },
        }),
        signal: AbortSignal.timeout(3_000),
      },
    );
    if (!res.ok) return null;
    const data = (await res.json()) as { result?: Array<{ lastRoleChangeAt?: number | null }> };
    return data.result?.[0]?.lastRoleChangeAt ?? null;
  } catch {
    return null;
  }
}

async function doRefresh(sid: string, session: Session): Promise<Session> {
  // MTN-39: force-invalidate if an admin bumped lastRoleChangeAt after session was created
  const tenantAlias = session.activeTenantAlias ?? 'default';
  const lastRoleChangeAt = await fetchLastRoleChangeAt(tenantAlias);
  if (lastRoleChangeAt && session.createdAt && session.createdAt < lastRoleChangeAt) {
    await store.delete(sid);
    void emitSessionEvent({
      userId:    session.sub,
      sessionId: sid,
      eventType: 'session_invalidated',
      result:    'failure',
    });
    throw new Error('session_invalidated_role_change');
  }

  const tokens = await refreshAccessToken(session.refreshToken);
  const userInfo = extractUserInfo(
    JSON.parse(Buffer.from(tokens.access_token.split('.')[1], 'base64url').toString()),
  );

  const updated: Session = {
    ...session,
    accessToken:     tokens.access_token,
    refreshToken:    tokens.refresh_token,
    accessExpiresAt: Date.now() + tokens.expires_in * 1000,
    sub:             userInfo.sub,
    username:        userInfo.username,
    role:            userInfo.role,
    scopes:          userInfo.scopes,
    email:           userInfo.email ?? session.email,
    firstName:       userInfo.firstName ?? session.firstName,
    lastName:        userInfo.lastName ?? session.lastName,
  };

  await store.update(sid, updated);
  return updated;
}

// ── Cleanup ──────────────────────────────────────────────────────────────────

/** CAP-03: Sweep expired sessions every 5 minutes (fallback until HEIMDALL scheduler). */
setInterval(async () => {
  await store.sweep().catch(() => {});
}, 5 * 60 * 1000).unref();
