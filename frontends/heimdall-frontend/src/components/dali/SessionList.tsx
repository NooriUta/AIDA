import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliSession } from '../../api/dali';
import { useIsMobile } from '../../hooks/useIsMobile';
import { SessionRow } from './SessionRow';
import css from './dali.module.css';

type FilterStatus = 'all' | 'RUNNING' | 'FAILED' | 'COMPLETED' | 'QUEUED';
type SortBy = 'date' | 'duration';

interface SessionListProps {
  sessions:        DaliSession[];
  onSessionUpdate: (s: DaliSession) => void;
  tenantAlias?:    string;
  /** UC-S07: when true, sessions from multiple tenants are shown — display tenant badge prominently */
  allTenantsMode?: boolean;
  /** Controlled expanded row id — when provided by parent (URL-driven). If absent, local state is used. */
  expandedId?:     string | null;
  onToggleExpand?: (id: string) => void;
}

export function SessionList({
  sessions, onSessionUpdate, tenantAlias,
  allTenantsMode = false,
  expandedId: controlledExpandedId, onToggleExpand,
}: SessionListProps) {
  const { t }    = useTranslation();
  const isMobile = useIsMobile();

  // Local expand state — used only when parent doesn't control it (Блок 6)
  const [localExpandedId, setLocalExpandedId] = useState<string | null>(null);
  const expandedId = controlledExpandedId !== undefined ? controlledExpandedId : localExpandedId;

  function toggleExpand(id: string) {
    if (onToggleExpand) {
      onToggleExpand(id);
    } else {
      setLocalExpandedId(prev => (prev === id ? null : id));
    }
  }

  // Tab filter + search + sort (Блок 3)
  const [filterStatus, setFilterStatus] = useState<FilterStatus>('all');
  const [searchText,   setSearchText]   = useState('');
  const [sortBy,       setSortBy]       = useState<SortBy>('date');

  // Reset filter/search when sessions list changes identity (tenant switch, etc.)
  useEffect(() => {
    setFilterStatus('all');
    setSearchText('');
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tenantAlias]);

  // Count per status for tab badges (always from full sessions list)
  const counts = useMemo(() => ({
    all:       sessions.length,
    RUNNING:   sessions.filter(s => s.status === 'RUNNING' || s.status === 'CANCELLING').length,
    FAILED:    sessions.filter(s => s.status === 'FAILED').length,
    COMPLETED: sessions.filter(s => s.status === 'COMPLETED').length,
    QUEUED:    sessions.filter(s => s.status === 'QUEUED').length,
  }), [sessions]);

  // Filtered + searched + sorted sessions for table
  const filtered = useMemo(() => {
    let list = sessions;

    // Status filter
    if (filterStatus !== 'all') {
      if (filterStatus === 'RUNNING') {
        list = list.filter(s => s.status === 'RUNNING' || s.status === 'CANCELLING');
      } else {
        list = list.filter(s => s.status === filterStatus);
      }
    }

    // Search by source filename
    if (searchText.trim()) {
      const q = searchText.trim().toLowerCase();
      list = list.filter(s => {
        const src = (s.source ?? '').toLowerCase();
        const filename = src.replace(/\\/g, '/').split('/').pop() ?? '';
        return src.includes(q) || filename.includes(q);
      });
    }

    // Sort
    if (sortBy === 'duration') {
      list = [...list].sort((a, b) => (b.durationMs ?? 0) - (a.durationMs ?? 0));
    } else {
      // date desc (default — newer first)
      list = [...list].sort((a, b) =>
        (b.startedAt ?? '').localeCompare(a.startedAt ?? ''));
    }

    return list;
  }, [sessions, filterStatus, searchText, sortBy]);

  // Tab filter buttons definition
  const tabs: { key: FilterStatus; labelKey: string }[] = [
    { key: 'all',       labelKey: 'dali.sessions.filterAll'       },
    { key: 'RUNNING',   labelKey: 'dali.sessions.filterRunning'   },
    { key: 'FAILED',    labelKey: 'dali.sessions.filterFailed'    },
    { key: 'COMPLETED', labelKey: 'dali.sessions.filterCompleted' },
    { key: 'QUEUED',    labelKey: 'dali.sessions.filterQueued'    },
  ];

  return (
    <div className={css.panel}>
      <div className={css.panelHeader}>
        <span className={css.panelTitle}>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <line x1="3" y1="9" x2="21" y2="9"/>
            <line x1="9" y1="21" x2="9" y2="9"/>
          </svg>
          {t('dali.sessions.panelTitle')}
        </span>
        <span style={{ fontSize: 11, color: 'var(--t3)', fontFamily: 'var(--mono)' }}>
          {t('dali.sessions.sessionCount', { count: sessions.length })}
        </span>
        {/* UC-S07 all-tenants indicator */}
        {allTenantsMode && (
          <span style={{
            fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 700,
            padding: '2px 7px', borderRadius: 3,
            background: 'color-mix(in srgb, var(--acc) 15%, transparent)',
            color: 'var(--acc)',
            border: '1px solid color-mix(in srgb, var(--acc) 35%, transparent)',
            whiteSpace: 'nowrap',
          }}>
            ◉ {t('dali.page.allTenantsBanner')}
          </span>
        )}
      </div>

      {/* ── Tab filter bar + search + sort (Блок 3) ──────────────────────── */}
      {sessions.length > 0 && (
        <div style={{
          display: 'flex', alignItems: 'center', gap: 6,
          padding: '7px 14px 0', flexWrap: 'wrap',
          borderBottom: '1px solid var(--bd)',
        }}>
          {/* Status tabs */}
          <div style={{ display: 'flex', gap: 2, flex: '0 0 auto' }}>
            {tabs.map(tab => {
              const count = counts[tab.key];
              if (tab.key !== 'all' && count === 0) return null;
              const active = filterStatus === tab.key;
              return (
                <button
                  key={tab.key}
                  onClick={() => setFilterStatus(tab.key)}
                  style={{
                    padding: '4px 9px',
                    background: active ? 'var(--bg3)' : 'transparent',
                    border: active ? '1px solid var(--bd)' : '1px solid transparent',
                    borderBottom: active ? '1px solid var(--bg3)' : '1px solid transparent',
                    borderRadius: '4px 4px 0 0',
                    color: active ? 'var(--t1)' : 'var(--t3)',
                    fontSize: 11,
                    cursor: 'pointer',
                    fontFamily: 'inherit',
                    display: 'flex', alignItems: 'center', gap: 5,
                    transition: 'color 0.1s',
                  }}
                >
                  {/* Colored dot for non-all tabs */}
                  {tab.key === 'RUNNING' && <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--inf)', flexShrink: 0 }} />}
                  {tab.key === 'FAILED'  && <span style={{ color: 'var(--danger)', fontSize: 9 }}>✗</span>}
                  {tab.key === 'COMPLETED' && <span style={{ color: 'var(--suc)', fontSize: 9 }}>✓</span>}
                  {tab.key === 'QUEUED'  && <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--t3)', flexShrink: 0 }} />}
                  {t(tab.labelKey)}
                  <span style={{
                    fontFamily: 'var(--mono)', fontSize: 9,
                    color: active ? 'var(--acc)' : 'var(--t4)',
                    background: active ? 'color-mix(in srgb, var(--acc) 12%, transparent)' : 'var(--bg3)',
                    padding: '0 4px', borderRadius: 3,
                  }}>
                    {count}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Spacer */}
          <div style={{ flex: 1 }} />

          {/* Search */}
          <input
            type="text"
            value={searchText}
            onChange={e => setSearchText(e.target.value)}
            placeholder={t('dali.sessions.searchPlaceholder')}
            style={{
              background: 'var(--bg2)', border: '1px solid var(--bd)',
              borderRadius: 4, padding: '3px 8px', fontSize: 11,
              color: 'var(--t1)', outline: 'none', fontFamily: 'var(--mono)',
              width: 170,
            }}
          />

          {/* Sort toggle */}
          <button
            onClick={() => setSortBy(prev => prev === 'date' ? 'duration' : 'date')}
            style={{
              padding: '3px 8px', background: 'var(--bg2)',
              border: '1px solid var(--bd)', borderRadius: 4,
              fontSize: 11, cursor: 'pointer', color: 'var(--t3)',
              fontFamily: 'inherit', whiteSpace: 'nowrap',
            }}
            title={sortBy === 'date' ? t('dali.sessions.sortDuration') : t('dali.sessions.sortDate')}
          >
            {sortBy === 'date' ? t('dali.sessions.sortDate') : t('dali.sessions.sortDuration')}
          </button>
        </div>
      )}

      {sessions.length === 0 ? (
        <div className={css.empty}>
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ opacity: 0.35 }}>
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <line x1="3" y1="9" x2="21" y2="9"/>
            <line x1="9" y1="21" x2="9" y2="9"/>
          </svg>
          <div className={css.emptyTitle}>{t('dali.sessions.emptyTitle')}</div>
          <div className={css.emptySub}>
            {t('dali.sessions.emptySub').split('\n').map((line, i) => (
              <span key={i}>{line}{i === 0 ? <br/> : null}</span>
            ))}
          </div>
        </div>
      ) : filtered.length === 0 ? (
        <div className={css.empty} style={{ padding: '24px 16px' }}>
          <div className={css.emptyTitle} style={{ fontSize: 13 }}>No sessions match the filter</div>
        </div>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table className={css.sessionTable} style={{ minWidth: isMobile ? 480 : undefined }}>
            <thead>
              <tr>
                <th style={{ width: isMobile ? 100 : 160 }}>{t('dali.sessions.colSessionId')}</th>
                <th style={{ width: 120 }}>{t('dali.sessions.colStatus')}</th>
                {!isMobile && <th style={{ width: 100 }}>{t('dali.sessions.colDialect')}</th>}
                <th>{t('dali.sessions.colSource')}</th>
                <th style={{ width: isMobile ? 110 : 130 }}>{t('dali.sessions.colProgress')}</th>
                {!isMobile && <th style={{ width: 72 }}>{t('dali.sessions.colDuration')}</th>}
                <th style={{ width: 36 }}></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(s => (
                <SessionRow
                  key={s.id}
                  session={s}
                  expanded={expandedId === s.id}
                  onToggle={() => toggleExpand(s.id)}
                  onUpdate={onSessionUpdate}
                  hideCols={isMobile}
                  tenantAlias={tenantAlias}
                  showTenant={allTenantsMode}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
