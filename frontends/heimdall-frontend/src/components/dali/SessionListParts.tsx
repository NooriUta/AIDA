import type { DaliSession, FileResult } from '../../api/dali';
import { rateColor, fmtK, truncSource } from './sessionListHelpers';
import css from './dali.module.css';

export function StatusBadge({ status }: { status: DaliSession['status'] }) {
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

export function ProgressBar({ session }: { session: DaliSession }) {
  const { status, progress, total } = session;

  if (status === 'COMPLETED') {
    return (
      <div className={css.prog}>
        <div className={css.progFill} style={{ width: '100%', background: 'var(--suc)' }} />
      </div>
    );
  }
  if (status === 'FAILED') {
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

export function SourceCell({ session, tenantAlias }: { session: DaliSession; tenantAlias?: string }) {
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

export function RowMetrics({ session: s }: { session: DaliSession }) {
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

// Keeps the import of FileResult used (avoids TS unused-import error)
export type { FileResult };
