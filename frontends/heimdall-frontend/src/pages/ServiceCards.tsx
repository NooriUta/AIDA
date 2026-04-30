import { useTranslation } from 'react-i18next';
import { type ClusterHealth } from '../hooks/useDatabases';
import {
  type Health, type Card,
  CARD_BASE, healthColor, latencyColor, healthWord,
} from './servicesPageTypes';

// ── statusPillStyles (private) ────────────────────────────────────────────────
function statusPillStyles(h: Health, latencyMs: number | null) {
  const c = healthColor(h);
  return {
    word: {
      padding:       '2px 6px',
      borderRadius:  3,
      fontSize:      9,
      letterSpacing: '0.08em',
      background:    `color-mix(in srgb, ${c} 18%, transparent)`,
      color:         c,
      fontFamily:    'var(--mono)',
      fontWeight:    600,
    } as React.CSSProperties,
    lat: {
      color:      latencyColor(h === 'up' || h === 'deg' ? latencyMs : null),
      fontSize:   11,
      fontWeight: 600,
      fontFamily: 'var(--mono)',
      marginLeft: 6,
    } as React.CSSProperties,
  };
}

// ── Status pill ───────────────────────────────────────────────────────────────
export function StatusPill({ health, text, latencyMs }: {
  health:     Health;
  text?:      string;
  latencyMs?: number | null;
}) {
  const { t } = useTranslation();
  const s = statusPillStyles(health, latencyMs ?? null);
  return (
    <span style={{ display: 'inline-flex', alignItems: 'baseline', marginTop: 'auto' }}>
      <span style={s.word}>{healthWord(health, t)}</span>
      <span style={s.lat}>{text ?? '—'}</span>
    </span>
  );
}

// ── Docker icon ───────────────────────────────────────────────────────────────
export function DockerIcon({ size = 11 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="#2496ED" aria-hidden="true" style={{ flexShrink: 0 }}>
      <path d="M13.983 11.078h2.119a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.119a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185m-2.954-5.43h2.118a.186.186 0 0 0 .186-.186V3.574a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m0 2.716h2.118a.187.187 0 0 0 .186-.186V6.29a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.887c0 .102.082.186.185.186m-2.93 0h2.12a.186.186 0 0 0 .184-.186V6.29a.185.185 0 0 0-.185-.185H8.1a.185.185 0 0 0-.185.185v1.887c0 .102.083.186.185.186m-2.964 0h2.119a.186.186 0 0 0 .185-.186V6.29a.185.185 0 0 0-.185-.185H5.136a.186.186 0 0 0-.186.185v1.887c0 .102.084.186.186.186m5.893 2.715h2.118a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m-2.93 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.184.185v1.888c0 .102.083.185.185.185m-2.964 0h2.119a.185.185 0 0 0 .185-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.186.186 0 0 0-.186.185v1.888c0 .102.084.185.186.185m-2.92 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185M23.763 9.89c-.065-.051-.672-.51-1.954-.51-.338.001-.676.03-1.01.087-.248-1.7-1.653-2.53-1.716-2.566l-.344-.199-.226.327c-.284.438-.49.922-.612 1.43-.23.97-.09 1.882.403 2.661-.595.332-1.55.413-1.744.42H.751a.751.751 0 0 0-.75.748 11.376 11.376 0 0 0 .692 4.062c.545 1.428 1.355 2.48 2.41 3.124 1.18.723 3.1 1.137 5.275 1.137.983.003 1.963-.086 2.93-.266a12.248 12.248 0 0 0 3.823-1.389c.98-.567 1.86-1.288 2.61-2.136 1.252-1.418 1.998-2.997 2.553-4.4h.221c1.372 0 2.215-.549 2.68-1.009.309-.293.55-.65.707-1.046l.098-.288Z"/>
    </svg>
  );
}

// ── Service tile ──────────────────────────────────────────────────────────────
export function ServiceTile({ card, dashed = false, docker = false }: {
  card:    Card;
  dashed?: boolean;
  docker?: boolean;
}) {
  const border = healthColor(card.health);
  return (
    <div
      role="button"
      onClick={card.onOpen}
      style={{
        ...CARD_BASE,
        borderLeftColor: border,
        borderStyle:     dashed ? 'dashed' : 'solid',
        borderLeftStyle: 'solid',
      }}
    >
      <span style={{
        position: 'absolute', top: 10, right: 10,
        width: 7, height: 7, borderRadius: '50%',
        background: border,
        boxShadow: card.health === 'up' || card.health === 'deg'
          ? `0 0 0 3px color-mix(in srgb, ${border} 18%, transparent)`
          : 'none',
      }} />
      <div style={{
        fontWeight: 600, color: 'var(--t1)', fontSize: 13,
        display: 'flex', alignItems: 'center', gap: 5,
      }}>
        {docker && <DockerIcon />}
        <span>{card.name}</span>
      </div>
      <div style={{ color: 'var(--t3)', fontSize: 11, fontFamily: 'var(--mono)' }}>{card.meta}</div>
      <StatusPill health={card.health} text={card.latency} latencyMs={card.latencyMs} />
    </div>
  );
}

// ── formatBytes (private) ─────────────────────────────────────────────────────
function formatBytes(b: number | null): string {
  if (b == null) return '—';
  if (b < 1024) return `${b} B`;
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
  if (b < 1024 * 1024 * 1024) return `${(b / 1024 / 1024).toFixed(1)} MB`;
  return `${(b / 1024 / 1024 / 1024).toFixed(2)} GB`;
}

// ── Metric cell (private) ─────────────────────────────────────────────────────
function MetricCell({ label, value }: { label: string; value: string }) {
  return (
    <div style={{
      background: 'var(--bg2)', padding: '3px 5px',
      borderRadius: 3, textAlign: 'center',
    }}>
      <div style={{ color: 'var(--t3)', fontSize: 9, letterSpacing: '0.06em' }}>{label}</div>
      <div style={{ color: 'var(--t1)', fontWeight: 600 }}>{value}</div>
    </div>
  );
}

// ── Cluster tile ──────────────────────────────────────────────────────────────
/** Live cluster tile — driven by chur `/heimdall/databases` (probeCluster). */
export function ClusterTile({ cluster }: { cluster: ClusterHealth }) {
  const health: Health = cluster.health === 'up' ? 'up' : 'down';
  const border = healthColor(health);
  const m = cluster.metrics;
  return (
    <div style={{
      ...CARD_BASE,
      padding: '8px 10px 10px',
      minHeight: 'unset',
      borderLeftColor: border,
    }}>
      <span style={{
        position: 'absolute', top: 10, right: 10,
        width: 7, height: 7, borderRadius: '50%',
        background: border,
        boxShadow: health === 'up'
          ? `0 0 0 3px color-mix(in srgb, ${border} 18%, transparent)` : 'none',
      }} />
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
        <span style={{ fontWeight: 600, color: 'var(--t1)', fontSize: 13 }}>{cluster.id}</span>
        <span style={{ color: 'var(--t3)', fontSize: 11, fontFamily: 'var(--mono)' }}>:{cluster.port}</span>
        {cluster.version && (
          <span
            title={cluster.version}
            style={{ color: 'var(--t3)', fontSize: 10, fontFamily: 'var(--mono)' }}>
            v{cluster.version.split(' ')[0]}
          </span>
        )}
        <span style={{
          fontSize: 10, color: latencyColor(cluster.latencyMs),
          fontFamily: 'var(--mono)', marginLeft: 'auto', fontWeight: 600,
        }}>{cluster.latencyMs != null ? `${cluster.latencyMs} ms` : '—'}</span>
      </div>

      {/* Metrics row (profiler) */}
      {m && (
        <div style={{
          marginTop: 6, display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)', gap: 6,
          fontSize: 10, fontFamily: 'var(--mono)',
        }}>
          <MetricCell label="cache" value={m.cacheHitPct != null ? `${m.cacheHitPct}%` : '—'} />
          <MetricCell label="q/min" value={m.queriesPerMin != null ? String(m.queriesPerMin) : '—'} />
          <MetricCell label="wal"   value={formatBytes(m.walBytesWritten)} />
          <MetricCell label="files" value={m.openFiles != null ? String(m.openFiles) : '—'} />
        </div>
      )}

      {/* Databases */}
      <div style={{
        marginTop: 6, borderTop: '1px solid var(--bd)', paddingTop: 6,
        display: 'flex', flexDirection: 'column', gap: 3,
      }}>
        {cluster.dbs.length === 0 && (
          <div style={{ color: 'var(--t3)', fontSize: 11, fontStyle: 'italic' }}>
            {cluster.error ?? 'no databases'}
          </div>
        )}
        {cluster.dbs.map(db => (
          <div key={db} style={{
            display: 'flex', alignItems: 'baseline', gap: 8,
            fontSize: 11, fontFamily: 'var(--mono)',
          }}>
            <span style={{
              width: 6, height: 6, borderRadius: '50%',
              background: healthColor(health),
              display: 'inline-block', flexShrink: 0,
            }} />
            <span style={{ color: 'var(--t1)', fontWeight: 500 }}>{db}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Section header ────────────────────────────────────────────────────────────
export function SectionHeader({ title, count, collapsed, onToggle }: {
  title:     string;
  count?:    string;
  collapsed: boolean;
  onToggle:  () => void;
}) {
  return (
    <div
      onClick={onToggle}
      style={{
        display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer',
        userSelect: 'none', marginBottom: 10,
      }}
    >
      <span style={{
        color: 'var(--t3)', fontSize: 10,
        transform: collapsed ? 'rotate(-90deg)' : 'none', transition: 'transform .12s',
      }}>▼</span>
      <span style={{
        fontSize: 12, fontWeight: 600, letterSpacing: '0.08em',
        textTransform: 'uppercase', color: 'var(--t2)',
      }}>{title}</span>
      {count && <span style={{ color: 'var(--t3)', fontSize: 11 }}>{count}</span>}
    </div>
  );
}
