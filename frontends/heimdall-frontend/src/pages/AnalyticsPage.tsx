/**
 * UA-05: HEIMDALL UX Analytics page — /heimdall/analytics
 *
 * Shows a 24-hour sliding-window UX summary:
 *   - Hot nodes (most-clicked in LOOM)
 *   - Level distribution (INFO / WARN / ERROR)
 *   - Slow renders (LOOM_VIEW_SLOW)
 *   - Active sessions count
 *
 * Auto-refreshes every 30 s. Admin-only (role guard is at route level).
 */
import { useCallback, useEffect, useState } from 'react';
import { useTranslation }                  from 'react-i18next';
import { usePageTitle }                    from '../hooks/usePageTitle';
import { fetchUxSummary }                  from '../api/analytics';
import { HotNodesChart }                   from '../components/analytics/HotNodesChart';
import { LevelDistributionPie }            from '../components/analytics/LevelDistributionPie';
import { SlowRendersList }                 from '../components/analytics/SlowRendersList';
import { ActiveUsersWidget }               from '../components/analytics/ActiveUsersWidget';
import type { UxSummary }                  from '../api/analytics';

const REFRESH_INTERVAL_MS = 30_000;

export default function AnalyticsPage() {
  const { t } = useTranslation();
  usePageTitle(t('nav.analytics', 'Analytics'));

  const [summary,   setSummary]   = useState<UxSummary | null>(null);
  const [loading,   setLoading]   = useState(true);
  const [error,     setError]     = useState<string | null>(null);
  const [lastFetch, setLastFetch] = useState<Date | null>(null);

  const load = useCallback(async () => {
    try {
      const data = await fetchUxSummary();
      setSummary(data);
      setLastFetch(new Date());
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load analytics');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
    const timer = setInterval(() => { void load(); }, REFRESH_INTERVAL_MS);
    return () => clearInterval(timer);
  }, [load]);

  /** Placeholder navigator — in prod this would use navigateTo from micro-frontend shell. */
  const navigateTo = useCallback((target: string, opts?: Record<string, unknown>) => {
    const url = target === 'verdandi'
      ? `${window.location.origin.replace(/:\d+$/, ':5173')}${opts?.sessionId ? `?session=${opts.sessionId}` : ''}`
      : window.location.href;
    window.open(url, '_blank', 'noopener');
  }, []);

  if (loading) {
    return <div className="page-loading" data-testid="analytics-loading">Loading analytics…</div>;
  }

  if (error) {
    return (
      <div className="page-error" data-testid="analytics-error">
        <span>{t('analytics.error', 'Could not load UX analytics')}</span>
        <span className="error-detail">{error}</span>
        <button className="btn btn-sm" onClick={() => { setLoading(true); void load(); }}>
          {t('actions.retry', 'Retry')}
        </button>
      </div>
    );
  }

  if (!summary) return null;

  return (
    <div className="analytics-page page-content" data-testid="analytics-page">
      <h1 className="page-title">{t('nav.analytics', 'UX Analytics')}</h1>
      {lastFetch && (
        <p className="analytics-meta">
          {t('analytics.lastUpdated', 'Last updated')}: {lastFetch.toLocaleTimeString()}
          {' · '}
          {t('analytics.autoRefresh', 'auto-refresh 30 s')}
        </p>
      )}

      <div className="analytics-grid">
        {/* Active sessions + event count */}
        <section className="analytics-card analytics-card--wide" data-testid="active-users-section">
          <h2 className="analytics-card-title">{t('analytics.activeUsers', 'Active Sessions')}</h2>
          <ActiveUsersWidget
            activeSessionCount={summary.activeSessionCount}
            totalEventsInWindow={summary.totalEventsInWindow}
            windowMs={summary.windowMs}
          />
        </section>

        {/* Level distribution */}
        <section className="analytics-card" data-testid="level-dist-section">
          <h2 className="analytics-card-title">{t('analytics.levelDist', 'Event Levels')}</h2>
          <LevelDistributionPie distribution={summary.levelDistribution} />
        </section>

        {/* Hot nodes */}
        <section className="analytics-card analytics-card--wide" data-testid="hot-nodes-section">
          <h2 className="analytics-card-title">{t('analytics.hotNodes', 'Most-Clicked Nodes')}</h2>
          <HotNodesChart nodes={summary.hotNodes} />
        </section>

        {/* Slow renders */}
        <section className="analytics-card analytics-card--full" data-testid="slow-renders-section">
          <h2 className="analytics-card-title">{t('analytics.slowRenders', 'Slow LOOM Renders')}</h2>
          <SlowRendersList renders={summary.slowRenders} navigateTo={navigateTo} />
        </section>
      </div>
    </div>
  );
}
