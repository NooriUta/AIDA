import type { FastifyPluginAsync } from 'fastify';
import { requireAdmin }              from '../middleware/requireAdmin';

const FRIGG_URL  = (process.env.FRIGG_URL  ?? 'http://127.0.0.1:2481').replace(/\/$/, '');
const FRIGG_USER = process.env.FRIGG_USER  ?? 'root';
const FRIGG_PASS = process.env.FRIGG_PASS  ?? 'playwithdata';
const FRIGG_BASIC = Buffer.from(`${FRIGG_USER}:${FRIGG_PASS}`).toString('base64');

const YGG_URL   = (process.env.YGG_URL  ?? 'http://127.0.0.1:2480').replace(/\/$/, '');
const YGG_USER  = process.env.YGG_USER  ?? 'root';
const YGG_PASS  = process.env.YGG_PASS  ?? 'playwithdata';
const YGG_BASIC = Buffer.from(`${YGG_USER}:${YGG_PASS}`).toString('base64');

interface ClusterSpec { id: 'frigg' | 'ygg'; url: string; auth: string; port: number }
const CLUSTERS: ClusterSpec[] = [
  { id: 'frigg', url: FRIGG_URL, auth: FRIGG_BASIC, port: new URL(FRIGG_URL).port ? Number(new URL(FRIGG_URL).port) : 2481 },
  { id: 'ygg',   url: YGG_URL,   auth: YGG_BASIC,   port: new URL(YGG_URL).port   ? Number(new URL(YGG_URL).port)   : 2480 },
];

/** Narrow profiler metrics we surface to the FE — ArcadeDB returns many more. */
interface ClusterMetrics {
  readonly cacheHitPct:     number | null;
  readonly queriesPerMin:   number | null;
  readonly walBytesWritten: number | null;
  readonly openFiles:       number | null;
}

interface ClusterHealth {
  id:        'frigg' | 'ygg';
  port:      number;
  health:    'up' | 'down';
  latencyMs: number | null;
  version:   string | null;
  dbs:       string[];
  metrics:   ClusterMetrics | null;
  error?:    string;
}

async function probeCluster(c: ClusterSpec): Promise<ClusterHealth> {
  const common = {
    headers: { 'Authorization': `Basic ${c.auth}` },
    signal:  AbortSignal.timeout(3_000),
  } as const;
  const started = Date.now();
  try {
    const [dbsRes, srvRes] = await Promise.all([
      fetch(`${c.url}/api/v1/databases`, common),
      fetch(`${c.url}/api/v1/server`,    common),
    ]);
    const latencyMs = Date.now() - started;
    if (!dbsRes.ok || !srvRes.ok) {
      return { id: c.id, port: c.port, health: 'down', latencyMs, version: null, dbs: [], metrics: null,
               error: `HTTP ${dbsRes.status}/${srvRes.status}` };
    }
    const dbsJson = await dbsRes.json() as { result?: string[] };
    const srvJson = await srvRes.json() as {
      version?: string;
      metrics?: { profiler?: Record<string, { count?: number; value?: number; space?: number; reqPerMinLastMinute?: number }> };
    };
    const p = srvJson.metrics?.profiler ?? {};
    const hits = p.pageCacheHits?.count  ?? 0;
    const miss = p.pageCacheMiss?.count  ?? 0;
    const total = hits + miss;
    const metrics: ClusterMetrics = {
      cacheHitPct:     total > 0 ? Math.round((hits / total) * 1000) / 10 : null,
      queriesPerMin:   p.queries?.reqPerMinLastMinute ?? null,
      walBytesWritten: p.walBytesWritten?.space ?? null,
      openFiles:       p.totalOpenFiles?.count ?? null,
    };
    return {
      id: c.id, port: c.port, health: 'up', latencyMs,
      version: srvJson.version ?? null,
      dbs:     (dbsJson.result ?? []).slice().sort(),
      metrics,
    };
  } catch (err) {
    return { id: c.id, port: c.port, health: 'down',
             latencyMs: Date.now() - started,
             version: null, dbs: [], metrics: null,
             error: (err as Error).message };
  }
}

/**
 * GET /heimdall/databases — live ArcadeDB cluster inventory.
 *
 * Proxies /api/v1/databases + /api/v1/server for frigg (:2481) and ygg (:2480).
 * Admin-only. Returns list of real DB names + cluster-level profiler metrics
 * (cache-hit %, queries/min, WAL bytes, open files). Per-DB size is NOT
 * exposed — ArcadeDB has no REST endpoint for it and running count(*) per DB
 * is too expensive for a polled endpoint. Caller can open ArcadeDB Studio
 * for deep inspection.
 */
export const databasesRoutes: FastifyPluginAsync = async (app) => {
  app.get(
    '/heimdall/databases',
    { preHandler: [app.authenticate, requireAdmin] },
    async (_request, reply) => {
      const clusters = await Promise.all(CLUSTERS.map(probeCluster));
      return reply.send({ clusters });
    },
  );
};
