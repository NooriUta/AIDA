/**
 * CAP-02: In-memory LRU cache wrapping a persistent SessionStore.
 * Read-through / write-through. 2000 entries, 60s TTL.
 */
import type { Session } from '../sessions';
import type { SessionStore } from './SessionStore';

const MAX_SIZE  = 2_000;
const TTL_MS    = 60_000; // 60 seconds

interface CacheEntry {
  session:   Session;
  expiresAt: number; // cache TTL (not session expiry)
}

/** Minimal LRU backed by an insertion-ordered Map. */
class LRU {
  private map = new Map<string, CacheEntry>();

  get(key: string): Session | undefined {
    const entry = this.map.get(key);
    if (!entry) return undefined;
    if (Date.now() > entry.expiresAt) {
      this.map.delete(key);
      return undefined;
    }
    // Re-insert at end (most-recently-used)
    this.map.delete(key);
    this.map.set(key, entry);
    return entry.session;
  }

  set(key: string, session: Session): void {
    if (this.map.has(key)) this.map.delete(key);
    else if (this.map.size >= MAX_SIZE) {
      // Evict least-recently-used (first entry)
      const first = this.map.keys().next().value;
      if (first !== undefined) this.map.delete(first);
    }
    this.map.set(key, { session, expiresAt: Date.now() + TTL_MS });
  }

  delete(key: string): void {
    this.map.delete(key);
  }
}

// ── CachedSessionStore ────────────────────────────────────────────────────────

export class CachedSessionStore implements SessionStore {
  private readonly lru = new LRU();

  constructor(private readonly backing: SessionStore) {}

  async create(sid: string, session: Session): Promise<void> {
    await this.backing.create(sid, session);
    this.lru.set(sid, session);
  }

  async get(sid: string): Promise<Session | undefined> {
    const cached = this.lru.get(sid);
    if (cached) return cached;

    const session = await this.backing.get(sid);
    if (session) this.lru.set(sid, session);
    return session;
  }

  async update(sid: string, session: Session): Promise<void> {
    await this.backing.update(sid, session);
    this.lru.set(sid, session);
  }

  async delete(sid: string): Promise<Session | undefined> {
    this.lru.delete(sid);
    return this.backing.delete(sid);
  }

  async sweep(): Promise<void> {
    return this.backing.sweep();
  }
}
