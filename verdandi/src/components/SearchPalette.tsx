import { memo, useState, useRef, useEffect, useCallback } from 'react';
import { Search, X, Table2, Code2, Columns3, Eye, Database, AppWindow, Variable, Braces, Clock, History } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../stores/loomStore';
import { useSearch } from '../services/hooks';
import type { SearchResult } from '../services/lineage';
import {
  getSearchHistory, pushSearchQuery, clearSearchHistory,
  getRecentNodes, pushRecentNode, clearRecentNodes,
  type RecentNode,
} from '../hooks/useSearchHistory';

// ─── Type icon ────────────────────────────────────────────────────────────────

function TypeIcon({ type }: { type: string }) {
  const size = 12;
  switch (type) {
    case 'DaliTable':
      return <Table2 size={size} color="var(--acc)" strokeWidth={1.5} />;
    case 'DaliColumn':
    case 'DaliOutputColumn':
      return <Columns3 size={size} color="var(--inf)" strokeWidth={1.5} />;
    case 'DaliRoutine':
    case 'DaliPackage':
    case 'DaliStatement':
    case 'DaliSession':
      return <Code2 size={size} color="var(--suc)" strokeWidth={1.5} />;
    case 'DaliParameter':
    case 'DaliVariable':
      return <Variable size={size} color="var(--suc)" strokeWidth={1.5} />;
    case 'DaliSchema':
    case 'DaliDatabase':
      return <Database size={size} color="var(--t3)" strokeWidth={1.5} />;
    case 'DaliApplication':
      return <AppWindow size={size} color="var(--t2)" strokeWidth={1.5} />;
    default:
      return <Braces size={size} color="var(--t3)" strokeWidth={1.5} />;
  }
}

// ─── Type filter tabs ─────────────────────────────────────────────────────────

const TYPE_FILTERS = [
  'all', 'tables', 'columns', 'routines', 'statements', 'databases', 'applications',
] as const;
type FilterKey = (typeof TYPE_FILTERS)[number];

function matchesFilter(type: string, filters: Set<FilterKey>): boolean {
  if (filters.size === 0) return true;
  if (filters.has('tables')       && type === 'DaliTable') return true;
  if (filters.has('columns')      && (type === 'DaliColumn' || type === 'DaliOutputColumn')) return true;
  if (filters.has('routines')     && (type === 'DaliRoutine' || type === 'DaliPackage' || type === 'DaliSession' || type === 'DaliParameter' || type === 'DaliVariable')) return true;
  if (filters.has('statements')   && type === 'DaliStatement') return true;
  if (filters.has('databases')    && (type === 'DaliDatabase' || type === 'DaliSchema')) return true;
  if (filters.has('applications') && type === 'DaliApplication') return true;
  return false;
}

// ─── Props ────────────────────────────────────────────────────────────────────

interface SearchPaletteProps {
  open: boolean;
  onClose: () => void;
}

// ─── Component ────────────────────────────────────────────────────────────────

export const SearchPalette = memo(({ open, onClose }: SearchPaletteProps) => {
  const { t } = useTranslation();
  const {
    jumpTo, selectNode, requestFocusNode,
    hiddenNodeIds, restoreNode, showAllNodes,
  } = useLoomStore();

  const [query, setQuery]                    = useState('');
  const [debouncedQuery, setDebounced]       = useState('');
  const [typeFilters, setTypeFilters]        = useState<Set<FilterKey>>(new Set());
  const [activeIndex, setActiveIndex]        = useState(0);
  const [recentQueries, setRecentQueries]    = useState<string[]>([]);
  const [recentNodes, setRecentNodes]        = useState<RecentNode[]>([]);
  const inputRef  = useRef<HTMLInputElement>(null);
  const listRef   = useRef<HTMLDivElement>(null);
  const timerRef  = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Focus + reset on open; load history
  useEffect(() => {
    if (open) {
      setQuery('');
      setDebounced('');
      setTypeFilters(new Set());
      setActiveIndex(0);
      setRecentQueries(getSearchHistory());
      setRecentNodes(getRecentNodes());
      requestAnimationFrame(() => inputRef.current?.focus());
    }
  }, [open]);

  // Debounced search
  const handleInput = useCallback((value: string) => {
    setQuery(value);
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setDebounced(value), 300);
  }, []);

  const clearQuery = useCallback(() => {
    setQuery('');
    setDebounced('');
    if (timerRef.current) clearTimeout(timerRef.current);
  }, []);

  // Backend search
  const searchQ = useSearch(debouncedQuery.length >= 2 ? debouncedQuery : '', 20);

  // Filter results
  const results = (searchQ.data ?? []).filter((r) => matchesFilter(r.type, typeFilters));

  // Hidden nodes
  const hiddenIds = [...hiddenNodeIds];
  const showHidden = hiddenIds.length > 0 && debouncedQuery.length < 2;

  // Total selectable items for keyboard nav
  const totalItems = results.length + (showHidden ? hiddenIds.length : 0);

  // Reset index on results change
  useEffect(() => { setActiveIndex(0); }, [results.length, showHidden]);

  // ── Navigation: select a search result ──────────────────────────────────────
  // Routing rules (ARCH):
  //   L1 — Application, Service, Database, Schema
  //   L2 — Table, Column (→ parent table), OutputColumn (→ parent stmt),
  //         Package, Routine (→ parent package), root Statement (pkg scope)
  //   L3 — Subquery / nested Statement (session scope)
  //   TODO — Session, Parameter, Variable (level TBD)
  const handleSelect = useCallback((result: SearchResult) => {
    // Push to search history
    if (debouncedQuery.length >= 2) pushSearchQuery(debouncedQuery);
    pushRecentNode({ id: result.id, label: result.label, type: result.type, scope: result.scope });
    onClose();
    const type = result.type as string;

    // ── L1: System / DB / Schema ──────────────────────────────────────────────
    if (
      type === 'DaliApplication' ||
      type === 'DaliService'     ||
      type === 'DaliDatabase'    ||
      type === 'DaliSchema'
    ) {
      jumpTo('L1', null, result.label);
      selectNode(result.id);
      requestFocusNode(result.id);
      return;
    }

    // ── L2: Table ─────────────────────────────────────────────────────────────
    if (type === 'DaliTable') {
      jumpTo('L2', result.id, result.label, 'DaliTable',
        { focusNodeId: result.id, expandDepth: 5 });
      return;
    }

    // ── L2: Column → shows parent table context ──────────────────────────────
    if (type === 'DaliColumn') {
      jumpTo('L2', result.id, result.label, 'DaliColumn');
      return;
    }

    // ── L2: OutputColumn → shows parent statement context ────────────────────
    if (type === 'DaliOutputColumn') {
      jumpTo('L2', result.id, result.label, 'DaliOutputColumn');
      return;
    }

    // ── L2: Package → package explore ────────────────────────────────────────
    if (type === 'DaliPackage') {
      jumpTo('L2', 'pkg-' + result.scope, result.scope, 'DaliPackage');
      return;
    }

    // ── L2: Routine → opens parent package ───────────────────────────────────
    if (type === 'DaliRoutine') {
      jumpTo('L2', 'pkg-' + result.scope, result.scope, 'DaliPackage');
      return;
    }

    // ── L2/L3: Statement — root (pkg scope) → L2, subquery → L3 ─────────────
    if (type === 'DaliStatement') {
      const isRootStmt = result.scope
        && !result.scope.startsWith('session-')
        && !result.scope.startsWith('#');
      if (isRootStmt) {
        jumpTo('L2', 'pkg-' + result.scope, result.scope, 'DaliPackage');
      } else {
        jumpTo('L3', result.id, result.label, 'DaliStatement');
      }
      return;
    }

    // ── TODO: level TBD — best-effort routing for now ────────────────────────
    if (type === 'DaliSession') {
      // TODO: confirm — currently opens session as L2 explore
      jumpTo('L2', result.id, result.label, 'DaliSession');
    } else if (type === 'DaliParameter' || type === 'DaliVariable') {
      // TODO: confirm — currently opens as L3 lineage
      jumpTo('L3', result.id, result.label, type as never);
    } else {
      selectNode(result.id);
    }
  }, [jumpTo, selectNode, requestFocusNode, onClose]);

  // ── Toggle type filter ──────────────────────────────────────────────────────
  const toggleFilter = useCallback((key: FilterKey) => {
    if (key === 'all') {
      setTypeFilters(new Set());
      return;
    }
    setTypeFilters((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key); else next.add(key);
      return next;
    });
  }, []);

  // ── Keyboard navigation ─────────────────────────────────────────────────────
  const onKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIndex((prev) => (prev + 1) % Math.max(totalItems, 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex((prev) => (prev - 1 + totalItems) % Math.max(totalItems, 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (activeIndex < results.length) {
        handleSelect(results[activeIndex]);
      }
    } else if (e.key === 'Escape') {
      e.preventDefault();
      onClose();
    }
  }, [totalItems, activeIndex, results, handleSelect, onClose]);

  // Scroll active into view
  useEffect(() => {
    const el = listRef.current?.querySelector('[data-active="true"]') as HTMLElement | undefined;
    el?.scrollIntoView({ block: 'nearest' });
  }, [activeIndex]);

  if (!open) return null;

  return (
    <>
      {/* Backdrop */}
      <div
        onClick={onClose}
        style={{
          position: 'fixed', inset: 0, zIndex: 9998,
          background: 'rgba(0,0,0,0.45)',
          backdropFilter: 'blur(2px)',
        }}
      />

      {/* Search Palette container */}
      <div
        role="dialog"
        aria-label={t('searchPalette.title')}
        onKeyDown={onKeyDown}
        style={{
          position: 'fixed',
          top: '12%', left: '50%',
          transform: 'translateX(-50%)',
          zIndex: 9999,
          width: '100%', maxWidth: 540,
          maxHeight: '70vh',
          background: 'var(--bg1)',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-xl)',
          boxShadow: '0 16px 48px rgba(0,0,0,0.5)',
          overflow: 'hidden',
          display: 'flex', flexDirection: 'column',
          animation: 'searchPaletteIn 0.15s ease-out',
        }}
      >
        <style>{`
          @keyframes searchPaletteIn {
            from { opacity: 0; transform: translateX(-50%) translateY(-6px) scale(0.98); }
            to   { opacity: 1; transform: translateX(-50%) translateY(0) scale(1); }
          }
        `}</style>

        {/* ── Search input ───────────────────────────────────────────────────── */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 8,
          padding: '10px 14px',
          borderBottom: '1px solid var(--bd)',
          flexShrink: 0,
        }}>
          <Search size={14} style={{ color: 'var(--t3)', flexShrink: 0 }} />
          <input
            ref={inputRef}
            value={query}
            onChange={(e) => handleInput(e.target.value)}
            placeholder={t('search.placeholder')}
            style={{
              flex: 1, border: 'none', outline: 'none',
              background: 'transparent',
              color: 'var(--t1)', fontSize: '13px',
              fontFamily: 'var(--font)',
            }}
          />
          {query.length > 0 && (
            <button
              onClick={clearQuery}
              style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '2px', display: 'flex' }}
            >
              <X size={12} color="var(--t3)" />
            </button>
          )}
          <span style={{ fontSize: '9px', color: 'var(--t3)', letterSpacing: '0.04em', flexShrink: 0 }}>
            ESC {t('profile.close').toLowerCase()}
          </span>
        </div>

        {/* ── Type filter tabs ────────────────────────────────────────────────── */}
        <div style={{
          display: 'flex', gap: 3, padding: '6px 14px',
          borderBottom: '1px solid var(--bd)',
          flexShrink: 0, overflowX: 'auto',
        }}>
          {TYPE_FILTERS.map((key) => {
            const isActive = key === 'all' ? typeFilters.size === 0 : typeFilters.has(key);
            return (
              <button
                key={key}
                onClick={() => toggleFilter(key)}
                style={{
                  padding: '3px 10px',
                  fontSize: '10px', fontWeight: 500,
                  borderRadius: 'var(--seer-radius-sm)',
                  border: '1px solid',
                  borderColor: isActive ? 'var(--acc)' : 'var(--bd)',
                  background: isActive
                    ? 'color-mix(in srgb, var(--acc) 15%, transparent)'
                    : 'transparent',
                  color: isActive ? 'var(--acc)' : 'var(--t2)',
                  cursor: 'pointer',
                  whiteSpace: 'nowrap',
                  transition: 'background 0.1s, border-color 0.1s',
                  letterSpacing: '0.03em',
                }}
              >
                {t(`search.filters.${key}`)}
              </button>
            );
          })}
        </div>

        {/* ── Results list ────────────────────────────────────────────────────── */}
        <div ref={listRef} style={{ flex: 1, overflowY: 'auto', padding: '4px 0' }}>

          {/* Loading */}
          {searchQ.isFetching && (
            <div style={{ padding: '16px', fontSize: '12px', color: 'var(--t3)', textAlign: 'center' }}>
              {t('status.loading')}
            </div>
          )}

          {/* Results */}
          {!searchQ.isFetching && results.length > 0 && (
            <>
              <div style={{
                padding: '6px 14px 3px', fontSize: '9px', fontWeight: 600,
                color: 'var(--t3)', letterSpacing: '0.08em', textTransform: 'uppercase',
              }}>
                {t('search.resultCount', { count: results.length })}
              </div>
              {results.map((r, i) => {
                const isActive = i === activeIndex;
                return (
                  <div
                    key={r.id}
                    data-active={isActive}
                    onClick={() => handleSelect(r)}
                    onMouseEnter={() => setActiveIndex(i)}
                    style={{
                      display: 'flex', alignItems: 'center', gap: 8,
                      padding: '7px 14px',
                      cursor: 'pointer',
                      background: isActive
                        ? 'color-mix(in srgb, var(--acc) 10%, transparent)'
                        : 'transparent',
                      transition: 'background 0.06s',
                    }}
                  >
                    <TypeIcon type={r.type} />
                    <div style={{ flex: 1, overflow: 'hidden' }}>
                      <div style={{
                        fontSize: '12px', fontWeight: 500,
                        color: isActive ? 'var(--acc)' : 'var(--t1)',
                        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                      }}>
                        {r.label}
                      </div>
                      {r.scope && (
                        <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: 1 }}>
                          {r.scope}
                        </div>
                      )}
                    </div>
                    <span style={{
                      fontSize: '10px', color: 'var(--t3)',
                      padding: '1px 6px', background: 'var(--bg2)',
                      borderRadius: '3px', flexShrink: 0,
                      letterSpacing: '0.03em',
                    }}>
                      {r.type.replace('Dali', '')}
                    </span>
                    {r.score != null && (
                      <span style={{
                        fontSize: '9px', color: 'var(--t3)', flexShrink: 0,
                        fontVariant: 'tabular-nums',
                      }}>
                        {(r.score * 100).toFixed(0)}%
                      </span>
                    )}
                  </div>
                );
              })}
            </>
          )}

          {/* No results */}
          {!searchQ.isFetching && debouncedQuery.length >= 2 && results.length === 0 && (
            <div style={{ padding: '24px 14px', fontSize: '12px', color: 'var(--t3)', textAlign: 'center' }}>
              {t('search.noResults')}
            </div>
          )}

          {/* Too short */}
          {debouncedQuery.length < 2 && debouncedQuery.length > 0 && (
            <div style={{ padding: '16px 14px', fontSize: '12px', color: 'var(--t3)', textAlign: 'center' }}>
              {t('searchPalette.minChars')}
            </div>
          )}

          {/* Idle state — no query: show recent queries + recent nodes */}
          {debouncedQuery.length === 0 && !showHidden && (
            <>
              {recentQueries.length === 0 && recentNodes.length === 0 && (
                <div style={{ padding: '24px 14px', textAlign: 'center' }}>
                  <Search size={20} style={{ color: 'var(--t3)', opacity: 0.4, marginBottom: 8 }} />
                  <div style={{ fontSize: '12px', color: 'var(--t3)' }}>
                    {t('search.placeholder')}
                  </div>
                  <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: 4, opacity: 0.6 }}>
                    {t('searchPalette.hint')}
                  </div>
                </div>
              )}

              {/* Recent searches */}
              {recentQueries.length > 0 && (
                <>
                  <div style={{
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    padding: '6px 14px 3px',
                  }}>
                    <span style={{
                      fontSize: '9px', fontWeight: 600, color: 'var(--t3)',
                      letterSpacing: '0.08em', textTransform: 'uppercase',
                    }}>
                      {t('search.recentSearches')}
                    </span>
                    <button
                      onClick={() => { clearSearchHistory(); setRecentQueries([]); }}
                      style={{
                        fontSize: '10px', color: 'var(--t3)',
                        background: 'none', border: 'none',
                        cursor: 'pointer', padding: '2px 6px',
                      }}
                    >
                      {t('search.clearHistory')}
                    </button>
                  </div>
                  {recentQueries.map((q) => (
                    <div
                      key={q}
                      onClick={() => { handleInput(q); setDebounced(q); }}
                      style={{
                        display: 'flex', alignItems: 'center', gap: 8,
                        padding: '6px 14px',
                        cursor: 'pointer',
                        transition: 'background 0.06s',
                      }}
                      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
                      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                    >
                      <History size={11} color="var(--t3)" strokeWidth={1.5} />
                      <span style={{ fontSize: '12px', color: 'var(--t2)' }}>{q}</span>
                    </div>
                  ))}
                </>
              )}

              {/* Recent nodes */}
              {recentNodes.length > 0 && (
                <>
                  <div style={{
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    padding: '6px 14px 3px', marginTop: recentQueries.length > 0 ? 4 : 0,
                  }}>
                    <span style={{
                      fontSize: '9px', fontWeight: 600, color: 'var(--t3)',
                      letterSpacing: '0.08em', textTransform: 'uppercase',
                    }}>
                      {t('search.recentNodes')}
                    </span>
                    <button
                      onClick={() => { clearRecentNodes(); setRecentNodes([]); }}
                      style={{
                        fontSize: '10px', color: 'var(--t3)',
                        background: 'none', border: 'none',
                        cursor: 'pointer', padding: '2px 6px',
                      }}
                    >
                      {t('search.clearHistory')}
                    </button>
                  </div>
                  {recentNodes.map((n) => (
                    <div
                      key={n.id}
                      onClick={() => handleSelect({ id: n.id, label: n.label, type: n.type, scope: n.scope } as SearchResult)}
                      style={{
                        display: 'flex', alignItems: 'center', gap: 8,
                        padding: '6px 14px',
                        cursor: 'pointer',
                        transition: 'background 0.06s',
                      }}
                      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
                      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                    >
                      <Clock size={11} color="var(--t3)" strokeWidth={1.5} />
                      <TypeIcon type={n.type} />
                      <div style={{ flex: 1, overflow: 'hidden' }}>
                        <div style={{
                          fontSize: '12px', color: 'var(--t1)',
                          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                        }}>
                          {n.label}
                        </div>
                        {n.scope && (
                          <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: 1 }}>
                            {n.scope}
                          </div>
                        )}
                      </div>
                      <span style={{
                        fontSize: '10px', color: 'var(--t3)',
                        padding: '1px 6px', background: 'var(--bg2)',
                        borderRadius: '3px', flexShrink: 0,
                      }}>
                        {n.type.replace('Dali', '')}
                      </span>
                    </div>
                  ))}
                </>
              )}
            </>
          )}

          {/* ── Hidden nodes section ───────────────────────────────────────────── */}
          {showHidden && (
            <>
              <div style={{ height: 1, background: 'var(--bd)', margin: '4px 14px' }} />
              <div style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '6px 14px 3px',
              }}>
                <span style={{
                  fontSize: '9px', fontWeight: 600, color: 'var(--t3)',
                  letterSpacing: '0.08em', textTransform: 'uppercase',
                }}>
                  {t('search.sections.hidden')} ({hiddenIds.length})
                </span>
                {hiddenIds.length > 1 && (
                  <button
                    onClick={() => { showAllNodes(); }}
                    style={{
                      fontSize: '10px', color: 'var(--acc)',
                      background: 'none', border: 'none',
                      cursor: 'pointer', padding: '2px 6px',
                    }}
                  >
                    {t('search.hidden.restore')} all
                  </button>
                )}
              </div>
              {hiddenIds.map((nodeId, i) => {
                const idx = results.length + i;
                const isActive = idx === activeIndex;
                return (
                  <div
                    key={nodeId}
                    data-active={isActive}
                    onMouseEnter={() => setActiveIndex(idx)}
                    style={{
                      display: 'flex', alignItems: 'center', gap: 8,
                      padding: '6px 14px',
                      background: isActive
                        ? 'color-mix(in srgb, var(--acc) 10%, transparent)'
                        : 'transparent',
                      transition: 'background 0.06s',
                    }}
                  >
                    <Eye size={12} color="var(--t3)" strokeWidth={1.5} />
                    <span style={{
                      flex: 1, fontSize: '12px', color: 'var(--t3)',
                      overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                    }}>
                      {nodeId}
                    </span>
                    <button
                      onClick={() => restoreNode(nodeId)}
                      style={{
                        fontSize: '10px', color: 'var(--acc)',
                        background: 'none', border: 'none',
                        cursor: 'pointer', padding: '2px 6px',
                        borderRadius: '3px', flexShrink: 0,
                      }}
                    >
                      {t('search.hidden.restore')}
                    </button>
                  </div>
                );
              })}
            </>
          )}
        </div>
      </div>
    </>
  );
});

SearchPalette.displayName = 'SearchPalette';
