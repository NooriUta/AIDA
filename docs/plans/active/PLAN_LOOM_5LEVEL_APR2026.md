# Plan — LOOM 5-level Architecture + KNOT Inspector SQL Snippet (Apr 2026)

**Branch:** `feature/lineage-gaps-s2`
**Scope:** Expand LOOM from 3 navigation levels to 5 by inserting an **aggregation
layer (L2)** between the current overview and exploration views, and inserting a
**statement-drill layer (L4)** between the current exploration and column-lineage
views. Renumber remaining levels, wire clickable package / routine references in
StatementNode headers (drill back to the new L2), and surface the existing `DaliSnippet`
GraphQL query in the LOOM `InspectorStatement` panel for SQL preview + validation.

---

## Renumbering scheme

| New level | Was | Content |
|-----------|-----|---------|
| **L1** | L1 | Database → Schema overview (unchanged) |
| **L2** | — **NEW** — | Tables (no columns) + Routines with **aggregated** `READS_FROM` / `WRITES_TO` edges. Cross-routine data flow at a glance. |
| **L3** | L2 | Tables + Routines + Statements + output/affected columns + **`DaliRecord` containers with their `DaliRecordField` children** (PL/SQL records — BULK COLLECT / RETURNING INTO / %ROWTYPE targets — rendered analogously to DaliTable/DaliColumn) |
| **L4** | — **NEW** — | Single-statement drill-down: one `DaliStatement` + all of its subqueries (child statements of type `SQ`, `CTE`, `INLINE_VIEW`, etc.) + the tables they read, with `DATA_FLOW` edges **between the `DaliOutputColumn` of each subquery and the consumer subquery / final SELECT**. Use case: understand where each output column of a complex SELECT actually came from. |
| **L5** | — **DEFERRED** — | Expression-column breakdown: for a `DaliColumn` / `DaliOutputColumn` whose value is a computed expression (e.g. `CASE WHEN …`, `a + b * c`, `NVL(col, 0)`), walk the atom chain of the expression and show every source column / constant / literal that contributes to it. **Out of scope for this sprint** — listed here for completeness; will be designed in a follow-up sprint once L1–L4 are stable. |

**Note on the old L3 (column-atom-column lineage):** the existing column-lineage
view is **not** part of the L1 → L2 → L3 → L4 drill-down chain under the new
numbering. It remains reachable via direct navigation (`navigateToLevel` with the
legacy identifier) but is no longer auto-routed from a drill-down click. The
authoritative column-level view for the new architecture is L4 for per-statement
analysis and (eventually) L5 for expression decomposition.

**Aggregation rule for new L2:**
For every `DaliRoutine`, union the `READS_FROM` and `WRITES_TO` edges of every
`DaliStatement` contained in it via `CONTAINS_STMT`. Each aggregated edge carries a
`stmt_count` weight so the LOOM transform can draw thicker / annotated lines.

```cypher
MATCH (r:DaliRoutine)-[:CONTAINS_STMT]->(s:DaliStatement)-[:READS_FROM]->(t:DaliTable)
WHERE r.schema_geoid = $scope OR r.package_geoid = $scope
RETURN r.routine_geoid AS src, t.table_geoid AS tgt,
       'READS_FROM' AS type, count(s) AS stmt_count
UNION ALL
MATCH (r:DaliRoutine)-[:CONTAINS_STMT]->(s:DaliStatement)-[:WRITES_TO]->(t:DaliTable)
WHERE r.schema_geoid = $scope OR r.package_geoid = $scope
RETURN r.routine_geoid AS src, t.table_geoid AS tgt,
       'WRITES_TO' AS type, count(s) AS stmt_count
```

---

## Phase 1 — Documentation

**New file:** `docs/loom/LOOM_5LEVEL_ARCHITECTURE.md`
- Level overview table
- Aggregation rule + Cypher query
- Navigation rules: `L1 → L2 → L3 → L4` (drill-down) and `L3 → L2` (back-link via StatementNode header click)
- Data-flow diagram: backend resolver → GraphQL → `useRoutineAggregate` → `transformRoutineAggregate` → React Flow canvas

**Updates:**
- `frontends/verdandi/README.md` line 6 — change "L1 > L2 > L3 > L4 drill-down" to reflect new meaning
- `docs/architecture/MODULES_TECH_STACK.md` lines 115, 722–723, 839 — rewrite level descriptions
- `docs/architecture/PERF_BOTTLENECK_LOOM.md` — update title + references
- `docs/reviews/daily/2026-04-13_VERDANDI_LOOM_REVIEW.md` — note renumbering in ADR-001
- `docs/architecture/DECISIONS_LOG.md` — add **Q36** "5-level LOOM architecture — aggregation layer inserted as new L2"

---

## Phase 2 — Backend GraphQL aggregation

**Files:**
- `services/shuttle/src/main/java/studio/seer/lineage/model/RoutineAggregateResult.java` — new record `(List<GraphNode>, List<GraphEdge>)` mirroring `ExploreResult` shape
- `services/shuttle/src/main/java/studio/seer/lineage/service/ExploreService.java` — add `exploreRoutineAggregate(scope)` method running the Cypher above + stitching rows into `GraphNode[]` (DaliTable + DaliRoutine) and `GraphEdge[]` (aggregated READS_FROM / WRITES_TO with `stmt_count` meta)
- `services/shuttle/src/main/java/studio/seer/lineage/resource/LineageResource.java` — add `@Query("exploreRoutineAggregate")` resolver mirroring the existing `explore` resolver's auth / scope resolution

**Contract:** same shape as existing `ExploreResult` so the frontend transform pipeline (`transformExplore` → `layoutGraph` → React Flow) can handle it with minimal rewiring.

---

## Phase 3 — Frontend renumbering (mechanical)

**Mass rename** — old `'L2'` → `'L3'`, old `'L3'` → `'L4'`, in this order (reverse first to avoid collisions):

1. `frontends/verdandi/src/types/domain.ts:54` — `ViewLevel` union: `'L1' | 'L2' | 'L3' | 'L4'`
2. `frontends/verdandi/src/stores/slices/navigationSlice.ts:32–129` — `drillDown`, `jumpTo`, `navigateBack`, `navigateToLevel` logic; update L1→L2 / L2→L3 / L3→L4 transitions
3. `frontends/verdandi/src/utils/displayPipeline.ts` lines 25, 58, 93, 107, 156, 195, 237 — L1-guard checks stay as `L1`; everywhere the current `'L1'` exclusion applies to the aggregate-plus-columns view, decide whether the new L2 needs its own exclusion
4. `frontends/verdandi/src/services/hooks.ts` lines 42, 56, 71 — comments + query routing
5. `frontends/verdandi/src/components/layout/Breadcrumb.tsx`, `StatusBar.tsx`, `Shell.tsx` — level badges
6. `frontends/verdandi/src/components/layout/LegendButton.tsx` — `EDGE_ROWS` and `NODE_ROWS` gain an L2 entry; current L2 entries move to L3

**Verification:** `npx tsc --noEmit` must pass after each file.

---

## Phase 4 — New L2 view (data + transform + nodes)

1. `frontends/verdandi/src/services/lineage.ts` — add `fetchRoutineAggregate(scope)` + GraphQL query `ExploreRoutineAggregate($scope: String!) { exploreRoutineAggregate(scope:$scope) { nodes{..} edges{..} } }`
2. `frontends/verdandi/src/services/hooks.ts` — `useRoutineAggregate(scope)` (React Query hook)
3. `frontends/verdandi/src/utils/transformRoutineAggregate.ts` — new transform: produces RoutineNode + TableNode (no columns) + aggregated edges with `stmt_count` in edge.data
4. `frontends/verdandi/src/utils/transformHelpers.ts` — edge-weight styling: thicker stroke for higher `stmt_count` (or numeric label on edge)
5. `frontends/verdandi/src/components/canvas/RoutineNode.tsx` — new node type showing routine name + kind badge (PROC/FUNC/PKG) without parameter rows (those live at L3)
6. `frontends/verdandi/src/services/hooks.ts::useGraphData` — route `viewLevel === 'L2'` to `useRoutineAggregate`, old L2 routing moves to `L3`

---

## Phase 4c — L3 rendering: DaliRecord + DaliRecordField

**Goal:** render PL/SQL records and their fields as first-class nodes on L3, analogous
to how `DaliTable` contains `DaliColumn` rows.

**Schema (already in place, verified in `RemoteSchemaCommands.java:47,50,89`):**

| Vertex / Edge | Properties | Notes |
|---------------|-----------|-------|
| `DaliRecord` | `record_geoid`, `record_name`, `routine_geoid`, `source_stmt_geoid` | Scoped to a routine via property lookup — no `HAS_RECORD` edge |
| `DaliRecordField` | `field_geoid`, `field_name`, `ordinal_position`, `data_type`, `record_geoid`, `source_column_geoid` | `source_column_geoid` links the field back to a DaliColumn when the record is built from `%ROWTYPE` |
| `HAS_RECORD_FIELD` | DaliRecord → DaliRecordField | Structural containment edge |
| `RETURNS_INTO` | DaliStatement → DaliRecordField / DaliVariable / DaliParameter / DaliRecord | From KI-RETURN-1 — Sprint 2 |

**Backend changes — `services/shuttle/src/main/java/studio/seer/lineage/service/ExploreService.java`:**

Extend `exploreSchema(...)` / `explorePackage(...)` Cypher UNION chain with three
new segments inside the scope of the routines being returned:

```cypher
// 13. DaliRecord vertices owned by routines in scope
MATCH (r:DaliRoutine)
WHERE r.routine_geoid IN $routineGeoids
MATCH (rec:DaliRecord) WHERE rec.routine_geoid = r.routine_geoid
RETURN rec.record_geoid AS id, rec.record_name AS label, 'DaliRecord' AS type,
       r.routine_geoid  AS scope
LIMIT 500

// 14. HAS_RECORD_FIELD structural edges
MATCH (rec:DaliRecord)-[e:HAS_RECORD_FIELD]->(f:DaliRecordField)
WHERE rec.routine_geoid IN $routineGeoids
RETURN id(rec) AS src, id(f) AS tgt, 'HAS_RECORD_FIELD' AS type
LIMIT 3000

// 15. RETURNS_INTO edges (stmt → record-field) for records owned by in-scope routines
MATCH (s:DaliStatement)-[e:RETURNS_INTO]->(tgt)
WHERE s.routine_geoid IN $routineGeoids
RETURN id(s) AS src, id(tgt) AS tgt, 'RETURNS_INTO' AS type
LIMIT 1000
```

`DaliRecordField` vertices are carried implicitly via their `id()` on the
`HAS_RECORD_FIELD` edge rows (like the existing `DaliColumn` handling); if the
current `exploreSchema` logic needs explicit node rows for each field, add a 14a
segment returning `f.field_geoid, f.field_name, 'DaliRecordField', rec.record_geoid`.

**Frontend changes:**

1. `frontends/verdandi/src/types/domain.ts` — extend `DaliNodeType` and `DaliEdgeType`
   unions with `'DaliRecord' | 'DaliRecordField'` and `'HAS_RECORD_FIELD' | 'RETURNS_INTO'`
   (if not already present)
2. `frontends/verdandi/src/utils/transformHelpers.ts` — add `NODE_TYPE_MAP` entries:
   `DaliRecord → 'recordNode'`, `DaliRecordField → 'columnNode'` (reuse columnNode visually,
   or create a dedicated `recordFieldNode` if the styling needs to differ)
3. `frontends/verdandi/src/components/canvas/nodes/RecordNode.tsx` — **new** component,
   clone of `TableNode.tsx` with:
   - Header: record_name + kind badge "REC"
   - Body: list of `DaliRecordField` children with `field_name`, `data_type`, and a
     small "→" badge when `source_column_geoid` is populated (indicates %ROWTYPE origin)
   - Handles: one Handle per field (for eventual DATA_FLOW / RETURNS_INTO edge endpoints)
4. `frontends/verdandi/src/utils/transformExplore.ts` — in the `rfEdges` /
   `regularEdges` build, accept `HAS_RECORD_FIELD` and `RETURNS_INTO` on the L3 canvas
   (add to the `L3_FLOW_EDGES` whitelist when Phase 3 renumbers the constant)
5. `frontends/verdandi/src/utils/transformHelpers.ts::getEdgeStyle` — new cases:
   - `HAS_RECORD_FIELD` → dashed, var(--t3) (structural, like `HAS_COLUMN`)
   - `RETURNS_INTO` → animated, var(--acc) (data flow, like `DATA_FLOW`)
6. `frontends/verdandi/src/components/layout/LegendButton.tsx` — L3 legend rows for
   the two new edge types + the RecordNode swatch
7. `frontends/verdandi/src/components/inspector/InspectorRecord.tsx` — **new**
   inspector panel for DaliRecord selection: routine geoid, source stmt geoid,
   field list (name, type, source column if %ROWTYPE)

**Why RecordNode is a separate component** (not just a re-styled TableNode):
records live inside routines, not schemas — the parentNode / grouping logic differs.
A record should be drawn near or inside its owning routine node, while tables live at
schema level. Cleaner to separate now than to branch inside TableNode later.

---

## Phase 4b — New L4 statement-drill view

**Data shape:** given a root `DaliStatement` geoid, the backend returns the root stmt,
all descendant subquery stmts (traversed via `CHILD_OF*` or `USES_SUBQUERY*` depending
on which edge type already exists in the schema), every `DaliTable` they read via
`READS_FROM`, and every `DaliOutputColumn` they produce, plus the `DATA_FLOW` edges
linking subquery output columns to the consumer statements that use them.

**Cypher sketch (subject to schema verification during implementation):**

```cypher
MATCH (root:DaliStatement {stmt_geoid: $root})
OPTIONAL MATCH (root)<-[:CHILD_OF*0..30]-(sub:DaliStatement)
WITH collect(DISTINCT sub) + [root] AS stmts
UNWIND stmts AS s
OPTIONAL MATCH (s)-[:READS_FROM]->(t:DaliTable)
OPTIONAL MATCH (s)-[:HAS_OUTPUT_COL]->(oc:DaliOutputColumn)
OPTIONAL MATCH (oc)-[df:DATA_FLOW]->(downstream)
RETURN s, t, oc, df, downstream
```

**Files:**
- `services/shuttle/src/main/java/studio/seer/lineage/service/ExploreService.java`
  — add `exploreStatementTree(rootStmtGeoid)` method
- `services/shuttle/src/main/java/studio/seer/lineage/resource/LineageResource.java`
  — add `@Query("exploreStatementTree")` resolver
- `frontends/verdandi/src/services/lineage.ts` — `fetchStatementTree(stmtGeoid)`
- `frontends/verdandi/src/services/hooks.ts` — `useStatementTree(stmtGeoid)`
- `frontends/verdandi/src/utils/transformStatementTree.ts` — new transform
- `frontends/verdandi/src/components/canvas/nodes/OutputColumnNode.tsx` — reuse
  existing or create if missing; the L4 canvas needs explicit `DaliOutputColumn`
  nodes (they are handles in L3's StatementNode, but at L4 they are first-class).

**Navigation into L4:** drill-down from L3 by double-clicking a `DaliStatement` node
(currently drill transitions L3 → old L3 / new L5; change routing so drilling from L3
on a Statement goes to L4, while drilling from L3 on a Column goes to L5).

---

## Phase 5 — StatementNode clickable header links

**File:** `frontends/verdandi/src/components/canvas/nodes/StatementNode.tsx`

Current header (lines 153–220) renders `groupPath` (schema/package/routine chain) as static `<div>`s.

**Change:** make the package segment and routine segment clickable. On click, call
`navigateToLevel('L2', <geoid>)` — scopes the new L2 view to that routine / package.

**Data source:** the geoids can be read from the edges already in the ExploreResult:
- `CONTAINS_STMT` (routine → statement) — source is the routine geoid
- `CONTAINS_ROUTINE` (package/schema → routine) — source is the package/schema geoid

Either (a) the backend `explore` resolver pre-populates `node.meta` with `routineGeoid` +
`packageGeoid` on every DaliStatement node, OR (b) the frontend post-walks the edges once
in `transformExplore` to build a `stmtGeoid → {routineGeoid, packageGeoid}` lookup and
stuffs the result into `node.data.metadata`.

**Pick (b)** — keeps the backend contract stable and is a pure transform change.

**UI:** clickable segments get `cursor: pointer` + underline on hover, same font as the
rest of `groupPath`. Click handler: `e.stopPropagation(); navigateToLevel('L2', geoid)`.

---

## Phase 6 — KNOT Inspector SQL snippet in LOOM

**Context:** backend resolver `knotSnippet(stmtGeoid)` already exists in
`services/shuttle/src/main/java/studio/seer/lineage/service/KnotService.java:587` and
the frontend hook `useKnotSnippet(geoid, enabled)` already exists in
`frontends/verdandi/src/services/hooks.ts:182`. Both are currently consumed only by
the standalone KNOT page (`KnotStatements.tsx:557`), **not** by the LOOM Inspector
panel that opens when a node is selected on the canvas.

**Task:** wire the existing hook into the LOOM Inspector so that selecting a
DaliStatement node on the canvas shows its SQL snippet in the right-side inspector
panel for preview + validation.

**File:** `frontends/verdandi/src/components/inspector/InspectorStatement.tsx`

**Change:**
1. Import `useKnotSnippet` from `services/hooks`
2. Add a collapsible `<InspectorSection>` titled "SQL" that is open by default
3. Call `useKnotSnippet(stmtGeoid, sectionOpen)` (lazy — fires only while section is open)
4. Render the returned string in a `<pre>` with mono font, theme-aware background, max-height 300px, horizontal + vertical scroll
5. Loading state → spinner / `…`; empty state → i18n message "No snippet recorded"
6. Copy-to-clipboard button in section header

**i18n keys to add** (en/ru `common.json`): `inspector.statement.sql.title`, `inspector.statement.sql.empty`, `inspector.statement.sql.copy`, `inspector.statement.sql.loading`

**No backend changes needed** — the resolver + hook + GraphQL query + index
(`DaliSnippet(stmt_geoid)` NOTUNIQUE) all already exist.

---

## Execution order (minimizes risk)

1. **Phase 1** (docs) — no code, high leverage for alignment
2. **Phase 6** (KNOT snippet in LOOM inspector) — smallest change, 100% additive, unblocks the user immediately
3. **Phase 2** (backend aggregate resolver for L2) — additive, no risk to existing `explore`
4. **Phase 3** (frontend renumbering) — mechanical, must land before Phase 4
5. **Phase 4** (new L2 view) — depends on Phase 2 + 3
6. **Phase 4c** (L3 rendering: DaliRecord + DaliRecordField) — depends on Phase 3; additive backend + frontend, independent of Phase 4 / 4b
7. **Phase 4b** (new L4 statement-drill view) — depends on Phase 3; independent of Phase 4 / 4c
8. **Phase 5** (StatementNode clickable header — drills back to L2) — depends on Phase 3 + 4

**Deferred:** **L5 (expression-column breakdown)** is explicitly **out of scope** for
this sprint. It will be designed and implemented after L1–L4 are validated in a
follow-up plan.

**Rollback strategy:** every phase is a separate commit. If Phase 4 or 4b or 5 breaks,
revert just that commit; Phases 1–3 + 6 can ship independently.
