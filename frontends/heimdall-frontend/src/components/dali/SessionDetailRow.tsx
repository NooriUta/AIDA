import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliSession } from '../../api/dali';
import { fmtDatetime, fmtDuration, rateColor } from './sessionListHelpers';
import { DurationTimeline, DurationChart, FileRow } from './SessionFileResults';
import css from './dali.module.css';

export function DetailRow({ session, tenantAlias }: { session: DaliSession; tenantAlias?: string }) {
  const { t } = useTranslation();
  const [warningsOpen, setWarningsOpen] = useState(false);
  const [errorsOpen,   setErrorsOpen]   = useState(true);
  const [vertexOpen,   setVertexOpen]   = useState(false);
  const ws = session.warnings ?? [];
  const es = session.errors   ?? [];
  const hasFiles = session.fileResults && session.fileResults.length > 0;
  const isFailed = session.status === 'FAILED';

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

          <div style={{
            display: 'flex', gap: 24, marginTop: 10, marginBottom: 2,
            fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t3)',
            borderTop: '1px solid var(--bd)', paddingTop: 8, flexWrap: 'wrap', alignItems: 'center',
          }}>
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
