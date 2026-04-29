import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { FileResult } from '../../api/dali';
import { fmtDuration, rateColor } from './sessionListHelpers';
import css from './dali.module.css';

export function DurationTimeline({ files }: { files: FileResult[] }) {
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

export function DurationChart({ files }: { files: FileResult[] }) {
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

export function FileRow({ fr, index }: { fr: FileResult; index: number }) {
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
