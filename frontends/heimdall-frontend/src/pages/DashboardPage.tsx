import { useEffect }             from 'react';
import { useTranslation }       from 'react-i18next';
import { usePageTitle }         from '../hooks/usePageTitle';
import { MetricsBar }           from '../components/MetricsBar';
import { ThroughputChart }      from '../components/ThroughputChart';
import { ResolutionGauge }      from '../components/ResolutionGauge';
import { EventLog }             from '../components/EventLog';
import { ServiceHealthStrip }   from '../components/ServiceHealthStrip';
import { RecentErrors }         from '../components/RecentErrors';
import { DaliStatsWidget }      from '../components/DaliStatsWidget';
import { TenantMetricsPanel }   from '../components/TenantMetricsPanel';
import { useMetrics }           from '../hooks/useMetrics';
import { useTenantContext }     from '../hooks/useTenantContext';
import { useEventStream }       from '../hooks/useEventStream';
import { useDashboardStore }    from '../stores/dashboardStore';

const PREVIEW_COUNT = 50;

// ── DashboardPage ─────────────────────────────────────────────────────────────
export default function DashboardPage() {
  const { t } = useTranslation();
  usePageTitle(t('nav.dashboard'));
  const { metrics }        = useMetrics();
  const { events, status } = useEventStream();
  const { isSuperAdmin }   = useTenantContext();
  const setEvents          = useDashboardStore(s => s.setEvents);
  const setMetrics         = useDashboardStore(s => s.setMetrics);

  // Sync live data into shared store so PresentationMode can read it
  useEffect(() => { setEvents(events); },  [events,  setEvents]);
  useEffect(() => { setMetrics(metrics); }, [metrics, setMetrics]);

  const lastEvents = events.slice(-PREVIEW_COUNT);

  const wsColor =
    status === 'open'       ? 'var(--suc)'
    : status === 'connecting' ? 'var(--wrn)'
    : 'var(--danger)';

  return (
    <div style={{ height: '100%', overflowY: 'auto', background: 'var(--bg0)' }}>
      {/* Health strip */}
      <ServiceHealthStrip />

      {/* Metrics row */}
      <MetricsBar metrics={metrics} />

      {/* Dali parse engine stats */}
      <DaliStatsWidget />

      {/* HTA-10: per-tenant metrics (superadmin sees all; others see own tenant via MT routing) */}
      {isSuperAdmin && (
        <div style={{ padding: '0 var(--seer-space-6) var(--seer-space-4)' }}>
          <TenantMetricsPanel />
        </div>
      )}

      {/* Chart + Gauge + RecentErrors row */}
      <div style={{ display: 'flex', gap: 'var(--seer-space-4)', padding: '0 var(--seer-space-6) var(--seer-space-4)', flexWrap: 'wrap' }}>
        <div style={{ flex: 2, minWidth: 240, background: 'var(--bg1)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', padding: 'var(--seer-space-3) 0' }}>
          <div style={{ fontSize: '11px', color: 'var(--t3)', padding: '0 var(--seer-space-4) var(--seer-space-2)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            {t('dashboard.throughput')}
          </div>
          <ThroughputChart events={events} />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg1)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', padding: 'var(--seer-space-4)' }}>
          <ResolutionGauge rate={metrics?.resolutionRate ?? NaN} />
        </div>
        <div style={{ flex: 2, minWidth: 240 }}>
          <RecentErrors events={events} />
        </div>
      </div>

      {/* Event log preview */}
      <div style={{ padding: '0 var(--seer-space-6) var(--seer-space-6)' }}>
        <div style={{ fontSize: '11px', color: 'var(--t3)', marginBottom: 'var(--seer-space-2)', display: 'flex', justifyContent: 'space-between', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
          <span>{t('dashboard.recentEvents', { count: PREVIEW_COUNT })}</span>
          <span style={{ color: wsColor }}>● {t(`ws.${status}`)}</span>
        </div>
        <EventLog events={lastEvents} connected={status === 'open'} height={280} />
      </div>
    </div>
  );
}
