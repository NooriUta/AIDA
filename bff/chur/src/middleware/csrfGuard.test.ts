/**
 * MTN-44 — Tests for the CSRF origin guard.
 */
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import Fastify, { type FastifyInstance } from 'fastify';
import { csrfGuard } from './csrfGuard';

const originalCorsOrigin = process.env.CORS_ORIGIN;

async function buildApp(): Promise<FastifyInstance> {
  const app = Fastify({ logger: false });
  app.post('/api/admin/test', { preHandler: csrfGuard }, async () => ({ ok: true }));
  app.put ('/api/admin/test', { preHandler: csrfGuard }, async () => ({ ok: true }));
  app.delete('/api/admin/test', { preHandler: csrfGuard }, async () => ({ ok: true }));
  app.get ('/api/admin/test', { preHandler: csrfGuard }, async () => ({ ok: true }));
  await app.ready();
  return app;
}

beforeEach(() => {
  // Ensure config.ts re-reads CORS_ORIGIN — set before any import side effect
  process.env.CORS_ORIGIN = 'http://localhost:5173,http://localhost:13000';
});
afterEach(() => {
  if (originalCorsOrigin === undefined) delete process.env.CORS_ORIGIN;
  else process.env.CORS_ORIGIN = originalCorsOrigin;
});

describe('csrfGuard', () => {
  it('allows GET without headers (safe method)', async () => {
    const app = await buildApp();
    const res = await app.inject({ method: 'GET', url: '/api/admin/test' });
    expect(res.statusCode).toBe(200);
  });

  it('rejects POST without Origin or Referer', async () => {
    const app = await buildApp();
    const res = await app.inject({ method: 'POST', url: '/api/admin/test' });
    expect(res.statusCode).toBe(403);
    expect(res.json().error).toBe('csrf_origin_missing');
  });

  it('allows POST with matching Origin', async () => {
    const app = await buildApp();
    const res = await app.inject({
      method:  'POST',
      url:     '/api/admin/test',
      headers: { origin: 'http://localhost:5173' },
    });
    expect(res.statusCode).toBe(200);
  });

  it('allows POST with matching Referer (fallback when Origin absent)', async () => {
    const app = await buildApp();
    const res = await app.inject({
      method:  'POST',
      url:     '/api/admin/test',
      headers: { referer: 'http://localhost:5173/admin/tenants' },
    });
    expect(res.statusCode).toBe(200);
  });

  it('rejects POST with mismatching Origin', async () => {
    const app = await buildApp();
    const res = await app.inject({
      method:  'POST',
      url:     '/api/admin/test',
      headers: { origin: 'https://evil.example.com' },
    });
    expect(res.statusCode).toBe(403);
    const body = res.json();
    expect(body.error).toBe('csrf_origin_mismatch');
    expect(body.origin).toBe('https://evil.example.com');
  });

  it('rejects PUT and DELETE similarly', async () => {
    const app = await buildApp();
    const put = await app.inject({
      method:  'PUT', url: '/api/admin/test',
      headers: { origin: 'https://evil.example.com' },
    });
    const del = await app.inject({
      method:  'DELETE', url: '/api/admin/test',
      headers: { origin: 'https://evil.example.com' },
    });
    expect(put.statusCode).toBe(403);
    expect(del.statusCode).toBe(403);
  });

  it('bypasses check for Bearer-auth (M2M callers)', async () => {
    const app = await buildApp();
    const res = await app.inject({
      method:  'POST',
      url:     '/api/admin/test',
      headers: {
        authorization: 'Bearer eyJabc.def.ghi',
        origin:        'https://someservice.internal',  // not in allow-list; bypass anyway
      },
    });
    expect(res.statusCode).toBe(200);
  });

  it('rejects malformed Origin header', async () => {
    const app = await buildApp();
    const res = await app.inject({
      method:  'POST',
      url:     '/api/admin/test',
      headers: { origin: 'not-a-url' },
    });
    expect(res.statusCode).toBe(403);
    expect(res.json().error).toBe('csrf_origin_missing');
  });
});
