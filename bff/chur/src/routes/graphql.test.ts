import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import Fastify from 'fastify';
import cookie from '@fastify/cookie';
import fastifyWebsocket from '@fastify/websocket';
import WebSocket from 'ws';
import { graphqlRoutes } from './graphql';

// ── Mock fetch for HTTP handlers ─────────────────────────────────────────────
const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

// ── Mock ws module for upstream WS connections ───────────────────────────────
vi.mock('ws', async () => {
  const EventEmitter = (await import('events')).EventEmitter;
  class MockWebSocket extends EventEmitter {
    static OPEN = 1;
    readyState = 1; // OPEN
    send  = vi.fn();
    close = vi.fn();
    terminate = vi.fn();
  }
  return { default: MockWebSocket };
});

// ── Minimal Fastify app factory ──────────────────────────────────────────────
async function buildApp() {
  const app = Fastify({ logger: false });
  await app.register(cookie);
  await app.register(fastifyWebsocket);

  app.decorate('authenticate', async (request: any, reply: any) => {
    const sid = request.cookies?.sid;
    if (sid === 'valid-token') {
      request.user = { sub: 'u1', username: 'admin', role: 'admin', scopes: [] };
    } else {
      return reply.status(401).send({ error: 'Unauthorized' });
    }
  });

  await app.register(graphqlRoutes, { prefix: '/graphql' });
  await app.ready();
  return app;
}

describe('POST /graphql — HTTP proxy', () => {
  let app: Awaited<ReturnType<typeof buildApp>>;

  beforeEach(async () => { app = await buildApp(); });
  afterEach(async () => { await app.close(); mockFetch.mockReset(); });

  it('proxies mutation to lineage-api and returns result', async () => {
    mockFetch.mockResolvedValueOnce({
      status: 200,
      json: async () => ({ data: { ok: true } }),
    });

    const res = await app.inject({
      method: 'POST',
      url: '/graphql',
      cookies: { sid: 'valid-token' },
      payload: { query: 'mutation { resetDemoState }' },
    });

    expect(res.statusCode).toBe(200);
    expect(JSON.parse(res.body)).toEqual({ data: { ok: true } });
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/graphql'),
      expect.objectContaining({
        headers: expect.objectContaining({
          'X-Seer-Role': 'admin',
          'X-Seer-User': 'admin',
        }),
      }),
    );
  });

  it('returns 401 when no session cookie', async () => {
    const res = await app.inject({ method: 'POST', url: '/graphql', payload: {} });
    expect(res.statusCode).toBe(401);
  });

  it('returns 503 when lineage-api is unreachable', async () => {
    mockFetch.mockRejectedValueOnce(new Error('ECONNREFUSED'));
    const res = await app.inject({
      method: 'POST',
      url: '/graphql',
      cookies: { sid: 'valid-token' },
      payload: { query: '{ __typename }' },
    });
    expect(res.statusCode).toBe(503);
  });
});

describe('GET /graphql — introspection passthrough', () => {
  let app: Awaited<ReturnType<typeof buildApp>>;

  beforeEach(async () => { app = await buildApp(); });
  afterEach(async () => { await app.close(); mockFetch.mockReset(); });

  it('proxies introspection query to lineage-api', async () => {
    mockFetch.mockResolvedValueOnce({
      status: 200,
      json: async () => ({ data: { __schema: {} } }),
    });

    const res = await app.inject({
      method: 'GET',
      url: '/graphql?query={__typename}',
      cookies: { sid: 'valid-token' },
    });

    expect(res.statusCode).toBe(200);
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/graphql'),
      expect.objectContaining({
        headers: expect.objectContaining({ 'X-Seer-Role': 'admin' }),
      }),
    );
  });
});

describe('WS /graphql — graphql-ws subscription proxy', () => {
  let app: Awaited<ReturnType<typeof buildApp>>;

  beforeEach(async () => { app = await buildApp(); });
  afterEach(async () => { await app.close(); });

  it('rejects WebSocket upgrade without session cookie', async () => {
    const res = await app.inject({
      method: 'GET',
      url: '/graphql',
      headers: {
        connection: 'upgrade',
        upgrade: 'websocket',
        'sec-websocket-key': Buffer.from('test').toString('base64'),
        'sec-websocket-version': '13',
      },
    });
    // Fastify inject doesn't complete WS upgrade — preHandler runs and 401s
    expect(res.statusCode).toBe(401);
  });
});
