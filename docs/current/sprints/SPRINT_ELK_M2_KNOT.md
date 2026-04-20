# Sprint: ELK M2 KNOT

**Status:** ✅ DONE (EK-01 visual quality → ⚠️ retest 2026-05-20)  
**Branch:** `feature/elk-m2-knot`  
**Target:** v1.3.0  
**Dates:** 2026-04-20 – 2026-04-20  

---

## Tasks

| ID | Title | Status | Estimate |
|----|-------|--------|----------|
| EK-01 | ELK algorithm auto-switch for dense graphs (E/V > 5 → stress) | ⚠️ BACKLOG-RETEST (2026-05-20) | 1d |
| EK-02 | KNOT Inspector — Statements section + deduplication by stmtGeoid | ✅ DONE | 1d |
| HOUND 2076N | ReactFlow virtualization for large graphs (onlyRenderVisibleElements) + perf marks | ✅ DONE | 1d |
| BUG-EK-01 | DB node click not opening L2 view | ✅ DONE | 0.5d |

---

## Changes

### EK-01 — Dense Graph Detection
- `DENSE_GRAPH_RATIO=5`, `STRESS_EDGE_LENGTH=150` added to `constants.ts`
- `getDenseOptions()` in `layoutGraph.ts` — uses `elk.algorithm: 'stress'`
- `isDense` field added to `LayoutResult` interface
- `useLoomLayout.ts` logs `[LOOM] Dense graph (E/V=N.N) — stress algorithm`

### EK-02 — Statements Section
- `TableStatementsSection` component in `InspectorTable.tsx`
- Deduplicates `KnotTableUsage` by `stmtGeoid` via `useMemo`
- Reuses `useKnotTableRoutines` — no new backend endpoint required
- i18n: `inspector.statements` / `inspector.noStatements` in `en/common.json` + `ru/common.json`
- Rendered after `TableRoutinesSection` in the Overview tab

### HOUND 2076N — Performance
- `PERF_VIRTUALIZE_THRESHOLD=1500` in `constants.ts`
- `onlyRenderVisibleElements={nodes.length > LAYOUT.PERF_VIRTUALIZE_THRESHOLD}` in `LoomCanvas.tsx`
- `performance.mark('loom-layout-start/end')` + `performance.measure('loom-layout', ...)` in `layoutGraph.ts`
- Grid path for nodes > `AUTO_GRID_THRESHOLD (800)` ensures O(n) layout for 2076-node graphs

### BUG-EK-01 — DB Node Drill-Down
- `useNodeInteractions.ts`: `onNodeClick` for `databaseNode` at L1 now calls `drillDown()`
- Previously only set `setL1HierarchyDb()` without drilling — L2 never opened on single click

---

## Tests

- `layoutGraph.test.ts`: EK-01 dense detection (isDense=true/false), HOUND grid path
- `InspectorTable.test.tsx`: EK-02 statements dedup (3 usages → 2 unique), empty state
- Coverage: Statements 73.6%, Branches 60.6%, Functions 70.9%, Lines 76.5%
- TypeScript: 0 errors

---

## Docs

- `docs/current/specs/verdandi/LOOM_LAYOUT_PERF.md` — layout algorithm thresholds, DevTools measurement
- `docs/current/specs/verdandi/KNOT_INSPECTOR.md` — InspectorTable structure, Statements section
