# LOOM — 5-Level Navigation Architecture

**Документ:** `LOOM_5LEVEL_ARCHITECTURE`
**Версия:** 1.0
**Дата:** 14.04.2026
**Статус:** ACTIVE — implementation plan in `docs/sprints/PLAN_LOOM_5LEVEL_APR2026.md`

---

## 1. Motivation

LOOM's previous 3-level navigation (`L1 overview → L2 exploration → L3 column-atom
lineage`) had two blind spots that surfaced repeatedly during demo preparation:

1. **No mid-scope view for "which routines in this package touch which tables?"** —
   L1 stops at schemas, L2 already renders every individual `DaliStatement`. A
   package with 30 routines × 20 statements each becomes a 600-node canvas at L2,
   and the answer to a simple question gets buried under atoms.
2. **No focused view of a single complex SELECT** — a CTE-heavy statement with 4–5
   subqueries renders as one StatementNode with output-column handles at L2, and
   gets thrown into the global column-atom-column soup at L3. There is nowhere to
   see *just this statement's subquery tree with dataflow between its subquery
   output columns*.

This document specifies the new 5-level scheme that addresses both gaps, together
with the auxiliary work (DaliRecord/DaliRecordField rendering on L3, KNOT SQL
snippet in the inspector, StatementNode click-back to the new L2).

---

## 2. Level overview

| Level | Node types | Edges | Role |
|-------|-----------|-------|------|
| **L1** | `DaliDatabase`, `DaliSchema`, `DaliPackage` | `CONTAINS_SCHEMA`, `HAS_DATABASE`, `USES_DATABASE` | System overview. Cluster = database, children = schemas. Unchanged from previous numbering. |
| **L2** — NEW | `DaliRoutine`, `DaliTable` (**no columns**) | **Aggregated** `READS_FROM` / `WRITES_TO` (union of all stmt edges inside each routine, with `stmt_count` weight) | Cross-routine dataflow at a glance. Answers "which routines in this package/schema read/write which tables, and how heavily?" |
| **L3** — was L2 | `DaliTable` with columns, `DaliColumn`, `DaliRoutine`, `DaliPackage`, `DaliStatement`, `DaliOutputColumn`, `DaliAffectedColumn`, **`DaliRecord`** + **`DaliRecordField`** | `HAS_COLUMN`, `READS_FROM`, `WRITES_TO`, `DATA_FLOW`, `FILTER_FLOW`, `CONTAINS_STMT`, `CONTAINS_ROUTINE`, `HAS_OUTPUT_COL`, `HAS_AFFECTED_COL`, `HAS_RECORD_FIELD`, `RETURNS_INTO` | Classical exploration view. Same as old L2 plus first-class rendering of PL/SQL records (from `BULK COLLECT`, `RETURNING INTO`, `%ROWTYPE` — Sprint 2 schema additions that the frontend never learned to draw). |
| **L4** — NEW | One root `DaliStatement` + its subquery-tree children (`CHILD_OF*`: `SQ`, `CTE`, `INLINE_VIEW`, ...) + their `DaliTable` sources + promoted `DaliOutputColumn` nodes | `CHILD_OF`, `READS_FROM`, `HAS_OUTPUT_COL`, `DATA_FLOW` | Single-statement drill-down. Answers "where did each output column of this complex SELECT actually come from, through which subqueries?" `DaliOutputColumn` is a first-class node here (at L3 it's a handle inside `StatementNode`). |
| **L5** — DEFERRED | `DaliColumn` / `DaliOutputColumn` expression atoms | `ATOM_REF_COLUMN`, `ATOM_PRODUCES`, `DATA_FLOW` | Expression-column breakdown. For a column whose value is a computed expression (`CASE WHEN …`, `a + b * c`, `NVL(col, 0)`), walk the atom chain and surface every source column / literal / constant that contributes. **Out of scope for this sprint** — designed in a follow-up. |

**Note on the old L3 (column-atom-column lineage):** the existing column-lineage
view is **no longer part of the drill-down chain** under the new numbering. It
remains reachable via direct `navigateToLevel` but drilling from L3 on a column no
longer auto-routes there. The authoritative column-level view is L4 for
per-statement analysis and (eventually) L5 for expression decomposition.

---

## 3. Aggregation rule for L2

For every `DaliRoutine` in scope, the new L2 resolver unions the `READS_FROM` and
`WRITES_TO` edges of every `DaliStatement` contained in that routine via
`CONTAINS_STMT`, grouping by `(routine, table, edge_type)` and carrying a
`stmt_count` weight on the resulting edge:

```cypher
MATCH (r:DaliRoutine)-[:CONTAINS_STMT]->(s:DaliStatement)-[:READS_FROM]->(t:DaliTable)
WHERE r.schema_geoid = $scope OR r.package_geoid = $scope
RETURN r.routine_geoid AS src,
       t.table_geoid   AS tgt,
       'READS_FROM'    AS type,
       count(s)        AS stmt_count
UNION ALL
MATCH (r:DaliRoutine)-[:CONTAINS_STMT]->(s:DaliStatement)-[:WRITES_TO]->(t:DaliTable)
WHERE r.schema_geoid = $scope OR r.package_geoid = $scope
RETURN r.routine_geoid AS src,
       t.table_geoid   AS tgt,
       'WRITES_TO'     AS type,
       count(s)        AS stmt_count
```

`stmt_count` is used by `transformRoutineAggregate` to draw thicker or labelled
edges — a routine that reads from a table in 20 statements looks visually heavier
than one that reads it once.

---

## 4. Navigation rules

```
              L1 (schemas)
                 │
                 ▼ double-click schema/package
              L2 (NEW: routines + tables, aggregated)
                 │
                 ▼ double-click routine/package
              L3 (was L2: stmts + columns + records)
                 │
                 ▼ double-click DaliStatement
              L4 (NEW: one stmt + its subquery tree)
```

**Back-links:**
- Breadcrumb back-button — pops one level
- **L3 → L2 shortcut:** clicking the package or routine segment in a
  `StatementNode` header scopes the new L2 view to that routine/package.
  Implemented by making header lines 2 and 3 clickable — the geoids come from
  `CONTAINS_STMT` / `CONTAINS_ROUTINE` edges already in `ExploreResult`, walked
  once in `transformExplore` and stuffed into `node.data.metadata`.

**Drill rules:**
- Drilling from L3 on a `DaliStatement` → **L4**
- Drilling from L3 on a `DaliColumn` → *direct navigation only* (legacy
  column-lineage view, not in the chain)
- Drilling from L4 on a subquery child statement → deeper L4 (recursive)
- L5 is reached only via direct navigation (not this sprint)

---

## 5. Data flow per level

```
L1 ─── query ─── exploreOverview(scope)      ── useOverview        ── transformOverview
L2 ─── query ─── exploreRoutineAggregate(s)  ── useRoutineAggregate ── transformRoutineAggregate
L3 ─── query ─── explore(scope)              ── useExplore         ── transformExplore
L4 ─── query ─── exploreStatementTree(geoid) ── useStatementTree   ── transformStatementTree
                          │
                          ▼
                     layoutGraph (ELK.js)
                          │
                          ▼
                     React Flow canvas
```

The L1 / L3 pipeline is unchanged — L2 and L4 are new additions that plug into the
same `layoutGraph → React Flow` tail. All four paths share `transformHelpers`
(`NODE_TYPE_MAP`, `getEdgeStyle`, `ANIMATED_EDGES`) so node/edge styling is
consistent across levels.

---

## 6. L3 additions: DaliRecord + DaliRecordField

Sprint 2 added the vertex types `DaliRecord` and `DaliRecordField` to model PL/SQL
records (BULK COLLECT, RETURNING INTO, %ROWTYPE variables) with the edge
`HAS_RECORD_FIELD` (DaliRecord → DaliRecordField) and `RETURNS_INTO`
(DaliStatement → DaliRecordField/Variable/Parameter/Record). Backend writes the
data but the LOOM frontend has no node component for it, so the records are
invisible on the canvas.

**This sprint fixes that on L3:**

- Backend: extend `ExploreService.exploreSchema / explorePackage` Cypher UNION
  chain with three new segments scoped by `routine_geoid IN $routineGeoids`:
  - `DaliRecord` vertices owned by routines in scope
  - `HAS_RECORD_FIELD` structural edges
  - `RETURNS_INTO` data-flow edges from in-scope statements
- Frontend: new `RecordNode.tsx` (cloned from `TableNode.tsx`), new
  `InspectorRecord.tsx`, new legend entries, `NODE_TYPE_MAP` + `getEdgeStyle`
  additions.

A record field with `source_column_geoid` populated (from `%ROWTYPE`) gets a small
`→` badge and a link back to the source `DaliColumn` in the inspector — this is
how the user can trace *"where does this record field's type come from"*.

---

## 7. KNOT SQL snippet in LOOM Inspector

Sprint 2 added the `DaliSnippet` vertex type and the backend resolver
`KnotService.knotSnippet(stmtGeoid)`, plus the frontend React Query hook
`useKnotSnippet(geoid, enabled)`. Both already work — but they're consumed only by
the standalone KNOT page (`KnotStatements.tsx`). The LOOM Inspector panel that
opens when a user selects a node on the canvas has no SQL section.

**This sprint wires the existing hook into `InspectorStatement.tsx`:** a
collapsible "SQL" section (default-open) that lazily calls `useKnotSnippet` only
while expanded, renders the returned string in a themed `<pre>`, shows an empty
state when no snippet is recorded, and has a copy-to-clipboard button. No backend
or resolver changes — the whole task is ~30 lines of JSX + 4 i18n keys.

---

## 8. Implementation order

See `docs/sprints/PLAN_LOOM_5LEVEL_APR2026.md` for full phase detail. The 8-phase
order is:

1. **Phase 1** — Documentation (this file + README + DECISIONS_LOG Q36 + comment updates)
2. **Phase 6** — KNOT SQL snippet in LOOM Inspector (smallest, 100% additive)
3. **Phase 2** — Backend `exploreRoutineAggregate` resolver
4. **Phase 3** — Frontend renumbering (mechanical, must land before 4/4b/4c/5)
5. **Phase 4** — New L2 view
6. **Phase 4c** — DaliRecord + DaliRecordField rendering on L3
7. **Phase 4b** — New L4 statement-drill view
8. **Phase 5** — StatementNode clickable header click-back to L2

Each phase ships as its own commit on `feature/loom-5level-apr2026` so any single
phase can be reverted without unwinding the others.

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 14.04.2026 | 1.0 | Initial document. Specifies 5-level scheme with new L2 (routine aggregate) and L4 (statement drill), records rendering on L3, KNOT snippet in LOOM inspector, StatementNode click-back. L5 deferred. |
