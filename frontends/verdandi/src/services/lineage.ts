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
  dataSource?: string;
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
  hasMore: boolean;
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
      nodes { id type label scope dataSource }
      edges { id source target type }
      hasMore
    }
  }
`;

const LINEAGE = /* GraphQL */ `
  query Lineage($nodeId: String!) {
    lineage(nodeId: $nodeId) {
      nodes { id type label scope dataSource }
      edges { id source target type }
      hasMore
    }
  }
`;

const UPSTREAM = /* GraphQL */ `
  query Upstream($nodeId: String!) {
    upstream(nodeId: $nodeId) {
      nodes { id type label scope dataSource }
      edges { id source target type }
      hasMore
    }
  }
`;

const DOWNSTREAM = /* GraphQL */ `
  query Downstream($nodeId: String!) {
    downstream(nodeId: $nodeId) {
      nodes { id type label scope dataSource }
      edges { id source target type }
      hasMore
    }
  }
`;

const EXPAND_DEEP = /* GraphQL */ `
  query ExpandDeep($nodeId: String!, $depth: Int!) {
    expandDeep(nodeId: $nodeId, depth: $depth) {
      nodes { id type label scope dataSource }
      edges { id source target type }
      hasMore
    }
  }
`;

const STMT_COLUMNS = /* GraphQL */ `
  query StmtColumns($ids: [String]!) {
    stmtColumns(ids: $ids) {
      nodes { id type label scope dataSource }
      edges { id source target type }
      hasMore
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
  const t0 = performance.now();
  const data = await gqlClient.request<{ overview: SchemaNode[] }>(OVERVIEW);
  const ms = (performance.now() - t0).toFixed(0);
  console.info(`[LOOM] overview — ${ms} ms  (${data.overview.length} databases)`);
  return data.overview;
}

export async function fetchExplore(scope: string): Promise<ExploreResult> {
  const t0 = performance.now();
  const data = await gqlClient.request<{ explore: ExploreResult }>(EXPLORE, { scope });
  const ms = (performance.now() - t0).toFixed(0);
  const n = data.explore.nodes?.length ?? 0;
  const e = data.explore.edges?.length ?? 0;
  console.info(`[LOOM] explore(${scope}) — ${ms} ms  (${n} nodes, ${e} edges)`);
  return data.explore;
}

export async function fetchLineage(nodeId: string): Promise<ExploreResult> {
  const t0 = performance.now();
  const data = await gqlClient.request<{ lineage: ExploreResult }>(LINEAGE, { nodeId });
  const ms = (performance.now() - t0).toFixed(0);
  const n = data.lineage.nodes?.length ?? 0;
  console.info(`[LOOM] lineage(${nodeId}) — ${ms} ms  (${n} nodes)`);
  return data.lineage;
}

export async function fetchUpstream(nodeId: string): Promise<ExploreResult> {
  const t0 = performance.now();
  const data = await gqlClient.request<{ upstream: ExploreResult }>(UPSTREAM, { nodeId });
  const ms = (performance.now() - t0).toFixed(0);
  const n = data.upstream.nodes?.length ?? 0;
  console.info(`[LOOM] upstream(${nodeId}) — ${ms} ms  (${n} nodes)`);
  return data.upstream;
}

export async function fetchDownstream(nodeId: string): Promise<ExploreResult> {
  const t0 = performance.now();
  const data = await gqlClient.request<{ downstream: ExploreResult }>(DOWNSTREAM, { nodeId });
  const ms = (performance.now() - t0).toFixed(0);
  const n = data.downstream.nodes?.length ?? 0;
  console.info(`[LOOM] downstream(${nodeId}) — ${ms} ms  (${n} nodes)`);
  return data.downstream;
}

export async function fetchExpandDeep(nodeId: string, depth: number): Promise<ExploreResult> {
  const t0 = performance.now();
  const data = await gqlClient.request<{ expandDeep: ExploreResult }>(EXPAND_DEEP, { nodeId, depth });
  const ms = (performance.now() - t0).toFixed(0);
  const n = data.expandDeep.nodes?.length ?? 0;
  console.info(`[LOOM] expandDeep(${nodeId}, depth=${depth}) — ${ms} ms  (${n} nodes)`);
  return data.expandDeep;
}

export async function fetchStmtColumns(ids: string[]): Promise<ExploreResult> {
  if (ids.length === 0) return { nodes: [], edges: [], hasMore: false };
  const data = await gqlClient.request<{ stmtColumns: ExploreResult }>(STMT_COLUMNS, { ids });
  return data.stmtColumns;
}

export async function fetchSearch(query: string, limit = 20): Promise<SearchResult[]> {
  const data = await gqlClient.request<{ search: SearchResult[] }>(SEARCH, { query, limit });
  return data.search;
}

// ── KNOT types ────────────────────────────────────────────────────────────────

export interface KnotSession {
  id: string;
  sessionId: string;
  sessionName: string;
  dialect: string;
  filePath: string;
  processingMs: number;
  // Counts
  tableCount: number;
  columnCount: number;
  schemaCount: number;
  packageCount: number;
  routineCount: number;
  parameterCount: number;
  variableCount: number;
  // Statement breakdown
  stmtSelect: number;
  stmtInsert: number;
  stmtUpdate: number;
  stmtDelete: number;
  stmtMerge: number;
  stmtCursor: number;
  stmtOther: number;
  // Atoms
  atomTotal: number;
  atomResolved: number;
  atomFailed: number;
  atomConstant: number;
  atomFuncCall: number;
  // Edge counts
  edgeReadsFrom: number;
  edgeWritesTo: number;
  edgeAtomRefColumn: number;
  edgeDataFlow: number;
}

export interface KnotColumn {
  id: string;
  name: string;
  dataType: string;
  position: number;
  atomRefCount: number;
  alias: string;
  isRequired: boolean;
  isPk: boolean;
  isFk: boolean;
  fkRefTable: string;
  defaultValue: string;
  dataSource: string;
}

export interface KnotTable {
  id: string;
  geoid: string;
  name: string;
  schema: string;
  tableType: string;
  columnCount: number;
  sourceCount: number;
  targetCount: number;
  dataSource: string;    // 'master' | 'reconstructed' | ''
  aliases: string[];
}

export interface KnotTableDetail {
  tableGeoid: string;
  dataSource: string;
  columns: KnotColumn[];
  snippet: string;
}

export interface KnotSourceRef {
  name:     string;
  geoid:    string;
  aliases:  string[];
  nodeType: 'TABLE' | 'STMT';  // TABLE = DaliTable, STMT = DaliStatement (CTE/subquery)
}

export interface KnotStatement {
  id: string;
  geoid: string;
  stmtType: string;
  lineNumber: number;
  routineName: string;
  packageName: string;
  routineType: string;
  sourceTables: KnotSourceRef[];
  targetTables: KnotSourceRef[];
  stmtAliases: string[];
  atomTotal: number;
  atomResolved: number;
  atomFailed: number;
  atomConstant: number;
  atomFuncCall: number;
  children: KnotStatement[];
}

export interface KnotSnippet {
  stmtGeoid: string;
  snippet: string;
}

export interface KnotAtom {
  stmtGeoid: string;
  atomText: string;
  columnName: string;
  tableGeoid: string;
  tableName: string;
  status: string;
  atomContext: string;
  parentContext: string;
  outputColumnSequence: number | null;
  outputColName: string;
  refSourceName: string;    // ATOM_REF_OUTPUT_COL → DaliOutputColumn.output_col_name
  refStmtGeoid: string;     // ATOM_REF_STMT        → DaliStatement.stmt_geoid
  refColEdge: string;       // ATOM_REF_COLUMN      → DaliColumn.column_name
  refTblEdge: string;       // ATOM_REF_TABLE       → DaliTable.table_name
  refTblGeoidEdge: string;  // ATOM_REF_TABLE       → DaliTable.table_geoid (NOT @rid!)
  columnReference: boolean;
  functionCall: boolean;
  constant: boolean;
  complex: boolean;
  routineParam: boolean;
  routineVar: boolean;
  nestedAtomsCount: number | null;
  atomLine: number;
  atomPos: number;
}

export interface KnotOutputColumnAtom {
  text:   string;
  col:    string;
  tbl:    string;
  status: string;
}

export interface KnotOutputColumn {
  stmtGeoid: string;
  name: string;
  expression: string;
  alias: string;
  colOrder: number;
  sourceType: string;
  tableRef: string;
  atoms: KnotOutputColumnAtom[];
}

export interface KnotCall {
  callerName: string;
  callerPackage: string;
  calleeName: string;
  lineStart: number;
}

export interface KnotParameter {
  routineName: string;
  paramName: string;
  dataType: string;
  direction: string;  // IN, OUT, IN OUT
}

export interface KnotVariable {
  routineName: string;
  varName: string;
  dataType: string;
}

export interface KnotAffectedColumn {
  stmtGeoid: string;
  columnName: string;
  tableName: string;
  position: number;
}

export interface KnotRoutine {
  routineName: string;
  routineType: string;
  packageGeoid: string;
}

export interface KnotReport {
  session: KnotSession;
  tables: KnotTable[];
  routines: KnotRoutine[];
  statements: KnotStatement[];
  snippets: KnotSnippet[];
  atoms: KnotAtom[];
  outputColumns: KnotOutputColumn[];
  affectedColumns: KnotAffectedColumn[];
  calls: KnotCall[];
  parameters: KnotParameter[];
  variables: KnotVariable[];
}

// ── KNOT queries ──────────────────────────────────────────────────────────────

const KNOT_SESSIONS = /* GraphQL */ `
  query KnotSessions {
    knotSessions {
      id sessionId sessionName dialect filePath processingMs
      tableCount columnCount schemaCount packageCount routineCount
      parameterCount variableCount
      stmtSelect stmtInsert stmtUpdate stmtDelete stmtMerge stmtCursor stmtOther
      atomTotal atomResolved atomFailed atomConstant atomFuncCall
      edgeReadsFrom edgeWritesTo edgeAtomRefColumn edgeDataFlow
    }
  }
`;

const KNOT_REPORT = /* GraphQL */ `
  query KnotReport($sessionId: String!) {
    knotReport(sessionId: $sessionId) {
      session {
        id sessionId sessionName dialect filePath processingMs
        tableCount columnCount schemaCount packageCount routineCount
        parameterCount variableCount
        stmtSelect stmtInsert stmtUpdate stmtDelete stmtMerge stmtCursor stmtOther
        atomTotal atomResolved atomFailed atomConstant atomFuncCall
        edgeReadsFrom edgeWritesTo edgeAtomRefColumn edgeDataFlow
      }
      tables {
        id geoid name schema tableType columnCount sourceCount targetCount dataSource aliases
      }
      routines {
        routineName routineType packageGeoid
      }
      statements {
        id geoid stmtType lineNumber routineName packageName routineType stmtAliases
        sourceTables { name geoid aliases nodeType }
        targetTables { name geoid aliases nodeType }
        atomTotal atomResolved atomFailed atomConstant atomFuncCall
        children {
          id geoid stmtType lineNumber routineName packageName routineType stmtAliases
          sourceTables { name geoid aliases nodeType }
          targetTables { name geoid aliases nodeType }
          atomTotal atomResolved atomFailed atomConstant atomFuncCall
          children {
            id geoid stmtType lineNumber routineName packageName routineType stmtAliases
            sourceTables { name geoid aliases }
            targetTables { name geoid aliases }
            atomTotal atomResolved atomFailed atomConstant atomFuncCall
            children {
              id geoid stmtType lineNumber routineName packageName routineType stmtAliases
              sourceTables { name geoid aliases }
              targetTables { name geoid aliases }
              atomTotal atomResolved atomFailed atomConstant atomFuncCall
              children {
                id geoid stmtType lineNumber
                sourceTables { name geoid aliases }
                targetTables { name geoid aliases }
                atomTotal atomFuncCall
              }
            }
          }
        }
      }
      snippets {
        stmtGeoid snippet
      }
      atoms {
        stmtGeoid atomText columnName tableGeoid tableName
        status atomContext parentContext outputColumnSequence outputColName refSourceName
        refStmtGeoid refColEdge refTblEdge refTblGeoidEdge
        columnReference functionCall constant
        complex routineParam routineVar nestedAtomsCount atomLine atomPos
      }
      outputColumns {
        stmtGeoid name expression alias colOrder sourceType tableRef
        atoms { text col tbl status }
      }
      affectedColumns {
        stmtGeoid columnName tableName position
      }
      calls {
        callerName callerPackage calleeName lineStart
      }
      parameters {
        routineName paramName dataType direction
      }
      variables {
        routineName varName dataType
      }
    }
  }
`;

// ── KNOT service functions ────────────────────────────────────────────────────

export async function fetchKnotSessions(): Promise<KnotSession[]> {
  const data = await gqlClient.request<{ knotSessions: KnotSession[] }>(KNOT_SESSIONS);
  return data.knotSessions;
}

export async function fetchKnotReport(sessionId: string): Promise<KnotReport> {
  const data = await gqlClient.request<{ knotReport: KnotReport }>(KNOT_REPORT, { sessionId });
  return data.knotReport;
}

const KNOT_TABLE_DETAIL = /* GraphQL */ `
  query KnotTableDetail($sessionId: String!, $tableGeoid: String!) {
    knotTableDetail(sessionId: $sessionId, tableGeoid: $tableGeoid) {
      tableGeoid
      dataSource
      columns {
        id name dataType position atomRefCount alias
        isRequired isPk isFk fkRefTable defaultValue dataSource
      }
      snippet
    }
  }
`;

export async function fetchKnotTableDetail(
  sessionId: string,
  tableGeoid: string,
): Promise<KnotTableDetail> {
  const data = await gqlClient.request<{ knotTableDetail: KnotTableDetail }>(
    KNOT_TABLE_DETAIL,
    { sessionId, tableGeoid },
  );
  return data.knotTableDetail;
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
