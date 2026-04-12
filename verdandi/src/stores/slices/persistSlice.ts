/**
 * persistSlice — saves/restores selected filter state to localStorage.
 *
 * Persisted keys (under `seer-loom-filters`):
 *   viewLevel, currentScope, currentScopeLabel,
 *   depth, upstream, downstream, tableLevelView, showCfEdges,
 *   startObjectId, startObjectType, startObjectLabel
 *
 * NOT persisted (populated from API):
 *   availableTables, availableStmts, availableFields, availableColumns
 */

import type { LoomStore, FilterState } from '../loomStore';

const STORAGE_KEY = 'seer-loom-filters';
const DEBOUNCE_MS = 500;

// ─── Shape of what we persist ────────────────────────────────────────────────

interface PersistedState {
  viewLevel:         string;
  currentScope:      string | null;
  currentScopeLabel: string | null;
  filter: Pick<
    FilterState,
    'startObjectId' | 'startObjectType' | 'startObjectLabel' |
    'depth' | 'upstream' | 'downstream' | 'tableLevelView' | 'showCfEdges'
  >;
}

// ─── Hydrate: read from localStorage on app start ────────────────────────────

export function hydratePersistedState(): Partial<LoomStore> {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return {};
    const saved: PersistedState = JSON.parse(raw);
    return {
      viewLevel:         (saved.viewLevel as LoomStore['viewLevel']) ?? 'L1',
      currentScope:      saved.currentScope ?? null,
      currentScopeLabel: saved.currentScopeLabel ?? null,
      filter: {
        startObjectId:    saved.filter?.startObjectId    ?? null,
        startObjectType:  saved.filter?.startObjectType  ?? null,
        startObjectLabel: saved.filter?.startObjectLabel ?? null,
        depth:            saved.filter?.depth            ?? 5,
        upstream:         saved.filter?.upstream         ?? true,
        downstream:       saved.filter?.downstream       ?? true,
        tableLevelView:   saved.filter?.tableLevelView   ?? false,
        showCfEdges:      saved.filter?.showCfEdges      ?? true,
        // Not persisted — will be populated from API
        tableFilter:  null,
        stmtFilter:   null,
        fieldFilter:  null,
      },
    };
  } catch {
    return {};
  }
}

// ─── Subscribe: debounced write on every store change ────────────────────────

export function subscribePersist(store: { getState: () => LoomStore; subscribe: (fn: () => void) => () => void }) {
  let timer: ReturnType<typeof setTimeout> | null = null;

  store.subscribe(() => {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      const s = store.getState();
      const data: PersistedState = {
        viewLevel:         s.viewLevel,
        currentScope:      s.currentScope,
        currentScopeLabel: s.currentScopeLabel,
        filter: {
          startObjectId:    s.filter.startObjectId,
          startObjectType:  s.filter.startObjectType,
          startObjectLabel: s.filter.startObjectLabel,
          depth:            s.filter.depth,
          upstream:         s.filter.upstream,
          downstream:       s.filter.downstream,
          tableLevelView:   s.filter.tableLevelView,
          showCfEdges:      s.filter.showCfEdges,
        },
      };
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
      } catch { /* quota exceeded — ignore */ }
    }, DEBOUNCE_MS);
  });
}
