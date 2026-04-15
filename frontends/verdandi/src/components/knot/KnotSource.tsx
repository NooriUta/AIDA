// src/components/knot/KnotSource.tsx
// KNOT — "Исходник" tab: full source file viewer with line numbers.
//
// Storage: DaliSnippetScript document in ArcadeDB (one per Hound parse run).
// Large files (30k–50k lines ≈ 2–5 MB) are paginated: PAGE_SIZE lines per
// render pass to avoid DOM bloat. Navigation: ← Prev / Next → buttons + jump.
// Line numbers rendered as left-gutter CSS text (no extra DOM nodes).

import { memo, useState, useMemo, useCallback, useRef, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useKnotScript } from '../../services/hooks';

const PAGE_SIZE   = 2000;   // lines rendered per page (keeps DOM ≤ ~2k nodes)
const WARN_LINES  = 30_000; // show performance warning above this

interface KnotSourceProps {
  sessionId: string;
  active:    boolean;        // tab is currently visible — enables lazy fetch
}

export const KnotSource = memo(({ sessionId, active }: KnotSourceProps) => {
  const { t } = useTranslation();
  const [page, setPage]           = useState(0);
  const [jumpValue, setJumpValue] = useState('');
  const containerRef              = useRef<HTMLDivElement>(null);

  const { data, isFetching, isError } = useKnotScript(sessionId, active);

  // Split once into lines; memo so we don't re-split on page change
  const lines = useMemo(() => {
    if (!data?.script) return [];
    return data.script.split('\n');
  }, [data?.script]);

  const totalLines = lines.length || data?.lineCount || 0;
  const pageCount  = Math.max(1, Math.ceil(totalLines / PAGE_SIZE));

  // Reset to page 0 when session changes
  useEffect(() => { setPage(0); setJumpValue(''); }, [sessionId]);

  // Scroll to top on page change
  useEffect(() => { containerRef.current?.scrollTo(0, 0); }, [page]);

  const visibleLines = useMemo(() => {
    const start = page * PAGE_SIZE;
    return lines.slice(start, start + PAGE_SIZE);
  }, [lines, page]);

  const lineStart = page * PAGE_SIZE + 1;  // 1-based first line on this page

  const handleJump = useCallback(() => {
    const n = parseInt(jumpValue, 10);
    if (!isNaN(n) && n >= 1 && n <= totalLines) {
      const targetPage = Math.floor((n - 1) / PAGE_SIZE);
      setPage(targetPage);
      // After render, scroll within page to specific line
      setTimeout(() => {
        const lineEl = containerRef.current?.querySelector(
          `[data-line="${n}"]`,
        );
        lineEl?.scrollIntoView({ block: 'center' });
      }, 50);
    }
    setJumpValue('');
  }, [jumpValue, totalLines]);

  const copyAll = useCallback(async () => {
    if (data?.script) {
      await navigator.clipboard.writeText(data.script);
    }
  }, [data?.script]);

  // ── Loading / error states ────────────────────────────────────────────────
  if (!active) return null;

  if (isFetching && !data) {
    return (
      <div style={centerStyle}>
        <span style={{ color: 'var(--t3)', fontSize: 13 }}>{t('knot.source.loading')}</span>
      </div>
    );
  }
  if (isError || (!isFetching && !data)) {
    return (
      <div style={centerStyle}>
        <span style={{ color: 'var(--t3)', fontSize: 13 }}>{t('knot.source.empty')}</span>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>

      {/* ── Toolbar ──────────────────────────────────────────────────────── */}
      <div style={{
        display:        'flex',
        alignItems:     'center',
        gap:            12,
        padding:        '6px 16px',
        borderBottom:   '0.5px solid var(--bd)',
        flexShrink:     0,
        background:     'var(--bg1)',
        flexWrap:       'wrap',
      }}>
        {/* File path */}
        <span style={{
          fontSize:     11,
          color:        'var(--t2)',
          fontFamily:   'var(--mono)',
          flex:         1,
          overflow:     'hidden',
          textOverflow: 'ellipsis',
          whiteSpace:   'nowrap',
          minWidth:     0,
        }} title={data?.filePath}>
          {data?.filePath ?? ''}
        </span>

        {/* Stats */}
        <span style={{ fontSize: 10, color: 'var(--t3)', flexShrink: 0, whiteSpace: 'nowrap' }}>
          {totalLines.toLocaleString()} {t('knot.source.lines')}
          {data?.charCount ? ` · ${(data.charCount / 1024).toFixed(1)} KB` : ''}
        </span>

        {/* Performance warning for very large files */}
        {totalLines >= WARN_LINES && (
          <span style={{
            fontSize:   10,
            color:      'var(--wrn)',
            flexShrink: 0,
            whiteSpace: 'nowrap',
          }}>
            ⚠ {t('knot.source.largeFile')}
          </span>
        )}

        {/* Jump to line */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 4, flexShrink: 0 }}>
          <input
            type="number"
            min={1}
            max={totalLines}
            value={jumpValue}
            onChange={(e) => setJumpValue(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') handleJump(); }}
            placeholder={t('knot.source.jumpPlaceholder')}
            style={{
              width:       64,
              background:  'var(--bg2)',
              border:      '1px solid var(--bd)',
              borderRadius: 4,
              padding:     '2px 6px',
              fontSize:    11,
              color:       'var(--t1)',
              outline:     'none',
              fontFamily:  'var(--mono)',
            }}
          />
          <button onClick={handleJump} style={smallBtnStyle}>
            {t('knot.source.go')}
          </button>
        </div>

        {/* Copy */}
        <button onClick={copyAll} style={smallBtnStyle}>
          {t('knot.source.copy')}
        </button>
      </div>

      {/* ── Line viewer ──────────────────────────────────────────────────── */}
      <div
        ref={containerRef}
        style={{
          flex:       1,
          overflowY:  'auto',
          overflowX:  'auto',
          background: 'var(--bg0)',
        }}
      >
        <table style={{
          borderCollapse: 'collapse',
          width:          '100%',
          tableLayout:    'fixed',
          fontFamily:     'var(--mono)',
          fontSize:       12,
          lineHeight:     '1.55',
        }}>
          <colgroup>
            <col style={{ width: lineNumWidth(totalLines) }} />
            <col />
          </colgroup>
          <tbody>
            {visibleLines.map((line, idx) => {
              const lineNo = lineStart + idx;
              return (
                <tr key={lineNo} data-line={lineNo} style={{ verticalAlign: 'top' }}>
                  {/* Line number gutter */}
                  <td style={{
                    userSelect:  'none',
                    textAlign:   'right',
                    paddingRight: 14,
                    paddingLeft:  10,
                    color:       'var(--t3)',
                    borderRight: '1px solid var(--bd)',
                    whiteSpace:  'nowrap',
                    opacity:     0.6,
                  }}>
                    {lineNo}
                  </td>
                  {/* Code */}
                  <td style={{
                    paddingLeft:  14,
                    paddingRight: 10,
                    whiteSpace:   'pre',
                    color:        'var(--t1)',
                    wordBreak:    'keep-all',
                  }}>
                    {line}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* ── Pagination ───────────────────────────────────────────────────── */}
      {pageCount > 1 && (
        <div style={{
          display:      'flex',
          alignItems:   'center',
          justifyContent: 'center',
          gap:          8,
          padding:      '6px 16px',
          borderTop:    '0.5px solid var(--bd)',
          background:   'var(--bg1)',
          flexShrink:   0,
          fontSize:     12,
        }}>
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            style={paginBtnStyle(page === 0)}
          >
            ← {t('knot.source.prev')}
          </button>

          <span style={{ color: 'var(--t2)', fontFamily: 'var(--mono)', fontSize: 11 }}>
            {t('knot.source.pageOf', {
              page: page + 1,
              total: pageCount,
              from: lineStart.toLocaleString(),
              to: Math.min(lineStart + PAGE_SIZE - 1, totalLines).toLocaleString(),
            })}
          </span>

          <button
            onClick={() => setPage((p) => Math.min(pageCount - 1, p + 1))}
            disabled={page === pageCount - 1}
            style={paginBtnStyle(page === pageCount - 1)}
          >
            {t('knot.source.next')} →
          </button>
        </div>
      )}
    </div>
  );
});

KnotSource.displayName = 'KnotSource';

// ── Helpers ───────────────────────────────────────────────────────────────────

const centerStyle: React.CSSProperties = {
  display: 'flex', alignItems: 'center', justifyContent: 'center',
  height: '100%', fontSize: 13,
};

const smallBtnStyle: React.CSSProperties = {
  padding:      '2px 8px',
  fontSize:     11,
  fontFamily:   'inherit',
  background:   'var(--bg2)',
  border:       '1px solid var(--bd)',
  borderRadius: 4,
  color:        'var(--t2)',
  cursor:       'pointer',
  flexShrink:   0,
};

function paginBtnStyle(disabled: boolean): React.CSSProperties {
  return {
    ...smallBtnStyle,
    opacity: disabled ? 0.35 : 1,
    cursor:  disabled ? 'default' : 'pointer',
    color:   disabled ? 'var(--t3)' : 'var(--acc)',
  };
}

/** Return a CSS width string for the line-number gutter based on digit count. */
function lineNumWidth(totalLines: number): string {
  const digits = String(totalLines).length;
  return `${digits * 8 + 24}px`;
}
