import { useEffect }          from 'react';
import type { HeimdallEvent, MetricsSnapshot } from 'aida-shared';
import { EVENT_LABELS }       from '../utils/eventFormat';

interface PresentationModeProps {
  events:  HeimdallEvent[];
  metrics: MetricsSnapshot | null;
  onExit:  () => void;
}

const COMP_COLORS: Record<string, string> = {
  hound:   'var(--suc)',
  shuttle: 'var(--wrn)',
  dali:    'var(--acc)',
  mimir:   '#a09ade',
  anvil:   'var(--inf)',
  chur:    'var(--t2)',
};

function compColor(comp: string): string {
  return COMP_COLORS[comp.toLowerCase()] ?? 'var(--t3)';
}

function resColor(rate: number | undefined): string {
  if (rate === undefined || isNaN(rate)) return 'var(--t3)';
  if (rate >= 85) return 'var(--suc)';
  if (rate >= 70) return 'var(--wrn)';
  return 'var(--danger)';
}

function formatTime(ts: number): string {
  return new Date(ts).toISOString().substring(11, 23);
}

export function PresentationMode({ events, metrics, onExit }: PresentationModeProps) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onExit(); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onExit]);

  const last8 = events.slice(-8);
  const rate  = metrics?.resolutionRate;

  return (
    <div
      onClick={onExit}
      style={{
        position:  'fixed',
        inset:     0,
        zIndex:    1000,
        background: 'var(--bg0)',
        display:   'flex',
        flexDirection: 'column',
        padding:   '48px 64px',
        gap:       '40px',
        overflow:  'hidden',
      }}
    >
      {/* ESC hint */}
      <div style={{ position: 'absolute', top: 16, right: 24, fontSize: '11px', color: 'var(--t3)', fontFamily: 'var(--mono)' }}>
        ESC / click to exit
      </div>

      {/* Title */}
      <div style={{ fontFamily: 'var(--font-display)', fontSize: '13px', letterSpacing: '0.1em', color: 'var(--aida-app-heimdall)', textTransform: 'uppercase' }}>
        Heimðallr · LIVE
      </div>

      {/* Big 3 metrics */}
      <div style={{ display: 'flex', gap: '64px', alignItems: 'flex-end' }}>
        <div>
          <div style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 8 }}>
            Atoms extracted
          </div>
          <div style={{ fontSize: '72px', fontFamily: 'var(--mono)', fontWeight: 700, color: 'var(--t1)', lineHeight: 1 }}>
            {metrics?.atomsExtracted?.toLocaleString() ?? '—'}
          </div>
        </div>
        <div>
          <div style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 8 }}>
            Resolution rate
          </div>
          <div style={{ fontSize: '72px', fontFamily: 'var(--mono)', fontWeight: 700, color: resColor(rate), lineHeight: 1 }}>
            {rate !== undefined && !isNaN(rate) ? `${rate.toFixed(1)}%` : '—'}
          </div>
        </div>
        <div>
          <div style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 8 }}>
            Files parsed
          </div>
          <div style={{ fontSize: '72px', fontFamily: 'var(--mono)', fontWeight: 700, color: 'var(--t1)', lineHeight: 1 }}>
            {metrics?.filesParsed?.toLocaleString() ?? '—'}
          </div>
        </div>
      </div>

      {/* Last 8 events (fading) */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end', gap: 6 }}>
        {last8.map((e, i) => {
          const opacity = 0.1 + (i / (last8.length - 1 || 1)) * 0.9;
          const comp    = (e.sourceComponent ?? '').toLowerCase();
          return (
            <div key={i} style={{
              display:    'flex',
              gap:        '20px',
              alignItems: 'center',
              opacity,
              fontSize:   '13px',
              fontFamily: 'var(--mono)',
            }}>
              <span style={{ color: 'var(--t3)', width: 100, flexShrink: 0 }}>{formatTime(e.timestamp)}</span>
              <span style={{ color: compColor(comp), width: 80, flexShrink: 0 }}>{e.sourceComponent}</span>
              <span style={{ color: 'var(--t2)' }}>{EVENT_LABELS[e.eventType] ?? e.eventType}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
