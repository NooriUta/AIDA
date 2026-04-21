/**
 * In-memory SessionStore — used in tests and as a local fallback.
 * No external dependencies; data is lost on process exit.
 */
import type { Session } from '../sessions';
import type { SessionStore } from './SessionStore';

const EXPIRY_GRACE_MS = 60 * 60 * 1_000;

export class InMemorySessionStore implements SessionStore {
  private map = new Map<string, Session>();

  async create(sid: string, session: Session): Promise<void> {
    this.map.set(sid, session);
  }

  async get(sid: string): Promise<Session | undefined> {
    const s = this.map.get(sid);
    if (!s) return undefined;
    if (Date.now() > s.accessExpiresAt + EXPIRY_GRACE_MS) {
      this.map.delete(sid);
      return undefined;
    }
    return s;
  }

  async update(sid: string, session: Session): Promise<void> {
    this.map.set(sid, session);
  }

  async delete(sid: string): Promise<Session | undefined> {
    const s = this.map.get(sid);
    this.map.delete(sid);
    return s;
  }

  async sweep(): Promise<void> {
    const now = Date.now();
    for (const [sid, s] of this.map) {
      if (s.accessExpiresAt + EXPIRY_GRACE_MS < now) this.map.delete(sid);
    }
  }
}
