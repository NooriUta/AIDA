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
        <td style={{ fontFamily: 'var(--mono)', fontSize: 11, padding: '5px 8px', textAlign: 'right', color: rateColor(fr.resolutionRate) }}>
          {(fr.resolutionRate * 100).toFixed(1)}%
        </td>
        <td style={{ fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t3)', padding: '5px 8px', textAlign: 'right' }}>
          {fmtDuration(fr.durationMs)}
        </td>
        <td style={{ padding: '5px 8px', textAlign: 'center', width: 28 }}>
          {fr.warnings.length > 0 && (
            <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--wrn)' }}>
              ⚠ {fr.warnings.length}
            </span>
          )}
          {!fr.success && (
            <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--danger)' }}>✗</span>
          )}
        </td>
      </tr>
      {open && fr.warnings.map((w, i) => (
        <tr key={i}>
          <td colSpan={7} style={{ padding: '2px 8px 2px 32px' }}>
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
  const ws = session.warnings ?? [];
  const es = session.errors   ?? [];
  const hasFiles = session.fileResults && session.fileResults.length > 0;
  const isFailed = session.status === 'FAILED';

  // For FAILED batch: compute partial stats from file results
  const partialAtoms    = hasFiles ? session.fileResults.reduce((a, f) => a + f.atomCount,    0) : null;
  const partialVertices = hasFiles ? session.fileResults.reduce((a, f) => a + f.vertexCount,  0) : null;

  const displayAtoms    = session.atomCount    ?? partialAtoms;
  const displayVertices = session.vertexCount  ?? partialVertices;
  const displayEdges    = session.edgeCount    ?? 0;

  return (
    <tr>
      <td colSpan={7} className={css.detailTd}>
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

          {/* Aggregate stats */}
          <div className={css.detailGrid}>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>Atoms</div>
              <div className={css.dstatVal} style={{ color: 'var(--acc)' }}>
                {(displayAtoms ?? 0).toLocaleString()}
              </div>
              <div className={css.dstatSub}>{isFailed && partialAtoms != null ? 'partial' : 'extracted'}</div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>Vertices</div>
              <div className={css.dstatVal} style={{ color: 'var(--t2)' }}>
                {(displayVertices ?? 0).toLocaleString()}
              </div>
              <div className={css.dstatSub}>{isFailed && partialVertices != null ? 'partial' : 'in YGG'}</div>
            </div>
            <div className={css.dstat}>
              <div className={css.dstatLabel}>Edges</div>
              <div className={css.dstatVal} style={{ color: 'var(--t2)' }}>
                {displayEdges.toLocaleString()}
              </div>
              <div className={css.dstatSub}>lineage links</div>
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
              <div style={{
                fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--t3)',
                textTransform: 'uppercase', letterSpacing: '0.07em', marginBottom: 6,
              }}>
                Files ({session.fileResults.length}{isFailed ? ` of ${session.total}` : ''})
              </div>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ background: 'color-mix(in srgb, var(--bg3) 50%, transparent)' }}>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>#</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'left', fontWeight: 500 }}>FILE</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>ATOMS</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>VERT</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>RATE</th>
                    <th style={{ fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)', padding: '4px 8px', textAlign: 'right', fontWeight: 500 }}>DUR</th>
                    <th style={{ width: 28 }}></th>
                  </tr>
                </thead>
                <tbody>
                  {session.fileResults.map((fr, i) => (
                    <FileRow key={fr.path} fr={fr} index={i} />
                  ))}
                </tbody>
              </table>
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

  useEffect(() => {
    if (!live) return;
    if (live.updatedAt === reportedAtRef.current) return;
    reportedAtRef.current = live.updatedAt;
    onUpdate(live);
  }, [live, onUpdate]);

  const s = live ?? session;

  const canExpand = s.status === 'COMPLETED' || s.status === 'FAILED';

  return (
    <>
      <tr
        className={`${css.dataRow} ${expanded ? css.dataRowSelected : ''}`}
        onClick={() => { if (canExpand) onToggle(); }}
        title={canExpand ? 'Click to expand details' : undefined}
      >
        <td>
          <span style={{ fontFamily: 'var(--mono)', color: 'var(--t2)', fontSize: 12 }} title={s.id}>
            {s.id.slice(0, 8)}
          </span>
        </td>
        <td><StatusBadge status={s.status} /></td>
        <td>
          <span style={{ fontFamily: 'var(--mono)', color: 'var(--t2)', fontSize: 12 }}>
            {DIALECT_LABEL[s.dialect] ?? s.dialect}
          </span>
        </td>
        <td><SourceCell session={s} /></td>
        <td><ProgressBar session={s} /></td>
        <td>
          <span style={{ fontFamily: 'var(--mono)', color: 'var(--t3)', fontSize: 11 }}>
            {s.status === 'COMPLETED' ? fmtDuration(s.durationMs) : '—'}
          </span>
        </td>
        <td style={{ textAlign: 'center' }}>
          {canExpand && (
            <svg
              width="13" height="13" viewBox="0 0 24 24" fill="none"
              stroke={s.status === 'FAILED' ? 'var(--danger)' : 'var(--t3)'}
              strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
              className={expanded ? css.chevronOpen : css.chevron}
            >
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          )}
        </td>
      </tr>
      {expanded && canExpand && <DetailRow session={s} />}
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
