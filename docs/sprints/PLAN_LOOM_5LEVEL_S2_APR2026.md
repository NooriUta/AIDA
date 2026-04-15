# Plan — LOOM 5-level Sprint 2 (continuation) — Apr 2026

**Branch:** `feature/loom-5level-s2-apr2026`
**Predecessor:** `feature/loom-5level-apr2026` merged as PR #11 → `f44d89d`
**Reference docs:**
- `docs/loom/LOOM_5LEVEL_ARCHITECTURE.md` — level scheme + Known issues §
- `docs/loom/LOOM_L2_EDGE_SEMANTICS.md` — edge model reference
- `docs/sprints/PLAN_LOOM_5LEVEL_APR2026.md` — original 8-phase plan

## Context

Sprint 1 shipped the Inspector rework, 4-way edge styling, knotStatementExtras,
source-tables breakdown, external-sources toggle, upstream hoist, and half of the
column-level routing. It left two known issues and six deferred phases. Sprint 2
finishes the known issues first (quick wins that unblock visual verification),
then executes the deferred phases in dependency order — backend L2 aggregation
resolver, frontend renumbering, new L2 view, L3 record rendering, L4 statement-
drill view, and finally the StatementNode click-back to L2.

## Execution order

### Phase S2.0 — Known issues cleanup (before new phases)

**Issue A: column-level `DATA_FLOW` target handle not mapped**

Symptom: backend emits `'tgt-' + id(oc)` but the lime-dashed edge lands on the
consumer statement's header instead of the matching output-column row inside
the card. FILTER_FLOW (no target handle by design) works end-to-end, which
isolates the bug to the TARGET-side handle identity.

Investigation plan (~30 min):
1. In preview, select a known DaliStatement that has DATA_FLOW inputs
2. Dump `data.columns` via React Flow fiber walk — record `col.id` values for
   the output columns
3. Run a direct GraphQL call to `exploreSchema` / `upstream` for that stmt and
   inspect the `id(oc)` values in the returned edge `targetHandle`
4. Compare the two identity sets
5. Fix location:
   - If `id(oc)` comes back as `#31:X` and `col.id` is `#31:X` → no mismatch,
     issue is somewhere else (React Flow handle mount timing?)
   - If `col.id` comes back as a synthetic id (e.g. `ocExtId`) built in
     `applyStmtColumns` → backend needs to emit the same synthetic id, or
     frontend needs to switch to emitting the raw `@rid`
   - If the stmt has column handles rendered with `'tgt-' + col.id` but
     `col.id` doesn't match the backend string → normalise one side

**File suspects for fix:**
- `frontends/verdandi/src/components/canvas/nodes/StatementNode.tsx` — handle mount `id={'tgt-' + col.id}`
- `frontends/verdandi/src/utils/transformColumns.ts` — `applyStmtColumns` where output-column `col.id` is assigned (line 58: `stmtCols.push({ id: col.id, ... })`)
- `services/shuttle/src/main/java/studio/seer/lineage/service/KnotService.java` / `ExploreService.java` — whatever supplies `ocByStmtAndName` and `ocByOrder` maps

**Issue B: edge palette mismatch between legend and cfEdges**

`applyStmtColumns` (`transformColumns.ts`) emits cfEdges with hard-coded colours
that predate the 4-way scheme from commit `618d7f6`:
- READS_FROM cfEdge uses `#88B8A8` (matches) but with hardcoded dash `'3 2'`
- WRITES_TO cfEdge uses `#D4922A` dash `'3 2'` (should be `'8 3'` per the new legend)

Fix: route cfEdges through `getEdgeStyle()` from `transformHelpers.ts` so all
edge types (both aggregated and cf) follow the same style token table.

**File to fix:**
- `frontends/verdandi/src/utils/transformColumns.ts` lines 108–142 — replace the
  two hard-coded style objects with `getEdgeStyle('WRITES_TO')` / `getEdgeStyle('READS_FROM')`

Both issues together: **~1 commit, ~15 lines changed.**

---

### Phase S2.1 — Backend `exploreRoutineAggregate` resolver (Phase 2 from original plan)

Adds a new GraphQL query that returns aggregated Routine → Table edges for the
new intermediate L2. Uses the aggregation Cypher already documented in
`LOOM_5LEVEL_ARCHITECTURE.md §3`:

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

**Files:**
- `services/shuttle/src/main/java/studio/seer/lineage/resource/LineageResource.java` — new `@Query("exploreRoutineAggregate")` resolver
- `services/shuttle/src/main/java/studio/seer/lineage/service/ExploreService.java` — new method mirroring `exploreSchema` shape, returns `ExploreResult` with DaliRoutine + DaliTable nodes + aggregated edges carrying `stmt_count` in edge meta

**Contract:** same `ExploreResult` shape so the frontend transform pipeline
reuses `transformGqlExplore` without modification.

---

### Phase S2.2 — Frontend level renumbering (Phase 3)

Mechanical rename: `ViewLevel` type union and all its hardcoded string usages
shift one slot so the new L2 slot is free for the aggregation view. Old L2 →
L3 (exploration), old L3 → L4 (column lineage).

**Approach:** reverse-rename order (L3 → L4 first, then L2 → L3) to avoid
collisions. Touch-points (from the original plan, ~24 files):
1. `frontends/verdandi/src/types/domain.ts` — `ViewLevel` union
2. `frontends/verdandi/src/stores/slices/navigationSlice.ts` — drill/jump/back transitions
3. `frontends/verdandi/src/utils/displayPipeline.ts` — L1/L2/L3 conditionals
4. `frontends/verdandi/src/services/hooks.ts` — level comments + query routing
5. `frontends/verdandi/src/components/layout/Breadcrumb.tsx` / `StatusBar.tsx` / `LegendButton.tsx`
6. `frontends/verdandi/src/utils/transformExplore.ts` — `L2_FLOW_EDGES` → `L3_FLOW_EDGES` rename

**Gate:** `npx tsc --noEmit` must pass after each file. Full Vitest suite before commit.

---

### Phase S2.3 — New L2 view (Phase 4)

Wire the aggregation data into a renderable React Flow canvas at the new L2 slot.

**Files:**
- `frontends/verdandi/src/services/lineage.ts` — `fetchRoutineAggregate(scope)` + GraphQL query
- `frontends/verdandi/src/services/hooks.ts` — `useRoutineAggregate(scope)` React Query hook
- `frontends/verdandi/src/utils/transformRoutineAggregate.ts` — **new** transform
- `frontends/verdandi/src/components/canvas/nodes/RoutineNode.tsx` — routine card with PROC/FUNC/PKG badge
- `frontends/verdandi/src/hooks/canvas/useGraphData.ts` — route `viewLevel === 'L2'` to the new hook

---

### Phase S2.4 — DaliRecord + DaliRecordField on L3 (Phase 4c)

Render PL/SQL records as first-class node cards on L3, analogous to DaliTable.
The backend schema (DaliRecord, DaliRecordField, HAS_RECORD_FIELD, RETURNS_INTO)
is already in place from Sprint 2 lineage-gaps work.

**Files:**
- `services/shuttle/src/main/java/studio/seer/lineage/service/ExploreService.java` — 3 new UNION ALL segments for records + HAS_RECORD_FIELD + RETURNS_INTO
- `frontends/verdandi/src/types/domain.ts` — extend `DaliNodeType` + `DaliEdgeType` unions
- `frontends/verdandi/src/utils/transformHelpers.ts` — `NODE_TYPE_MAP` entries, `getEdgeStyle` cases
- `frontends/verdandi/src/components/canvas/nodes/RecordNode.tsx` — **new** (cloned from TableNode)
- `frontends/verdandi/src/components/inspector/InspectorRecord.tsx` — **new**
- `frontends/verdandi/src/components/layout/LegendButton.tsx` — L3 legend rows

---

### Phase S2.5 — L4 statement-drill view (Phase 4b)

Single-statement drill with subquery tree + OUTPUT column flow.

**Files:**
- `services/shuttle/src/main/java/studio/seer/lineage/service/ExploreService.java` — `exploreStatementTree(stmtGeoid)` method
- `services/shuttle/src/main/java/studio/seer/lineage/resource/LineageResource.java` — `@Query("exploreStatementTree")` resolver
- `frontends/verdandi/src/services/lineage.ts` — `fetchStatementTree(stmtGeoid)`
- `frontends/verdandi/src/services/hooks.ts` — `useStatementTree(stmtGeoid)`
- `frontends/verdandi/src/utils/transformStatementTree.ts` — **new** transform
- `frontends/verdandi/src/components/canvas/nodes/OutputColumnNode.tsx` — **new** or extracted from StatementNode

---

### Phase S2.6 — StatementNode clickable header → L2 (Phase 5)

Make the groupPath breadcrumb in the StatementNode header clickable so users
can drill back to the new L2 view scoped to the specific routine / package.

**Files:**
- `frontends/verdandi/src/utils/transformExplore.ts` — post-walk `CONTAINS_STMT` / `CONTAINS_ROUTINE` edges to build `stmtGeoid → { routineGeoid, packageGeoid }` lookup, stuff into `node.data.metadata`
- `frontends/verdandi/src/components/canvas/nodes/StatementNode.tsx` — make header segments clickable, call `navigateToLevel('L2', geoid)`

---

## Execution order (minimises risk)

1. **S2.0** — Known issues cleanup (fast wins; ~1 commit)
2. **S2.1** — Backend `exploreRoutineAggregate` resolver (additive; ~1 commit)
3. **S2.2** — Frontend level renumbering (mechanical; must land before S2.3)
4. **S2.3** — New L2 view (depends on S2.1 + S2.2)
5. **S2.4** — DaliRecord rendering (independent; can slot anywhere after S2.2)
6. **S2.5** — L4 statement-drill (depends on S2.2)
7. **S2.6** — StatementNode click-back (depends on S2.2 + S2.3)

Each phase = own commit so any single phase can be reverted without unwinding
the others. Expected sprint size: ~10–12 commits + 1 PR.

## Verification

- Each phase: `tsc --noEmit` clean, Gradle build clean, no new runtime errors
- S2.0: visual column-to-column DATA_FLOW routing and palette-matching legend
- S2.1: GraphQL introspection shows `exploreRoutineAggregate` field; manual query returns aggregated edges with `stmt_count`
- S2.3: drill from L1 → L2 shows routines + tables with cross-routine edges
- S2.4: parse a `%ROWTYPE` PL/SQL file, drill to L3, see RecordNode with fields
- S2.5: drill from L3 on a multi-CTE statement, see root + CTE tree + OUTPUT column flow
- S2.6: click package name on a Statement header → canvas navigates to L2 scoped to that package
