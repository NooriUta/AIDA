import { memo, useState, useRef, useCallback, useEffect } from 'react';
import { Search, X, Table2, Code2, Columns3, Eye } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';
import { useSearch } from '../../services/hooks';
import type { SearchResult } from '../../services/lineage';

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
    case 'DaliStatement':
    case 'DaliSession':
      return <Code2 size={size} color="var(--suc)" strokeWidth={1.5} />;
    default:
      return <span style={{ width: size, height: size, display: 'inline-block' }} />;
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
  const { jumpTo, selectNode, hiddenNodeIds, restoreNode, showAllNodes } = useLoomStore();

  const [query, setQuery]              = useState('');
  const [debouncedQuery, setDebounced] = useState('');
  const [typeFilters, setTypeFilters]  = useState<Set<string>>(new Set());
  const [canScrollRight, setCanScrollRight] = useState(false);
  const timerRef  = useRef<ReturnType<typeof setTimeout> | null>(null);
  const tabsRef   = useRef<HTMLDivElement>(null);

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

  // Track tab-bar overflow to show/hide ">>" scroll button
  useEffect(() => {
    const el = tabsRef.current;
    if (!el) return;
    const check = () => setCanScrollRight(el.scrollWidth > el.clientWidth + 2);
    check();
    const ro = new ResizeObserver(check);
    ro.observe(el);
    el.addEventListener('scroll', check);
    return () => { ro.disconnect(); el.removeEventListener('scroll', check); };
  }, []);

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
    if (typeFilters.has('routines')     && (r.type === 'DaliRoutine' || r.type === 'DaliPackage' || r.type === 'DaliSession')) return true;
    if (typeFilters.has('columns')      && (r.type === 'DaliColumn' || r.type === 'DaliOutputColumn')) return true;
    if (typeFilters.has('databases')    && (r.type === 'DaliDatabase' || r.type === 'DaliSchema')) return true;
    if (typeFilters.has('applications') && r.type === 'DaliApplication') return true;
    return false;
  });

  // Handle result click — deterministic navigation regardless of current level
  const handleSelect = useCallback((result: SearchResult) => {
    const type = result.type as string;
    if (type === 'DaliTable') {
      // L3: column-level lineage for this table
      jumpTo('L3', result.id, result.label, 'DaliTable');
    } else if (type === 'DaliSchema') {
      // L2: explore this schema
      jumpTo('L2', `schema-${result.label}`, result.label, 'DaliSchema');
    } else if (type === 'DaliPackage') {
      // L2: explore this package
      jumpTo('L2', result.id, result.label, 'DaliPackage');
    } else if (type === 'DaliDatabase' || type === 'DaliApplication') {
      // L1: go to overview, select/highlight that node
      jumpTo('L1', null, result.label);
      selectNode(result.id);
    } else {
      // Routines, Statements, Columns — highlight on current canvas
      selectNode(result.id);
    }
  }, [jumpTo, selectNode]);

  const tabs: { key: string; label: string }[] = [
    { key: 'all',          label: t('search.filters.all') },
    { key: 'tables',       label: t('search.filters.tables') },
    { key: 'columns',      label: t('search.filters.columns') },
    { key: 'routines',     label: t('search.filters.routines') },
    { key: 'databases',    label: t('search.filters.databases') },
    { key: 'applications', label: t('search.filters.applications') },
  ];

  const isAllActive = typeFilters.size === 0;

  const scrollTabsRight = () => {
    tabsRef.current?.scrollBy({ left: 80, behavior: 'smooth' });
  };

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
      <div style={{ display: 'flex', alignItems: 'center', flexShrink: 0, position: 'relative' }}>
        <div
          ref={tabsRef}
          style={{
            display:    'flex',
            gap:        '2px',
            padding:    '2px 4px',
            flex:       1,
            overflowX:  'auto',
            scrollbarWidth: 'none',
          }}
        >
          {tabs.map((tab) => {
            const active = tab.key === 'all' ? isAllActive : typeFilters.has(tab.key);
            return (
              <button
                key={tab.key}
                onClick={() => toggleFilter(tab.key)}
                style={{
                  padding:      '2px 7px',
                  borderRadius: '4px',
                  border:       'none',
                  cursor:       'pointer',
                  fontSize:     '10px',
                  fontWeight:   active ? 600 : 400,
                  background:   active ? 'var(--acc)' : 'transparent',
                  color:        active ? 'var(--bg1)' : 'var(--t3)',
                  whiteSpace:   'nowrap',
                  transition:   'background 0.1s, color 0.1s',
                  flexShrink:   0,
                }}
              >
                {tab.label}
              </button>
            );
          })}
        </div>

        {/* ">>" scroll arrow — appears when tabs overflow */}
        {canScrollRight && (
          <button
            onClick={scrollTabsRight}
            title="Ещё фильтры"
            style={{
              flexShrink:   0,
              padding:      '2px 5px',
              background:   'linear-gradient(to right, transparent, var(--bg2) 40%)',
              border:       'none',
              cursor:       'pointer',
              fontSize:     '10px',
              color:        'var(--t3)',
              position:     'absolute',
              right:        0,
              top:          0,
              bottom:       0,
              display:      'flex',
              alignItems:   'center',
            }}
          >
            »
          </button>
        )}
      </div>

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
            Type at least 2 characters…
          </div>
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
