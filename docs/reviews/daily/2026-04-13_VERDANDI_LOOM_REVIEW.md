# DAILY ARCHITECTURE REVIEW REPORT
## VERDANDI LOOM — Sprint 7→8

**Date:** 2026-04-13
**Branch:** `fix/stabilizing-sprint-apr13-2026`
**Baseline commit:** `32e7494` (fix(heimdall): add Bean Validation)
**Previous review:** 2026-04-12 (`SPRINT_07_VERDANDI_REVIEW.md`)
**Reviewer:** Claude Code (automated scheduled task)

---

## SUMMARY

| Category | Score | Status | Trend (vs 12.04) |
|----------|-------|--------|-------------------|
| Modularity & SoC | 4/5 | :yellow_circle: Good | → stable |
| State Management | 5/5 | :green_circle: Excellent | → stable |
| Rendering & Performance | 3/5 | :yellow_circle: ELK thread blocker | → stable |
| Security & RBAC | 4/5 | :green_circle: Good | :arrow_up: (logout fix) |
| Deployment & DevOps | 5/5 | :green_circle: Complete | → stable |
| Testing & Observability | 4/5 | :green_circle: Good (199 tests) | → stable |
| Design System & UI | 5/5 | :green_circle: Excellent | → stable |
| API & Integration | 4/5 | :green_circle: Good | :arrow_up: (dataSource field) |
| **Overall** | **4.25/5** | **:green_circle: ON TRACK** | **:arrow_up:** |

---

## PHASE COMPLETION STATUS

```
Phase 1    ████████████████████  100%  Scaffold + Mock Data          03.04 ✅
Phase 1.5  ████████████████████  100%  Auth + i18n                   04.04 ✅
Phase 2    ████████████████████  100%  Quarkus + RBAC + Real Data    04.04 ✅
Phase 3    ████████████████████  100%  Core Features                 07.04 ✅
Sprint 6   ████████████████████  100%  Refactor + Tests + Worker     08.04 ✅
Sprint 7   █████████████████░░░   84%  Polish + Perf + DevOps        ⟵ HERE
```

### Sprint 7 Block Progress

| Block | Tasks | Done | % | Status |
|-------|-------|------|---|--------|
| Block 1: Reliability | 5 | 5 | 100% | :white_check_mark: |
| Block 2: Testing | 7 | 6 | 86% | :white_check_mark: |
| Block 3: Performance | 4 | 2 | 50% | :arrows_counterclockwise: |
| Block 4: DevOps | 3 | 3 | 100% | :white_check_mark: |
| **TOTAL** | **19** | **16** | **84%** | |

---

## CHANGES SINCE LAST REVIEW (13.04.2026)

### Uncommitted changes on `fix/stabilizing-sprint-apr13-2026` (9 files, +45/-23)

#### 1. `authStore.ts` — Logout race condition fix :arrow_up: Security
- **Change:** `logout()` is now `async` — awaits server `POST /auth/logout` before clearing local state
- **Why:** Fire-and-forget caused cyclic redirect: ProtectedRoute sent user to `/login` while cookie was still valid; `checkSession()` found an active session and navigated back to `/`, creating a loop
- **Assessment:** :green_circle: **Good fix.** Addresses a real bug. `catch` block still clears local state on network error — correct fallback.

#### 2. `lineage.ts` — `dataSource` field added to all GraphQL queries :arrow_up: API
- **Change:** All 6 GQL queries (Explore, Lineage, Upstream, Downstream, ExpandDeep, StmtColumns) now request `dataSource` field
- **GraphNode interface** extended with `dataSource?: string`
- **Assessment:** :green_circle: **Clean addition.** Backend must expose this field in SHUTTLE GraphQL schema. Verify SHUTTLE compatibility.

#### 3. `transformExplore.ts` — `dataSource` propagated to node metadata
- **Change:** `metadata.dataSource` set from `node.dataSource` in 3 transform locations (schemaExplore tables, external tables, generic nodes)
- **Assessment:** :green_circle: **Consistent.** All transform paths now carry the field.

#### 4. `TableNode.tsx` — `dataSource` badge rendered in table header
- **Change:** Inline badge showing "master" (green) or other source (yellow/warning) next to column count
- **LOC:** 313 → ~328 (+15 lines)
- **Assessment:** :green_circle: **Good UX.** Uses CSS variables (`--suc`, `--wrn`) — consistent with design system. Two `as string` casts could be tightened with proper typing.

#### 5. Minor changes
- `index.html` — title/meta update
- `StatusBar.tsx` — minor text change
- `ProfileModal.tsx` — minor update
- `i18n/locales/{en,ru}/common.json` — translation key updates

---

## ACTIVE RISKS

### :red_circle: BREAKING — None

No breaking architectural issues detected.

### :yellow_circle: WARNINGS

| ID | Risk | Severity | Details | Since |
|----|------|----------|---------|-------|
| WARN-01 | **ELK on UI thread** | :yellow_circle: HIGH | `elkWorker.ts` exists (48 LOC) but unused. `layoutGraph.ts` runs ELK on main thread. Vite CJS→ESM breaks Worker global. **2-5 sec freeze at 500-1000 nodes.** | 12.04 |
| WARN-05 | **SearchPanel.tsx growing** | :yellow_circle: MEDIUM | 516 LOC (target ≤350). Needs split into SearchInput + SearchResults + SearchFilters. | 12.04 |
| WARN-06 | **LoomCanvas.tsx > target** | :green_circle: LOW | 425 LOC (target ≤350). Manageable, most logic delegated to hooks. | 12.04 |
| WARN-07 | **`as string` casts in TableNode** | :green_circle: LOW | Two `as string` casts for `data.metadata.dataSource`. Should type `metadata` interface properly. | 13.04 (NEW) |
| WARN-08 | **Test gaps: 4 hooks untested** | :yellow_circle: MEDIUM | useGraphData, useExpansion, useDisplayGraph, useLoomLayout — critical hooks without tests. | 12.04 |

---

## ADR COMPLIANCE

| ADR | Status | Notes |
|-----|--------|-------|
| ADR-001: Lazy Exploration (3 levels) | :white_check_mark: COMPLIANT | L1/L2/L3 fully implemented with drill-down, breadcrumb, navigation stack |
| ADR-002: React Flow (xyflow) | :white_check_mark: COMPLIANT | @xyflow/react 12.10.2, 13 custom nodes, MiniMap, Controls, Background |
| ADR-003: Tech Stack | :white_check_mark: COMPLIANT | React 19.2, Vite 8, Tailwind 4.2, Zustand 5, ELK 0.11, TanStack Query 5.96 |
| ADR-004: RBAC Proxy | :white_check_mark: COMPLIANT | Chur BFF with JWT httpOnly, Keycloak integration, role-based auth store |

### Stack Version Check (package.json)

| Dependency | Required | Actual | Status |
|------------|----------|--------|--------|
| react | ^19.0 | ^19.2.5 | :white_check_mark: |
| typescript | ^5.0 | ~5.9.3 | :white_check_mark: |
| vite | ^6.0 | ^8.0.1 | :white_check_mark: (exceeds) |
| @xyflow/react | ^11.0 | ^12.10.2 | :white_check_mark: |
| zustand | ^4.0 | ^5.0.12 | :white_check_mark: (exceeds) |
| elkjs | ^0.8 | ^0.11.1 | :white_check_mark: |
| @tanstack/react-query | ^5.0 | ^5.96.2 | :white_check_mark: |
| react-router-dom | ^7.0 | ^7.14.0 | :white_check_mark: |
| tailwindcss | ^4.0 | ^4.2.2 | :white_check_mark: |
| lucide-react | any | ^1.7.0 | :white_check_mark: |
| vitest | any | ^4.1.3 | :white_check_mark: |
| playwright | any | ^1.50.0 | :white_check_mark: |

---

## ARCHITECTURE VECTORS — DETAILED

### Vector 1: Modularity & Separation of Concerns — 4/5

**Strengths:**
- Clean directory structure: `components/`, `stores/`, `services/`, `hooks/`, `utils/`, `types/`
- loomStore decomposed into 11 slices (251 LOC main + ~800 LOC slices) — down from 647 LOC monolith
- Transform pipeline properly separated: `transformOverview`, `transformExplore`, `transformColumns`, `transformHelpers`
- Canvas hooks extracted: `useGraphData`, `useLoomLayout`, `useDisplayGraph`, `useExpansion`, `useFilterSync`, `useFitView`

**Issues:**
- SearchPanel.tsx at 516 LOC (target ≤350) — needs decomposition
- LoomCanvas.tsx at 425 LOC (target ≤350) — borderline, most logic in hooks

### Vector 2: State Management & Data Flow — 5/5

**Strengths:**
- Zustand 5 with modular slice architecture (11 slices)
- TanStack React Query for all data fetching (proper staleTime, auto-logout on 401)
- localStorage persistence via persistSlice (hydrates on mount)
- No conflicting sources of truth — single Zustand store for all global state
- Undo/redo stack implemented (LOOM-030)
- Debug inspector: `window.__LOOM__()` in browser console

**Data flow:**
```
GQL (React Query) → useGraphData → transform* → useDisplayGraph → useLoomLayout → React Flow
```

### Vector 3: Rendering & Performance — 3/5

**Strengths:**
- ELK layout for automatic graph positioning
- LOD zoom tracking (compact rendering below 0.2 zoom)
- useMemo in useDisplayGraph (2×) and useGraphData (2×)
- Column capping at 30 per node

**Issues:**
- :yellow_circle: **WARN-01: ELK runs on main thread** — elkWorker.ts exists but can't be loaded (Vite CJS→ESM issue). 2-5 sec freeze at 500+ nodes. This is the #1 performance blocker.
- Edge virtualization gap: React Flow doesn't virtualize edges; 17K edges cause jank

### Vector 4: Security & RBAC — 4/5 (:arrow_up: from 3.5)

**Strengths:**
- httpOnly JWT cookies (no localStorage for tokens)
- CORS hardened (Set-based allowlist, no wildcard)
- Rate limiting on /auth/login (5/15min prod, 50/1min dev)
- JWT silent refresh every 30 min
- :arrow_up: **NEW:** Logout now properly awaits server cookie clearing before redirecting

**Notes:**
- No `console.log(password)` or credential leaks in code
- Auth store properly clears state even on network failure

### Vector 5: Deployment & DevOps — 5/5

**Complete:**
- Dockerfile (multi-stage: Node 24 → Nginx alpine)
- .dockerignore (excludes node_modules, dist, .env)
- nginx.conf (SPA fallback, reverse proxy to Chur)
- docker-compose.yml (port 15173, depends on Chur/SHUTTLE/Keycloak)
- .env.example + .env.k8s.example
- CI pipeline (ci.yml + cd.yml)
- Module Federation configured (exposes `./App` as `verdandi/App`)

### Vector 6: Testing & Observability — 4/5

**199 unit tests** across 23 test files:
- Store slices: filterSlice, persistSlice, undoSlice
- Utils: transformColumns, transformExplore, transformHelpers, displayPipeline
- Hooks: useHotkeys, useSearchHistory
- Components: CommandPalette, SearchPalette, ProfileModal, Inspector*
- E2E: 1 smoke test (Playwright)
- Vitest config with coverage thresholds (70% lines, 70% functions, 60% branches)

**Gaps:**
- 4 critical canvas hooks untested: useGraphData, useExpansion, useDisplayGraph, useLoomLayout

### Vector 7: Design System & UI — 5/5

- CSS variables throughout (--bg0, --t3, --bd, --suc, --wrn, --acc, --inf, etc.)
- Dark theme default, light theme supported
- 5 palettes (amber-forest + 4 others)
- Tailwind CSS 4 with CVA for component variants
- Lucide icons consistent across all components
- i18n: 420 keys in EN + RU
- New dataSource badge in TableNode uses CSS variables correctly

### Vector 8: API & Integration — 4/5 (:arrow_up:)

**Strengths:**
- GraphQL client with proper typing (GraphNode, GraphEdge, ExploreResult, etc.)
- 9 queries defined (Overview, Explore, Lineage, Upstream, Downstream, ExpandDeep, StmtColumns, Search, KNOT)
- :arrow_up: **NEW:** `dataSource` field added to all applicable queries — frontend ready for multi-source lineage
- credentials: 'include' for httpOnly cookie support
- Error helpers: `isUnauthorized()`, `isUnavailable()`

**Notes:**
- Verify SHUTTLE GraphQL schema exposes `dataSource` field on `GraphNode` type
- `api.ts` types (ApiNode, ApiEdge, ApiGraphResponse) appear unused — consider cleanup

---

## CODEBASE METRICS

| Metric | 09.04 | 12.04 | 13.04 | Target |
|--------|-------|-------|-------|--------|
| Total src/ LOC | ~6,000 | ~6,178 | ~6,200 | — |
| Components | 60+ | 65+ | 65+ | — |
| Store slices | 10 | 11 | 11 | — |
| Custom hooks | 11 | 11 | 11 | — |
| Unit tests | ~108 | 199 | 199 | 250+ |
| E2E tests | 0 | 5 | 5 | 10+ |
| Test files | 4 | 23 | 23 | 30+ |
| loomStore LOC | 647 | 251 | 251 | <400 :white_check_mark: |
| LoomCanvas LOC | — | 403 | 425 | ≤350 :yellow_circle: |
| SearchPanel LOC | 470 | 516 | 516 | ≤350 :red_circle: |
| Node types | 11 | 13 | 13 | — |
| i18n keys | ~350 | ~420 | ~420 | — |

---

## SPRINT 7 REMAINING WORK

### Block 3: Performance (50% — 2 of 4 remaining)

| Task | Status | Priority |
|------|--------|----------|
| P-01 ELK Web Worker | :red_circle: BLOCKED (Vite CJS→ESM) | P0 — main perf bottleneck |
| P-03 Edge virtualization | :white_square_button: Not started | P1 |
| P-02 Memoization | :white_check_mark: Done | — |
| P-04 Lazy loading | :white_check_mark: Done | — |

### Testing Gaps (carry to Sprint 8)

| Hook | LOC | Priority |
|------|-----|----------|
| useLoomLayout | 352 | P1 (most complex) |
| useGraphData | 110 | P1 |
| useExpansion | 88 | P2 |
| useDisplayGraph | 69 | P2 |

---

## RECOMMENDATIONS

1. **P0 — Fix ELK Worker (WARN-01):** Investigate Vite's worker bundling for ESM. Options: (a) use `?worker` import syntax, (b) use `comlink` wrapper, (c) bundle ELK separately as IIFE. This is the single biggest performance risk.

2. **P1 — Split SearchPanel.tsx:** 516 LOC and growing. Decompose into SearchInput + SearchResults + SearchFilters subcomponents.

3. **P1 — Type `metadata` properly in TableNode:** Replace `as string` casts with a typed metadata interface that includes `dataSource?: string`.

4. **P1 — Verify SHUTTLE `dataSource` field:** Ensure the GraphQL schema on SHUTTLE side returns `dataSource` for all node queries. If not available yet, frontend will silently ignore (optional field) — no breaking risk.

5. **P2 — Clean up `api.ts`:** Types `ApiNode`, `ApiEdge`, `ApiGraphResponse` appear unused (replaced by `lineage.ts` types). Verify and remove if dead code.

6. **P2 — Add tests for canvas hooks:** Prioritize useLoomLayout (352 LOC, most complex), then useGraphData.

---

## HEALTH CHECK

- **Schedule Adherence:** 84% (Sprint 7, 16/19 tasks)
- **Technical Risk:** :yellow_circle: MEDIUM (ELK worker is the main blocker)
- **Code Quality:** :green_circle: HIGH (clean architecture, good test coverage)
- **Confidence in Sprint 7 Completion:** :yellow_circle: MEDIUM (ELK worker and SearchPanel split remain)

---

*Next review: 2026-04-14*
*Report generated by Claude Code automated scheduled task*
