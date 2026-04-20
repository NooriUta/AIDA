import { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

const JR_BASE   = '/jobrunr';
const LIMIT     = 30;
const REFRESH_S = 5;

// ── API types (JobRunr 8.x) ───────────────────────────────────────────────────
interface BgServer  { id: string; workerPoolSize: number; processedJobs: number }
interface JobStats  {
  enqueued: number; processing: number; succeeded: number; failed: number;
  scheduled: number; recurringJobs: number;
  backgroundJobServers: BgServer[];
}
interface Job {
  id: string; jobName: string; state: string;
  createdAt: string; updatedAt: string; labels?: string[];
}
interface RecurringJob { id: string; jobName: string; cronExpression: string; nextRun: string | null }
interface JobPage { total: number; items: Job[] }
type Tab  = 'ALL' | 'ENQUEUED' | 'PROCESSING' | 'SUCCEEDED' | 'FAILED' | 'SCHEDULED' | 'RECURRING';
type Conn = 'connecting' | 'online' | 'offline';

// ── Fetchers (JobRunr 8.x: no /api/jobStats — counts come from per-state queries) ──
async function fetchStatCount(state: string): Promise<number> {
  const r = await fetch(`${JR_BASE}/api/jobs?state=${state}&offset=0&limit=1`);
  if (!r.ok) return 0;
  const d = await r.json() as JobPage;
  return d.total ?? 0;
}
async function fetchServers(): Promise<BgServer[]> {
  try {
    const r = await fetch(`${JR_BASE}/api/servers`);
    if (!r.ok) return [];
    const d = await r.json();
    return Array.isArray(d) ? d : [];
  } catch { return []; }
}
async function fetchStats(): Promise<JobStats> {
  const [enqueued, processing, succeeded, failed, scheduled, servers] = await Promise.all([
    fetchStatCount('ENQUEUED'),
    fetchStatCount('PROCESSING'),
    fetchStatCount('SUCCEEDED'),
    fetchStatCount('FAILED'),
    fetchStatCount('SCHEDULED'),
    fetchServers(),
  ]);
  return { enqueued, processing, succeeded, failed, scheduled, recurringJobs: 0, backgroundJobServers: servers };
}
async function fetchJobs(state: Tab): Promise<JobPage> {
  const q = state !== 'ALL' ? `&state=${state}` : '';
  const r = await fetch(`${JR_BASE}/api/jobs?offset=0&limit=${LIMIT}&order=updatedAt:desc${q}`);
  if (!r.ok) throw new Error(r.statusText);
  return r.json() as Promise<JobPage>;
}
async function fetchRecurring(): Promise<RecurringJob[]> {
  const r = await fetch(`${JR_BASE}/api/recurring-jobs?offset=0&limit=${LIMIT}`);
  if (!r.ok) return [];
  const d = await r.json();
  return Array.isArray(d) ? d : (d as { items?: RecurringJob[] }).items ?? [];
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function fmtTime(iso: string) {
  try { return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }); }
  catch { return iso; }
}
function fmtDur(a: string, b: string) {
  const ms = new Date(b).getTime() - new Date(a).getTime();
  if (ms < 0)    return '—';
  if (ms < 1000) return `${ms}ms`;
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)}s`;
  return `${Math.floor(ms / 60_000)}m ${Math.floor((ms % 60_000) / 1000)}s`;
}

const STATE_CLR: Record<string, string> = {
  ENQUEUED:   'var(--t2)',
  PROCESSING: 'var(--acc)',
  SUCCEEDED:  '#4caf83',
  FAILED:     '#e05252',
  SCHEDULED:  '#7b8cde',
  DELETED:    'var(--t4)',
};

// ── Page ──────────────────────────────────────────────────────────────────────
export default function DaliJobRunrPage() {
  const { t } = useTranslation();

  const [conn,      setConn]      = useState<Conn>('connecting');
  const [stats,     setStats]     = useState<JobStats | null>(null);
  const [jobs,      setJobs]      = useState<Job[]>([]);
  const [total,     setTotal]     = useState(0);
  const [recurring, setRecurring] = useState<RecurringJob[]>([]);
  const [tab,       setTab]       = useState<Tab>('ALL');
  const [loading,   setLoading]   = useState(false);
  const [countdown, setCountdown] = useState(REFRESH_S);
  const timerRef    = useRef<ReturnType<typeof setInterval> | null>(null);
  const countRef    = useRef<ReturnType<typeof setInterval> | null>(null);

  const load = useCallback(async (activeTab: Tab) => {
    setLoading(true);
    try {
      if (activeTab === 'RECURRING') {
        const [st, rec] = await Promise.all([fetchStats(), fetchRecurring()]);
        setStats(st);
        setRecurring(rec);
      } else {
        const [st, page] = await Promise.all([fetchStats(), fetchJobs(activeTab)]);
        setStats(st);
        setJobs(page.items);
        setTotal(page.total);
      }
      setConn('online');
    } catch {
      setConn(c => c === 'online' ? 'offline' : c === 'connecting' ? 'offline' : c);
    } finally {
      setLoading(false);
    }
  }, []);

  const startTimers = useCallback((activeTab: Tab) => {
    if (timerRef.current)  clearInterval(timerRef.current);
    if (countRef.current)  clearInterval(countRef.current);
    setCountdown(REFRESH_S);
    timerRef.current = setInterval(() => {
      void load(activeTab);
      setCountdown(REFRESH_S);
    }, REFRESH_S * 1000);
    countRef.current = setInterval(() => setCountdown(n => n - 1), 1000);
  }, [load]);

  useEffect(() => {
    void load('ALL');
    startTimers('ALL');
    return () => {
      if (timerRef.current)  clearInterval(timerRef.current);
      if (countRef.current)  clearInterval(countRef.current);
    };
  }, []);                                // eslint-disable-line react-hooks/exhaustive-deps

  const switchTab = (next: Tab) => {
    setTab(next);
    void load(next);
    startTimers(next);
  };

  const manualRefresh = () => {
    void load(tab);
    startTimers(tab);
  };

  const TABS: { id: Tab; label: string; badge?: number }[] = [
    { id: 'ALL',        label: t('daliJobrunr.tabAll') },
    { id: 'ENQUEUED',   label: t('daliJobrunr.tabEnqueued'),   badge: stats?.enqueued },
    { id: 'PROCESSING', label: t('daliJobrunr.tabProcessing'), badge: stats?.processing },
    { id: 'SUCCEEDED',  label: t('daliJobrunr.tabSucceeded') },
    { id: 'FAILED',     label: t('daliJobrunr.tabFailed'),     badge: stats?.failed },
    { id: 'SCHEDULED',  label: t('daliJobrunr.tabScheduled'),  badge: stats?.scheduled },
    { id: 'RECURRING',  label: t('daliJobrunr.tabRecurring') },
  ];

  const connDot      = { connecting: '#7b8cde', online: '#4caf83', offline: '#e05252' }[conn];
  const totalWorkers = stats?.backgroundJobServers.reduce((s, b) => s + b.workerPoolSize, 0) ?? 0;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>

      {/* ── Header ── */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '10px var(--seer-space-6)',
        borderBottom: '1px solid var(--bd)', flexShrink: 0,
        background: 'var(--bg0)',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <span style={{ width: 8, height: 8, borderRadius: '50%', background: connDot, flexShrink: 0 }} />
          <span style={{ fontSize: '15px', fontWeight: 700, color: 'var(--t1)' }}>
            {t('daliJobrunr.title')}
          </span>
          <span style={{ fontSize: '12px', color: 'var(--t4)' }}>
            {conn === 'offline' ? t('daliJobrunr.offline') : t('daliJobrunr.port', { port: '29091' })}
          </span>
          {totalWorkers > 0 && (
            <span style={{
              fontSize: '11px', padding: '2px 8px', borderRadius: 99,
              background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
              color: 'var(--acc)',
              border: '1px solid color-mix(in srgb, var(--acc) 30%, transparent)',
            }}>
              {t('daliJobrunr.workers', { count: totalWorkers })}
            </span>
          )}
        </div>

        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <span style={{ fontSize: '11px', color: 'var(--t4)', fontVariantNumeric: 'tabular-nums' }}>
            {t('daliJobrunr.nextRefresh', { s: countdown })}
          </span>
          <button
            onClick={manualRefresh}
            disabled={loading}
            style={{
              padding: '5px 12px', fontSize: '12px', fontWeight: 600,
              background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
              border: '1px solid color-mix(in srgb, var(--acc) 35%, transparent)',
              borderRadius: 'var(--seer-radius-md)', color: 'var(--acc)',
              cursor: loading ? 'default' : 'pointer', opacity: loading ? 0.6 : 1,
            }}
          >
            {loading ? '…' : t('daliJobrunr.refresh')}
          </button>
          <a
            href="http://localhost:29091/dashboard"
            target="_blank" rel="noopener noreferrer"
            style={{
              padding: '5px 12px', fontSize: '12px', fontWeight: 500,
              background: 'transparent',
              border: '1px solid var(--bd)',
              borderRadius: 'var(--seer-radius-md)', color: 'var(--t2)', textDecoration: 'none',
            }}
          >
            ↗
          </a>
        </div>
      </div>

      {/* ── Stats row ── */}
      {stats && (
        <div style={{
          display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)',
          borderBottom: '1px solid var(--bd)', flexShrink: 0,
        }}>
          {([
            { key: 'enqueued',   val: stats.enqueued,   color: 'var(--t1)' },
            { key: 'processing', val: stats.processing, color: 'var(--acc)' },
            { key: 'succeeded',  val: stats.succeeded,  color: '#4caf83' },
            { key: 'failed',     val: stats.failed,     color: '#e05252' },
          ] as const).map(s => (
            <button
              key={s.key}
              onClick={() => switchTab(s.key.toUpperCase() as Tab)}
              style={{
                padding: '10px 16px', textAlign: 'left', cursor: 'pointer',
                background: 'transparent',
                border: 'none', borderRight: '1px solid var(--bd)',
                transition: 'background .1s',
              }}
              onMouseEnter={e => (e.currentTarget.style.background = 'color-mix(in srgb, var(--bd) 60%, transparent)')}
              onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}
            >
              <div style={{ fontSize: '22px', fontWeight: 700, color: s.color, fontVariantNumeric: 'tabular-nums', lineHeight: 1 }}>
                {s.val.toLocaleString()}
              </div>
              <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: 3 }}>
                {t(`daliJobrunr.stat${s.key.charAt(0).toUpperCase() + s.key.slice(1)}`)}
              </div>
            </button>
          ))}
        </div>
      )}

      {/* ── Tabs ── */}
      <div style={{
        display: 'flex', borderBottom: '1px solid var(--bd)',
        padding: '0 var(--seer-space-6)', flexShrink: 0,
        background: 'var(--bg0)', overflowX: 'auto',
      }}>
        {TABS.map(it => (
          <button
            key={it.id}
            onClick={() => switchTab(it.id)}
            style={{
              padding: '8px 12px', fontSize: '12px',
              fontWeight: tab === it.id ? 700 : 400,
              color: tab === it.id ? 'var(--acc)' : 'var(--t3)',
              background: 'transparent', border: 'none',
              borderBottom: tab === it.id ? '2px solid var(--acc)' : '2px solid transparent',
              cursor: 'pointer', whiteSpace: 'nowrap',
              display: 'flex', alignItems: 'center', gap: 5,
            }}
          >
            {it.label}
            {!!it.badge && it.badge > 0 && (
              <span style={{
                fontSize: '10px', padding: '1px 5px', borderRadius: 99,
                background: 'color-mix(in srgb, var(--acc) 18%, transparent)',
                color: 'var(--acc)',
              }}>{it.badge}</span>
            )}
          </button>
        ))}
      </div>

      {/* ── Body ── */}
      <div style={{ flex: 1, overflow: 'auto' }}>

        {conn !== 'online' && (
          <div style={{ padding: '60px 24px', textAlign: 'center', fontSize: '13px',
            color: conn === 'offline' ? '#e05252' : 'var(--t3)' }}>
            {conn === 'connecting' ? t('daliJobrunr.connecting') : t('daliJobrunr.offline')}
          </div>
        )}

        {conn === 'online' && tab === 'RECURRING' && (
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px' }}>
            <thead>
              <tr style={{ color: 'var(--t4)', textAlign: 'left', position: 'sticky', top: 0, background: 'var(--bg0)' }}>
                <th style={{ padding: '8px 16px', fontWeight: 500 }}>{t('daliJobrunr.colName')}</th>
                <th style={{ padding: '8px 16px', fontWeight: 500 }}>{t('daliJobrunr.colCron')}</th>
                <th style={{ padding: '8px 16px', fontWeight: 500 }}>{t('daliJobrunr.colNextRun')}</th>
              </tr>
            </thead>
            <tbody>
              {recurring.length === 0 && (
                <tr><td colSpan={3} style={{ padding: '48px 16px', textAlign: 'center', color: 'var(--t4)' }}>{t('daliJobrunr.empty')}</td></tr>
              )}
              {recurring.map(r => (
                <tr key={r.id} style={{ borderTop: '1px solid var(--bd)' }}>
                  <td style={{ padding: '9px 16px', color: 'var(--t1)', fontFamily: 'monospace' }}>{r.jobName}</td>
                  <td style={{ padding: '9px 16px', color: 'var(--t3)', fontFamily: 'monospace' }}>{r.cronExpression}</td>
                  <td style={{ padding: '9px 16px', color: 'var(--t2)' }}>{r.nextRun ? fmtTime(r.nextRun) : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {conn === 'online' && tab !== 'RECURRING' && (
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px' }}>
            <thead>
              <tr style={{ color: 'var(--t4)', textAlign: 'left', position: 'sticky', top: 0, background: 'var(--bg0)' }}>
                <th style={{ padding: '8px 16px', fontWeight: 500 }}>{t('daliJobrunr.colName')}</th>
                <th style={{ padding: '8px 16px', fontWeight: 500, width: 110 }}>{t('daliJobrunr.colState')}</th>
                <th style={{ padding: '8px 16px', fontWeight: 500, width: 90 }}>{t('daliJobrunr.colCreated')}</th>
                <th style={{ padding: '8px 16px', fontWeight: 500, width: 80 }}>{t('daliJobrunr.colDuration')}</th>
              </tr>
            </thead>
            <tbody>
              {jobs.length === 0 && !loading && (
                <tr><td colSpan={4} style={{ padding: '48px 16px', textAlign: 'center', color: 'var(--t4)' }}>{t('daliJobrunr.empty')}</td></tr>
              )}
              {jobs.map(j => (
                <tr key={j.id} style={{ borderTop: '1px solid var(--bd)' }}>
                  <td style={{
                    padding: '8px 16px', color: 'var(--t1)', fontFamily: 'monospace', fontSize: '11px',
                    maxWidth: 0, width: '100%', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                  }} title={j.jobName}>
                    {j.jobName}
                  </td>
                  <td style={{ padding: '8px 16px' }}>
                    <span style={{ fontSize: '10px', fontWeight: 700, letterSpacing: '0.05em', color: STATE_CLR[j.state] ?? 'var(--t3)' }}>
                      {j.state}
                    </span>
                  </td>
                  <td style={{ padding: '8px 16px', color: 'var(--t3)', whiteSpace: 'nowrap' }}>
                    {fmtTime(j.createdAt)}
                  </td>
                  <td style={{ padding: '8px 16px', color: 'var(--t3)', fontVariantNumeric: 'tabular-nums' }}>
                    {j.state === 'SUCCEEDED' || j.state === 'FAILED'
                      ? fmtDur(j.createdAt, j.updatedAt)
                      : j.state === 'PROCESSING'
                        ? fmtDur(j.createdAt, new Date().toISOString())
                        : '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {conn === 'online' && tab !== 'RECURRING' && total > LIMIT && (
          <div style={{ padding: '10px 16px', fontSize: '11px', color: 'var(--t4)', borderTop: '1px solid var(--bd)' }}>
            {t('daliJobrunr.showingOf', { shown: Math.min(LIMIT, jobs.length), total })}
          </div>
        )}
      </div>
    </div>
  );
}
