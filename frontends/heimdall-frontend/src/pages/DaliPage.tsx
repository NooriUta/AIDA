import { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation }                            from 'react-i18next';
import { useSearchParams }                           from 'react-router-dom';
import { type DaliSession, type JobRunrStats, type YggStats, getDaliHealth, getJobRunrStats, getSessions, getSessionsArchive, getYggStats, resetStuckJobs } from '../api/dali';
import { usePageTitle }    from '../hooks/usePageTitle';
import { ParseForm }       from '../components/dali/ParseForm';
import { SessionList }     from '../components/dali/SessionList';
import { useAuthStore }    from '../stores/authStore';
import css from '../components/dali/dali.module.css';

// ── Dali health ───────────────────────────────────────────────────────────────
type DaliState = 'connecting' | 'online' | 'offline';
const RETRY_MS = 4000;

// ── Toast ────────────────────────────────────────────────────────────────────
interface Toast {
  id: number;
  msg: string;
  type: 'suc' | 'err' | 'inf';
}

let _toastId = 0;

// ── YGG inline helpers ────────────────────────────────────────────────────────
function YggMetric({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <span>
      <span style={{ color }}>{value.toLocaleString()}</span>
      <span style={{ color: 'var(--t4)', marginLeft: 3 }}>{label}</span>
    </span>
  );
}
function Sep() {
  return <span style={{ color: 'var(--bd)', margin: '0 2px' }}>·</span>;
}

// ── DaliPage ─────────────────────────────────────────────────────────────────
export default function DaliPage() {
  const { t } = useTranslation();
  const sessionUser = useAuthStore(s => s.user);

  // Seed from localStorage so super-admin's last pick survives navigation.
  const [tenantAlias, setTenantAlias] = useState<string>(() => {
    const stored = localStorage.getItem('seer-active-tenant');
    if (stored && stored !== '__all__') return stored;
    return sessionUser?.activeTenantAlias ?? 'default';
  });

  // Non-super-admins must always see their own tenant, regardless of localStorage.
  useEffect(() => {
    if (sessionUser && sessionUser.role !== 'super-admin') {
      setTenantAlias(sessionUser.activeTenantAlias ?? 'default');
    }
  }, [sessionUser?.id, sessionUser?.role]);

  useEffect(() => {
    const handler = (e: Event) => {
      const alias = (e as CustomEvent<{ activeTenant: string }>).detail?.activeTenant;
      if (alias && alias !== '__all__') setTenantAlias(alias);
    };
    window.addEventListener('aida:tenant', handler);
    return () => window.removeEventListener('aida:tenant', handler);
  }, []);

  // URL-driven expanded session (Блок 6)
  const [searchParams, setSearchParams] = useSearchParams();
  const expandedId = searchParams.get('expanded');

  function toggleExpand(id: string) {
    setSearchParams(id === expandedId ? {} : { expanded: id }, { replace: true });
  }

  const [sessions,        setSessions]        = useState<DaliSession[]>([]);
  const [archive,         setArchive]         = useState<DaliSession[]>([]);
  const [archiveOpen,     setArchiveOpen]     = useState(false);
  const [archiveLoaded,   setArchiveLoaded]   = useState(false);
  const [archiveLoading,  setArchiveLoading]  = useState(false);
  // UC-S07 — all-tenants live view (superadmin only)
  const [allSessionsMode, setAllSessionsMode] = useState(false);
  const [toasts,          setToasts]          = useState<Toast[]>([]);
  const [daliState,       setDaliState]       = useState<DaliState>('connecting');
  const [friggHealthy,    setFriggHealthy]    = useState<boolean | null>(null);
  const [yggStats,        setYggStats]        = useState<YggStats | null>(null);
  const [jobrunrStats,    setJobrunrStats]    = useState<JobRunrStats | null>(null);
  const [resettingStuck,  setResettingStuck]  = useState(false);
  const clockRef       = useRef<ReturnType<typeof setInterval> | null>(null);
  const retryRef       = useRef<ReturnType<typeof setInterval> | null>(null);
  const friggPollRef   = useRef<ReturnType<typeof setInterval> | null>(null);
  const yggPollRef     = useRef<ReturnType<typeof setInterval> | null>(null);
  const jobrunrPollRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const [clock, setClock] = useState('');

  // Load sessions + check Dali availability; auto-retry until online
  // allTenants=true → superadmin sees all tenants' live sessions (UC-S07)
  function loadSessions(allTenants = allSessionsMode) {
    // Always send tenantAlias header so Dali can build TenantContext.
    // When allTenants=true, backend uses isSuperadmin() + listAllTenants() regardless of alias.
    const alias = tenantAlias;
    getSessions(200, alias, allTenants)
      .then(data => {
        setSessions(data);
        setDaliState('online');
        if (retryRef.current) { clearInterval(retryRef.current); retryRef.current = null; }
      })
      .catch(() => {
        setDaliState('offline');
        if (!retryRef.current) {
          retryRef.current = setInterval(() => {
            getSessions(1, alias, allTenants).then(data => {
              setSessions(data);
              setDaliState('online');
              if (retryRef.current) { clearInterval(retryRef.current); retryRef.current = null; }
              getSessions(200, alias, allTenants).then(d => setSessions(d)).catch(() => {});
            }).catch(() => setDaliState('offline'));
          }, RETRY_MS);
        }
      });
  }

  useEffect(() => {
    // Reset archive state so switching tenant forces a fresh fetch
    setArchive([]);
    setArchiveLoaded(false);
    setArchiveOpen(false);
    if (!allSessionsMode) loadSessions(false);
    return () => { if (retryRef.current) clearInterval(retryRef.current); };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tenantAlias]);

  // UC-S07: reload when allSessionsMode toggles
  useEffect(() => {
    if (retryRef.current) { clearInterval(retryRef.current); retryRef.current = null; }
    loadSessions(allSessionsMode);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [allSessionsMode]);

  // FRIGG health polling — independent of Dali availability, every 15s
  useEffect(() => {
    function checkFrigg() {
      getDaliHealth(tenantAlias)
        .then(h => setFriggHealthy(h.frigg === 'ok'))
        .catch(() => setFriggHealthy(false));
    }
    checkFrigg();
    friggPollRef.current = setInterval(checkFrigg, 15_000);
    return () => { if (friggPollRef.current) clearInterval(friggPollRef.current); };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tenantAlias]);

  // YGG stats polling — every 30s
  useEffect(() => {
    function fetchYgg() {
      getYggStats(tenantAlias).then(setYggStats).catch(() => {});
    }
    fetchYgg();
    yggPollRef.current = setInterval(fetchYgg, 30_000);
    return () => { if (yggPollRef.current) clearInterval(yggPollRef.current); };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tenantAlias]);

  // JobRunr stats polling — every 10s (Блок 7 FE)
  useEffect(() => {
    function fetchJobRunr() {
      getJobRunrStats().then(setJobrunrStats).catch(() => {});
    }
    fetchJobRunr();
    jobrunrPollRef.current = setInterval(fetchJobRunr, 10_000);
    return () => { if (jobrunrPollRef.current) clearInterval(jobrunrPollRef.current); };
  }, []);

  usePageTitle('Ðali');

  // Footer clock
  useEffect(() => {
    const tick = () => setClock(new Date().toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
    tick();
    clockRef.current = setInterval(tick, 1000);
    return () => clearInterval(clockRef.current!);
  }, []);

  const addToastRef = useRef<(msg: string, type: Toast['type']) => void>(null!);
  addToastRef.current = (msg: string, type: Toast['type']) => {
    const id = ++_toastId;
    setToasts(prev => [...prev, { id, msg, type }]);
    setTimeout(() => setToasts(prev => prev.filter(toast => toast.id !== id)), 3200);
  };

  function handleSessionCreated(s: DaliSession) {
    setSessions(prev => [s, ...prev]);
    addToastRef.current(t('dali.page.toastQueued', { id: s.id.slice(0, 8) }), 'inf');
  }

  const handleSessionUpdate = useCallback((updated: DaliSession) => {
    setSessions(prev => prev.map(s =>
      s.id === updated.id ? updated : s,
    ));
    if (updated.status === 'COMPLETED') {
      addToastRef.current(
        t('dali.page.toastCompleted', { id: updated.id.slice(0, 8), atoms: (updated.atomCount ?? 0).toLocaleString() }),
        'suc',
      );
    } else if (updated.status === 'FAILED') {
      addToastRef.current(t('dali.page.toastFailed', { id: updated.id.slice(0, 8) }), 'err');
    }
  }, [t]);

  const isSuperAdmin = sessionUser?.role === 'super-admin';

  function loadArchive(allTenants = false) {
    setArchiveLoading(true);
    const alias = tenantAlias;
    getSessionsArchive(200, alias, allTenants)
      .then(data => { setArchive(data); setArchiveLoaded(true); })
      .catch(() => addToastRef.current(t('dali.page.toastArchiveErr'), 'err'))
      .finally(() => setArchiveLoading(false));
  }

  // JobRunr stuck-jobs reset handler (Блок 7 FE)
  async function handleResetStuck() {
    setResettingStuck(true);
    try {
      await resetStuckJobs();
      const fresh = await getJobRunrStats();
      setJobrunrStats(fresh);
      addToastRef.current(t('dali.page.stuckReset'), 'suc');
    } catch {
      addToastRef.current(t('dali.page.toastArchiveErr'), 'err');
    } finally {
      setResettingStuck(false);
    }
  }

  // Derived stats
  const running   = sessions.filter(s => s.status === 'RUNNING').length;
  const completed = sessions.filter(s => s.status === 'COMPLETED');
  const totalAtoms = completed.reduce((a, s) => a + (s.atomCount ?? 0), 0);
  const rates = completed
    .filter(s => s.resolutionRate != null)
    .map(s => s.resolutionRate as number);
  const avgRate = rates.length
    ? rates.reduce((a, b) => a + b, 0) / rates.length
    : null;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--bg0)', overflow: 'hidden' }}>
      {/* Scrollable content area */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '24px 28px 0' }}>

        {/* Page header */}
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 20 }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 3 }}>
              <span style={{ fontSize: 15, fontWeight: 700, color: 'var(--t1)' }}>{t('dali.page.title')}</span>
              <span className="comp comp-dali">DALI :9090</span>
            </div>
            <div style={{ fontSize: 12, color: 'var(--t3)' }}>
              {t('dali.page.description')}
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            {/* UC-S07 — All-tenants live view toggle (superadmin only) */}
            {isSuperAdmin && (
              <button
                className="btn btn-secondary btn-sm"
                onClick={() => setAllSessionsMode(m => !m)}
                style={allSessionsMode ? {
                  background: 'color-mix(in srgb, var(--acc) 15%, transparent)',
                  borderColor: 'color-mix(in srgb, var(--acc) 40%, transparent)',
                  color: 'var(--acc)',
                } : undefined}
                title={allSessionsMode ? t('dali.page.allTenantsActiveTitle') : t('dali.page.allTenantsTitle')}
              >
                {allSessionsMode ? '◉ ALL' : '○ ALL'}
              </button>
            )}
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => loadSessions()}
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="23 4 23 10 17 10"/>
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
              </svg>
              {t('dali.page.refreshBtn')}
            </button>
          </div>
        </div>

        {/* Stats strip — in-memory session counters */}
        <div className={css.statsStrip}>
          <div className={css.statCard}>
            <div className={css.statLabel}>{t('dali.page.statTotal')}</div>
            <div className={css.statVal} style={{ color: 'var(--t2)' }}>{sessions.length}</div>
            <div className={css.statSub}>{t('dali.page.statTotalSub')}</div>
          </div>
          <div className={css.statCard}>
            <div className={css.statLabel}>{t('dali.page.statRunning')}</div>
            <div className={css.statVal} style={{ color: 'var(--inf)' }}>{running}</div>
            <div className={css.statSub}>{t('dali.page.statRunningSub')}</div>
          </div>
          <div className={css.statCard}>
            <div className={css.statLabel}>{t('dali.page.statCompleted')}</div>
            <div className={css.statVal} style={{ color: 'var(--suc)' }}>{completed.length}</div>
            <div className={css.statSub}>{t('dali.page.statCompletedSub')}</div>
          </div>
          <div className={css.statCard}>
            <div className={css.statLabel}>{t('dali.page.statAtoms')}</div>
            <div className={css.statVal} style={{ color: 'var(--acc)' }}>{totalAtoms.toLocaleString()}</div>
            <div className={css.statSub}>{t('dali.page.statAtomsSub')}</div>
          </div>
          <div className={css.statCard}>
            <div className={css.statLabel}>{t('dali.page.statAvgRes')}</div>
            <div className={css.statVal} style={{ color: 'var(--wrn)' }}>
              {avgRate != null ? `${(avgRate * 100).toFixed(1)}%` : '—'}
            </div>
            <div className={css.statSub}>{t('dali.page.statAvgResSub')}</div>
          </div>
        </div>

        {/* JobRunr stats strip (Блок 7 FE) */}
        {jobrunrStats && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 6,
            marginBottom: 10, padding: '7px 12px',
            background: 'var(--bg2)', border: '1px solid var(--bd)',
            borderRadius: 6, fontSize: 11, color: 'var(--t3)',
            fontFamily: 'var(--mono)',
          }}>
            <span style={{ color: 'var(--t4)', marginRight: 4, fontSize: 10, letterSpacing: '0.04em' }}>JobRunr</span>
            {jobrunrStats.processing > 0 && (
              <span style={{ color: 'var(--inf)' }}>
                ● {jobrunrStats.processing} {t('dali.page.jobrunrProcessing')}
              </span>
            )}
            {jobrunrStats.processing > 0 && <span style={{ color: 'var(--bd)' }}>·</span>}
            <span style={{ color: jobrunrStats.enqueued > 0 ? 'var(--t2)' : 'var(--t4)' }}>
              ○ {jobrunrStats.enqueued} {t('dali.page.jobrunrEnqueued')}
            </span>
            <span style={{ color: 'var(--bd)' }}>·</span>
            <span style={{ color: jobrunrStats.failed > 0 ? 'var(--wrn)' : 'var(--t4)' }}>
              ⚠ {jobrunrStats.failed} {t('dali.page.jobrunrFailed')}
            </span>
            <span style={{ color: 'var(--bd)' }}>·</span>
            <span style={{ color: 'var(--suc)' }}>
              ✓ {jobrunrStats.succeeded} {t('dali.page.jobrunrSucceeded')}
            </span>
          </div>
        )}

        {/* Stuck-jobs alert (Блок 7 FE) — shown when PROCESSING > 0 but no RUNNING sessions */}
        {jobrunrStats && jobrunrStats.processing > 0 && running === 0 && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 10,
            padding: '8px 14px', marginBottom: 12, borderRadius: 6,
            background: 'color-mix(in srgb, var(--wrn) 8%, transparent)',
            border: '1px solid color-mix(in srgb, var(--wrn) 30%, transparent)',
          }}>
            <span style={{ color: 'var(--wrn)', flexShrink: 0 }}>⚠</span>
            <span style={{ fontSize: 12, color: 'var(--t2)', flex: 1 }}>
              {t('dali.page.stuckAlert', { count: jobrunrStats.processing })}
            </span>
            <button
              onClick={handleResetStuck}
              disabled={resettingStuck}
              style={{
                padding: '3px 10px', borderRadius: 4, fontSize: 11,
                background: 'color-mix(in srgb, var(--wrn) 15%, transparent)',
                border: '1px solid color-mix(in srgb, var(--wrn) 40%, transparent)',
                color: 'var(--wrn)', cursor: resettingStuck ? 'default' : 'pointer',
                opacity: resettingStuck ? 0.5 : 1, flexShrink: 0,
              }}
            >
              {t('dali.page.stuckReset')}
            </button>
          </div>
        )}

        {/* YGG stats strip — live counts from ArcadeDB hound schema */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 6,
          marginBottom: 14, padding: '7px 12px',
          background: 'var(--bg2)', border: '1px solid var(--bd)',
          borderRadius: 6, fontSize: 11, color: 'var(--t3)',
          fontFamily: 'var(--mono)',
        }}>
          <span style={{ color: 'var(--t4)', marginRight: 4, fontSize: 10, letterSpacing: '0.04em' }}>YGG</span>
          {yggStats == null ? (
            <span style={{ color: 'var(--t4)' }}>{t('dali.page.yggLoading')}</span>
          ) : (
            <>
              <YggMetric label={t('dali.page.yggTables')}   value={yggStats.tables}     color="var(--t2)" />
              <Sep />
              <YggMetric label={t('dali.page.yggColumns')}  value={yggStats.columns}    color="var(--t2)" />
              <Sep />
              <YggMetric label={t('dali.page.yggStmts')}    value={yggStats.statements} color="var(--t2)" />
              <Sep />
              <YggMetric label={t('dali.page.yggRoutines')} value={yggStats.routines}   color="var(--t2)" />
              <Sep />
              <YggMetric label={t('dali.page.yggAtoms')}    value={yggStats.atomsTotal} color="var(--acc)" />
              <span style={{ margin: '0 4px', color: 'var(--bd)' }}>·</span>
              <span style={{ color: 'var(--suc)' }}>
                {yggStats.atomsTotal > 0
                  ? t('dali.page.yggResolved', { pct: ((yggStats.atomsResolved / yggStats.atomsTotal) * 100).toFixed(1) })
                  : '—'}
              </span>
              {yggStats.atomsUnresolved > 0 && (
                <>
                  <span style={{ margin: '0 4px', color: 'var(--bd)' }}>·</span>
                  <span style={{ color: 'var(--wrn)' }}>{t('dali.page.yggUnresolved', { count: yggStats.atomsUnresolved })}</span>
                </>
              )}
              {yggStats.atomsPending > 0 && (
                <>
                  <span style={{ margin: '0 4px', color: 'var(--bd)' }}>·</span>
                  <span style={{ color: 'var(--inf)' }}>{t('dali.page.yggPending', { count: yggStats.atomsPending })}</span>
                </>
              )}
            </>
          )}
          <button
            onClick={() => getYggStats(tenantAlias).then(setYggStats).catch(() => {})}
            style={{
              marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer',
              color: 'var(--t4)', padding: '2px 4px', borderRadius: 3,
            }}
            title={t('dali.page.yggRefreshTitle')}
          >
            ↻
          </button>
        </div>

        {/* Dali availability banner */}
        {daliState !== 'online' && (
          <div style={{
            display: 'flex', alignItems: 'center', gap: 10,
            padding: '10px 16px', marginBottom: 14, borderRadius: 6,
            background: daliState === 'connecting'
              ? 'color-mix(in srgb, var(--inf) 8%, transparent)'
              : 'color-mix(in srgb, var(--danger) 8%, transparent)',
            border: `1px solid ${daliState === 'connecting'
              ? 'color-mix(in srgb, var(--inf) 25%, transparent)'
              : 'color-mix(in srgb, var(--danger) 25%, transparent)'}`,
          }}>
            {daliState === 'connecting' ? (
              <div className={css.pulse} style={{ background: 'var(--inf)', flexShrink: 0 }} />
            ) : (
              <div style={{ width: 7, height: 7, borderRadius: '50%', background: 'var(--danger)', flexShrink: 0 }} />
            )}
            <span style={{ fontSize: 12, color: daliState === 'connecting' ? 'var(--inf)' : 'var(--danger)', fontFamily: 'var(--mono)' }}>
              {daliState === 'connecting'
                ? t('dali.page.connecting')
                : t('dali.page.offline', { retryS: RETRY_MS / 1000 })}
            </span>
            <button
              className="btn btn-secondary btn-sm"
              style={{ marginLeft: 'auto', padding: '3px 10px', fontSize: 11 }}
              onClick={() => loadSessions()}
            >
              {t('dali.page.retryBtn')}
            </button>
          </div>
        )}

        {/* Parse form */}
        <ParseForm onSessionCreated={handleSessionCreated} tenantAlias={tenantAlias} />

        {/* Active sessions (current Dali process, in-memory) */}
        {/* allSessionsMode=true → tenant shown prominently per row (UC-S07) */}
        <SessionList
          sessions={sessions}
          onSessionUpdate={handleSessionUpdate}
          tenantAlias={allSessionsMode ? undefined : tenantAlias}
          allTenantsMode={allSessionsMode}
          expandedId={expandedId}
          onToggleExpand={toggleExpand}
        />

        {/* FRIGG Archive — lazy-loaded on expand */}
        <div style={{ marginTop: 16, marginBottom: 16 }}>
          {/* div instead of button to avoid nested-button HTML violation (isSuperAdmin "ALL" button inside) */}
          <div
            role="button"
            tabIndex={0}
            onClick={() => {
              if (!archiveOpen && !archiveLoaded) loadArchive(false);
              setArchiveOpen(o => !o);
            }}
            onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { if (!archiveOpen && !archiveLoaded) loadArchive(false); setArchiveOpen(o => !o); } }}
            style={{
              display: 'flex', alignItems: 'center', gap: 8,
              width: '100%', padding: '9px 14px',
              background: 'var(--bg2)', border: '1px solid var(--bd)',
              borderRadius: archiveOpen ? '6px 6px 0 0' : 6,
              cursor: 'pointer', color: 'var(--t2)', fontSize: 12, fontWeight: 600,
              userSelect: 'none',
            }}
          >
            <svg
              width="13" height="13" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
              style={{ transform: archiveOpen ? 'rotate(180deg)' : undefined, transition: 'transform 0.18s' }}
            >
              <polyline points="6 9 12 15 18 9"/>
            </svg>
            <span>{t('dali.page.archiveTitle')}</span>
            {archiveLoaded && (
              <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)', marginLeft: 4 }}>
                {t('dali.page.archiveRecords', { count: archive.length })}
              </span>
            )}
            {archiveLoading && (
              <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--acc)', marginLeft: 4 }}>
                {t('dali.page.archiveLoading')}
              </span>
            )}
            <div style={{
              marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 5,
            }}>
              {isSuperAdmin && (
                <button
                  onClick={e => { e.stopPropagation(); setArchive([]); setArchiveLoaded(false); loadArchive(true); setArchiveOpen(true); }}
                  style={{
                    fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 700,
                    padding: '2px 7px', borderRadius: 3, cursor: 'pointer',
                    background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
                    color: 'var(--acc)',
                    border: '1px solid color-mix(in srgb, var(--acc) 30%, transparent)',
                  }}
                  title="Load sessions from ALL tenant databases"
                >
                  ALL
                </button>
              )}
              <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)' }}>
                <div style={{
                  width: 6, height: 6, borderRadius: '50%',
                  background: friggHealthy === null ? 'var(--wrn)' : friggHealthy ? 'var(--suc)' : 'var(--danger)',
                }}/>
                frigg :2481
              </div>
            </div>
          </div>
          {archiveOpen && (
            <div style={{ border: '1px solid var(--bd)', borderTop: 'none', borderRadius: '0 0 6px 6px', overflow: 'hidden' }}>
              <SessionList sessions={archive} onSessionUpdate={() => {}} tenantAlias={tenantAlias} />
            </div>
          )}
        </div>
      </div>

      {/* Footer */}
      <div className={css.footer}>
        <div className={css.footerItem}>
          <div className={css.footerDot} style={{
            background: daliState === 'online' ? 'var(--suc)' : daliState === 'connecting' ? 'var(--wrn)' : 'var(--danger)',
          }}/>
          dali :9090
        </div>
        <div className={css.footerSep}/>
        <div className={css.footerItem}>
          {sessions.length > 0
            ? t('dali.page.footerJobsTracked', { count: sessions.length })
            : t('dali.page.footerReady')}
        </div>
        <div className={css.footerSep}/>
        <div className={css.footerItem}>
          <div className={css.footerDot} style={{
            background: friggHealthy === null ? 'var(--wrn)' : friggHealthy ? 'var(--suc)' : 'var(--danger)',
          }}/>
          frigg :2481
        </div>
        <div className={css.footerSep}/>
        <div className={css.footerItem}>ygg :2480</div>
        <div className={css.footerSep}/>
        <div className={css.footerItem} style={{ marginLeft: 'auto' }}>{clock}</div>
      </div>

      {/* Toast stack */}
      <div className={css.toastStack}>
        {toasts.map(toast => (
          <div key={toast.id} className={`${css.toastItem} ${css[toast.type as keyof typeof css] ?? ''}`}>
            {toast.msg}
          </div>
        ))}
      </div>
    </div>
  );
}
