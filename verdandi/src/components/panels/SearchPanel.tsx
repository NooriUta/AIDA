import { memo, useState, useRef, useCallback, useEffect } from 'react';
import { Search, X, Table2, Code2, Columns3, Eye, Database, AppWindow, Variable, Braces, Clock, History } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';
import { useSearch } from '../../services/hooks';
import type { SearchResult } from '../../services/lineage';
import { ToolbarScrollTabs } from '../ui/ToolbarPrimitives';
import {
  getSearchHistory, pushSearchQuery, clearSearchHistory,
  getRecentNodes, pushRecentNode, clearRecentNodes,
  type RecentNode,
} from '../../hooks/useSearchHistory';

// ─── Type icon ─────────────────────────────────────────────────────────────────

function TypeIcon({ type }: { type: string }) {
  const size = 11;
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

// ─── Single result row ─────────────────────────────────────────────────────────

function ResultRow({
  result,
  onSelect,
}: {
  result: SearchResult;
  onSelect: (r: SearchResult) => void;
}) {
  const [hovered, setHovered] = useState(false);
  return (
    <div
      style={{
        display:      'flex',
        alignItems:   'center',
        gap:          '6px',
        padding:      '5px 8px',
        cursor:       'pointer',
        borderRadius: '4px',
        background:   hovered ? 'var(--bg3)' : 'transparent',
        transition:   'background 0.1s',
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onClick={() => onSelect(result)}
    >
      <TypeIcon type={result.type} />
      <div style={{ flex: 1, overflow: 'hidden' }}>
        <div style={{
          fontSize:     '12px',
          color:        'var(--t1)',
          overflow:     'hidden',
          textOverflow: 'ellipsis',
          whiteSpace:   'nowrap',
          fontWeight:   500,
        }}>
          {result.label}
        </div>
        {result.scope && (
          <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: '1px' }}>
            {result.scope}
          </div>
        )}
      </div>
      {result.score != null && (
        <span style={{
          fontSize:     '9px',
          color:        'var(--t3)',
          flexShrink:   0,
          fontVariant:  'tabular-nums',
        }}>
          {(result.score * 100).toFixed(0)}%
        </span>
      )}
    </div>
  );
}

// ─── Hidden node row ───────────────────────────────────────────────────────────

function HiddenNodeRow({
  nodeId,
  label,
  onRestore,
}: {
  nodeId: string;
  label: string;
  onRestore: (nodeId: string) => void;
}) {
  const { t } = useTranslation();
  const [hovered, setHovered] = useState(false);
  return (
    <div
      style={{
        display:      'flex',
        alignItems:   'center',
        gap:          '6px',
        padding:      '5px 8px',
        borderRadius: '4px',
        background:   hovered ? 'var(--bg3)' : 'transparent',
        transition:   'background 0.1s',
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      <Eye size={11} color="var(--t3)" strokeWidth={1.5} />
      <span style={{ flex: 1, fontSize: '12px', color: 'var(--t3)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
        {label || nodeId}
      </span>
      <button
        onClick={() => onRestore(nodeId)}
        style={{
          fontSize:     '10px',
          color:        'var(--acc)',
          background:   'none',
          border:       'none',
          cursor:       'pointer',
          padding:      '1px 4px',
          borderRadius: '3px',
          flexShrink:   0,
        }}
      >
        {t('search.hidden.restore')}
      </button>
    </div>
  );
}

// ─── Section label ─────────────────────────────────────────────────────────────

function SectionLabel({ label }: { label: string }) {
  return (
    <div style={{
      fontSize:    '9px',
      fontWeight:  600,
      color:       'var(--t3)',
      textTransform: 'uppercase',
      letterSpacing: '0.08em',
      padding:     '6px 8px 2px',
    }}>
      {label}
    </div>
  );
}

// ─── SearchPanel ───────────────────────────────────────────────────────────────

export const SearchPanel = memo(() => {
  const { t } = useTranslation();
  const { jumpTo, selectNode, requestFocusNode, hiddenNodeIds, restoreNode, showAllNodes } = useLoomStore();

  const [query, setQuery]              = useState('');
  const [debouncedQuery, setDebounced] = useState('');
  const [typeFilters, setTypeFilters]  = useState<Set<string>>(new Set());
  const [recentQueries, setRecentQueries] = useState<string[]>(() => getSearchHistory());
  const [recentNodesList, setRecentNodesList] = useState<RecentNode[]>(() => getRecentNodes());
  const timerRef  = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Refresh history when query is cleared
  useEffect(() => {
    if (query.length === 0) {
      setRecentQueries(getSearchHistory());
      setRecentNodesList(getRecentNodes());
    }
  }, [query]);

  // Debounced input: fire search 300ms after last keystroke
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

  const searchQ = useSearch(debouncedQuery.length >= 2 ? debouncedQuery : '');


  // Toggle a type filter; 'all' clears the entire set
  const toggleFilter = useCallback((key: string) => {
    if (key === 'all') {
      setTypeFilters(new Set());
      return;
    }
    setTypeFilters((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  }, []);

  // Filter results — empty set means show everything
  const results = (searchQ.data ?? []).filter((r) => {
    if (typeFilters.size === 0) return true;
    if (typeFilters.has('tables')       && r.type === 'DaliTable') return true;
    if (typeFilters.has('columns')      && (r.type === 'DaliColumn' || r.type === 'DaliOutputColumn')) return true;
    if (typeFilters.has('routines')     && (r.type === 'DaliRoutine' || r.type === 'DaliPackage' || r.type === 'DaliSession' || r.type === 'DaliParameter' || r.type === 'DaliVariable')) return true;
    if (typeFilters.has('statements')   && r.type === 'DaliStatement') return true;
    if (typeFilters.has('databases')    && (r.type === 'DaliDatabase' || r.type === 'DaliSchema')) return true;
    if (typeFilters.has('applications') && r.type === 'DaliApplication') return true;
    return false;
  });

  // Handle result click — deterministic navigation regardless of current level
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
  }, [jumpTo, selectNode, requestFocusNode]);

  const tabs: { key: string; label: string }[] = [
    { key: 'all',          label: t('search.filters.all') },
    { key: 'tables',       label: t('search.filters.tables') },
    { key: 'columns',      label: t('search.filters.columns') },
    { key: 'routines',     label: t('search.filters.routines') },
    { key: 'statements',   label: t('search.filters.statements') },
    { key: 'databases',    label: t('search.filters.databases') },
    { key: 'applications', label: t('search.filters.applications') },
  ];

  const hiddenIds = [...hiddenNodeIds];
  const showHiddenSection = hiddenIds.length > 0 && debouncedQuery.length < 2;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>

      {/* ── Search input ──────────────────────────────────────────────────────── */}
      <div style={{ padding: '4px', flexShrink: 0 }}>
        <div style={{
          display:      'flex',
          alignItems:   'center',
          gap:          '6px',
          padding:      '5px 8px',
          background:   'var(--bg1)',
          border:       '1px solid var(--bd)',
          borderRadius: '6px',
        }}>
          <Search size={12} color="var(--t3)" style={{ flexShrink: 0 }} />
          <input
            type="text"
            data-search-input
            placeholder={t('search.placeholder')}
            value={query}
            onChange={(e) => handleInput(e.target.value)}
            style={{
              flex:       1,
              background: 'none',
              border:     'none',
              outline:    'none',
              fontSize:   '12px',
              color:      'var(--t1)',
              minWidth:   0,
            }}
          />
          {query.length > 0 && (
            <button
              onClick={clearQuery}
              style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '1px', display: 'flex', alignItems: 'center' }}
            >
              <X size={11} color="var(--t3)" />
            </button>
          )}
        </div>
      </div>

      {/* ── Type filter tabs ──────────────────────────────────────────────────── */}
      <ToolbarScrollTabs
        tabs={tabs}
        activeKeys={typeFilters.size === 0 ? 'all' : typeFilters as Set<string>}
        onToggle={toggleFilter}
      />

      {/* ── Results / empty states ────────────────────────────────────────────── */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '2px 0' }}>

        {/* Loading */}
        {searchQ.isFetching && (
          <div style={{ padding: '8px', fontSize: '11px', color: 'var(--t3)', textAlign: 'center' }}>
            {t('status.loading')}
          </div>
        )}

        {/* Results */}
        {!searchQ.isFetching && results.length > 0 && (
          <>
            <SectionLabel label={t('search.resultCount', { count: results.length })} />
            {results.map((r) => (
              <ResultRow key={r.id} result={r} onSelect={handleSelect} />
            ))}
          </>
        )}

        {/* No results */}
        {!searchQ.isFetching && debouncedQuery.length >= 2 && results.length === 0 && (
          <div style={{ padding: '12px 8px', fontSize: '11px', color: 'var(--t3)', textAlign: 'center' }}>
            {t('search.noResults')}
          </div>
        )}

        {/* Prompt when query is too short */}
        {debouncedQuery.length < 2 && debouncedQuery.length > 0 && (
          <div style={{ padding: '8px', fontSize: '11px', color: 'var(--t3)', textAlign: 'center' }}>
            {t('searchPalette.minChars')}
          </div>
        )}

        {/* Idle state — recent searches + recent nodes */}
        {debouncedQuery.length === 0 && (
          <>
            {/* Recent searches */}
            {recentQueries.length > 0 && (
              <>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingRight: '8px' }}>
                  <SectionLabel label={t('search.recentSearches')} />
                  <button
                    onClick={() => { clearSearchHistory(); setRecentQueries([]); }}
                    style={{ fontSize: '9px', color: 'var(--t3)', background: 'none', border: 'none', cursor: 'pointer', padding: '2px 4px' }}
                  >
                    {t('search.clearHistory')}
                  </button>
                </div>
                {recentQueries.map((q) => (
                  <div
                    key={q}
                    onClick={() => { handleInput(q); setDebounced(q); }}
                    style={{
                      display: 'flex', alignItems: 'center', gap: 6,
                      padding: '4px 8px', cursor: 'pointer', borderRadius: 4,
                      transition: 'background 0.1s',
                    }}
                    onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
                    onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                  >
                    <History size={10} color="var(--t3)" strokeWidth={1.5} />
                    <span style={{ fontSize: '11px', color: 'var(--t2)' }}>{q}</span>
                  </div>
                ))}
              </>
            )}

            {/* Recent nodes */}
            {recentNodesList.length > 0 && (
              <>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingRight: '8px', marginTop: recentQueries.length > 0 ? 2 : 0 }}>
                  <SectionLabel label={t('search.recentNodes')} />
                  <button
                    onClick={() => { clearRecentNodes(); setRecentNodesList([]); }}
                    style={{ fontSize: '9px', color: 'var(--t3)', background: 'none', border: 'none', cursor: 'pointer', padding: '2px 4px' }}
                  >
                    {t('search.clearHistory')}
                  </button>
                </div>
                {recentNodesList.map((n) => (
                  <div
                    key={n.id}
                    onClick={() => handleSelect({ id: n.id, label: n.label, type: n.type, scope: n.scope } as SearchResult)}
                    style={{
                      display: 'flex', alignItems: 'center', gap: 6,
                      padding: '4px 8px', cursor: 'pointer', borderRadius: 4,
                      transition: 'background 0.1s',
                    }}
                    onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
                    onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                  >
                    <Clock size={10} color="var(--t3)" strokeWidth={1.5} />
                    <TypeIcon type={n.type} />
                    <div style={{ flex: 1, overflow: 'hidden' }}>
                      <div style={{
                        fontSize: '11px', color: 'var(--t1)',
                        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                      }}>
                        {n.label}
                      </div>
                    </div>
                    <span style={{ fontSize: '9px', color: 'var(--t3)', flexShrink: 0 }}>
                      {n.type.replace('Dali', '')}
                    </span>
                  </div>
                ))}
              </>
            )}
          </>
        )}

        {/* Hidden nodes section */}
        {showHiddenSection && (
          <>
            <div style={{ height: '1px', background: 'var(--bd)', margin: '4px 8px' }} />
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingRight: '8px' }}>
              <SectionLabel label={t('search.sections.hidden')} />
              {hiddenIds.length > 1 && (
                <button
                  onClick={showAllNodes}
                  style={{ fontSize: '10px', color: 'var(--acc)', background: 'none', border: 'none', cursor: 'pointer', padding: '2px 4px' }}
                >
                  {t('search.hidden.restore')} all
                </button>
              )}
            </div>
            {hiddenIds.map((nodeId) => (
              <HiddenNodeRow
                key={nodeId}
                nodeId={nodeId}
                label={nodeId}
                onRestore={restoreNode}
              />
            ))}
          </>
        )}
      </div>
    </div>
  );
});

SearchPanel.displayName = 'SearchPanel';
