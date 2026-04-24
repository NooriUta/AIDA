/**
 * MTN-34 — API key authentication middleware.
 *
 * Bearer sk_live_* tokens are hashed with SHA-256 and looked up in the
 * ApiKey vertex. If found and not expired, attaches a synthetic SeerUser
 * to request.user with role 'viewer' and scopes from the key record.
 *
 * Usage: add `apiKeyAuth` as preHandler before `app.authenticate` on
 * endpoints that should accept both session cookies AND API keys.
 */
import type { FastifyRequest, FastifyReply } from 'fastify';
import { createHash } from 'node:crypto';
import { config } from '../config';

const FRIGG_BASIC = Buffer.from(`${config.friggUser}:${config.friggPass}`).toString('base64');

interface ApiKeyRow {
  keyId:            string;
  serviceAccountId: string;
  hashedSecret:     string;
  expiresAt:        number | null;
  scopes:           string;
}

interface ServiceAccountRow {
  id:          string;
  tenantAlias: string;
  name:        string;
  enabled:     boolean;
}

async function lookupApiKey(hash: string): Promise<(ApiKeyRow & { tenantAlias: string; saName: string }) | null> {
  try {
    const res = await fetch(
      `${config.friggUrl}/api/v1/query/${encodeURIComponent(config.friggTenantsDb)}`,
      {
        method:  'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Basic ${FRIGG_BASIC}` },
        body:    JSON.stringify({
          language: 'sql',
          command:  `SELECT k.keyId, k.serviceAccountId, k.hashedSecret, k.expiresAt, k.scopes,
                            s.tenantAlias, s.name AS saName
                     FROM ApiKey k
                     LEFT JOIN ServiceAccount s ON s.id = k.serviceAccountId
                     WHERE k.hashedSecret = :hash AND (s.enabled = true) LIMIT 1`,
          params:   { hash },
        }),
        signal: AbortSignal.timeout(5_000),
      },
    );
    if (!res.ok) return null;
    const data = (await res.json()) as { result?: unknown[] };
    return (data.result?.[0] as (ApiKeyRow & { tenantAlias: string; saName: string }) | undefined) ?? null;
  } catch {
    return null;
  }
}

export async function apiKeyAuth(request: FastifyRequest, reply: FastifyReply): Promise<void> {
  const auth = request.headers.authorization;
  if (!auth?.startsWith('Bearer sk_live_')) return; // not an API key, fall through

  const rawToken = auth.slice('Bearer '.length);
  const hash = createHash('sha256').update(rawToken).digest('hex');
  const row  = await lookupApiKey(hash);

  if (!row) {
    return reply.status(401).send({ error: 'invalid_api_key' });
  }

  if (row.expiresAt && row.expiresAt < Date.now()) {
    return reply.status(401).send({ error: 'api_key_expired' });
  }

  const scopes = row.scopes ? row.scopes.split(' ').filter(Boolean) : [];

  request.user = {
    sub:               `sa:${row.serviceAccountId}`,
    username:          row.saName ?? row.serviceAccountId,
    role:              'viewer',
    scopes,
    activeTenantAlias: row.tenantAlias,
  };
}
