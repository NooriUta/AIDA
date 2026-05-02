import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliSession } from '../../api/dali';
import { cancelSession, restartSession } from '../../api/dali';
import { useDaliSession } from '../../hooks/useDaliSession';
import { TERMINAL, DIALECT_LABEL, deriveSessionName, fmtDuration, fmtDateShort } from './sessionListHelpers';
import { StatusBadge, ProgressBar, SourceCell, RowMetrics } from './SessionListParts';
import { DetailRow } from './SessionDetailRow';
import css from './dali.module.css';

export interface SessionRowProps {
  session:     DaliSession;
  expanded:    boolean;
  onToggle:    () => void;
  onUpdate:    (s: DaliSession) => void;
  hideCols?:   boolean;
  tenantAlias?: string;
  /** UC-S07: show tenant badge prominently in row (all-tenants mode) */
  showTenant?: boolean;
}

export function SessionRow({ session, expanded, onToggle, onUpdate, hideCols = false, tenantAlias, showTenant = false }: SessionRowProps) {
  const { t } = useTranslation();
  const isTerminal = TERMINAL.has(session.status);
  const effectiveTenant = session.tenantAlias ?? tenantAlias;
  const live = useDaliSession(session.id, !isTerminal, effectiveTenant);
  const [cancelling, setCancelling] = useState(false);
  const [restarting, setRestarting] = useState(false);
  const reportedAtRef = useRef('');
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

  async function handleRestart(e: React.MouseEvent) {
    e.stopPropagation();
    setRestarting(true);
    try {
      const fresh = await restartSession(s.id, effectiveTenant);
      onUpdate(fresh);
    } catch { /* leave row as-is; user can retry */ }
    finally { setRestarting(false); }
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
            {(s.status === 'FAILED' || s.status === 'CANCELLED')
              && !(s.source ?? '').startsWith('jdbc:') && (
              <button
                onClick={handleRestart}
                disabled={restarting}
                title={t('dali.sessions.restartBtn', { defaultValue: 'Restart with same input' })}
                style={{
                  background: 'none',
                  border: '1px solid color-mix(in srgb, var(--acc) 35%, transparent)',
                  borderRadius: 3,
                  color: restarting ? 'var(--t4)' : 'var(--acc)',
                  cursor: restarting ? 'default' : 'pointer',
                  padding: '1px 5px',
                  fontSize: 10,
                  fontFamily: 'var(--mono)',
                  lineHeight: '1.5',
                  opacity: restarting ? 0.5 : 0.8,
                  flexShrink: 0,
                }}
              >
                ↻
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
