import React from 'react';
import { useTranslation } from 'react-i18next';
import type { MetricsSnapshot } from 'aida-shared';

interface MetricsBarProps {
  metrics: MetricsSnapshot | null;
}

interface CardProps {
  label: string;
  value: string;
  accent?: string;
}

const cardStyle: React.CSSProperties = {
  background:   'var(--bg2)',
  border:       '1px solid var(--bd)',
  borderRadius: 'var(--seer-radius-md)',
  padding:      'var(--seer-space-3) var(--seer-space-4)',
  minWidth:     '110px',
};

function MetricCard({ label, value, accent }: CardProps) {
  return (
    <div style={cardStyle}>
      <div style={{ fontSize: '11px', color: 'var(--t3)', marginBottom: '4px', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
        {label}
      </div>
      <div style={{ fontSize: '20px', fontWeight: 600, color: accent ?? 'var(--t1)', fontFamily: 'var(--mono)' }}>
        {value}
      </div>
    </div>
  );
}

function resolutionColor(rate: number): string {
  if (Number.isNaN(rate)) return 'var(--t3)';
  if (rate >= 85) return 'var(--suc)';
  if (rate >= 70) return 'var(--wrn)';
  return 'var(--danger)';
}

export function MetricsBar({ metrics }: MetricsBarProps) {
  const { t } = useTranslation();
  const na = '--';
  return (
    <div style={{ display: 'flex', gap: 'var(--seer-space-3)', padding: 'var(--seer-space-4) var(--seer-space-6)', flexWrap: 'wrap' }}>
      <MetricCard label={t('metrics.atoms')}     value={metrics ? String(metrics.atomsExtracted) : na} />
      <MetricCard label={t('metrics.files')}     value={metrics ? String(metrics.filesParsed)    : na} />
      <MetricCard label={t('metrics.toolCalls')} value={metrics ? String(metrics.toolCallsTotal) : na} />
      <MetricCard label={t('metrics.workers')}   value={metrics ? String(metrics.activeWorkers)  : na} />
      <MetricCard label={t('metrics.queue')}     value={metrics ? String(metrics.queueDepth)     : na} />
      <MetricCard
        label={t('metrics.resolution')}
        value={
          !metrics
            ? na
            : Number.isNaN(Number(metrics.resolutionRate))
              ? t('metrics.na')
              : `${Number(metrics.resolutionRate).toFixed(1)}%`
        }
        accent={metrics ? resolutionColor(Number(metrics.resolutionRate)) : undefined}
      />
    </div>
  );
}
