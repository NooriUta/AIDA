# SEER Studio System Architecture v3.0 (post-Sprint 9)

**Date:** 2026-04-11
**Scope:** Full system overview after Sprint 9 completion

---

## 1. Component Inventory

- **~90+ files**, **~19.7K LOC** total across the VERDANDI front-end

### Component Tree

```
verdandi/src/components/
|-- canvas/
|   |-- LoomCanvas.tsx
|   |-- nodes/
|       |-- StatementNode.tsx
|       |-- TableNode.tsx
|-- layout/
|   |-- Shell.tsx
|   |-- Header.tsx
|   |-- FilterToolbar.tsx
|   |-- FilterToolbarL1.tsx
|-- profile/                          # 11 files
|   |-- ProfileModal.tsx
|   |-- ProfileTabAppearance.tsx
|   |-- ProfileTabGraph.tsx
|   |-- ProfileTabKeybindings.tsx
|   |-- ProfileTabLanguage.tsx
|   |-- ProfileTabNotifications.tsx
|   |-- ProfileTabPerformance.tsx
|   |-- ProfileTabPrivacy.tsx
|   |-- ProfileTabProfile.tsx
|   |-- ProfileTabSessions.tsx
|   |-- ProfileTabAbout.tsx
|-- inspector/
|   |-- InspectorPanel.tsx
|   |-- InspectorColumn.tsx           # +new, ~50 LOC
|   |-- InspectorJoin.tsx             # +new, ~50 LOC
|   |-- InspectorParameter.tsx        # +new, ~50 LOC
|-- stubs/
|   |-- UnderConstructionPage.tsx     # 89 LOC
|-- ui/
|   |-- ToolbarPrimitives.tsx         # 73 LOC
|-- CommandPalette.tsx                # 312 LOC
|-- SearchPalette.tsx                 # 643 LOC
```

---

## 2. Store Slices (13 Total)

All slices are combined into a single Zustand store via `loomStore.ts`.

| # | Slice | LOC | Purpose |
|---|-------|-----|---------|
| 1 | `navigationSlice` | ~50 | Breadcrumb stack, `drillDown` / `jumpTo` actions |
| 2 | `l1Slice` | ~80 | L1 scope selection, hierarchy traversal, DB expansion |
| 3 | `selectionSlice` | ~20 | Node selection state, highlight tracking |
| 4 | `filterSlice` | ~80 | L2/L3 filter chips, cross-clearing between levels |
| 5 | `expansionSlice` | ~60 | Upstream/downstream node expansion |
| 6 | `visibilitySlice` | ~50 | Hide/restore/showAll node visibility |
| 7 | `viewportSlice` | ~40 | Fit view, focus node, deep expand triggers |
| 8 | `themeSlice` | ~30 | Dark/light mode toggle, palette selection |
| 9 | `undoSlice` | ~115 | Undo/redo stack for visibility operations |
| 10 | `persistSlice` | ~92 | localStorage-backed filter state persistence |
| 11 | `authStore` | ~93 | Keycloak authentication state (standalone store) |
| 12 | `searchSlice` | -- | Search state for CommandPalette/SearchPalette |
| 13 | `inspectorSlice` | -- | Inspector panel open/close and selected entity |

---

## 3. Hooks Inventory

| Hook | LOC | Purpose |
|------|-----|---------|
| `useGraphData` | ~80 | GraphQL queries via Apollo, statement column enrichment |
| `useDisplayGraph` | ~70 | 9-phase display pipeline (wrapped in `useMemo`) |
| `useLoomLayout` | ~330 | ELK-based L1 layout engine + post-layout dimming |
| `useFilterSync` | ~143 | Populate filter panel option lists from `rawGraph` |
| `useFitView` | ~50 | Viewport zoom/pan control helpers |
| `useExpansion` | ~60 | Node expand/collapse side-effect management |
| `useHotkeys` | ~79 | Keyboard shortcut registration (Ctrl+K, Ctrl+P, etc.) |
| `useSearchHistory` | ~68 | localStorage-backed search query and recent node persistence |

---

## 4. localStorage Keys Map

| Key | Module | Purpose |
|-----|--------|---------|
| `seer-theme` | `themeSlice` | Dark/light mode preference |
| `seer-palette` | `themeSlice` | Active color palette ID |
| `seer-loom-filters` | `persistSlice` | Serialized filter state (L2/L3 chips) |
| `seer-search-history` | `useSearchHistory` | Recent search query strings |
| `seer-recent-nodes` | `useSearchHistory` | Recently visited node IDs |
| `seer-ui-font` | `ProfileTabAppearance` | UI font family override |
| `seer-mono-font` | `ProfileTabAppearance` | Monospace font family override |
| `seer-font-size` | `ProfileTabAppearance` | Base font size (px) |
| `seer-density` | `ProfileTabAppearance` | Layout density: `compact` or `normal` |
| `seer-graph-prefs` | `ProfileTabGraph` | Graph rendering preferences |

---

## 5. CSS Variables Set by Profile

The profile appearance tab writes the following CSS custom properties to `:root`:

| Variable | Source | Example Value |
|----------|--------|---------------|
| `--font` | `seer-ui-font` | `"Inter", sans-serif` |
| `--mono` | `seer-mono-font` | `"JetBrains Mono", monospace` |
| `fontSize` | `seer-font-size` | `14px` (applied to `<html>`) |
| `data-density` | `seer-density` | `compact` (applied as `data-density` attribute on `<body>`) |

---

## 6. Norn Architecture (Header.tsx)

The top-level navigation follows the Norse Norn naming convention. Each Norn maps to a major subsystem of SEER Studio.

```
SEER Studio
|
|-- URD (H3, disabled)
|   Past-facing analysis tools (future roadmap)
|
|-- VERDANDI (active)
|   |-- LOOM   /          Active -- Data Lineage visualizer
|   |-- KNOT   /knot      Active -- Compact graph view
|   |-- ANVIL  H2         Disabled -- Schema editor (future)
|
|-- SKULD (H3, disabled)
    Future-facing prediction tools (future roadmap)
```

**Legend:**
- **Active** -- Routable, functional module
- **H2** -- Planned for Horizon 2 (next major release)
- **H3** -- Planned for Horizon 3 (long-term roadmap)
- **disabled** -- Navigation link rendered but non-clickable

---

## 7. i18n Stats

| Metric | Value |
|--------|-------|
| Total keys | ~420 |
| Supported locales | `en`, `ru` |
| Key file format | JSON (flat namespace) |
| Integration | `react-i18next` with `useTranslation` hook |

---

## 8. Test Stats

| Category | Count |
|----------|-------|
| Existing tests (pre-Sprint 9) | 108 |
| Sprint 9.5 additions | 91 |
| **Total** | **199** |

---

*End of document.*
