/**
 * AIDA QG-PERFORMANCE-weekly — k6 load test skeleton.
 *
 * SLOs (from PROMPT.md §7.1):
 *   p50 ≤ 200 ms, p95 ≤ 800 ms, error_rate < 1%
 *
 * Usage:
 *   # Quick smoke (local dev)
 *   k6 run scripts/load-test.js
 *
 *   # Weekly QG run (CI / performance-weekly scheduled task)
 *   AIDA_BASE_URL=https://seer.test.seidrstudio.pro \
 *   AIDA_TOKEN=<bearer> \
 *   k6 run --out json=/tmp/k6-results.json scripts/load-test.js
 *
 *   # With Prometheus remote-write
 *   k6 run --out experimental-prometheus-rw scripts/load-test.js
 *
 * Environment variables:
 *   AIDA_BASE_URL   — base URL of the shell / BFF (default: http://localhost:3000)
 *   SHUTTLE_URL     — Shuttle GraphQL endpoint (default: http://localhost:8080)
 *   HEIMDALL_URL    — Heimdall backend endpoint (default: http://localhost:8090)
 *   AIDA_TOKEN      — Bearer token for authenticated requests
 *   TENANT_ALIAS    — Tenant alias for MT routing headers (default: acme)
 *   K6_SCENARIO     — 'smoke' | 'load' | 'stress' | 'soak' (default: load)
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// ── Custom metrics ────────────────────────────────────────────────────────────

const errorRate      = new Rate('aida_errors');
const shuttleLatency = new Trend('shuttle_latency',   true);
const churLatency    = new Trend('chur_latency',      true);
const heimdallLatency= new Trend('heimdall_latency',  true);
const requestCount   = new Counter('aida_requests');

// ── Environment ───────────────────────────────────────────────────────────────

const BASE_URL     = __ENV.AIDA_BASE_URL   || 'http://localhost:3000';
const SHUTTLE_URL  = __ENV.SHUTTLE_URL     || 'http://localhost:8080';
const HEIMDALL_URL = __ENV.HEIMDALL_URL    || 'http://localhost:8090';
const TOKEN        = __ENV.AIDA_TOKEN      || '';
const TENANT       = __ENV.TENANT_ALIAS    || 'acme';
const SCENARIO     = __ENV.K6_SCENARIO     || 'load';

// ── Scenarios ─────────────────────────────────────────────────────────────────

const SCENARIOS = {
  smoke: {
    executor: 'constant-vus',
    vus: 2,
    duration: '30s',
    tags: { scenario: 'smoke' },
  },
  load: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '30s', target: 10 },   // ramp-up
      { duration: '2m',  target: 10 },   // steady state
      { duration: '30s', target: 0 },    // ramp-down
    ],
    tags: { scenario: 'load' },
  },
  stress: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '1m',  target: 30 },
      { duration: '3m',  target: 30 },
      { duration: '1m',  target: 60 },
      { duration: '3m',  target: 60 },
      { duration: '1m',  target: 0 },
    ],
    tags: { scenario: 'stress' },
  },
  soak: {
    executor: 'constant-vus',
    vus: 5,
    duration: '30m',
    tags: { scenario: 'soak' },
  },
};

// ── Options ───────────────────────────────────────────────────────────────────

export const options = {
  scenarios: { [SCENARIO]: SCENARIOS[SCENARIO] },

  thresholds: {
    // SLOs from PROMPT.md §7.1
    'http_req_duration{endpoint:shuttle-graphql}': ['p(50)<200', 'p(95)<800'],
    'http_req_duration{endpoint:chur-me}':         ['p(50)<200', 'p(95)<800'],
    'http_req_duration{endpoint:heimdall-events}': ['p(50)<200', 'p(95)<800'],
    'http_req_failed':                             ['rate<0.01'],  // < 1% errors
    'aida_errors':                                 ['rate<0.01'],
    // Per-service custom trends (for Prometheus baseline-diff)
    'shuttle_latency':  ['p(95)<800'],
    'chur_latency':     ['p(95)<800'],
    'heimdall_latency': ['p(95)<800'],
  },

  // HTML summary for CI artefact upload
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(50)', 'p(90)', 'p(95)', 'p(99)'],
};

// ── Shared headers ────────────────────────────────────────────────────────────

function headers(extra = {}) {
  return {
    'Content-Type':   'application/json',
    'X-Tenant-Alias': TENANT,
    ...(TOKEN ? { 'Authorization': `Bearer ${TOKEN}` } : {}),
    ...extra,
  };
}

// ── VU lifecycle ──────────────────────────────────────────────────────────────

export default function () {
  group('chur — BFF health', () => {
    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/health`, {
      headers: headers(),
      tags:    { endpoint: 'chur-health' },
    });
    churLatency.add(Date.now() - start);
    requestCount.add(1);

    const ok = check(res, {
      'chur /health 200': (r) => r.status === 200,
    });
    errorRate.add(!ok);
  });

  group('chur — /me (authenticated)', () => {
    if (!TOKEN) return;   // skip if no auth token provided

    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/me`, {
      headers: headers(),
      tags:    { endpoint: 'chur-me' },
    });
    churLatency.add(Date.now() - start);
    requestCount.add(1);

    const ok = check(res, {
      'chur /me 200': (r) => r.status === 200,
      'chur /me has sub': (r) => {
        try { return JSON.parse(r.body).sub !== undefined; } catch { return false; }
      },
    });
    errorRate.add(!ok);
  });

  group('shuttle — GraphQL introspection', () => {
    const query = JSON.stringify({ query: '{ __typename }' });
    const start = Date.now();
    const res = http.post(`${SHUTTLE_URL}/graphql`, query, {
      headers: headers(),
      tags:    { endpoint: 'shuttle-graphql' },
    });
    shuttleLatency.add(Date.now() - start);
    requestCount.add(1);

    const ok = check(res, {
      'shuttle graphql 200':  (r) => r.status === 200,
      'shuttle no errors':    (r) => {
        try {
          const body = JSON.parse(r.body);
          return !body.errors || body.errors.length === 0;
        } catch { return false; }
      },
    });
    errorRate.add(!ok);
  });

  group('heimdall — POST /events (heartbeat)', () => {
    const event = JSON.stringify({
      timestamp:       Date.now(),
      sourceComponent: 'k6-load-test',
      eventType:       'seer.platform.k6_heartbeat',
      level:           'INFO',
      payload:         { vus: __VU, iter: __ITER },
    });

    const start = Date.now();
    const res = http.post(`${HEIMDALL_URL}/events`, event, {
      headers: headers(),
      tags:    { endpoint: 'heimdall-events' },
    });
    heimdallLatency.add(Date.now() - start);
    requestCount.add(1);

    const ok = check(res, {
      'heimdall /events accepted': (r) => r.status === 202,
    });
    errorRate.add(!ok);
  });

  // Think time between iterations (realistic user pacing)
  sleep(1 + Math.random());
}

// ── Teardown: print baseline comparison hint ──────────────────────────────────

export function handleSummary(data) {
  const p50shuttle = data.metrics['shuttle_latency']?.values?.['p(50)'] ?? '—';
  const p95shuttle = data.metrics['shuttle_latency']?.values?.['p(95)'] ?? '—';
  const p50chur    = data.metrics['chur_latency']?.values?.['p(50)']    ?? '—';
  const p95chur    = data.metrics['chur_latency']?.values?.['p(95)']    ?? '—';
  const errRate    = (data.metrics['aida_errors']?.values?.rate ?? 0) * 100;

  const summary = [
    '═══════════════════════════════════════════════',
    ' AIDA QG-PERFORMANCE-weekly — k6 result',
    '═══════════════════════════════════════════════',
    ` Shuttle  p50=${Math.round(p50shuttle)}ms  p95=${Math.round(p95shuttle)}ms`,
    ` Chur     p50=${Math.round(p50chur)}ms  p95=${Math.round(p95chur)}ms`,
    ` Error rate: ${errRate.toFixed(2)}%`,
    ` SLO: p50≤200ms / p95≤800ms / err<1%`,
    '═══════════════════════════════════════════════',
    '',
    ' Copy these values into docs/review_fact/data/YYYY-MM-DD.json',
    ' under performance{}.slo{}',
    '═══════════════════════════════════════════════',
  ].join('\n');

  // Print to stdout AND write machine-readable JSON for CI
  return {
    stdout: summary + '\n',
    'k6-summary.json': JSON.stringify(data, null, 2),
  };
}
