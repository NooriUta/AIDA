import { useTranslation } from 'react-i18next';
import { MetricsBar }      from '../components/MetricsBar';
import { ThroughputChart } from '../components/ThroughputChart';
import { ResolutionGauge } from '../components/ResolutionGauge';
import { EventLog }        from '../components/EventLog';
import { useMetrics }      from '../hooks/useMetrics';
import { useEventStream }  from '../hooks/useEventStream';

const PREVIEW_COUNT = 50;

export default function DashboardPage() {
  const { t } = useTranslation();
  const { metrics }         = useMetrics();
  const { events, status }  = useEventStream();

  const lastEvents = events.slice(-PREVIEW_COUNT);

  const wsColor =
    status === 'open'       ? 'var(--suc)'
    : status === 'connecting' ? 'var(--wrn)'
    : 'var(--danger)';

  return (
    <div style={{ height: '100%', overflowY: 'auto', background: 'var(--bg0)' }}>
      {/* Metrics row */}
      <MetricsBar metrics={metrics} />

      {/* Chart + Gauge row */}
      <div style={{ display: 'flex', gap: 'var(--seer-space-4)', padding: '0 var(--seer-space-6) var(--seer-space-4)' }}>
        <div style={{ flex: 1, background: 'var(--bg1)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', padding: 'var(--seer-space-3) 0' }}>
          <div style={{ fontSize: '11px', color: 'var(--t3)', padding: '0 var(--seer-space-4) var(--seer-space-2)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            {t('dashboard.throughput')}
          </div>
          <ThroughputChart events={events} />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg1)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', padding: 'var(--seer-space-4)' }}>
          <ResolutionGauge rate={metrics?.resolutionRate ?? NaN} />
        </div>
      </div>

      {/* Event log preview */}
      <div style={{ padding: '0 var(--seer-space-6) var(--seer-space-6)' }}>
        <div style={{ fontSize: '11px', color: 'var(--t3)', marginBottom: 'var(--seer-space-2)', display: 'flex', justifyContent: 'space-between', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
          <span>{t('dashboard.recentEvents', { count: PREVIEW_COUNT })}</span>
          <span style={{ color: wsColor }}>● {t(`ws.${status}`)}</span>
        </div>
        <EventLog events={lastEvents} maxHeight="320px" />
      </div>
    </div>
  );
}
