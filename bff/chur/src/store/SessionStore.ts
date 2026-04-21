/**
 * CAP-01: Session persistence abstraction.
 * Implementations: InMemorySessionStore (legacy), ArcadeDbSessionStore, CachedSessionStore.
 */
import type { Session } from '../sessions';

export interface SessionStore {
  /** Persist a new session. */
  create(sid: string, session: Session): Promise<void>;

  /** Retrieve a session; returns undefined if not found or expired. */
  get(sid: string): Promise<Session | undefined>;

  /** Overwrite an existing session (used after token refresh). */
  update(sid: string, session: Session): Promise<void>;

  /** Delete a session. Returns the deleted session or undefined. */
  delete(sid: string): Promise<Session | undefined>;

  /** Remove all sessions where accessExpiresAt + 1h < now. */
  sweep(): Promise<void>;
}
