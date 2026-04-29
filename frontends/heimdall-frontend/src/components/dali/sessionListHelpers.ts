import type { DaliSession } from '../../api/dali';

export const TERMINAL = new Set<string>(['COMPLETED', 'FAILED', 'CANCELLED']);

export const DIALECT_LABEL: Record<string, string> = {
  plsql:      'PL/SQL',
  postgresql: 'PostgreSQL',
  clickhouse: 'ClickHouse',
};

export function fmtDuration(ms: number | null | undefined): string {
  if (ms == null) return '—';
  if (ms < 1000) return `${ms}ms`;
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)}s`;
  const m = Math.floor(ms / 60_000);
  const s = Math.floor((ms % 60_000) / 1000);
  return `${m}m ${s}s`;
}

export function fmtDatetime(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '—';
  return d.toLocaleString(undefined, {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    hour12: false,
  });
}

export function fmtDateShort(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '—';
  return d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false });
}

export function truncSource(path: string | null, max = 40): string {
  if (!path) return '—';
  if (path.length <= max) return path;
  const filename = path.replace(/\\/g, '/').split('/').pop() ?? path;
  return filename.length >= max - 3 ? `…${filename.slice(-(max - 3))}` : `…/${filename}`;
}

export function rateColor(rate: number | null): string {
  if (rate == null) return 'var(--t3)';
  if (rate >= 0.85) return 'var(--suc)';
  if (rate >= 0.70) return 'var(--wrn)';
  return 'var(--danger)';
}

export function fmtK(n: number): string {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
  if (n >= 1_000)     return `${(n / 1_000).toFixed(1)}k`;
  return n.toLocaleString();
}

export function deriveSessionName(session: DaliSession): string {
  const src = session.source ?? '';
  const time = session.startedAt
    ? new Date(session.startedAt).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', hour12: false })
    : '';
  const timePart = time ? ` · ${time}` : '';

  if (session.batch) {
    const filename = src.replace(/\\/g, '/').split('/').pop() ?? src;
    const base = filename || src || 'batch';
    return `${base} (${session.total > 0 ? session.total : '…'})${timePart}`;
  }
  if (src.startsWith('jdbc:')) {
    const label = src.slice(0, 28) + (src.length > 28 ? '…' : '');
    return `${label}${timePart}`;
  }
  const filename = src.replace(/\\/g, '/').split('/').pop() ?? src;
  return `${filename || src || '—'}${timePart}`;
}
