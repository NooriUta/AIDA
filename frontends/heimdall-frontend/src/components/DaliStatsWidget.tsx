import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { getSessions } from '../api/dali';
import type { DaliSession } from '../api/dali';

const POLL_MS = 10_000;

interface DaliStats {
  running:       number;
  completed:     number;
  failed:        number;
  totalAtoms:    number;
  avgResolution: number | null;  // null = no data
  daliOnline:    boolean;
}

function computeStats(sessions: DaliSession[]): DaliStats {
  let running = 0, completed = 0, failed = 0;
  let totalAtoms = 0;
  let resSum = 0, resCount = 0;

  for (const s of sessions) {
    if (s.status === 'RUNNING')    running++;
    if (s.status === 'COMPLETED')  completed++;
    if (s.status === 'FAILED' || s.status === 'CANCELLED') failed++;
    if (s.status === 'COMPLETED' && s.atomCount)     totalAtoms += s.atomCount;
    if (s.status === 'COMPLETED' && s.resolutionRate != null && !Number.isNaN(s.resolutionRate)) {
      resSum += s.resolutionRate;
      resCount++;
    }
  }

  return {
    running,
    completed,
    failed,
    totalAtoms,
    avgResolution: resCount > 0 ? resSum / resCount : null,
    daliOnline: true,
  };
}

function resColor(rate: number | null): string {
  if (rate == null) return 'var(--t3)';
  if (rate >= 85)   return 'var(--suc)';
  if (rate >= 70)   return 'var(--wrn)';
  return 'var(--danger)';
}

// ── DaliStatsWidget ───────────────────────────────────────────────────────────
export function DaliStatsWidget() {
  const { t } = useTranslation();
  const [stats, setStats] = useState<DaliStats | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function poll() {
      try {
        const sessions = await getSessions(50);
        if (!cancelled) setStats(computeStats(sessions));
      } catch {
        if (!cancelled) setStats(prev => prev ? { ...prev, daliOnline: false } : null);
      }
    }

    poll();
    const id = setInterval(poll, POLL_MS);
    return () => { cancelled = true; clearInterval(id); };
  }, []);

  const na = '—';

  const statusColor = !stats
    ? 'var(--t3)'
    : stats.daliOnline ? 'var(--suc)' : 'var(--danger)';

  const statusLabel = !stats
    ? t('daliWidget.connecting')
    : stats.daliOnline ? t('daliWidget.online') : t('daliWidget.offline');

  return (
    <div style={{ padding: '0 var(--seer-space-6) var(--seer-space-2)' }}>
      {/* Section header */}
      <div style={{
        fontSize: '11px', color: 'var(--t3)', marginBottom: 'var(--seer-space-2)',
        display: 'flex', justifyContent: 'space-between',
        textTransform: 'uppercase', letterSpacing: '0.06em',
      }}>
        <span>{t('daliWidget.title')}</span>
        <span style={{ color: statusColor }}>● {statusLabel}</span>
      </div>

      {/* Cards row */}
      <div style={{ display: 'flex', gap: 'var(--seer-space-3)', flexWrap: 'wrap' }}>
        {/* Running */}
        <div style={cardStyle}>
          <div style={labelStyle}>{t('daliWidget.running')}</div>
          <div style={{ ...valueStyle, color: stats?.running ? 'var(--wrn)' : 'var(--t1)' }}>
            {stats ? String(stats.running) : na}
          </div>
        </div>

        {/* Completed */}
        <div style={cardStyle}>
          <div style={labelStyle}>{t('daliWidget.completed')}</div>
          <div style={{ ...valueStyle, color: stats?.completed ? 'var(--suc)' : 'var(--t1)' }}>
            {stats ? String(stats.completed) : na}
          </div>
        </div>

        {/* Failed */}
        <div style={cardStyle}>
          <div style={labelStyle}>{t('daliWidget.failed')}</div>
          <div style={{ ...valueStyle, color: stats?.failed ? 'var(--danger)' : 'var(--t1)' }}>
            {stats ? String(stats.failed) : na}
          </div>
        </div>

        {/* Total atoms */}
        <div style={cardStyle}>
          <div style={labelStyle}>{t('daliWidget.totalAtoms')}</div>
          <div style={valueStyle}>
            {stats ? stats.totalAtoms.toLocaleString() : na}
          </div>
        </div>

        {/* Avg resolution */}
        <div style={cardStyle}>
          <div style={labelStyle}>{t('daliWidget.avgResolution')}</div>
          <div style={{ ...valueStyle, color: resColor(stats?.avgResolution ?? null) }}>
            {stats?.avgResolution != null
              ? `${stats.avgResolution.toFixed(1)}%`
              : na}
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Styles ───────────────────────────────────────────────────────────────────

const cardStyle: React.CSSProperties = {
  background:   'var(--bg2)',
  border:       '1px solid var(--bd)',
  borderRadius: 'var(--seer-radius-md)',
  padding:      'var(--seer-space-3) var(--seer-space-4)',
  minWidth:     '110px',
};

const labelStyle: React.CSSProperties = {
  fontSize:      '11px',
  color:         'var(--t3)',
  marginBottom:  '4px',
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
};

const valueStyle: React.CSSProperties = {
  fontSize:    '20px',
  fontWeight:  600,
  color:       'var(--t1)',
  fontFamily:  'var(--mono)',
};
