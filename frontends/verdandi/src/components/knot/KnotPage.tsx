import { memo, useEffect, useState, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Header } from '../layout/Header';
import { KnotSummary } from './KnotSummary';
import { KnotStructure } from './KnotStructure';
import { KnotRoutines } from './KnotRoutines';
import { KnotStatements } from './KnotStatements';
import { KnotAtoms } from './KnotAtoms';
import { KnotSource } from './KnotSource';
import { KnotRecord } from './KnotRecord';
import { useKnotSessions, useKnotReport } from '../../services/hooks';
import type { KnotSession } from '../../services/lineage';

type TabId = 'summary' | 'structure' | 'routines' | 'statements' | 'atoms' | 'source';

export const KnotPage = memo(() => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const urlPkg     = searchParams.get('pkg')     ?? '';
  const urlStmt    = searchParams.get('stmt')    ?? '';
  const urlRecord  = searchParams.get('record')  ?? '';   // e.g. "T_CUSTOMER_REC"
  const urlSchema  = searchParams.get('schema')  ?? '';
  const urlRoutine = searchParams.get('routine') ?? '';   // routineGeoid

  const [selectedId,    setSelectedId]    = useState<string | null>(null);
  const [activeTab,     setActiveTab]     = useState<TabId>('summary');
  const [sessionSearch, setSessionSearch] = useState(urlPkg);
  const [dialectFilter, setDialectFilter] = useState<string | null>(null);

  const { data: sessions, isLoading: sessionsLoading, isError: sessionsError } = useKnotSessions();
  const { data: report, isLoading: reportLoading, isError: reportError } = useKnotReport(selectedId);

  // Auto-select session: prefer URL pkg match, fall back to first
  useEffect(() => {
    if (!sessions || sessions.length === 0) return;
    if (selectedId) return;                               // already selected

    if (urlPkg) {
      const q = urlPkg.toLowerCase();
      const match = sessions.find((s) =>
        s.sessionName.toLowerCase().includes(q) ||
        q.includes(s.sessionName.toLowerCase()),
      );
      if (match) {
        setSelectedId(match.sessionId);
        // If a stmt was passed → switch to Statements tab after session loads
        if (urlStmt) setActiveTab('statements');
        // Clear URL params so they don't interfere on manual navigation
        setSearchParams({}, { replace: true });
        return;
      }
    }

    setSelectedId(sessions[0].sessionId);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessions]);

  // Unique dialects for filter pills
  const dialects = useMemo(() =>
    [...new Set((sessions ?? []).map((s) => s.dialect.toUpperCase()))].sort(),
  [sessions]);

  // Selected session object (for nav button — available before report loads)
  const selectedSession = useMemo(
    () => (sessions ?? []).find((s) => s.sessionId === selectedId) ?? null,
    [sessions, selectedId],
  );

  // Filtered session list — match on sessionName OR filePath fragment
  const filteredSessions = useMemo(() => {
    const all = sessions ?? [];
    const q = sessionSearch.trim().toLowerCase();
    return all.filter((s) => {
      const matchName = !q ||
        s.sessionName.toLowerCase().includes(q) ||
        (s.filePath && s.filePath.toLowerCase().includes(q));
      const matchDialect = !dialectFilter || s.dialect.toUpperCase() === dialectFilter;
      return matchName && matchDialect;
    });
  }, [sessions, sessionSearch, dialectFilter]);

  const clearSearch = useCallback(() => setSessionSearch(''), []);

  const tabCounts = useMemo(() => {
    if (!report) return {};
    const s = report.session;
    const stmtTotal = s.stmtSelect + s.stmtInsert + s.stmtUpdate +
      s.stmtDelete + s.stmtMerge + s.stmtCursor + s.stmtOther;
    return {
      structure: s.tableCount || report.tables.length,
      routines: s.routineCount,
      statements: stmtTotal || report.statements.length,
      atoms: s.atomTotal,
    };
  }, [report]);

  const TABS: { id: TabId; key: string; count?: number }[] = [
    { id: 'summary',    key: 'knot.tabs.summary' },
    { id: 'structure',  key: 'knot.tabs.structure',  count: tabCounts.structure },
    { id: 'routines',   key: 'knot.tabs.routines',   count: tabCounts.routines },
    { id: 'statements', key: 'knot.tabs.statements', count: tabCounts.statements },
    { id: 'atoms',      key: 'knot.tabs.atoms',      count: tabCounts.atoms },
    { id: 'source',     key: 'knot.tabs.source' },
  ];

  // ── Standalone record view ───────────────────────────────────────────────
  if (urlRecord) {
    return (
      <div style={{
        display: 'grid', gridTemplateRows: '42px 1fr',
        height: '100%', overflow: 'hidden', background: 'var(--bg0)',
      }}>
        <Header />
        <KnotRecord
          record={urlRecord}
          pkg={urlPkg || undefined}
          schema={urlSchema || undefined}
          routineGeoid={urlRoutine || undefined}
        />
      </div>
    );
  }

  return (
    <div style={{
      display: 'grid',
      gridTemplateRows: '42px 1fr',
      height: '100%',
      overflow: 'hidden',
      background: 'var(--bg0)',
    }}>
      <Header />

      <div style={{ display: 'flex', overflow: 'hidden' }}>

        {/* ── Sessions sidebar ─────────────────────────────────────────────── */}
        <aside style={{
          width: 220,
          flexShrink: 0,
          background: 'var(--bg0)',
          borderRight: '1px solid var(--bd)',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}>
          {/* Header row */}
          <div style={{
            padding: '10px 14px 8px',
            borderBottom: '0.5px solid var(--bd)',
            flexShrink: 0,
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: 8,
            }}>
              <span style={{
                fontSize: 10,
                fontWeight: 500,
                letterSpacing: '0.1em',
                color: 'var(--t3)',
                textTransform: 'uppercase',
              }}>
                {t('knot.sessions')}
              </span>
              {sessions && sessions.length > 0 && (
                <span style={{ fontSize: 10, color: 'var(--t3)' }}>
                  {filteredSessions.length}
                  {filteredSessions.length !== sessions.length && ` / ${sessions.length}`}
                </span>
              )}
            </div>

            {/* Search input */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: 6,
              padding: '4px 8px',
              background: 'var(--bg1)',
              border: '1px solid var(--bd)',
              borderRadius: 'var(--seer-radius-md)',
              marginBottom: dialects.length > 1 ? 6 : 0,
            }}>
              <span style={{ fontSize: 10, color: 'var(--t3)', flexShrink: 0 }}>⌕</span>
              <input
                type="text"
                value={sessionSearch}
                onChange={(e) => setSessionSearch(e.target.value)}
                placeholder={t('knot.searchSessions')}
                style={{
                  flex: 1,
                  background: 'none',
                  border: 'none',
                  outline: 'none',
                  fontSize: 11,
                  color: 'var(--t1)',
                  minWidth: 0,
                }}
              />
              {sessionSearch && (
                <button
                  onClick={clearSearch}
                  style={{
                    background: 'none', border: 'none', cursor: 'pointer',
                    padding: 0, color: 'var(--t3)', fontSize: 11, lineHeight: 1,
                    flexShrink: 0,
                  }}
                >
                  ×
                </button>
              )}
            </div>

            {/* Dialect filter pills — only when 2+ dialects present */}
            {dialects.length > 1 && (
              <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                {dialects.map((d) => {
                  const active = dialectFilter === d;
                  return (
                    <button
                      key={d}
                      onClick={() => setDialectFilter(active ? null : d)}
                      style={{
                        padding: '2px 6px',
                        borderRadius: 3,
                        border: `1px solid ${active ? 'var(--acc)' : 'var(--bd)'}`,
                        background: active ? 'color-mix(in srgb, var(--acc) 15%, transparent)' : 'transparent',
                        color: active ? 'var(--acc)' : 'var(--t3)',
                        fontSize: 9,
                        fontFamily: 'var(--mono)',
                        cursor: 'pointer',
                        transition: 'border-color 0.1s, color 0.1s',
                      }}
                    >
                      {d}
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          {/* Session list */}
          <div style={{ flex: 1, overflowY: 'auto', padding: '8px' }}>
            {sessionsLoading && (
              <div style={{ padding: '16px 10px', fontSize: 12, color: 'var(--t3)' }}>
                {t('knot.loading')}
              </div>
            )}
            {sessionsError && (
              <div style={{ padding: '16px 10px', fontSize: 12, color: 'var(--danger)' }}>
                {t('knot.error')}
              </div>
            )}
            {!sessionsLoading && sessions?.length === 0 && (
              <div style={{ padding: '16px 10px', fontSize: 12, color: 'var(--t3)' }}>
                {t('knot.emptyList')}
              </div>
            )}
            {!sessionsLoading && sessions && sessions.length > 0 && filteredSessions.length === 0 && (
              <div style={{ padding: '16px 10px', fontSize: 12, color: 'var(--t3)' }}>
                {t('knot.noSessionsMatch')}
              </div>
            )}
            {filteredSessions.map((sess) => (
              <SessionCard
                key={sess.sessionId}
                session={sess}
                selected={selectedId === sess.sessionId}
                onClick={() => setSelectedId(sess.sessionId)}
              />
            ))}
          </div>
        </aside>

        {/* ── Main area ───────────────────────────────────────────────────── */}
        <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>

          {/* Tab bar */}
          <div style={{
            background: 'var(--bg0)',
            borderBottom: '0.5px solid var(--bd)',
            display: 'flex',
            alignItems: 'center',
            flexShrink: 0,
            height: 36,
            minWidth: 0,
          }}>
            {/* Scrollable tabs */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              flex: 1,
              gap: 2,
              padding: '0 16px',
              overflowX: 'auto',
              minWidth: 0,
            }}>
              {TABS.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  style={{
                    padding: '0 14px',
                    height: 36,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 6,
                    fontSize: 12,
                    fontWeight: 500,
                    border: 'none',
                    background: 'transparent',
                    color: activeTab === tab.id ? 'var(--t1)' : 'var(--t3)',
                    cursor: 'pointer',
                    borderBottom: activeTab === tab.id
                      ? '2px solid var(--acc)'
                      : '2px solid transparent',
                    transition: 'color 0.12s',
                    fontFamily: 'inherit',
                    whiteSpace: 'nowrap',
                    flexShrink: 0,
                  }}
                >
                  {t(tab.key)}
                  {tab.count != null && tab.count > 0 && (
                    <span style={{
                      padding: '1px 6px',
                      borderRadius: 10,
                      fontSize: 10,
                      background: activeTab === tab.id
                        ? 'color-mix(in srgb, var(--acc) 12%, transparent)'
                        : 'var(--bg3)',
                      color: activeTab === tab.id ? 'var(--acc)' : 'var(--t3)',
                    }}>
                      {tab.count.toLocaleString()}
                    </span>
                  )}
                </button>
              ))}
            </div>

            {/* LOOM navigation button — always visible at right */}
            {selectedSession && (
              <div style={{ padding: '0 8px', flexShrink: 0 }}>
                <button
                  onClick={() => navigate(`/?pkg=${encodeURIComponent(selectedSession.sessionName)}`)}
                  title={selectedSession.sessionName}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 5,
                    padding: '0 10px',
                    height: 26,
                    fontSize: 11,
                    fontWeight: 500,
                    fontFamily: 'inherit',
                    background: 'var(--bg2)',
                    border: '1px solid var(--bd)',
                    borderRadius: 'var(--seer-radius-sm)',
                    color: 'var(--acc)',
                    cursor: 'pointer',
                    whiteSpace: 'nowrap',
                    transition: 'border-color 0.1s',
                  }}
                  onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--acc)'; }}
                  onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)'; }}
                >
                  ◈ {t('knot.openInLoom')}
                </button>
              </div>
            )}
          </div>

          {/* Tab content */}
          <div style={{ flex: 1, overflow: 'hidden', position: 'relative' }}>
            {!selectedId && (
              <EmptyState message={t('knot.noSession')} />
            )}
            {selectedId && reportLoading && (
              <EmptyState message={t('status.loading')} />
            )}
            {selectedId && reportError && (
              <EmptyState message={t('knot.error')} error />
            )}
            {selectedId && report && !reportLoading && (
              <>
                {activeTab === 'summary'    && <KnotSummary    session={report.session} tables={report.tables} statements={report.statements} />}
                {activeTab === 'structure'  && <KnotStructure  sessionId={selectedId ?? ''} tables={report.tables} statements={report.statements} />}
                {activeTab === 'routines'   && <KnotRoutines   session={report.session} routines={report.routines ?? []} statements={report.statements} calls={report.calls ?? []} parameters={report.parameters ?? []} variables={report.variables ?? []} />}
                {activeTab === 'statements' && <KnotStatements statements={report.statements} snippets={report.snippets} atoms={report.atoms} outputColumns={report.outputColumns} affectedColumns={report.affectedColumns} />}
                {activeTab === 'atoms'      && <KnotAtoms      session={report.session} atoms={report.atoms} />}
                {/* Source tab: lazy-loaded, stays mounted to avoid re-fetch on tab switch */}
                <div style={{ display: activeTab === 'source' ? 'contents' : 'none' }}>
                  <KnotSource sessionId={selectedId ?? ''} active={activeTab === 'source'} />
                </div>
              </>
            )}
          </div>
        </main>
      </div>
    </div>
  );
});

KnotPage.displayName = 'KnotPage';

// ── Sub-components ────────────────────────────────────────────────────────────

function SessionCard({
  session, selected, onClick,
}: {
  session: KnotSession;
  selected: boolean;
  onClick: () => void;
}) {
  return (
    <div
      onClick={onClick}
      style={{
        padding: '8px 12px',
        borderRadius: 'var(--seer-radius-md)',
        cursor: 'pointer',
        background: selected ? 'var(--bg2)' : 'transparent',
        border: selected ? '1px solid var(--bd)' : '1px solid transparent',
        marginBottom: 2,
        transition: 'background 0.1s',
      }}
      onMouseEnter={(e) => {
        if (!selected) (e.currentTarget as HTMLElement).style.background = 'var(--bg2)';
      }}
      onMouseLeave={(e) => {
        if (!selected) (e.currentTarget as HTMLElement).style.background = 'transparent';
      }}
    >
      <div style={{
        fontSize: 12,
        fontWeight: selected ? 500 : 400,
        color: 'var(--t1)',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        marginBottom: 3,
      }}>
        {session.sessionName}
      </div>
      <div style={{ display: 'flex', gap: 8, alignItems: 'center', fontSize: 10, color: 'var(--t3)' }}>
        <span style={{
          padding: '1px 5px',
          borderRadius: 3,
          background: 'var(--bg3)',
          fontFamily: 'var(--mono)',
          fontSize: 9,
          color: 'var(--t2)',
        }}>
          {session.dialect.toUpperCase()}
        </span>
        {session.processingMs > 0 && (
          <span>{session.processingMs.toLocaleString()} ms</span>
        )}
      </div>
    </div>
  );
}

function EmptyState({ message, error = false }: { message: string; error?: boolean }) {
  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100%',
      fontSize: 13,
      color: error ? 'var(--danger)' : 'var(--t3)',
    }}>
      {message}
    </div>
  );
}
