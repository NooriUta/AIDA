import { ResponsivePie } from '@nivo/pie';
import { useTranslation } from 'react-i18next';

interface ResolutionGaugeProps {
  rate: number; // percentage 0-100, NaN if no data
}

function gaugeColor(rate: number): string {
  if (Number.isNaN(rate)) return '#444444';
  if (rate >= 85) return '#3fb950';
  if (rate >= 70) return '#d29922';
  return '#f85149';
}

export function ResolutionGauge({ rate }: ResolutionGaugeProps) {
  const { t } = useTranslation();
  const valid = !Number.isNaN(rate);
  const pct   = valid ? Math.min(100, Math.max(0, rate)) : 0;
  const color = gaugeColor(rate);

  const chartData = [
    { id: 'fill',  value: pct,       color },
    { id: 'empty', value: 100 - pct, color: 'var(--bg3)' },
  ];

  return (
    <div style={{ position: 'relative', width: '160px', height: '160px' }}>
      <ResponsivePie
        data={chartData}
        colors={d => d.data.color}
        innerRadius={0.72}
        startAngle={-90}
        endAngle={270}
        enableArcLabels={false}
        enableArcLinkLabels={false}
        isInteractive={false}
        animate
        motionConfig="gentle"
        theme={{ background: 'transparent' }}
      />
      {/* Centre overlay */}
      <div style={{
        position:       'absolute',
        inset:          0,
        display:        'flex',
        flexDirection:  'column',
        alignItems:     'center',
        justifyContent: 'center',
        pointerEvents:  'none',
      }}>
        <span style={{ fontSize: '22px', fontWeight: 700, fontFamily: 'var(--mono)', color }}>
          {valid ? `${pct.toFixed(1)}%` : t('metrics.na')}
        </span>
        <span style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
          {t('metrics.resolution')}
        </span>
      </div>
    </div>
  );
}
