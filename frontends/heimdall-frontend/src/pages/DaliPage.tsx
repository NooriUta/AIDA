import { useCallback, useEffect, useRef, useState } from 'react';
import { type DaliSession, getDaliHealth, getSessions, getSessionsArchive } from '../api/dali';
import { ParseForm } from '../components/dali/ParseForm';
import { SessionList } from '../components/dali/SessionList';
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

// ── DaliPage ─────────────────────────────────────────────────────────────────
export default function DaliPage() {
  const [sessions,      setSessions]      = useState<DaliSession[]>([]);
  const [archive,       setArchive]       = useState<DaliSession[]>([]);
  const [archiveOpen,   setArchiveOpen]   = useState(false);
  const [archiveLoaded, setArchiveLoaded] = useState(false);
  const [archiveLoading,setArchiveLoading]= useState(false);
  const [toasts,        setToasts]        = useState<Toast[]>([]);
  const [daliState,     setDaliState]     = useState<DaliState>('connecting');
  const [friggHealthy,  setFriggHealthy]  = useState<boolean | null>(null);
  const clockRef     = useRef<ReturnType<typeof setInterval> | null>(null);
  const retryRef     = useRef<ReturnType<typeof setInterval> | null>(null);
  const friggPollRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const [clock, setClock] = useState('');

  // Load sessions + check Dali availability; auto-retry until online
  function loadSessions() {
    getSessions(50)
      .then(data => {
        setSessions(data);
        setDaliState('online');
        if (retryRef.current) { clearInterval(retryRef.current); retryRef.current = null; }
      })
      .catch(() => {
        setDaliState('offline');
        if (!retryRef.current) {
          retryRef.current = setInterval(() => {
            getSessions(1).then(data => {
              setSessions(data);
              setDaliState('online');
              if (retryRef.current) { clearInterval(retryRef.current); retryRef.current = null; }
              // Full reload once we're back
              getSessions(50).then(setSessions).catch(() => {});
            }).catch(() => setDaliState('offline'));
          }, RETRY_MS);
        }
      });
  }

  useEffect(() => {
    loadSessions();
    return () => { if (retryRef.current) clearInterval(retryRef.current); };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // FRIGG health polling — independent of Dali availability, every 15s
  useEffect(() => {
    function checkFrigg() {
      getDaliHealth()
        .then(h => setFriggHealthy(h.frigg === 'ok'))
        .catch(() => setFriggHealthy(false));
    }
    checkFrigg();
    friggPollRef.current = setInterval(checkFrigg, 15_000);
    return () => { if (friggPollRef.current) clearInterval(friggPollRef.current); };
  }, []);

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
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 3200);
  };

  function handleSessionCreated(s: DaliSession) {
    setSessions(prev => [s, ...prev]);
    addToastRef.current(`Session ${s.id.slice(0, 8)} queued`, 'inf');
  }

  const handleSessionUpdate = useCallback((updated: DaliSession) => {
    setSessions(prev => prev.map(s => s.id === updated.id ? updated : s));
    if (updated.status === 'COMPLETED') {
      addToastRef.current(
        `Session ${updated.id.slice(0, 8)} completed · ${(updated.atomCount ?? 0).toLocaleString()} atoms`,
        'suc',
      );
    } else if (updated.status === 'FAILED') {
      addToastRef.current(`Session ${updated.id.slice(0, 8)}: parse failed`, 'err');
    }
  }, []);

  function loadArchive() {
    setArchiveLoading(true);
    getSessionsArchive(200)
      .then(data => { setArchive(data); setArchiveLoaded(true); })
      .catch(() => addToastRef.current('FRIGG archive unavailable', 'err'))
      .finally(() => setArchiveLoading(false));
  }

  function toggleArchive() {
    if (!archiveOpen && !archiveLoaded) loadArchive();
    setArchiveOpen(o => !o);
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
              <span style={{ fontSize: 15, fontWeight: 700, color: 'var(--t1)' }}>Parse engine</span>
              <span className="comp comp-dali">DALI :9090</span>
            </div>
            <div style={{ fontSize: 12, color: 'var(--t3)' }}>
              Запуск парсинга SQL-источников · мониторинг JobRunr · lineage → YGG
            </div>
          </div>
          <button
            className="btn btn-secondary btn-sm"
            onClick={loadSessions}
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
            </svg>
            Refresh
          </button>
        </div>

        {/* Stats strip */}
        <div className={css.statsStrip}>
          <div className={css.statCard}>
            <div className={css.statLabel}>Total sessions</div>
            <div className={css.statVal} style={{ color: 'var(--t2)' }}>{sessions.length}</div>
            <div className={css.statSub}>all time</div>
          </div>
          <div className={css.statCard}>
            <div className={css.statLabel}>Running</div>
            <div className={css.statVal} style={{ color: 'var(--inf)' }}>{running}</div>
            <div className={css.statSub}>active</div>
          </div>
          <div className={css.statCard}>
            <div className={css.statLabel}>Completed</div>
            <div className={css.statVal} style={{ color: 'var(--suc)' }}>{completed.length}</div>
            <div className={css.statSub}>since startup</div>
          </div>
          <div className={css.statCard}>
            <div className={css.statLabel}>Atoms parsed</div>
            <div className={css.statVal} style={{ color: 'var(--acc)' }}>{totalAtoms.toLocaleString()}</div>
            <div className={css.statSub}>total extracted</div>
          </div>
          <div className={css.statCard}>
            <div className={css.statLabel}>Avg resolution</div>
            <div className={css.statVal} style={{ color: 'var(--wrn)' }}>
              {avgRate != null ? `${(avgRate * 100).toFixed(1)}%` : '—'}
            </div>
            <div className={css.statSub}>column-level</div>
          </div>
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
                ? 'Подключение к Dali :9090…'
                : `Dali :9090 недоступен — повтор через ${RETRY_MS / 1000}с`}
            </span>
            <button
              className="btn btn-secondary btn-sm"
              style={{ marginLeft: 'auto', padding: '3px 10px', fontSize: 11 }}
              onClick={loadSessions}
            >
              Retry
            </button>
          </div>
        )}

        {/* Parse form */}
        <ParseForm onSessionCreated={handleSessionCreated} />

        {/* Active sessions (current Dali process, in-memory) */}
        <SessionList sessions={sessions} onSessionUpdate={handleSessionUpdate} />

        {/* FRIGG Archive — lazy-loaded on expand */}
        <div style={{ marginTop: 16, marginBottom: 16 }}>
          <button
            onClick={toggleArchive}
            style={{
              display: 'flex', alignItems: 'center', gap: 8,
              width: '100%', padding: '9px 14px',
              background: 'var(--bg2)', border: '1px solid var(--bd)',
              borderRadius: archiveOpen ? '6px 6px 0 0' : 6,
              cursor: 'pointer', color: 'var(--t2)', fontSize: 12, fontWeight: 600,
            }}
          >
            <svg
              width="13" height="13" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
              style={{ transform: archiveOpen ? 'rotate(180deg)' : undefined, transition: 'transform 0.18s' }}
            >
              <polyline points="6 9 12 15 18 9"/>
            </svg>
            <span>Архив сессий (FRIGG)</span>
            {archiveLoaded && (
              <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)', marginLeft: 4 }}>
                {archive.length} записей
              </span>
            )}
            {archiveLoading && (
              <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--acc)', marginLeft: 4 }}>
                загрузка…
              </span>
            )}
            <div style={{
              marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 5,
              fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)',
            }}>
              <div style={{
                width: 6, height: 6, borderRadius: '50%',
                background: friggHealthy === null ? 'var(--wrn)' : friggHealthy ? 'var(--suc)' : 'var(--danger)',
              }}/>
              frigg :2481
            </div>
          </button>
          {archiveOpen && (
            <div style={{ border: '1px solid var(--bd)', borderTop: 'none', borderRadius: '0 0 6px 6px', overflow: 'hidden' }}>
              <SessionList sessions={archive} onSessionUpdate={() => {}} />
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
          {sessions.length > 0 ? `${sessions.length} jobs tracked` : 'jobrunr ready'}
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
        {toasts.map(t => (
          <div key={t.id} className={`${css.toastItem} ${css[t.type as keyof typeof css] ?? ''}`}>
            {t.msg}
          </div>
        ))}
      </div>
    </div>
  );
}
