/**
 * CAP-01: ArcadeDB-backed session store.
 * Writes to the `frigg-sessions` database (FRIGG, port 2481).
 * Document type: DaliChurSession.
 */
import type { Session } from '../sessions';
import type { SessionStore } from './SessionStore';
import { encryptToken, decryptToken } from '../session/encryption';

const FRIGG_URL  = (process.env.FRIGG_URL  ?? 'http://127.0.0.1:2481').replace(/\/$/, '');
const FRIGG_DB   = process.env.FRIGG_SESSION_DB ?? 'frigg-sessions';
const FRIGG_USER = process.env.FRIGG_USER ?? 'root';
const FRIGG_PASS = process.env.FRIGG_PASSWORD ?? '';

const BASIC = Buffer.from(`${FRIGG_USER}:${FRIGG_PASS}`).toString('base64');
const HEADERS = {
  'Content-Type': 'application/json',
  'Authorization': `Basic ${BASIC}`,
} as const;

const EXPIRY_GRACE_MS = 60 * 60 * 1_000; // 1 hour beyond accessExpiresAt

// ── Database + schema bootstrap ───────────────────────────────────────────────

let dbBootstrapped = false;

async function ensureDatabase(): Promise<void> {
  if (dbBootstrapped) return;
  try {
    const res = await fetch(`${FRIGG_URL}/api/v1/databases`, {
      headers: { 'Authorization': `Basic ${BASIC}` },
      signal: AbortSignal.timeout(5_000),
    });
    if (res.ok) {
      const data = await res.json() as { result: string[] };
      if (!data.result?.includes(FRIGG_DB)) {
        await fetch(`${FRIGG_URL}/api/v1/server`, {
          method: 'POST',
          headers: HEADERS,
          body: JSON.stringify({ command: `create database ${FRIGG_DB}` }),
          signal: AbortSignal.timeout(5_000),
        });
      }
    }
    dbBootstrapped = true;
  } catch {
    // Non-fatal: schema bootstrap will surface a clearer error
  }
}

let schemaBootstrapped = false;

async function ensureSchema(): Promise<void> {
  if (schemaBootstrapped) return;
  await ensureDatabase();
  try {
    await friggSql(
      `CREATE DOCUMENT TYPE DaliChurSession IF NOT EXISTS`,
    );
    await friggSql(
      `CREATE PROPERTY DaliChurSession.sessionId IF NOT EXISTS STRING`,
    );
    await friggSql(
      `CREATE PROPERTY DaliChurSession.expiresAt IF NOT EXISTS LONG`,
    );
    await friggSql(
      `CREATE INDEX IF NOT EXISTS ON DaliChurSession (sessionId) UNIQUE`,
    );
    await friggSql(
      `CREATE INDEX IF NOT EXISTS ON DaliChurSession (expiresAt) NOTUNIQUE`,
    );
    schemaBootstrapped = true;
  } catch {
    // Non-fatal: schema may already exist
    schemaBootstrapped = true;
  }
}

// ── Internal HTTP helper ──────────────────────────────────────────────────────

async function friggSql(command: string, params?: Record<string, unknown>): Promise<unknown[]> {
  const url = `${FRIGG_URL}/api/v1/command/${encodeURIComponent(FRIGG_DB)}`;
  const res = await fetch(url, {
    method:  'POST',
    headers: HEADERS,
    body:    JSON.stringify({ language: 'sql', command, params: params ?? {} }),
    signal:  AbortSignal.timeout(5_000),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`FRIGG ${res.status}: ${text}`);
  }
  const data = await res.json() as { result: unknown[] };
  return data.result ?? [];
}

// ── Mapping ───────────────────────────────────────────────────────────────────

function sessionToDoc(sid: string, s: Session): Record<string, unknown> {
  // MTN-59: tokens encrypted at rest via AIDA_SESSION_DEK_KEY. When the key is
  // absent (dev), encryptToken() is a no-op and we fall back to plaintext.
  return {
    sessionId:       sid,
    accessToken:     encryptToken(s.accessToken),
    refreshToken:    encryptToken(s.refreshToken),
    accessExpiresAt: s.accessExpiresAt,
    expiresAt:       s.accessExpiresAt + EXPIRY_GRACE_MS,
    sub:             s.sub,
    username:        s.username,
    role:            s.role,
    scopes:          s.scopes.join(','),
    lastSeenAt:      Date.now(),
  };
}

function docToSession(doc: Record<string, unknown>): Session {
  // MTN-59: decryptToken() passes legacy plaintext through unchanged, so
  // pre-MTN-59 sessions keep working until they expire naturally (5m JWT).
  return {
    accessToken:     decryptToken(String(doc['accessToken']  ?? '')),
    refreshToken:    decryptToken(String(doc['refreshToken'] ?? '')),
    accessExpiresAt: Number(doc['accessExpiresAt'] ?? 0),
    sub:             String(doc['sub']      ?? ''),
    username:        String(doc['username'] ?? ''),
    role:            (doc['role'] as Session['role']) ?? 'viewer',
    scopes:          String(doc['scopes'] ?? '').split(',').filter(Boolean),
  };
}

// ── Store implementation ──────────────────────────────────────────────────────

export class ArcadeDbSessionStore implements SessionStore {

  async create(sid: string, session: Session): Promise<void> {
    await ensureSchema();
    const doc = sessionToDoc(sid, session);
    const cols = Object.keys(doc).map(k => `\`${k}\``).join(', ');
    const vals = Object.keys(doc).map(k => `:${k}`).join(', ');
    await friggSql(
      `INSERT INTO DaliChurSession (${cols}) VALUES (${vals})`,
      doc as Record<string, unknown>,
    );
  }

  async get(sid: string): Promise<Session | undefined> {
    await ensureSchema();
    const rows = await friggSql(
      `SELECT * FROM DaliChurSession WHERE sessionId = :sid LIMIT 1`,
      { sid },
    );
    if (!rows.length) return undefined;
    const doc = rows[0] as Record<string, unknown>;
    if (Date.now() > Number(doc['expiresAt'] ?? 0)) {
      await this.delete(sid).catch(() => {});
      return undefined;
    }
    return docToSession(doc);
  }

  async update(sid: string, session: Session): Promise<void> {
    await ensureSchema();
    const doc = sessionToDoc(sid, session);
    const sets = Object.keys(doc)
      .filter(k => k !== 'sessionId')
      .map(k => `\`${k}\` = :${k}`)
      .join(', ');
    await friggSql(
      `UPDATE DaliChurSession SET ${sets} WHERE sessionId = :sessionId`,
      doc as Record<string, unknown>,
    );
  }

  async delete(sid: string): Promise<Session | undefined> {
    await ensureSchema();
    const rows = await friggSql(
      `SELECT * FROM DaliChurSession WHERE sessionId = :sid LIMIT 1`,
      { sid },
    );
    await friggSql(
      `DELETE FROM DaliChurSession WHERE sessionId = :sid`,
      { sid },
    );
    if (!rows.length) return undefined;
    return docToSession(rows[0] as Record<string, unknown>);
  }

  async sweep(): Promise<void> {
    await ensureSchema();
    const cutoff = Date.now();
    await friggSql(
      `DELETE FROM DaliChurSession WHERE expiresAt < :cutoff`,
      { cutoff },
    );
  }
}
