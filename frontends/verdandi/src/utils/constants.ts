// ─── Shared constants — single source of truth ────────────────────────────────
// Import from here instead of duplicating magic numbers across files.

/** Layout — ELK & grid sizing */
export const LAYOUT = {
  NODE_WIDTH:                400,
  NODE_HEIGHT_BASE:           80,
  COL_ROW_HEIGHT:             22,
  GRID_SPACING:               60,
  LARGE_GRAPH_THRESHOLD:     500,   // switches ELK strategy to LINEAR_SEGMENTS
  AUTO_GRID_THRESHOLD:       800,   // M-3: skip ELK entirely, use grid directly
  TABLE_LEVEL_THRESHOLD:     500,   // M-5: auto-enable tableLevelView (hides cf-edges)
  ELK_TIMEOUT_LARGE:       8_000,   // M-7: reduced timeout when nodeCount > 1000
  ELK_BETWEEN_LAYERS:        200,
  ELK_NODE_SPACING:          100,
  ELK_COMPONENT_SPACING:     140,
  TIMEOUT_MS:             15_000,
  DENSE_GRAPH_RATIO:           3,   // EK-01: E/V > 3 → switch to stress algorithm (was 5, lowered for DWH-like schemas)
  STRESS_EDGE_LENGTH:        220,   // EK-01: desired edge length for stress layout
  PERF_VIRTUALIZE_THRESHOLD: 1500,  // EK-03: enable ReactFlow virtualization above this
} as const;

/** Transform — explore graph processing */
export const TRANSFORM = {
  L2_MAX_COLS:      50,
  MAX_PARTIAL_COLS: 50,
  EDGE_CURVATURE:   0.15,
} as const;

/** Canvas — React Flow viewport */
export const CANVAS = {
  FIT_VIEW_DURATION: 500,
  FIT_VIEW_PADDING:  0.15,
  FIT_VIEW_MAX_ZOOM: 2,
  LOD_TIMEOUT:       16,
} as const;

/** SQL operation categories */
export const WRITE_OPS = new Set(['INSERT', 'UPDATE', 'MERGE'] as const);
