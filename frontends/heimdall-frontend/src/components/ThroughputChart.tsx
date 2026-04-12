import { useMemo } from 'react';
import { ResponsiveLine } from '@nivo/line';
import type { HeimdallEvent } from 'aida-shared';

interface ThroughputChartProps {
  events: HeimdallEvent[];
}

const WINDOW_SEC = 30;

const COMPONENT_COLORS: Record<string, string> = {
  hound:   '#7F77DD',
  dali:    '#1D9E75',
  mimir:   '#BA7517',
  shuttle: '#3B82F6',
};

function getColor(component: string): string {
  const key = component.toLowerCase();
  for (const [k, v] of Object.entries(COMPONENT_COLORS)) {
    if (key.includes(k)) return v;
  }
  return '#888888';
}

export function ThroughputChart({ events }: ThroughputChartProps) {
  const data = useMemo(() => {
    const now        = Date.now();
    const windowStart = now - WINDOW_SEC * 1000;
    const recent     = events.filter(e => e.timestamp >= windowStart);

    // Group by component → bucket by second
    const byComponent: Record<string, Record<number, number>> = {};
    for (const ev of recent) {
      const sec  = Math.floor(ev.timestamp / 1000);
      const comp = ev.sourceComponent ?? 'unknown';
      byComponent[comp] ??= {};
      byComponent[comp][sec] = (byComponent[comp][sec] ?? 0) + 1;
    }

    return Object.entries(byComponent).map(([comp, buckets]) => ({
      id:    comp,
      color: getColor(comp),
      data:  Object.entries(buckets)
        .sort(([a], [b]) => Number(a) - Number(b))
        .map(([sec, count]) => ({
          x: new Date(Number(sec) * 1000).toISOString().substring(11, 19),
          y: count,
        })),
    }));
  }, [events]);

  if (data.length === 0) {
    return (
      <div style={{ height: '180px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--t3)', fontSize: '13px' }}>
        Waiting for events…
      </div>
    );
  }

  return (
    <div style={{ height: '180px', padding: '0 var(--seer-space-6)' }}>
      <ResponsiveLine
        data={data}
        colors={d => d.color as string}
        margin={{ top: 10, right: 20, bottom: 30, left: 40 }}
        xScale={{ type: 'point' }}
        yScale={{ type: 'linear', min: 0, stacked: false }}
        axisBottom={{ tickSize: 0, tickPadding: 8, tickRotation: -30 }}
        axisLeft={{ tickSize: 0, tickPadding: 8 }}
        enablePoints={false}
        enableGridX={false}
        gridYValues={4}
        animate
        motionConfig="gentle"
        theme={{
          background: 'transparent',
          text:       { fill: 'var(--t3)', fontSize: 11 },
          grid:       { line: { stroke: 'var(--bd)', strokeWidth: 1 } },
          axis:       { ticks: { text: { fill: 'var(--t3)' } } },
        }}
      />
    </div>
  );
}
