// ─── Dali Node Types ────────────────────────────────────────────────────────
export type DaliNodeType =
  | 'DaliApplication'
  | 'DaliService'
  | 'DaliDatabase'
  | 'DaliSchema'
  | 'DaliPackage'
  | 'DaliTable'
  | 'DaliColumn'
  | 'DaliJoin'
  | 'DaliRoutine'
  | 'DaliStatement'
  | 'DaliSession'
  | 'DaliAtom'
  | 'DaliOutputColumn'
  | 'DaliParameter'
  | 'DaliVariable'
  | 'DaliAffectedColumn';

// ─── Dali Edge Types ─────────────────────────────────────────────────────────
export type DaliEdgeType =
  | 'HAS_DATABASE'      // Application → DaliDatabase (система владеет СУБД)
  | 'CONTAINS_SCHEMA'   // DaliDatabase → DaliSchema (СУБД содержит схему)
  | 'HAS_SERVICE'       // зарезервировано — будущее использование
  | 'USES_DATABASE'     // зарезервировано — будущее использование
  | 'HAS_ATOM'
  | 'ATOM_REF_COLUMN'
  | 'ATOM_REF_TABLE'
  | 'HAS_COLUMN'
  | 'HAS_JOIN'
  | 'READS_FROM'
  | 'WRITES_TO'
  | 'CONTAINS_STMT'
  | 'CONTAINS_ROUTINE'
  | 'CONTAINS_TABLE'
  | 'CONTAINS_PACKAGE'
  | 'BELONGS_TO_SESSION'
  | 'CALLS'
  | 'CHILD_OF'
  | 'USES_SUBQUERY'
  | 'NESTED_IN'
  | 'HAS_OUTPUT_COL'
  | 'HAS_AFFECTED_COL'
  | 'HAS_PARAMETER'
  | 'HAS_VARIABLE'
  | 'DATA_FLOW'
  | 'FILTER_FLOW'
  | 'JOIN_FLOW'
  | 'UNION_FLOW'
  | 'ATOM_PRODUCES'
  | 'ROUTINE_USES_TABLE';

// ─── Visualisation levels ────────────────────────────────────────────────────
export type ViewLevel = 'L1' | 'L2' | 'L3';

// ─── Column info (used inside TableNodeData) ─────────────────────────────────
export interface ColumnInfo {
  id: string;
  name: string;
  type: string;
  isPrimaryKey: boolean;
  isForeignKey?: boolean;
}

// ─── Schema chip entry (used inside DatabaseNode for inline schema list) ─────
export interface SchemaEntry {
  id: string;
  name: string;
  tableCount?: number;
}

// ─── Base node data (all nodes share this) ───────────────────────────────────
export interface DaliNodeData {
  [key: string]: unknown;          // index sig required by @xyflow/react Node
  label: string;
  nodeType: DaliNodeType;
  childrenAvailable: boolean;
  metadata: Record<string, unknown>;
  // L1 grouped: schema chips inside a DB node
  schemas?: SchemaEntry[];
  // Schema
  tablesCount?: number;
  routinesCount?: number;
  // Table
  schema?: string;
  columns?: ColumnInfo[];
  // Package / Routine
  packageType?: string;
  language?: string;
  // Atom / Column
  operation?: string;
  dataType?: string;
}

// ─── Typed sub-interfaces ────────────────────────────────────────────────────
export interface ApplicationNodeData extends DaliNodeData {
  nodeType: 'DaliApplication';
  serviceCount: number;
  databaseCount: number;
}

export interface ServiceNodeData extends DaliNodeData {
  nodeType: 'DaliService';
  technology?: string;
  databaseCount: number;
}

export interface SchemaNodeData extends DaliNodeData {
  nodeType: 'DaliSchema';
  tablesCount: number;
  routinesCount: number;
}

export interface TableNodeData extends DaliNodeData {
  nodeType: 'DaliTable';
  schema: string;
  columns: ColumnInfo[];
}

export interface PackageNodeData extends DaliNodeData {
  nodeType: 'DaliPackage';
  routinesCount: number;
}

export interface ColumnNodeData extends DaliNodeData {
  nodeType: 'DaliColumn' | 'DaliOutputColumn';
  dataType: string;
}

// ─── Breadcrumb item ─────────────────────────────────────────────────────────
export interface BreadcrumbItem {
  level: ViewLevel;
  scope: string | null;
  label: string;
  /** React Flow node ID of the node that was drilled from (enables back-nav focus). */
  fromNodeId?: string;
}
