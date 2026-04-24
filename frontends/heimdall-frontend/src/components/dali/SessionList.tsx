import { useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation }              from 'react-i18next';
import { type DaliSession, type FileResult, cancelSession } from '../../api/dali';
import { useDaliSession } from '../../hooks/useDaliSession';
import { useIsMobile }    from '../../hooks/useIsMobile';
import css from './dali.module.css';

const TERMINAL = new Set<string>(['COMPLETED', 'FAILED', 'CANCELLED']);

const DIALECT_LABEL: Record<string, string> = {
  plsql:      'PL/SQL',
  postgresql: 'PostgreSQL',
  clickhouse: 'ClickHouse',
};

function fmtDuration(ms: number | null | undefined): string {
  if (ms == null) return '—';
  if (ms < 1000) return `${ms}ms`;
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)}s`;
  const m = Math.floor(ms / 60_000);
  const s = Math.floor((ms % 60_000) / 1000);
  return `${m}m ${s}s`;
}

function fmtDatetime(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '—';
  return d.toLocaleString(undefined, {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    hour12: false,
  });
}

function fmtDateShort(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '—';
  return d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false });
}

function truncSource(path: string | null, max = 40): string {
  if (!path) return '—';
  if (path.length <= max) return path;
  const filename = path.replace(/\\/g, '/').split('/').pop() ?? path;
  return filename.length >= max - 3 ? `…${filename.slice(-(max - 3))}` : `…/${filename}`;
}

function rateColor(rate: number | null): string {
  if (rate == null) return 'var(--t3)';
  if (rate >= 0.85) return 'var(--suc)';
  if (rate >= 0.70) return 'var(--wrn)';
  return 'var(--danger)';
}

function fmtK(n: number): string {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
  if (n >= 1_000)     return `${(n / 1_000).toFixed(1)}k`;
  return n.toLocaleString();
}

// ── Session name derivation (Блок 1) ─────────────────────────────────────────
// Derives a human-readable session name from the source path + start time.
// Examples: "LOG_PKG.SQL · 14:32",  "DWH batch (322) · 13:15",  "jdbc:pg:// · 09:00"
function deriveSessionName(session: DaliSession): string {
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

// ── Status badge ──────────────────────────────────────────────────────────────
function StatusBadge({ status }: { status: DaliSession['status'] }) {
  const badgeCls = {
    QUEUED:      css.badgeQueued,
    RUNNING:     css.badgeRunning,
    CANCELLING:  css.badgeCancelling,
    COMPLETED:   css.badgeCompleted,
    FAILED:      css.badgeFailed,
    CANCELLED:   css.badgeCancelled,
  }[status] ?? css.badgeQueued;

  return (
    <span className={`badge ${badgeCls}`}>
      {status === 'RUNNING' && <span className={css.pulse} />}
      {status}
    </span>
  );
}

// ── Progress bar ──────────────────────────────────────────────────────────────
function ProgressBar({ session }: { session: DaliSession }) {
  const { status, progress, total } = session;

  if (status === 'COMPLETED') {
    return (
      <div className={css.prog}>
        <div className={css.progFill} style={{ width: '100%', background: 'var(--suc)' }} />
      </div>
    );
  }
  if (status === 'FAILED') {
    // Batch with partial progress: show how far it got in red
    if (total > 1) {
      const pct = total > 0 ? Math.round((progress / total) * 100) : 0;
      return (
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div className={css.prog} style={{ flex: 1 }}>
            <div className={css.progFill} style={{ width: `${pct}%`, background: 'var(--danger)' }} />
          </div>
          <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--danger)', whiteSpace: 'nowrap' }}>
            {progress}/{total}
          </span>
        </div>
      );
    }
    return (
      <div className={css.prog}>
        <div className={css.progFill} style={{ width: '100%', background: 'var(--danger)' }} />
      </div>
    );
  }
  if (status === 'RUNNING' && total > 1) {
    const pct = total > 0 ? Math.round((progress / total) * 100) : 0;
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <div className={css.prog} style={{ flex: 1 }}>
          <div className={css.progFill} style={{ width: `${pct}%`, background: 'var(--inf)' }} />
        </div>
        <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)', whiteSpace: 'nowrap' }}>
          {progress}/{total}
        </span>
      </div>
    );
  }
  if (status === 'RUNNING') {
    return (
      <div className={`${css.prog} ${css.progInd}`}>
        <div className={css.progFill} style={{ background: 'var(--inf)' }} />
      </div>
    );
  }
  return (
    <div className={css.prog}>
      <div className={css.progFill} style={{ width: '0' }} />
    </div>
  );
}

// ── Source cell: path + BATCH / FILE tag + error preview (Блок 2) ────────────
function SourceCell({ session, tenantAlias }: { session: DaliSession; tenantAlias?: string }) {
  const errors = session.errors ?? [];
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <span
          style={{ fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t2)' }}
          title={session.source ?? ''}
        >
          {truncSource(session.source)}
        </span>
        {session.batch ? (
          <span style={{
            fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 600,
            padding: '1px 5px', borderRadius: 3,
            background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
            color: 'var(--acc)',
            border: '1px solid color-mix(in srgb, var(--acc) 28%, transparent)',
            whiteSpace: 'nowrap',
          }}>
            BATCH{session.total > 0 ? ` ${session.total}` : ''}
          </span>
        ) : (
          <span style={{
            fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 600,
            padding: '1px 5px', borderRadius: 3,
            background: 'var(--bg3)', color: 'var(--t3)',
            border: '1px solid var(--bd)', whiteSpace: 'nowrap',
          }}>
            FILE
          </span>
        )}
      </div>
      {session.dbName && (
        <span style={{
          fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 600,
          color: 'var(--suc)', letterSpacing: '0.04em',
        }}
          title={`Database: ${session.dbName}`}
        >
          DB: {session.dbName}
        </span>
      )}
      {/* Error preview for FAILED sessions (Блок 2) */}
      {session.status === 'FAILED' && errors.length > 0 && (
        <div
          style={{
            color: 'var(--danger)', fontSize: 10, fontFamily: 'var(--mono)',
            overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
            maxWidth: 260, marginTop: 1,
          }}
          title={errors[0]}
        >
          ✗ {errors[0].slice(0, 80)}
        </div>
      )}

      {/* Tenant + clear-before-write — always shown so operator sees write context */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginTop: 1 }}>
        {tenantAlias && (
          <span style={{
            fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 700,
            padding: '1px 5px', borderRadius: 3,
            background: 'color-mix(in srgb, var(--inf) 12%, transparent)',
            color: 'var(--inf)',
            border: '1px solid color-mix(in srgb, var(--inf) 28%, transparent)',
            whiteSpace: 'nowrap',
          }}
            title={`Writing to tenant: ${tenantAlias}`}
          >
            → {tenantAlias}
          </span>
        )}
        <span style={{
          fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 700,
          padding: '1px 5px', borderRadius: 3,
          background: session.clearBeforeWrite
            ? 'color-mix(in srgb, var(--danger) 12%, transparent)'
            : 'color-mix(in srgb, var(--suc) 10%, transparent)',
          color: session.clearBeforeWrite ? 'var(--danger)' : 'var(--suc)',
          border: `1px solid ${session.clearBeforeWrite
            ? 'color-mix(in srgb, var(--danger) 28%, transparent)'
            : 'color-mix(in srgb, var(--suc) 22%, transparent)'}`,
          whiteSpace: 'nowrap',
        }}
          title={session.clearBeforeWrite ? 'YGG was cleared before this write' : 'YGG was NOT cleared — append mode'}
        >
          {session.clearBeforeWrite ? '↺ cleared' : '+ append'}
        </span>
      </div>
    </div>
  );
}

// ── Row mini-metrics (shown inline in the session row) ───────────────────────
function RowMetrics({ session: s }: { session: DaliSession }) {
  const partialAtoms = s.fileResults?.reduce((a, f) => a + f.atomCount, 0) ?? 0;
  const partialVtxs  = s.fileResults?.reduce((a, f) => a + f.vertexCount, 0) ?? 0;
  const atoms = s.atomCount    ?? (partialAtoms > 0 ? partialAtoms : null);
  const vtxs  = s.vertexCount  ?? (partialVtxs  > 0 ? partialVtxs  : null);
  const rate  = s.resolutionRate;
  if (atoms == null && vtxs == null) return null;
  return (
    <div style={{ display: 'flex', gap: 6, alignItems: 'center', marginTop: 3, flexWrap: 'nowrap' }}>
      {atoms != null && (
        <span title={`${atoms.toLocaleString()} atoms`} style={{
          fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--acc)', whiteSpace: 'nowrap',
        }}>A {fmtK(atoms)}</span>
      )}
      {vtxs != null && (
        <span title={`${vtxs.toLocaleString()} vertices`} style={{
          fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t2)', whiteSpace: 'nowrap',
        }}>V {fmtK(vtxs)}</span>
      )}
      <span title="Column-level resolution rate" style={{
        fontFamily: 'var(--mono)', fontSize: 9,
        color: rate != null ? rateColor(rate) : 'var(--t3)',
        whiteSpace: 'nowrap',
      }}>
        {rate != null ? `${(rate * 100).toFixed(0)}%` : '—%'}
      </span>
    </div>
  );
}

// ── Duration timeline (parse order) ──────────────────────────────────────────
function DurationTimeline({ files }: { files: FileResult[] }) {
  const { t } = useTranslation();
  const [hovered, setHovered] = useState<number | null>(null);
  const withDur = files.filter(f => f.durationMs > 0);
  if (withDur.length < 3) return null;

  const W = 800, H = 80, PX = 10, PY = 8;
  const maxMs = Math.max(...withDur.map(f => f.durationMs));
  const n = withDur.length;

  const x = (i: number) => PX + (i / Math.max(n - 1, 1)) * (W - PX * 2);
  const y = (ms: number) => PY + (1 - ms / maxMs) * (H - PY * 2);

  const polyline = withDur.map((f, i) => `${x(i)},${y(f.durationMs)}`).join(' ');
  const area = `${x(0)},${H} ` + withDur.map((f, i) => `${x(i)},${y(f.durationMs)}`).join(' ') + ` ${x(n - 1)},${H}`;

  const hov = hovered !== null ? withDur[hovered] : null;
  const hovFilename = hov ? hov.path.replace(/\\/g, '/').split('/').pop() : '';

  return (
    <div style={{ marginTop: 12, marginBottom: 2, marginLeft: -14, marginRight: -14 }}>
      <div style={{
        fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--t3)',
        textTransform: 'uppercase', letterSpacing: '0.07em', marginBottom: 6,
        display: 'flex', justifyContent: 'space-between',
        paddingLeft: 14, paddingRight: 14,
      }}>
        <span>{t('dali.sessions.parseTimeline', { count: n })}</span>
        {hov && (
          <span style={{ color: hov.success ? 'var(--t2)' : 'var(--danger)', maxWidth: 380, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {hovFilename} · {fmtDuration(hov.durationMs)}
          </span>
        )}
      </div>
      <svg
        viewBox={`0 0 ${W} ${H}`}
        style={{ width: '100%', height: H, display: 'block', overflow: 'visible' }}
        onMouseLeave={() => setHovered(null)}
      >
        {/* area fill */}
        <polygon
          points={area}
          fill="color-mix(in srgb, var(--inf) 8%, transparent)"
        />
        {/* line */}
        <polyline
          points={polyline}
          fill="none"
          stroke="var(--inf)"
          strokeWidth="1.5"
          strokeLinejoin="round"
          strokeLinecap="round"
          opacity="0.6"
        />
        {/* dots */}
        {withDur.map((f, i) => {
          const cx = x(i), cy = y(f.durationMs);
          const isHov = hovered === i;
          const color = !f.success ? 'var(--danger)' : f.durationMs > maxMs * 0.6 ? 'var(--wrn)' : 'var(--inf)';
          return (
            <circle
              key={f.path}
              cx={cx} cy={cy}
              r={isHov ? 4 : 2.5}
              fill={color}
              opacity={isHov ? 1 : 0.75}
              style={{ cursor: 'default', transition: 'r 0.1s' }}
              onMouseEnter={() => setHovered(i)}
            />
          );
        })}
        {/* hover vertical line */}
        {hovered !== null && (
          <line
            x1={x(hovered)} y1={PY} x2={x(hovered)} y2={H - PY + 2}
            stroke="var(--t3)" strokeWidth="1" strokeDasharray="3 2"
          />
        )}
      </svg>
    </div>
  );
}

// ── Duration bar chart for batch detail ──────────────────────────────────────
function DurationChart({ files }: { files: FileResult[] }) {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const withDur = files.filter(f => f.durationMs > 0);
  if (withDur.length < 2) return null;
  const sorted  = [...withDur].sort((a, b) => b.durationMs - a.durationMs).slice(0, 25);
  const maxDur  = sorted[0].durationMs;
  const totalMs = withDur.reduce((s, f) => s + f.durationMs, 0);
  return (
    <div style={{ marginTop: 14 }}>
      <div
        onClick={() => setOpen(o => !o)}
        style={{
          fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--t3)',
          textTransform: 'uppercase', letterSpacing: '0.07em',
          marginBottom: open ? 8 : 0,
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          cursor: 'pointer', userSelect: 'none',
        }}
      >
        <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
          <span style={{ fontSize: 8, opacity: 0.6 }}>{open ? '▼' : '▶'}</span>
          {withDur.length > 25
            ? t('dali.sessions.fileBreakdownTitleTop')
            : t('dali.sessions.fileBreakdownTitle')}
        </span>
        <span style={{ color: 'var(--t3)' }}>
          {t('dali.sessions.fileBreakdownTotal', { dur: fmtDuration(totalMs) })}
        </span>
      </div>
      {open && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {sorted.map(f => {
            const pct      = maxDur > 0 ? (f.durationMs / maxDur) * 100 : 0;
            const filename = f.path.replace(/\\/g, '/').split('/').pop() ?? f.path;
            const barColor = !f.success ? 'var(--danger)' : f.durationMs > totalMs / withDur.length * 2 ? 'var(--wrn)' : 'var(--inf)';
            return (
              <div key={f.path} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <div
                  title={f.path}
                  style={{
                    width: 180, fontSize: 9, fontFamily: 'var(--mono)',
                    color: f.success ? 'var(--t2)' : 'var(--danger)',
                    overflow: 'hidden', textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap', flexShrink: 0,
                  }}
                >{filename}</div>
                <div style={{
                  flex: 1, background: 'var(--bg3)', borderRadius: 2,
                  height: 8, overflow: 'hidden',
                }}>
                  <div style={{
                    width: `${pct}%`, height: '100%',
                    background: barColor, borderRadius: 2,
                    transition: 'width 0.2s',
                  }} />
                </div>
                <div style={{
                  width: 38, fontSize: 9, fontFamily: 'var(--mono)',
                  color: 'var(--t3)', textAlign: 'right', flexShrink: 0,
                }}>{fmtDuration(f.durationMs)}</div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

// ── Per-file row in detail ────────────────────────────────────────────────────
function FileRow({ fr, index }: { fr: FileResult; index: number }) {
  const filename = fr.path.replace(/\\/g, '/').split('/').pop() ?? fr.path;
  const [open, setOpen] = useState(false);

  return (
    <>
      <tr
        style={{ cursor: fr.warnings.length > 0 ? 'pointer' : 'default' }}
        onClick={() => fr.warnings.length > 0 && setOpen(o => !o)}
      >
        <td style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)', padding: '5px 8px', width: 24, textAlign: 'right' }}>
          {index + 1}
        </td>
        <td style={{ padding: '5px 8px', maxWidth: 280 }}>
          <span style={{ fontFamily: 'var(--mono)', fontSize: 11, color: fr.success ? 'var(--t2)' : 'var(--danger)' }} title={fr.path}>
            {filename}
          </span>
        </td>
        <td style={{ fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--acc)', padding: '5px 8px', textAlign: 'right' }}>
          {fr.atomCount.toLocaleString()}
        </td>
        <td style={{ fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t2)', padding: '5px 8px', textAlign: 'right' }}>
          {fr.vertexCount.toLocaleString()}
        </td>
        <td style={{ padding: '4px 8px', textAlign: 'right' }}>
          {fr.atomCount === 0 ? (
            <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)' }}>—</span>
          ) : fr.atomsResolved !== undefined ? (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 1 }}>
              <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--suc)' }}>
                ✓ {fr.atomsResolved.toLocaleString()} ({fr.atomCount > 0 ? ((fr.atomsResolved / fr.atomCount) * 100).toFixed(0) : 0}%)
              </span>
              {fr.atomsUnresolved! > 0 && (
                <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--danger)' }}>
                  ✗ {fr.atomsUnresolved!.toLocaleString()} ({((fr.atomsUnresolved! / fr.atomCount) * 100).toFixed(0)}%)
                </span>
              )}
              {(() => {
                const pending = fr.atomCount - fr.atomsResolved - (fr.atomsUnresolved ?? 0);
                return pending > 0 ? (
                  <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)' }}>
                    … {pending.toLocaleString()}
                  </span>
                ) : null;
              })()}
            </div>
          ) : (
            <span style={{ fontFamily: 'var(--mono)', fontSize: 11, color: rateColor(fr.resolutionRate) }}>
              {(fr.resolutionRate * 100).toFixed(1)}%
            </span>
          )}
        </td>
        <td style={{ fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t3)', padding: '5px 8px', textAlign: 'right', whiteSpace: 'nowrap' }}>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
            {/* fixed-width icon slot so duration numbers stay aligned */}
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 3, width: 28, justifyContent: 'flex-end' }}>
              {fr.warnings.length > 0 && (
                <span style={{ fontSize: 9, color: 'var(--wrn)' }}>⚠{fr.warnings.length}</span>
              )}
              {!fr.success && (
                <span style={{ fontSize: 10, color: 'var(--danger)' }}>✗</span>
              )}
            </span>
            <span style={{ minWidth: 38, textAlign: 'right' }}>{fmtDuration(fr.durationMs)}</span>
          </span>
        </td>
      </tr>
      {open && fr.warnings.map((w, i) => (
        <tr key={i}>
          <td colSpan={20} style={{ padding: '2px 8px 2px 32px' }}>
            <div className={css.warnItem}>{w}</div>
          </td>
        </tr>
      ))}
    </>
  );
}

// ── Detail expand row ─────────────────────────────────────────────────────────
function DetailRow({ session, tenantAlias }: { session: DaliSession; tenantAlias?: string }) {
  const { t } = useTranslation();
  const [warningsOpen, setWarningsOpen] = useState(false);
  const [errorsOpen,   setErrorsOpen]   = useState(true);   // errors open by default
  const [vertexOpen,   setVertexOpen]   = useState(false);
  const ws = session.warnings ?? [];
  const es = session.errors   ?? [];
  const hasFiles = session.fileResults && session.fileResults.length > 0;
  const isFailed = session.status === 'FAILED';

  // For FAILED batch: compute partial stats from file results
  const partialAtoms    = hasFiles ? session.fileResults.reduce((a, f) => a + f.atomCount,    0) : null;
  const partialVertices = hasFiles ? session.fileResults.reduce((a, f) => a + f.vertexCount,  0) : null;

  const displayAtoms    = session.atomCount    ?? partialAtoms;
  const displayVertices = session.vertexCount  ?? partialVertices;
  const displayEdges    = session.edgeCount       ?? 0;
  const displayDropped  = session.droppedEdgeCount ?? 0;

  return (
    <tr>
      <td colSpan={20} className={css.detailTd}>
        <div className={css.detailInner}>

          {/* Error banner for FAILED sessions */}
          {isFailed && es.length > 0 && (
            <div style={{ marginBottom: 12 }}>
              <button className={css.warnToggle} style={{ color: 'var(--danger)', borderColor: 'color-mix(in srgb, var(--danger) 30%, transparent)' }} onClick={() => setErrorsOpen(o => !o)}>
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                {t('dali.sessions.errorsBtn', { count: es.length })}
                <svg
                  width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                  className={errorsOpen ? css.chevronOpen : css.chevron}
                >
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </button>
              {errorsOpen && (
                <div className={css.warnList} style={{ borderColor: 'color-mix(in srgb, var(--danger) 25%, transparent)', background: 'color-mix(in srgb, var(--danger) 5%, transparent)' }}>
                  {es.map((e, i) => (
                    <div key={i} className={css.warnItem} style={{ color: 'var(--danger)', fontFamily: 'var(--mono)', fontSize: 11 }}>{e}</div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Aggregate stats strip */}
          <div className={css.detailGrid}>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>{t('dali.sessions.statAtoms')}</div>
              <div className={css.dstatVal} style={{ color: 'var(--acc)' }}>
                {(displayAtoms ?? 0).toLocaleString()}
              </div>
              <div className={css.dstatSub}>
                {isFailed && partialAtoms != null
                  ? t('dali.sessions.statSubPartial')
                  : t('dali.sessions.statSubInYgg')}
              </div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>{t('dali.sessions.statVertices')}</div>
              <div className={css.dstatVal} style={{ color: 'var(--t2)' }}>
                {(displayVertices ?? 0).toLocaleString()}
              </div>
              <div className={css.dstatSub}>
                {isFailed && partialVertices != null
                  ? t('dali.sessions.statSubPartial')
                  : t('dali.sessions.statSubInserted')}
              </div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>{t('dali.sessions.statEdges')}</div>
              <div className={css.dstatVal} style={{ color: 'var(--t2)' }}>
                {displayEdges.toLocaleString()}
              </div>
              <div className={css.dstatSub}>
                {displayDropped > 0
                  ? <span style={{ color: 'var(--wrn)' }}>{t('dali.sessions.statSubDropped', { n: displayDropped.toLocaleString() })}</span>
                  : t('dali.sessions.statSubLineage')}
              </div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>{t('dali.sessions.statResolution')}</div>
              <div className={css.dstatVal} style={{ color: rateColor(session.resolutionRate) }}>
                {session.resolutionRate != null
                  ? `${(session.resolutionRate * 100).toFixed(1)}%`
                  : '—'}
              </div>
              <div className={css.dstatSub}>{t('dali.sessions.statSubColumnLevel')}</div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>{t('dali.sessions.statDuration')}</div>
              <div className={css.dstatVal} style={{ color: isFailed ? 'var(--danger)' : 'var(--t2)' }}>
                {session.batch && session.total > 0 && session.durationMs == null
                  ? `${session.progress}/${session.total}`
                  : fmtDuration(session.durationMs)}
              </div>
              <div className={css.dstatSub}>
                {session.batch && session.total > 0 && session.durationMs == null
                  ? t('dali.sessions.statSubFilesDone')
                  : t('dali.sessions.statSubWallTime')}
              </div>
            </div>
          </div>

          {/* Timing row — start / finish timestamps + tenant + FRIGG persistence (Блок 5) */}
          <div style={{
            display: 'flex', gap: 24, marginTop: 10, marginBottom: 2,
            fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t3)',
            borderTop: '1px solid var(--bd)', paddingTop: 8, flexWrap: 'wrap', alignItems: 'center',
          }}>
            {/* FRIGG persistence badge — moved here from SessionRow (Блок 5) */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
              <span style={{ fontSize: 9, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                {t('dali.sessions.friggInDetail')}
              </span>
              <span
                title={session.friggPersisted ? t('dali.sessions.friggSaved') : t('dali.sessions.friggPending')}
                style={{
                  fontFamily: 'var(--mono)', fontSize: 10, fontWeight: 700,
                  padding: '1px 5px', borderRadius: 3,
                  background: session.friggPersisted
                    ? 'color-mix(in srgb, var(--suc) 14%, transparent)'
                    : 'color-mix(in srgb, var(--wrn) 14%, transparent)',
                  color: session.friggPersisted ? 'var(--suc)' : 'var(--wrn)',
                  border: `1px solid ${session.friggPersisted
                    ? 'color-mix(in srgb, var(--suc) 30%, transparent)'
                    : 'color-mix(in srgb, var(--wrn) 30%, transparent)'}`,
                }}
              >
                {session.friggPersisted ? '✓' : '⏳'}
              </span>
            </div>

            {tenantAlias && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                <span style={{ fontSize: 9, textTransform: 'uppercase', letterSpacing: '0.06em' }}>tenant</span>
                <span style={{
                  fontFamily: 'var(--mono)', fontSize: 10, fontWeight: 700,
                  padding: '1px 6px', borderRadius: 3,
                  background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
                  color: 'var(--acc)',
                  border: '1px solid color-mix(in srgb, var(--acc) 30%, transparent)',
                }}>
                  {tenantAlias}
                </span>
              </div>
            )}
            <div>
              <span style={{ fontSize: 9, textTransform: 'uppercase', letterSpacing: '0.06em', marginRight: 6 }}>{t('dali.sessions.timingStarted')}</span>
              <span title={session.startedAt} style={{ color: 'var(--t2)' }}>
                {fmtDatetime(session.startedAt)}
              </span>
            </div>
            {(session.status === 'COMPLETED' || session.status === 'FAILED' || session.status === 'CANCELLED') && (
              <div>
                <span style={{ fontSize: 9, textTransform: 'uppercase', letterSpacing: '0.06em', marginRight: 6 }}>
                  {session.status === 'COMPLETED' ? t('dali.sessions.timingFinished')
                    : session.status === 'FAILED' ? t('dali.sessions.timingFailed')
                    : t('dali.sessions.timingCancelled')}
                </span>
                <span title={session.updatedAt} style={{ color: session.status === 'COMPLETED' ? 'var(--suc)' : session.status === 'FAILED' ? 'var(--danger)' : 'var(--t3)' }}>
                  {fmtDatetime(session.updatedAt)}
                </span>
              </div>
            )}
            {session.durationMs != null && session.durationMs >= 60_000 && (
              <div>
                <span style={{ fontSize: 9, textTransform: 'uppercase', letterSpacing: '0.06em', marginRight: 6 }}>{t('dali.sessions.timingTotal')}</span>
                <span style={{ color: 'var(--t2)' }}>{fmtDuration(session.durationMs)}</span>
              </div>
            )}
          </div>

          {/* Per-type vertex breakdown */}
          {session.vertexStats && session.vertexStats.length > 0 && (
            <div style={{ marginTop: 14 }}>
              <div
                onClick={() => setVertexOpen(o => !o)}
                style={{
                  fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--t3)',
                  textTransform: 'uppercase', letterSpacing: '0.07em',
                  marginBottom: vertexOpen ? 6 : 0,
                  display: 'flex', alignItems: 'center', gap: 5,
                  cursor: 'pointer', userSelect: 'none',
                }}
              >
                <span style={{ fontSize: 8, opacity: 0.6 }}>{vertexOpen ? '▼' : '▶'}</span>
                {t('dali.sessions.vertexBreakdown')}
                <span style={{ opacity: 0.5 }}>({session.vertexStats.reduce((a, s) => a + s.inserted + s.duplicate, 0).toLocaleString()} {t('dali.sessions.vertexTotal').toLowerCase()})</span>
              </div>
              {vertexOpen && <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
                <thead>
                  <tr style={{ background: 'var(--bg2)' }}>
                    {[t('dali.sessions.colType'), t('dali.sessions.colInserted'), t('dali.sessions.colDuplicate'), t('dali.sessions.colTotal')].map(h => (
                      <th key={h} style={{ padding: '4px 8px', textAlign: h === t('dali.sessions.colType') ? 'left' : 'right', fontWeight: 600, color: 'var(--t2)', borderBottom: '1px solid var(--bd)' }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {session.vertexStats.map((s, i) => {
                    const isCanonical = ['DaliSchema','DaliTable','DaliColumn','DaliDatabase'].includes(s.type);
                    return (
                      <tr key={s.type} style={{ background: i % 2 === 0 ? 'transparent' : 'var(--bg2)' }}>
                        <td style={{ padding: '3px 8px', fontFamily: 'var(--mono)', color: isCanonical ? 'var(--acc)' : 'var(--t1)', borderBottom: '1px solid var(--bd)' }}>
                          {s.type}
                        </td>
                        <td style={{ padding: '3px 8px', textAlign: 'right', color: 'var(--suc)', borderBottom: '1px solid var(--bd)' }}>
                          {s.inserted.toLocaleString()}
                        </td>
                        <td style={{ padding: '3px 8px', textAlign: 'right', color: s.duplicate > 0 ? 'var(--wrn)' : 'var(--t3)', borderBottom: '1px solid var(--bd)' }}>
                          {s.duplicate > 0 ? s.duplicate.toLocaleString() : '—'}
                        </td>
                        <td style={{ padding: '3px 8px', textAlign: 'right', color: 'var(--t2)', borderBottom: '1px solid var(--bd)' }}>
                          {((s.inserted ?? 0) + (s.duplicate ?? 0)).toLocaleString()}
                        </td>
                      </tr>
                    );
                  })}
                  <tr style={{ background: 'var(--bg2)', fontWeight: 600 }}>
                    <td style={{ padding: '4px 8px', color: 'var(--t2)' }}>{t('dali.sessions.vertexTotal')}</td>
                    <td style={{ padding: '4px 8px', textAlign: 'right', color: 'var(--suc)' }}>
                      {session.vertexStats.reduce((a, s) => a + s.inserted, 0).toLocaleString()}
                    </td>
                    <td style={{ padding: '4px 8px', textAlign: 'right', color: 'var(--wrn)' }}>
                      {session.vertexStats.reduce((a, s) => a + s.duplicate, 0).toLocaleString()}
                    </td>
                    <td style={{ padding: '4px 8px', textAlign: 'right', color: 'var(--t2)' }}>
                      {session.vertexStats.reduce((a, s) => a + s.inserted + s.duplicate, 0).toLocaleString()}
                    </td>
                  </tr>
                </tbody>
              </table>}
            </div>
          )}

          {/* Warnings toggle (aggregate) */}
          {ws.length > 0 && (
            <>
              <button className={css.warnToggle} onClick={() => setWarningsOpen(o => !o)}>
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/>
                  <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
                </svg>
                {t('dali.sessions.warningsBtn', { count: ws.length })}
                <svg
                  width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                  className={warningsOpen ? css.chevronOpen : css.chevron}
                >
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </button>
              {warningsOpen && (
                <div className={css.warnList}>
                  {ws.map((w, i) => <div key={i} className={css.warnItem}>{w}</div>)}
                </div>
              )}
            </>
          )}

          {/* Per-file breakdown for batch sessions */}
          {hasFiles && (
            <div style={{ marginTop: 12 }}>
              <DurationTimeline files={session.fileResults} />
              <div style={{
                fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--t3)',
                textTransform: 'uppercase', letterSpacing: '0.07em', marginBottom: 6,
                marginTop: 14,
              }}>
                {isFailed
                  ? t('dali.sessions.filesHeaderPartial', { done: session.fileResults.length, total: session.total })
                  : t('dali.sessions.filesHeader', { count: session.fileResults.length })}
              </div>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ background: 'color-mix(in srgb, var(--bg3) 50%, transparent)' }}>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>#</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'left', fontWeight: 500 }}>FILE</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>ATOMS</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>VTXS</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>RESOLUTION</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>DUR</th>
                  </tr>
                </thead>
                <tbody>
                  {session.fileResults.map((fr, i) => (
                    <FileRow key={fr.path} fr={fr} index={i} />
                  ))}
                </tbody>
              </table>
              <DurationChart files={session.fileResults} />
            </div>
          )}
        </div>
      </td>
    </tr>
  );
}

// ── Session row with per-row polling ─────────────────────────────────────────
interface SessionRowProps {
  session:     DaliSession;
  expanded:    boolean;
  onToggle:    () => void;
  onUpdate:    (s: DaliSession) => void;
  hideCols?:   boolean;
  tenantAlias?: string;
  /** UC-S07: show tenant badge prominently in row (all-tenants mode) */
  showTenant?: boolean;
}

function SessionRow({ session, expanded, onToggle, onUpdate, hideCols = false, tenantAlias, showTenant = false }: SessionRowProps) {
  const { t } = useTranslation();
  const isTerminal = TERMINAL.has(session.status);
  // Use the tenant stored in the session itself (set by server at creation time)
  const effectiveTenant = session.tenantAlias ?? tenantAlias;
  const live = useDaliSession(session.id, !isTerminal, effectiveTenant);
  const [cancelling, setCancelling] = useState(false);
  // Track last updatedAt we propagated so we don't depend on the session prop
  const reportedAtRef = useRef('');
  // Stable ref for onUpdate so the effect doesn't re-run on every parent render
  const onUpdateRef = useRef(onUpdate);
  useEffect(() => { onUpdateRef.current = onUpdate; });

  useEffect(() => {
    if (!live) return;
    if (live.updatedAt === reportedAtRef.current) return;
    reportedAtRef.current = live.updatedAt;
    onUpdateRef.current(live);
  }, [live]);

  const s = live ?? session;

  async function handleCancel(e: React.MouseEvent) {
    e.stopPropagation();
    setCancelling(true);
    try {
      await cancelSession(s.id, effectiveTenant);
      onUpdate({ ...s, status: 'CANCELLING' });
    } catch { /* polling will reflect true state */ }
    finally { setCancelling(false); }
  }

  const chevronColor =
    s.status === 'FAILED'    ? 'var(--danger)' :
    s.status === 'RUNNING'   ? 'var(--acc)'    :
    s.status === 'COMPLETED' ? 'var(--suc)'    : 'var(--t3)';

  return (
    <>
      <tr
        className={`${css.dataRow} ${expanded ? css.dataRowSelected : ''}`}
        onClick={onToggle}
        title="Click to expand details"
        style={{ cursor: 'pointer' }}
      >
        <td>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {/* Session name (Блок 1) */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
              <span style={{
                fontFamily: 'var(--mono)', color: 'var(--t1)', fontSize: 11,
                fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis',
                whiteSpace: 'nowrap', maxWidth: showTenant ? 140 : 180,
              }} title={`${deriveSessionName(s)}\nID: ${s.id}`}>
                {deriveSessionName(s)}
              </span>
              {/* UC-S07 — tenant badge: prominent when viewing all tenants */}
              {showTenant && effectiveTenant && (
                <span style={{
                  fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 700,
                  padding: '1px 6px', borderRadius: 3, flexShrink: 0,
                  background: 'color-mix(in srgb, var(--acc) 15%, transparent)',
                  color: 'var(--acc)',
                  border: '1px solid color-mix(in srgb, var(--acc) 35%, transparent)',
                  whiteSpace: 'nowrap',
                }} title={`Tenant: ${effectiveTenant}`}>
                  {effectiveTenant}
                </span>
              )}
            </div>
            {/* ID + instanceId badge row */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 4, flexWrap: 'nowrap' }}>
              <span style={{ fontFamily: 'var(--mono)', color: 'var(--t3)', fontSize: 10 }} title={s.id}>
                {s.id.slice(0, 8)}
              </span>
              {/* instanceId badge (Блок 4) — ⚙ icon + informative tooltip */}
              {s.instanceId && (
                <span
                  title={t('dali.sessions.instanceTooltip', { id: s.instanceId, alias: effectiveTenant ?? '…' })}
                  style={{
                    fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 600,
                    padding: '1px 5px', borderRadius: 3,
                    background: 'color-mix(in srgb, var(--acc) 10%, transparent)',
                    color: 'var(--acc)',
                    border: '1px solid color-mix(in srgb, var(--acc) 25%, transparent)',
                    lineHeight: '1.2', whiteSpace: 'nowrap',
                    cursor: 'help',
                  }}
                >
                  ⚙ {s.instanceId}
                </span>
              )}
            </div>
          </div>
        </td>
        <td><StatusBadge status={s.status} /></td>
        {!hideCols && (
          <td>
            <span style={{ fontFamily: 'var(--mono)', color: 'var(--t2)', fontSize: 12 }}>
              {DIALECT_LABEL[s.dialect] ?? s.dialect}
            </span>
          </td>
        )}
        <td><SourceCell session={s} tenantAlias={effectiveTenant} /></td>
        <td>
          <ProgressBar session={s} />
          <RowMetrics session={s} />
        </td>
        {!hideCols && (
          <td>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              <span style={{ fontFamily: 'var(--mono)', color: 'var(--t3)', fontSize: 11 }}>
                {fmtDuration(s.durationMs)}
              </span>
              <span style={{ fontFamily: 'var(--mono)', color: 'var(--t3)', fontSize: 9, opacity: 0.7 }}
                    title={s.startedAt}>
                {fmtDateShort(s.startedAt)}
              </span>
            </div>
          </td>
        )}
        <td style={{ textAlign: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 4 }}>
            {(s.status === 'QUEUED' || s.status === 'RUNNING') && (
              <button
                onClick={handleCancel}
                disabled={cancelling}
                title={t('dali.sessions.cancelBtn')}
                style={{
                  background: 'none',
                  border: '1px solid color-mix(in srgb, var(--danger) 35%, transparent)',
                  borderRadius: 3,
                  color: cancelling ? 'var(--t4)' : 'var(--danger)',
                  cursor: cancelling ? 'default' : 'pointer',
                  padding: '1px 5px',
                  fontSize: 10,
                  fontFamily: 'var(--mono)',
                  lineHeight: '1.5',
                  opacity: cancelling ? 0.5 : 0.8,
                  flexShrink: 0,
                }}
              >
                ✕
              </button>
            )}
            <svg
              width="13" height="13" viewBox="0 0 24 24" fill="none"
              stroke={chevronColor}
              strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
              className={expanded ? css.chevronOpen : css.chevron}
            >
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>
        </td>
      </tr>
      {expanded && <DetailRow session={s} tenantAlias={effectiveTenant} />}
    </>
  );
}

// ── SessionList ───────────────────────────────────────────────────────────────
type FilterStatus = 'all' | 'RUNNING' | 'FAILED' | 'COMPLETED' | 'QUEUED';
type SortBy = 'date' | 'duration';

interface SessionListProps {
  sessions:        DaliSession[];
  onSessionUpdate: (s: DaliSession) => void;
  tenantAlias?:    string;
  /** UC-S07: when true, sessions from multiple tenants are shown — display tenant badge prominently */
  allTenantsMode?: boolean;
  /** Controlled expanded row id — when provided by parent (URL-driven). If absent, local state is used. */
  expandedId?:     string | null;
  onToggleExpand?: (id: string) => void;
}

export function SessionList({
  sessions, onSessionUpdate, tenantAlias,
  allTenantsMode = false,
  expandedId: controlledExpandedId, onToggleExpand,
}: SessionListProps) {
  const { t }    = useTranslation();
  const isMobile = useIsMobile();

  // Local expand state — used only when parent doesn't control it (Блок 6)
  const [localExpandedId, setLocalExpandedId] = useState<string | null>(null);
  const expandedId = controlledExpandedId !== undefined ? controlledExpandedId : localExpandedId;

  function toggleExpand(id: string) {
    if (onToggleExpand) {
      onToggleExpand(id);
    } else {
      setLocalExpandedId(prev => (prev === id ? null : id));
    }
  }

  // Tab filter + search + sort (Блок 3)
  const [filterStatus, setFilterStatus] = useState<FilterStatus>('all');
  const [searchText,   setSearchText]   = useState('');
  const [sortBy,       setSortBy]       = useState<SortBy>('date');

  // Reset filter/search when sessions list changes identity (tenant switch, etc.)
  useEffect(() => {
    setFilterStatus('all');
    setSearchText('');
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tenantAlias]);

  // Count per status for tab badges (always from full sessions list)
  const counts = useMemo(() => ({
    all:       sessions.length,
    RUNNING:   sessions.filter(s => s.status === 'RUNNING' || s.status === 'CANCELLING').length,
    FAILED:    sessions.filter(s => s.status === 'FAILED').length,
    COMPLETED: sessions.filter(s => s.status === 'COMPLETED').length,
    QUEUED:    sessions.filter(s => s.status === 'QUEUED').length,
  }), [sessions]);

  // Filtered + searched + sorted sessions for table
  const filtered = useMemo(() => {
    let list = sessions;

    // Status filter
    if (filterStatus !== 'all') {
      if (filterStatus === 'RUNNING') {
        list = list.filter(s => s.status === 'RUNNING' || s.status === 'CANCELLING');
      } else {
        list = list.filter(s => s.status === filterStatus);
      }
    }

    // Search by source filename
    if (searchText.trim()) {
      const q = searchText.trim().toLowerCase();
      list = list.filter(s => {
        const src = (s.source ?? '').toLowerCase();
        const filename = src.replace(/\\/g, '/').split('/').pop() ?? '';
        return src.includes(q) || filename.includes(q);
      });
    }

    // Sort
    if (sortBy === 'duration') {
      list = [...list].sort((a, b) => (b.durationMs ?? 0) - (a.durationMs ?? 0));
    } else {
      // date desc (default — newer first)
      list = [...list].sort((a, b) =>
        (b.startedAt ?? '').localeCompare(a.startedAt ?? ''));
    }

    return list;
  }, [sessions, filterStatus, searchText, sortBy]);

  // Tab filter buttons definition
  const tabs: { key: FilterStatus; labelKey: string }[] = [
    { key: 'all',       labelKey: 'dali.sessions.filterAll'       },
    { key: 'RUNNING',   labelKey: 'dali.sessions.filterRunning'   },
    { key: 'FAILED',    labelKey: 'dali.sessions.filterFailed'    },
    { key: 'COMPLETED', labelKey: 'dali.sessions.filterCompleted' },
    { key: 'QUEUED',    labelKey: 'dali.sessions.filterQueued'    },
  ];

  return (
    <div className={css.panel}>
      <div className={css.panelHeader}>
        <span className={css.panelTitle}>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <line x1="3" y1="9" x2="21" y2="9"/>
            <line x1="9" y1="21" x2="9" y2="9"/>
          </svg>
          {t('dali.sessions.panelTitle')}
        </span>
        <span style={{ fontSize: 11, color: 'var(--t3)', fontFamily: 'var(--mono)' }}>
          {t('dali.sessions.sessionCount', { count: sessions.length })}
        </span>
        {/* UC-S07 all-tenants indicator */}
        {allTenantsMode && (
          <span style={{
            fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 700,
            padding: '2px 7px', borderRadius: 3,
            background: 'color-mix(in srgb, var(--acc) 15%, transparent)',
            color: 'var(--acc)',
            border: '1px solid color-mix(in srgb, var(--acc) 35%, transparent)',
            whiteSpace: 'nowrap',
          }}>
            ◉ {t('dali.page.allTenantsBanner')}
          </span>
        )}
      </div>

      {/* ── Tab filter bar + search + sort (Блок 3) ──────────────────────── */}
      {sessions.length > 0 && (
        <div style={{
          display: 'flex', alignItems: 'center', gap: 6,
          padding: '7px 14px 0', flexWrap: 'wrap',
          borderBottom: '1px solid var(--bd)',
        }}>
          {/* Status tabs */}
          <div style={{ display: 'flex', gap: 2, flex: '0 0 auto' }}>
            {tabs.map(tab => {
              const count = counts[tab.key];
              if (tab.key !== 'all' && count === 0) return null;
              const active = filterStatus === tab.key;
              return (
                <button
                  key={tab.key}
                  onClick={() => setFilterStatus(tab.key)}
                  style={{
                    padding: '4px 9px',
                    background: active ? 'var(--bg3)' : 'transparent',
                    border: active ? '1px solid var(--bd)' : '1px solid transparent',
                    borderBottom: active ? '1px solid var(--bg3)' : '1px solid transparent',
                    borderRadius: '4px 4px 0 0',
                    color: active ? 'var(--t1)' : 'var(--t3)',
                    fontSize: 11,
                    cursor: 'pointer',
                    fontFamily: 'inherit',
                    display: 'flex', alignItems: 'center', gap: 5,
                    transition: 'color 0.1s',
                  }}
                >
                  {/* Colored dot for non-all tabs */}
                  {tab.key === 'RUNNING' && <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--inf)', flexShrink: 0 }} />}
                  {tab.key === 'FAILED'  && <span style={{ color: 'var(--danger)', fontSize: 9 }}>✗</span>}
                  {tab.key === 'COMPLETED' && <span style={{ color: 'var(--suc)', fontSize: 9 }}>✓</span>}
                  {tab.key === 'QUEUED'  && <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--t3)', flexShrink: 0 }} />}
                  {t(tab.labelKey)}
                  <span style={{
                    fontFamily: 'var(--mono)', fontSize: 9,
                    color: active ? 'var(--acc)' : 'var(--t4)',
                    background: active ? 'color-mix(in srgb, var(--acc) 12%, transparent)' : 'var(--bg3)',
                    padding: '0 4px', borderRadius: 3,
                  }}>
                    {count}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Spacer */}
          <div style={{ flex: 1 }} />

          {/* Search */}
          <input
            type="text"
            value={searchText}
            onChange={e => setSearchText(e.target.value)}
            placeholder={t('dali.sessions.searchPlaceholder')}
            style={{
              background: 'var(--bg2)', border: '1px solid var(--bd)',
              borderRadius: 4, padding: '3px 8px', fontSize: 11,
              color: 'var(--t1)', outline: 'none', fontFamily: 'var(--mono)',
              width: 170,
            }}
          />

          {/* Sort toggle */}
          <button
            onClick={() => setSortBy(prev => prev === 'date' ? 'duration' : 'date')}
            style={{
              padding: '3px 8px', background: 'var(--bg2)',
              border: '1px solid var(--bd)', borderRadius: 4,
              fontSize: 11, cursor: 'pointer', color: 'var(--t3)',
              fontFamily: 'inherit', whiteSpace: 'nowrap',
            }}
            title={sortBy === 'date' ? t('dali.sessions.sortDuration') : t('dali.sessions.sortDate')}
          >
            {sortBy === 'date' ? t('dali.sessions.sortDate') : t('dali.sessions.sortDuration')}
          </button>
        </div>
      )}

      {sessions.length === 0 ? (
        <div className={css.empty}>
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ opacity: 0.35 }}>
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <line x1="3" y1="9" x2="21" y2="9"/>
            <line x1="9" y1="21" x2="9" y2="9"/>
          </svg>
          <div className={css.emptyTitle}>{t('dali.sessions.emptyTitle')}</div>
          <div className={css.emptySub}>
            {t('dali.sessions.emptySub').split('\n').map((line, i) => (
              <span key={i}>{line}{i === 0 ? <br/> : null}</span>
            ))}
          </div>
        </div>
      ) : filtered.length === 0 ? (
        <div className={css.empty} style={{ padding: '24px 16px' }}>
          <div className={css.emptyTitle} style={{ fontSize: 13 }}>No sessions match the filter</div>
        </div>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table className={css.sessionTable} style={{ minWidth: isMobile ? 480 : undefined }}>
            <thead>
              <tr>
                <th style={{ width: isMobile ? 100 : 160 }}>{t('dali.sessions.colSessionId')}</th>
                <th style={{ width: 120 }}>{t('dali.sessions.colStatus')}</th>
                {!isMobile && <th style={{ width: 100 }}>{t('dali.sessions.colDialect')}</th>}
                <th>{t('dali.sessions.colSource')}</th>
                <th style={{ width: isMobile ? 110 : 130 }}>{t('dali.sessions.colProgress')}</th>
                {!isMobile && <th style={{ width: 72 }}>{t('dali.sessions.colDuration')}</th>}
                <th style={{ width: 36 }}></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(s => (
                <SessionRow
                  key={s.id}
                  session={s}
                  expanded={expandedId === s.id}
                  onToggle={() => toggleExpand(s.id)}
                  onUpdate={onSessionUpdate}
                  hideCols={isMobile}
                  tenantAlias={tenantAlias}
                  showTenant={allTenantsMode}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
