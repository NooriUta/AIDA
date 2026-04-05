import { GraphQLClient, ClientError } from 'graphql-request';

// ── Endpoint ──────────────────────────────────────────────────────────────────
// Production: requests go through rbac-proxy (absolute URL via VITE_GRAPHQL_URL).
// Dev: graphql-request v7 requires an absolute URL; build from location.origin
//      so the Vite dev-server proxy (/graphql → localhost:8080) still applies.
const ENDPOINT = import.meta.env.VITE_GRAPHQL_URL
  ?? `${location.origin}/graphql`;

const gqlClient = new GraphQLClient(ENDPOINT, {
  credentials: 'include',  // send httpOnly JWT cookie cross-origin
});

// ── Domain types (mirror GraphQL schema from lineage-api) ─────────────────────

export interface SchemaNode {
  id: string;
  name: string;
  tableCount: number;
  routineCount: number;
  packageCount: number;
  // L1 hierarchy refs — populated by SHUTTLE once DaliDatabase/DaliApplication are registered.
  // Absent/null → LOOM falls back to synthetic grouping (HoundDB / System-N stubs).
  databaseGeoid?:    string;  // @rid of parent DaliDatabase vertex
  databaseName?:     string;  // database_name property
  databaseEngine?:   string;  // database_engine (e.g. "PostgreSQL", "Oracle")
  applicationGeoid?: string;  // @rid of parent DaliApplication vertex
  applicationName?:  string;  // app_name property
}

export interface GraphNode {
  id: string;
  type: string;
  label: string;
  scope: string;
}

export interface GraphEdge {
  id: string;
  source: string;
  target: string;
  type: string;
}

export interface ExploreResult {
  nodes: GraphNode[];
  edges: GraphEdge[];
}

export interface SearchResult {
  id: string;
  type: string;
  label: string;
  scope: string;
  score: number;
}

// ── Queries ───────────────────────────────────────────────────────────────────

const OVERVIEW = /* GraphQL */ `
  query Overview {
    overview {
      id
      name
      tableCount
      routineCount
      packageCount
      databaseGeoid
      databaseName
      databaseEngine
      applicationGeoid
      applicationName
    }
  }
`;

const EXPLORE = /* GraphQL */ `
  query Explore($scope: String!) {
    explore(scope: $scope) {
      nodes { id type label scope }
      edges { id source target type }
    }
  }
`;

const LINEAGE = /* GraphQL */ `
  query Lineage($nodeId: String!) {
    lineage(nodeId: $nodeId) {
      nodes { id type label scope }
      edges { id source target type }
    }
  }
`;

const UPSTREAM = /* GraphQL */ `
  query Upstream($nodeId: String!) {
    upstream(nodeId: $nodeId) {
      nodes { id type label scope }
      edges { id source target type }
    }
  }
`;

const DOWNSTREAM = /* GraphQL */ `
  query Downstream($nodeId: String!) {
    downstream(nodeId: $nodeId) {
      nodes { id type label scope }
      edges { id source target type }
    }
  }
`;

const SEARCH = /* GraphQL */ `
  query Search($query: String!, $limit: Int) {
    search(query: $query, limit: $limit) {
      id type label scope score
    }
  }
`;

// ── Service functions ─────────────────────────────────────────────────────────

export async function fetchOverview(): Promise<SchemaNode[]> {
  const data = await gqlClient.request<{ overview: SchemaNode[] }>(OVERVIEW);
  return data.overview;
}

export async function fetchExplore(scope: string): Promise<ExploreResult> {
  const data = await gqlClient.request<{ explore: ExploreResult }>(EXPLORE, { scope });
  return data.explore;
}

export async function fetchLineage(nodeId: string): Promise<ExploreResult> {
  const data = await gqlClient.request<{ lineage: ExploreResult }>(LINEAGE, { nodeId });
  return data.lineage;
}

export async function fetchUpstream(nodeId: string): Promise<ExploreResult> {
  const data = await gqlClient.request<{ upstream: ExploreResult }>(UPSTREAM, { nodeId });
  return data.upstream;
}

export async function fetchDownstream(nodeId: string): Promise<ExploreResult> {
  const data = await gqlClient.request<{ downstream: ExploreResult }>(DOWNSTREAM, { nodeId });
  return data.downstream;
}

export async function fetchSearch(query: string, limit = 20): Promise<SearchResult[]> {
  const data = await gqlClient.request<{ search: SearchResult[] }>(SEARCH, { query, limit });
  return data.search;
}

// ── Error helpers ─────────────────────────────────────────────────────────────

/** Returns true if the error is a 401 (session expired) */
export function isUnauthorized(err: unknown): boolean {
  if (err instanceof ClientError) {
    return err.response?.status === 401;
  }
  return false;
}

/** Returns true if the lineage-api is unreachable (503) */
export function isUnavailable(err: unknown): boolean {
  if (err instanceof ClientError) {
    return err.response?.status === 503;
  }
  return false;
}
