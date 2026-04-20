# SPRINT: Verdandi UX Mobile — FilterToolbar L1/L2 Unification + Responsive

**Version:** 1.0  
**Date:** 2026-04-20  
**Branch:** `develop` (committed `f6c6bfd`)  
**Status:** ✅ DONE  
**Release:** v1.2.0  

---

## Scope

Mobile-responsive pass for Verdandi frontend + FilterToolbar style unification (L1 ↔ L2) + new routineFilter dimension.

---

## Tasks

### VM-01 — FilterToolbarL1: Two-row layout ✅
- App/DB/Schema CascadePills moved from inline Row 1 into collapsible Row 2
- Row 2 pills stacked vertically, full-width on mobile
- `row2Collapsed` state with `▾/▴` toggle on mobile only
- Desktop: Row 2 always visible (not collapsible)
- **Files:** `src/components/layout/FilterToolbarL1.tsx`

### VM-02 — FilterToolbar (L2/L3): L1 style parity ✅
- Background → `var(--bg0)`, border → `0.5px solid var(--bd)`
- All fonts → 9–10px, `fontFamily: var(--mono)` on pills
- All ToolbarToggleButton → `size="sm"` (10px font)
- Depth buttons: `0.5px` border, `borderRadius:3`, `color-mix` active background
- Badge: `0.5px` bordered box, 9px mono
- Row 2 `borderTop: 0.5px solid var(--bd)`, `padding: 4px 10px`
- **Files:** `src/components/layout/FilterToolbar.tsx`

### VM-03 — RoutineFilter: new filter dimension ✅
- `FilterState.routineFilter: string | null` added to store
- `setRoutineFilter()` action: clears tableFilter, stmtFilter, fieldFilter, availableColumns
- `FILTER_DEFAULTS.routineFilter = null` (loomStore + navigationSlice local copy)
- `useFilterSync`: populates `availableRoutines` from `routineNode`/`packageNode` types
- `useLoomLayout`: `activeId = stmtFilter ?? tableFilter ?? routineFilter` for BFS dimming
- `FilterToolbar`: new `FilterPill` for routines (shown when `viewLevel='L2'` and routines available)
- `persistSlice`: `routineFilter: null` in hydrated state (not persisted)
- **Files:** `loomStore.ts`, `filterSlice.ts`, `navigationSlice.ts`, `persistSlice.ts`, `useFilterSync.ts`, `useLoomLayout.ts`, `FilterToolbar.tsx`

### VM-04 — ProfileModal: mobile responsive ✅
- Full-screen on mobile (`width/height: 100%`, no border/radius/shadow)
- Left sidebar (192px) replaced by horizontal scrollable tab strip on mobile
- All 10 tabs + logout button in one `overflowX: auto` row
- Active tab: `borderBottom: 2px solid var(--acc)` underline
- Header: username hidden on mobile to save space
- Content padding: `16px` mobile / `24px 28px` desktop
- **Files:** `src/components/profile/ProfileModal.tsx`

### VM-05 — useIsMobile hook ✅
- `window.matchMedia('(max-width: 639px)')` with SSR-safe `typeof window` guard
- Returns reactive boolean, updates on resize
- **File:** `src/hooks/useIsMobile.ts`

### VM-06 — MobileInspectorDrawer ✅
- Slide-up drawer component for mobile inspector panel
- **File:** `src/components/layout/MobileInspectorDrawer.tsx`

### VM-07 — i18n keys ✅
- `toolbar.showFilters` / `toolbar.hideFilters` — EN + RU
- `inspector.noSqlText` — EN + RU
- **Files:** `src/i18n/locales/en/common.json`, `src/i18n/locales/ru/common.json`

### VM-08 — Test setup: matchMedia mock ✅
- `src/test/setup.ts`: `window.matchMedia` mock (jsdom doesn't implement it)
- Guarded with `typeof window !== 'undefined'` (some test envs are non-browser)
- **File:** `src/test/setup.ts`

---

## CI Results

| Check | Result |
|-------|--------|
| `npm run build` (tsc + vite) | ✅ 0 errors |
| `npx vitest run` | ✅ 216/216 passed |

---

## Definition of Done

- [x] `npm run build` passes with 0 TypeScript errors
- [x] All 216 vitest tests pass
- [x] FilterToolbar L1/L2 visually unified (same fonts, borders, backgrounds)
- [x] Routine filter pill present and functional in L2
- [x] ProfileModal mobile layout works on 375px viewport
- [x] Committed to `develop`, merged to `release`, tagged `v1.2.0`
