// Central registry of AIDA services — single source of truth for ports,
// categories, URLs. Used by ServicesPage, topology, dev shortcuts.
//
// TODO: long-term this should come from `/heimdall/services/registry` so
// the server-side catalog (chur `services/health` checks) and the FE stay
// in sync. For now it mirrors docker-compose.yml + scripts/.
//
// Port hygiene:
//   - Platform services expose one public port per mode (dev vs docker).
//   - `portDev` — binding when running via `pnpm dev` / `quarkus dev` locally.
//   - `portDocker` — binding inside docker-compose (may coincide with dev).

export type ServiceCategory = 'platform' | 'worker' | 'integration';

/** Topology layer: L0=edge (nginx), L1=client shell, L2=remote MFs, L3=BFF, L4=services, L5=auth/worker, L6=storage. */
export type TopologyLayer = 0 | 1 | 2 | 3 | 4 | 5 | 6;

export interface ServiceSpec {
  /** Stable identifier returned by chur `/services/health`. */
  id:         string;
  /** Human-facing name. */
  label:      string;
  category:   ServiceCategory;
  portDev?:   number;
  portDocker?: number;
  /** "Open service" link in dev mode (local pnpm/quarkus dev). */
  devUrl?:    string;
  /** "Open service" link in docker mode (host-mapped external URL). */
  dockerUrl?: string;
  /** Short description (shown in detail drawer — TODO). */
  summary?:   string;
  /** Topology layer (1..6) — used by ServiceTopology auto-layout. */
  layer?:     TopologyLayer;
  /** IDs of services this service calls — drives topology edges. */
  deps?:      readonly string[];
  /** Edge label for the call to each dep (by index; falls back to "→"). */
  depLabels?: readonly string[];
  /** Accent colour for the node (CSS variable or hex). */
  color?:     string;
}

export const SERVICES: ServiceSpec[] = [
  // L0 — edge: TLS termination + per-vhost routing (seer.* + heimdall.*)
  { id: 'nginx',              label: 'nginx (edge)',       category: 'platform',                portDocker: 443,                                                            dockerUrl: 'https://seer.local',
    layer: 0, color: 'var(--inf)',
    deps: ['shell', 'verdandi', 'heimdall-frontend', 'chur'],
    depLabels: ['/', 'seer.*', 'heimdall.*', '/auth,/api,/graphql,/heimdall'] },

  // Platform — always global, one instance per cluster
  { id: 'shell',              label: 'shell',              category: 'platform', portDev: 5175, portDocker: 25175, devUrl: 'http://localhost:5175',                         dockerUrl: 'http://localhost:25175',
    layer: 1, color: 'var(--acc)',
    deps: ['heimdall-frontend', 'verdandi'], depLabels: ['MF', 'MF'] },
  { id: 'heimdall-frontend',  label: 'heimdall UI',        category: 'platform', portDev: 5174, portDocker: 25174, devUrl: 'http://localhost:5174/heimdall',                dockerUrl: 'http://localhost:25174/heimdall',
    layer: 2, color: 'var(--inf)',
    deps: ['chur', 'heimdall-backend'], depLabels: ['auth', 'REST/WS'] },
  { id: 'verdandi',           label: 'verdandi',           category: 'platform', portDev: 5173, portDocker: 15173, devUrl: 'http://localhost:5173',                         dockerUrl: 'http://localhost:15173',
    layer: 2, color: 'var(--inf)',
    deps: ['chur', 'shuttle'], depLabels: ['auth', 'GraphQL'] },
  { id: 'chur',               label: 'chur',               category: 'platform', portDev: 3000, portDocker: 13000,
    layer: 3, color: 'var(--suc)',
    deps: ['keycloak', 'heimdall-backend'], depLabels: ['OIDC', 'proxy'] },
  { id: 'heimdall-backend',   label: 'heimdall-backend',   category: 'platform', portDev: 9093, portDocker: 19093, devUrl: 'http://localhost:9093/q/dev-ui',                dockerUrl: 'http://localhost:19093/q/health',
    layer: 4, color: 'var(--suc)',
    deps: ['frigg'], depLabels: ['SQL'] },
  { id: 'shuttle',            label: 'shuttle',            category: 'platform', portDev: 8080, portDocker: 18080, devUrl: 'http://localhost:8080/graphql',                 dockerUrl: 'http://localhost:18080/graphql',
    layer: 4, color: 'var(--suc)',
    deps: ['ygg'], depLabels: ['SQL'] },
  { id: 'keycloak',           label: 'keycloak',           category: 'platform',                portDocker: 18180,                                                          dockerUrl: 'http://localhost:18180/admin/aida/console/',
    layer: 5, color: 'var(--wrn)' },

  // Workers — spawned per tenant or per schedule
  // Note: dali pushes events UP into heimdall-backend (POST /events) —
  //       heimdall-backend does NOT call dali.
  { id: 'dali',               label: 'dali-worker',        category: 'worker',   portDev: 9090, portDocker: 19090, devUrl: 'http://localhost:9090/q/dev-ui',                dockerUrl: 'http://localhost:19090/q/health',
    layer: 5, color: 'var(--acc)',
    deps: ['heimdall-backend', 'frigg', 'ygg'],
    depLabels: ['events', 'write', 'write'] },

  // External integrations (none deployed in current docker-compose).
  // When re-introduced add portDocker + layer to make them topology-visible.
];

// Storage — not services (no health poll), but they're topology nodes.
// Rendered as ArcadeDB clusters at layer 6.
export interface StorageNode {
  id:       'frigg' | 'ygg';
  label:    string;
  layer:    TopologyLayer;
  color:    string;
  portExt:  number;
}
export const STORAGE: StorageNode[] = [
  { id: 'frigg', label: 'frigg (ArcadeDB)', layer: 6, color: 'var(--t3)', portExt: 2481 },
  { id: 'ygg',   label: 'ygg (ArcadeDB)',   layer: 6, color: 'var(--t3)', portExt: 2480 },
];

export const BY_ID: Record<string, ServiceSpec> =
  Object.fromEntries(SERVICES.map(s => [s.id, s]));

// ── Dev-only tools (browser shortcuts) — hidden outside vite dev build ───────
export interface DevTool {
  id:      string;
  label:   string;
  meta:    string;
  url?:    string;
}

// Keycloak runs only in Docker — its admin UI is accessed through the same
// :8081 port regardless of where the FE runs, so it is NOT a dev-only tool.
export const DEV_TOOLS: DevTool[] = [
  { id: 'arcade',   label: 'ArcadeDB Studio', meta: ':2480',  url: 'http://localhost:2480/studio' },
  { id: 'jobrunr',  label: 'JobRunr',         meta: ':18080', url: 'http://localhost:18080/jobrunr/dashboard' },
  { id: 'graphiql', label: 'GraphiQL',        meta: ':19090', url: 'http://localhost:19090/graphql' },
  { id: 'demo',     label: 'Demo / Debug',    meta: 'local',  url: '/heimdall/demodebug' },
];

// ── Latency thresholds — kept here so FE + alerts stay aligned ───────────────
export const LATENCY_GOOD_MAX = 50;   // ≤50 ms → green
export const LATENCY_WARN_MAX = 200;  // 51..200 ms → yellow; >200 ms → red
