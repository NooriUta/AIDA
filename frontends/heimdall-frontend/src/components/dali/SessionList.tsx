import { useEffect, useRef, useState } from 'react';
import { type DaliSession, type FileResult } from '../../api/dali';
import { useDaliSession } from '../../hooks/useDaliSession';
import css from './dali.module.css';

const TERMINAL = new Set<string>(['COMPLETED', 'FAILED', 'CANCELLED']);

const DIALECT_LABEL: Record<string, string> = {
  plsql:      'PL/SQL',
  postgresql: 'PostgreSQL',
  clickhouse: 'ClickHouse',
};

function fmtDuration(ms: number | null | undefined): string {
  if (ms == null) return '—';
  return ms < 1000 ? `${ms}ms` : `${(ms / 1000).toFixed(1)}s`;
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

// ── Status badge ──────────────────────────────────────────────────────────────
function StatusBadge({ status }: { status: DaliSession['status'] }) {
  const badgeCls = {
    QUEUED:    css.badgeQueued,
    RUNNING:   css.badgeRunning,
    COMPLETED: css.badgeCompleted,
    FAILED:    css.badgeFailed,
    CANCELLED: css.badgeCancelled,
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

// ── Source cell: path + BATCH / FILE tag ─────────────────────────────────────
function SourceCell({ session }: { session: DaliSession }) {
  return (
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
  const [hovered, setHovered] = useState<number | null>(null);
  const withDur = files.filter(f => f.durationMs > 0);
  if (withDur.length < 3) return null;

  const W = 600, H = 52, PX = 8, PY = 6;
  const maxMs = Math.max(...withDur.map(f => f.durationMs));
  const n = withDur.length;

  const x = (i: number) => PX + (i / Math.max(n - 1, 1)) * (W - PX * 2);
  const y = (ms: number) => PY + (1 - ms / maxMs) * (H - PY * 2);

  const polyline = withDur.map((f, i) => `${x(i)},${y(f.durationMs)}`).join(' ');
  const area = `${x(0)},${H} ` + withDur.map((f, i) => `${x(i)},${y(f.durationMs)}`).join(' ') + ` ${x(n - 1)},${H}`;

  const hov = hovered !== null ? withDur[hovered] : null;
  const hovFilename = hov ? hov.path.replace(/\\/g, '/').split('/').pop() : '';

  return (
    <div style={{ marginTop: 12, marginBottom: 2 }}>
      <div style={{
        fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--t3)',
        textTransform: 'uppercase', letterSpacing: '0.07em', marginBottom: 6,
        display: 'flex', justifyContent: 'space-between',
      }}>
        <span>Parse timeline ({n} files)</span>
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
          Duration by file{withDur.length > 25 ? ' (top 25)' : ''}
        </span>
        <span style={{ color: 'var(--t3)' }}>total {fmtDuration(totalMs)}</span>
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
          <td colSpan={6} style={{ padding: '2px 8px 2px 32px' }}>
            <div className={css.warnItem}>{w}</div>
          </td>
        </tr>
      ))}
    </>
  );
}

// ── Detail expand row ─────────────────────────────────────────────────────────
function DetailRow({ session }: { session: DaliSession }) {
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
      <td colSpan={6} className={css.detailTd}>
        <div className={css.detailInner}>

          {/* Error banner for FAILED sessions */}
          {isFailed && es.length > 0 && (
            <div style={{ marginBottom: 12 }}>
              <button className={css.warnToggle} style={{ color: 'var(--danger)', borderColor: 'color-mix(in srgb, var(--danger) 30%, transparent)' }} onClick={() => setErrorsOpen(o => !o)}>
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                {es.length} error{es.length !== 1 ? 's' : ''}
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
              <div className={css.dstatLabel}>Atoms</div>
              <div className={css.dstatVal} style={{ color: 'var(--acc)' }}>
                {(displayAtoms ?? 0).toLocaleString()}
              </div>
              <div className={css.dstatSub}>{isFailed && partialAtoms != null ? 'partial' : 'in YGG'}</div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>Vertices</div>
              <div className={css.dstatVal} style={{ color: 'var(--t2)' }}>
                {(displayVertices ?? 0).toLocaleString()}
              </div>
              <div className={css.dstatSub}>{isFailed && partialVertices != null ? 'partial' : 'inserted'}</div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>Edges</div>
              <div className={css.dstatVal} style={{ color: 'var(--t2)' }}>
                {displayEdges.toLocaleString()}
              </div>
              <div className={css.dstatSub}>
                {displayDropped > 0
                  ? <span style={{ color: 'var(--wrn)' }}>↓{displayDropped.toLocaleString()} dropped</span>
                  : 'lineage links'}
              </div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>Resolution</div>
              <div className={css.dstatVal} style={{ color: rateColor(session.resolutionRate) }}>
                {session.resolutionRate != null
                  ? `${(session.resolutionRate * 100).toFixed(1)}%`
                  : '—'}
              </div>
              <div className={css.dstatSub}>column-level</div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>Progress</div>
              <div className={css.dstatVal} style={{ color: isFailed ? 'var(--danger)' : 'var(--t2)' }}>
                {session.batch && session.total > 0
                  ? `${session.progress}/${session.total}`
                  : fmtDuration(session.durationMs)}
              </div>
              <div className={css.dstatSub}>
                {session.batch && session.total > 0 ? 'files done' : 'wall time'}
              </div>
            </div>
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
                Vertex breakdown
                <span style={{ opacity: 0.5 }}>({session.vertexStats.reduce((a, s) => a + s.inserted + s.duplicate, 0).toLocaleString()} total)</span>
              </div>
              {vertexOpen && <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
                <thead>
                  <tr style={{ background: 'var(--bg2)' }}>
                    {['Type', 'Inserted', 'Duplicate', 'Total'].map(h => (
                      <th key={h} style={{ padding: '4px 8px', textAlign: h === 'Type' ? 'left' : 'right', fontWeight: 600, color: 'var(--t2)', borderBottom: '1px solid var(--bd)' }}>{h}</th>
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
                    <td style={{ padding: '4px 8px', color: 'var(--t2)' }}>Total</td>
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
                {ws.length} warning{ws.length !== 1 ? 's' : ''} total
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
                Files ({session.fileResults.length}{isFailed ? ` of ${session.total}` : ''})
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
  session: DaliSession;
  expanded: boolean;
  onToggle: () => void;
  onUpdate: (s: DaliSession) => void;
}

function SessionRow({ session, expanded, onToggle, onUpdate }: SessionRowProps) {
  const isTerminal = TERMINAL.has(session.status);
  const live = useDaliSession(session.id, !isTerminal);
  // Track last updatedAt we propagated so we don't depend on the session prop
  // (which would cause: onUpdate→setSessions→session.updatedAt changes→effect re-runs→loop)
  const reportedAtRef = useRef('');
  // Stable ref for onUpdate so the effect doesn't re-run when the parent re-renders
  // without useCallback (which would create a new function identity on every render).
  const onUpdateRef = useRef(onUpdate);
  useEffect(() => { onUpdateRef.current = onUpdate; });

  useEffect(() => {
    if (!live) return;
    if (live.updatedAt === reportedAtRef.current) return;
    reportedAtRef.current = live.updatedAt;
    onUpdateRef.current(live);
  }, [live]);

  const s = live ?? session;

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
          <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
            <span style={{ fontFamily: 'var(--mono)', color: 'var(--t2)', fontSize: 12 }} title={s.id}>
              {s.id.slice(0, 8)}
            </span>
            <span
              title={s.friggPersisted ? 'Saved to FRIGG' : 'Not yet saved to FRIGG'}
              style={{
                fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 700,
                padding: '1px 4px', borderRadius: 3,
                background: s.friggPersisted
                  ? 'color-mix(in srgb, var(--suc) 14%, transparent)'
                  : 'color-mix(in srgb, var(--wrn) 14%, transparent)',
                color: s.friggPersisted ? 'var(--suc)' : 'var(--wrn)',
                border: `1px solid ${s.friggPersisted
                  ? 'color-mix(in srgb, var(--suc) 30%, transparent)'
                  : 'color-mix(in srgb, var(--wrn) 30%, transparent)'}`,
                lineHeight: '1.2',
              }}
            >
              {s.friggPersisted ? 'F✓' : 'F?'}
            </span>
          </div>
        </td>
        <td><StatusBadge status={s.status} /></td>
        <td>
          <span style={{ fontFamily: 'var(--mono)', color: 'var(--t2)', fontSize: 12 }}>
            {DIALECT_LABEL[s.dialect] ?? s.dialect}
          </span>
        </td>
        <td><SourceCell session={s} /></td>
        <td>
          <ProgressBar session={s} />
          <RowMetrics session={s} />
        </td>
        <td>
          <span style={{ fontFamily: 'var(--mono)', color: 'var(--t3)', fontSize: 11 }}>
            {s.status === 'COMPLETED' ? fmtDuration(s.durationMs) : '—'}
          </span>
        </td>
        <td style={{ textAlign: 'center' }}>
          <svg
            width="13" height="13" viewBox="0 0 24 24" fill="none"
            stroke={chevronColor}
            strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
            className={expanded ? css.chevronOpen : css.chevron}
          >
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </td>
      </tr>
      {expanded && <DetailRow session={s} />}
    </>
  );
}

// ── SessionList ───────────────────────────────────────────────────────────────
interface SessionListProps {
  sessions: DaliSession[];
  onSessionUpdate: (s: DaliSession) => void;
}

export function SessionList({ sessions, onSessionUpdate }: SessionListProps) {
  const [expandedId, setExpandedId] = useState<string | null>(null);

  function toggleExpand(id: string) {
    setExpandedId(prev => (prev === id ? null : id));
  }

  return (
    <div className={css.panel}>
      <div className={css.panelHeader}>
        <span className={css.panelTitle}>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <line x1="3" y1="9" x2="21" y2="9"/>
            <line x1="9" y1="21" x2="9" y2="9"/>
          </svg>
          Sessions
        </span>
        <span style={{ fontSize: 11, color: 'var(--t3)', fontFamily: 'var(--mono)' }}>
          {sessions.length} session{sessions.length !== 1 ? 's' : ''}
        </span>
      </div>

      {sessions.length === 0 ? (
        <div className={css.empty}>
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ opacity: 0.35 }}>
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <line x1="3" y1="9" x2="21" y2="9"/>
            <line x1="9" y1="21" x2="9" y2="9"/>
          </svg>
          <div className={css.emptyTitle}>No sessions yet</div>
          <div className={css.emptySub}>
            Start a parse session using the form above<br/>
            or press Enter after filling the source path
          </div>
        </div>
      ) : (
        <table className={css.sessionTable}>
          <thead>
            <tr>
              <th style={{ width: 90  }}>Session ID</th>
              <th style={{ width: 120 }}>Status</th>
              <th style={{ width: 100 }}>Dialect</th>
              <th>Source</th>
              <th style={{ width: 130 }}>Progress</th>
              <th style={{ width: 72  }}>Duration</th>
              <th style={{ width: 36  }}></th>
            </tr>
          </thead>
          <tbody>
            {sessions.map(s => (
              <SessionRow
                key={s.id}
                session={s}
                expanded={expandedId === s.id}
                onToggle={() => toggleExpand(s.id)}
                onUpdate={onSessionUpdate}
              />
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
