import {
  LATENCY_GOOD_MAX, LATENCY_WARN_MAX,
} from '../config/services';

// ── Types ─────────────────────────────────────────────────────────────────────
export type Health = 'up' | 'deg' | 'down' | 'idle';

export interface RawService {
  name:      string;
  port:      number;
  mode:      'dev' | 'docker';
  status:    'up' | 'degraded' | 'down' | 'self';
  latencyMs: number;
  /** Only populated when peer Quarkus service exposes /q/info/build. */
  version?:  string | null;
}

export interface Card {
  id:         string;
  name:       string;
  meta:       string;
  health:     Health;
  latency?:   string;
  latencyMs?: number | null;
  tenant?:    string;
  onOpen?:    () => void;
}

export const POLL_MS = 10_000;

// ── Helpers ───────────────────────────────────────────────────────────────────
export function mapStatus(s: RawService['status']): Health {
  if (s === 'up' || s === 'self') return 'up';
  if (s === 'degraded')            return 'deg';
  return 'down';
}

export function healthColor(h: Health): string {
  return h === 'up'   ? 'var(--suc)'
       : h === 'deg'  ? 'var(--wrn)'
       : h === 'down' ? 'var(--danger)'
       :                'var(--t3)';
}

/** Latency tier color — aligned with alerting thresholds. */
export function latencyColor(ms: number | null): string {
  if (ms == null)                return 'var(--t3)';
  if (ms <= LATENCY_GOOD_MAX)    return 'var(--suc)';
  if (ms <= LATENCY_WARN_MAX)    return 'var(--wrn)';
  return 'var(--danger)';
}

export function healthWord(h: Health, t: (k: string) => string): string {
  return h === 'up'  ? t('services.up')
       : h === 'deg' ? t('services.degraded')
       : h === 'down'? t('services.down')
       :               'IDLE';
}

// ── Shared inline styles ──────────────────────────────────────────────────────
export const CARD_BASE: React.CSSProperties = {
  position:      'relative',
  padding:       '7px 10px 8px',
  background:    'var(--bg1)',
  border:        '1px solid var(--bd)',
  borderLeftWidth: 3,
  borderRadius:  'var(--seer-radius-sm, 4px)',
  cursor:        'pointer',
  display:       'flex',
  flexDirection: 'column',
  gap:           2,
  minHeight:     58,
};

export const GRID: React.CSSProperties = {
  display:             'grid',
  gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
  gap:                 6,
};
