/**
 * End-to-end test for AGG view column enrichment pipeline.
 *
 * Simulates: transformGqlExplore(AGG response) → applyStmtColumns(stmtCols response)
 * Uses realistic ArcadeDB-style IDs (#bucket:position) to catch ID mismatch bugs.
 */
import { describe, it, expect } from 'vitest';
import { transformGqlExplore } from './transformExplore';
import { applyStmtColumns } from './transformColumns';
import type { ExploreResult, GraphNode, GraphEdge } from '../services/lineage';
import type { ColumnInfo } from '../types/domain';

// ── Helpers ──────────────────────────────────────────────────────────────────

function gNode(id: string, type: string, label: string, scope = '', meta?: Array<{ key: string; value: string }>): GraphNode {
  return { id, type, label, scope, meta };
}

function gEdge(id: string, source: string, target: string, type: string): GraphEdge {
  return { id, source, target, type };
}

function mkResult(nodes: GraphNode[], edges: GraphEdge[], hasMore = false): ExploreResult {
  return { nodes, edges, hasMore };
}

// ── Realistic AGG response ───────────────────────────────────────────────────
// Mirrors what ExploreRoutineAggregateService.buildResult() returns for a
// schema with 2 routines, 2 tables, 1 package.

function makeAggResponse(): ExploreResult {
  return mkResult(
    [
      gNode('#50:0', 'DaliPackage', 'PKG_ETL_SALES', 'DWH', [{ key: 'routineType', value: 'PKG' }]),
      gNode('#20:0', 'DaliRoutine', 'LOAD_FACT_SALES', 'DWH', [{ key: 'packageName', value: 'PKG_ETL_SALES' }, { key: 'routineType', value: 'PROCEDURE' }]),
      gNode('#20:1', 'DaliRoutine', 'CALC_TOTALS', 'DWH', [{ key: 'packageName', value: 'PKG_ETL_SALES' }, { key: 'routineType', value: 'FUNCTION' }]),
      gNode('#10:0', 'DaliTable', 'FACT_SALES', 'DWH'),
      gNode('#10:1', 'DaliTable', 'DIM_PRODUCT', 'DWH'),
    ],
    [
      // Package → routine containment
      gEdge('#50:0__CONTAINS_ROUTINE__#20:0', '#50:0', '#20:0', 'CONTAINS_ROUTINE'),
      gEdge('#50:0__CONTAINS_ROUTINE__#20:1', '#50:0', '#20:1', 'CONTAINS_ROUTINE'),
      // Self-loop node-only entries
      gEdge('#50:0__NODE_ONLY__#50:0', '#50:0', '#50:0', 'NODE_ONLY'),
      gEdge('#20:0__NODE_ONLY__#20:0', '#20:0', '#20:0', 'NODE_ONLY'),
      gEdge('#20:1__NODE_ONLY__#20:1', '#20:1', '#20:1', 'NODE_ONLY'),
      gEdge('#10:0__NODE_ONLY__#10:0', '#10:0', '#10:0', 'NODE_ONLY'),
      gEdge('#10:1__NODE_ONLY__#10:1', '#10:1', '#10:1', 'NODE_ONLY'),
      // Data-flow edges (READS_FROM = table→routine, WRITES_TO = routine→table)
      gEdge('#10:1__READS_FROM__#20:0', '#10:1', '#20:0', 'READS_FROM'),
      gEdge('#20:0__WRITES_TO__#10:0',  '#20:0', '#10:0', 'WRITES_TO'),
      // Routine→routine call
      gEdge('#20:0__CALLS__#20:1', '#20:0', '#20:1', 'CALLS'),
    ],
  );
}

// ── Realistic stmtColumns response ───────────────────────────────────────────
// Mirrors what ExploreStatementService.exploreStmtColumns() returns when given
// DaliTable IDs ['#10:0', '#10:1']. Only HAS_COLUMN edges (no HAS_OUTPUT_COL
// or HAS_AFFECTED_COL — those are for DaliStatement nodes, absent in AGG).

function makeStmtColumnsResponse(): ExploreResult {
  return mkResult(
    [
      // Source table nodes (repeated from AGG, used for matching)
      gNode('#10:0', 'DaliTable', 'FACT_SALES', 'DWH'),
      gNode('#10:1', 'DaliTable', 'DIM_PRODUCT', 'DWH'),
      // Column nodes
      gNode('#30:0', 'DaliColumn', 'SALE_ID', '', [{ key: 'isPk', value: 'true' }, { key: 'dataType', value: 'NUMBER' }]),
      gNode('#30:1', 'DaliColumn', 'AMOUNT', '', [{ key: 'dataType', value: 'NUMBER' }]),
      gNode('#30:2', 'DaliColumn', 'SALE_DATE', '', [{ key: 'dataType', value: 'DATE' }]),
      gNode('#31:0', 'DaliColumn', 'PRODUCT_ID', '', [{ key: 'isPk', value: 'true' }, { key: 'dataType', value: 'NUMBER' }]),
      gNode('#31:1', 'DaliColumn', 'PRODUCT_NAME', '', [{ key: 'dataType', value: 'VARCHAR2' }]),
    ],
    [
      // HAS_COLUMN: table → column
      gEdge('#10:0__HAS_COLUMN__#30:0', '#10:0', '#30:0', 'HAS_COLUMN'),
      gEdge('#10:0__HAS_COLUMN__#30:1', '#10:0', '#30:1', 'HAS_COLUMN'),
      gEdge('#10:0__HAS_COLUMN__#30:2', '#10:0', '#30:2', 'HAS_COLUMN'),
      gEdge('#10:1__HAS_COLUMN__#31:0', '#10:1', '#31:0', 'HAS_COLUMN'),
      gEdge('#10:1__HAS_COLUMN__#31:1', '#10:1', '#31:1', 'HAS_COLUMN'),
    ],
  );
}

// ── Tests ────────────────────────────────────────────────────────────────────

describe('AGG view column enrichment pipeline', () => {

  it('transformGqlExplore preserves ArcadeDB node IDs', () => {
    const agg = makeAggResponse();
    const { nodes } = transformGqlExplore(agg);

    const tableNode = nodes.find((n) => n.data.label === 'FACT_SALES');
    expect(tableNode).toBeDefined();
    expect(tableNode!.id).toBe('#10:0');

    const routineNode = nodes.find((n) => n.data.label === 'LOAD_FACT_SALES');
    expect(routineNode).toBeDefined();
    expect(routineNode!.id).toBe('#20:0');
  });

  it('transformGqlExplore produces no columns from AGG (no HAS_COLUMN edges)', () => {
    const agg = makeAggResponse();
    const { nodes } = transformGqlExplore(agg);

    const tableNodes = nodes.filter((n) => n.data.nodeType === 'DaliTable');
    expect(tableNodes.length).toBe(2);
    for (const tn of tableNodes) {
      expect(tn.data.columns).toBeUndefined();
    }
  });

  it('stmtIds collection: ENRICHABLE filters DaliTable IDs from AGG nodes', () => {
    const agg = makeAggResponse();
    const ENRICHABLE = new Set(['DaliStatement', 'DaliTable']);
    const ids = new Set<string>();
    for (const n of agg.nodes) {
      if (ENRICHABLE.has(n.type)) ids.add(n.id);
    }
    expect(ids.size).toBe(2);
    expect(ids.has('#10:0')).toBe(true);
    expect(ids.has('#10:1')).toBe(true);
  });

  it('applyStmtColumns patches table nodes with HAS_COLUMN data', () => {
    const agg = makeAggResponse();
    const { nodes, edges } = transformGqlExplore(agg);
    const stmtCols = makeStmtColumnsResponse();

    const { nodes: enrichedNodes } = applyStmtColumns(nodes, edges, stmtCols);

    const factSales = enrichedNodes.find((n) => n.id === '#10:0');
    expect(factSales).toBeDefined();
    const cols = factSales!.data.columns as ColumnInfo[];
    expect(cols).toBeDefined();
    expect(cols.length).toBe(3);
    expect(cols[0].name).toBe('SALE_ID');
    expect(cols[0].isPrimaryKey).toBe(true);
    expect(cols[0].type).toBe('NUMBER');
  });

  it('applyStmtColumns patches ALL table nodes, not just the first', () => {
    const agg = makeAggResponse();
    const { nodes, edges } = transformGqlExplore(agg);
    const stmtCols = makeStmtColumnsResponse();

    const { nodes: enrichedNodes } = applyStmtColumns(nodes, edges, stmtCols);

    const dimProduct = enrichedNodes.find((n) => n.id === '#10:1');
    expect(dimProduct).toBeDefined();
    const cols = dimProduct!.data.columns as ColumnInfo[];
    expect(cols).toBeDefined();
    expect(cols.length).toBe(2);
    expect(cols[0].name).toBe('PRODUCT_ID');
    expect(cols[1].name).toBe('PRODUCT_NAME');
  });

  it('routine nodes remain without columns after enrichment', () => {
    const agg = makeAggResponse();
    const { nodes, edges } = transformGqlExplore(agg);
    const stmtCols = makeStmtColumnsResponse();

    const { nodes: enrichedNodes } = applyStmtColumns(nodes, edges, stmtCols);

    const routineNodes = enrichedNodes.filter((n) => n.data.nodeType === 'DaliRoutine');
    for (const rn of routineNodes) {
      expect(rn.data.columns).toBeUndefined();
    }
  });

  it('no cfEdges are produced in AGG (no stmtColMap — routines have no HAS_OUTPUT_COL)', () => {
    const agg = makeAggResponse();
    const { nodes, edges } = transformGqlExplore(agg);
    const stmtCols = makeStmtColumnsResponse();

    const { cfEdges } = applyStmtColumns(nodes, edges, stmtCols);
    // In AGG: READS_FROM/WRITES_TO connect routine↔table, not stmt↔table.
    // stmtColMap is empty (no HAS_OUTPUT_COL/HAS_AFFECTED_COL for routines),
    // so no column-flow edges can be built.
    expect(cfEdges).toHaveLength(0);
  });

  it('full pipeline: AGG response → transform → enrich → table nodes have columns', () => {
    // End-to-end pipeline as useGraphData does it:
    // 1. transformGqlExplore(aggData) → base graph
    // 2. applyStmtColumns(base.nodes, base.edges, stmtColsData) → enriched graph
    const agg = makeAggResponse();
    const base = transformGqlExplore(agg);

    // Verify enrichment gate conditions (mirrors useGraphData line 139)
    const needsEnrichment = true; // L2
    const stmtColsData = makeStmtColumnsResponse();
    const gatePass = needsEnrichment && stmtColsData && stmtColsData.edges.length > 0;
    expect(gatePass).toBe(true);

    const { nodes: enrichedNodes, cfEdges } = applyStmtColumns(base.nodes, base.edges, stmtColsData);

    // Every DaliTable node must have columns
    const tableNodes = enrichedNodes.filter((n) => n.data.nodeType === 'DaliTable');
    expect(tableNodes.length).toBe(2);
    for (const tn of tableNodes) {
      const cols = tn.data.columns as ColumnInfo[];
      expect(cols, `Table ${tn.data.label} should have columns`).toBeDefined();
      expect(cols.length, `Table ${tn.data.label} should have >0 columns`).toBeGreaterThan(0);
    }

    // Total column count: FACT_SALES(3) + DIM_PRODUCT(2) = 5
    const totalCols = tableNodes.reduce(
      (sum, tn) => sum + ((tn.data.columns as ColumnInfo[])?.length ?? 0),
      0,
    );
    expect(totalCols).toBe(5);
  });
});
