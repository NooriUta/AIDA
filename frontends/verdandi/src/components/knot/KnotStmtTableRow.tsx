import type { KnotStatement } from '../../services/lineage';
import { TYPE_BADGE, typeFromGeoid, lineFromGeoid, shortName, truncGeoid } from './knotStmtHelpers';

export function StmtTableRow({ stmt, depth, isOpen, toggle, levelLabel, indent }: {
  stmt: KnotStatement; depth: number; isOpen: boolean;
  toggle: (id: string) => void; levelLabel: string; indent?: boolean;
}) {
  const effectiveType = typeFromGeoid(stmt.geoid) || stmt.stmtType;
  const effectiveLine = lineFromGeoid(stmt.geoid) ?? stmt.lineNumber;
  const badge = TYPE_BADGE[effectiveType] || TYPE_BADGE.UNKNOWN;
  const pad = indent ? depth * 12 : 0;
  const isRoot = depth === 0;
  const baseBg  = isRoot ? 'var(--bg3)' : '';
  const hoverBg = isRoot ? 'color-mix(in srgb, var(--bg3) 70%, var(--acc) 30%)' : 'var(--bg3)';

  return (
    <tr
      onClick={() => toggle(stmt.id)}
      style={{
        cursor: 'pointer', transition: 'background 0.1s',
        background: isOpen ? (isRoot ? 'color-mix(in srgb, var(--bg3) 80%, var(--acc) 20%)' : 'var(--bg3)') : baseBg,
      }}
      onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = hoverBg; }}
      onMouseLeave={e => {
        (e.currentTarget as HTMLElement).style.background = isOpen
          ? (isRoot ? 'color-mix(in srgb, var(--bg3) 80%, var(--acc) 20%)' : 'var(--bg3)')
          : baseBg;
      }}
    >
      {/* Arrow */}
      <td style={{ textAlign: 'center', padding: '6px 4px', borderBottom: '1px solid var(--bd)' }}>
        <span style={{
          fontSize: 9, color: 'var(--t3)', display: 'inline-block',
          transition: 'transform 0.15s',
          transform: isOpen ? 'rotate(90deg)' : 'none',
        }}>
          {'▶'}
        </span>
      </td>
      {/* Name (geoid) */}
      <td style={{
        padding: '6px 8px', paddingLeft: 8 + pad, borderBottom: '1px solid var(--bd)',
        fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t2)',
      }}>
        {indent && <span style={{ color: 'var(--t3)', fontSize: 10, marginRight: 4 }}>{'└'}</span>}
        {truncGeoid(stmt.geoid)}
      </td>
      {/* Short name */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)',
        fontWeight: depth === 0 ? 500 : 400, fontSize: depth === 0 ? 12 : 11,
      }}>
        {shortName(stmt)}
      </td>
      {/* Aliases */}
      <td style={{ padding: '6px 8px', borderBottom: '1px solid var(--bd)', maxWidth: 120 }}>
        {(stmt.stmtAliases?.length || 0) > 0 ? (
          <div style={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            {(stmt.stmtAliases || []).slice(0, 3).map(a => (
              <span key={a} style={{
                padding: '1px 5px', borderRadius: 3, fontSize: 9,
                fontFamily: 'var(--mono)',
                background: 'color-mix(in srgb, var(--acc) 10%, transparent)',
                color: 'var(--acc)',
              }}>{a}</span>
            ))}
            {(stmt.stmtAliases?.length || 0) > 3 && (
              <span style={{ fontSize: 9, color: 'var(--t3)' }}>+{(stmt.stmtAliases?.length || 0) - 3}</span>
            )}
          </div>
        ) : (
          <span style={{ color: 'var(--t3)', fontSize: 10 }}>—</span>
        )}
      </td>
      {/* Type */}
      <td style={{ padding: '6px 8px', borderBottom: '1px solid var(--bd)' }}>
        <span style={{
          padding: '2px 6px', borderRadius: 3,
          fontSize: 10, fontFamily: 'var(--mono)',
          background: badge.bg, color: badge.color,
        }}>
          {effectiveType}
        </span>
      </td>
      {/* Level */}
      <td style={{ padding: '6px 8px', borderBottom: '1px solid var(--bd)', color: 'var(--t3)', fontSize: 11, textAlign: 'center' }}>
        {levelLabel}
      </td>
      {/* Line */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t2)',
      }}>
        {effectiveLine || '—'}
      </td>
      {/* Src */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        color: (stmt.sourceTables?.length || 0) > 0 ? 'var(--inf)' : 'var(--t3)',
        fontWeight: (stmt.sourceTables?.length || 0) > 0 ? 500 : 400,
      }}>
        {stmt.sourceTables?.length || 0}
      </td>
      {/* Tgt */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        color: (stmt.targetTables?.length || 0) > 0 ? 'var(--suc)' : 'var(--t3)',
        fontWeight: (stmt.targetTables?.length || 0) > 0 ? 500 : 400,
      }}>
        {stmt.targetTables?.length || 0}
      </td>
      {/* Subqueries count */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        color: (stmt.children?.length || 0) > 0 ? 'var(--t2)' : 'var(--t3)',
      }}>
        {stmt.children?.length || '—'}
      </td>
      {/* Atoms */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        fontWeight: stmt.atomTotal > 0 ? 500 : 400,
        color: stmt.atomTotal > 0 ? 'var(--t1)' : 'var(--t3)',
      }}>
        {stmt.atomTotal || '—'}
      </td>
    </tr>
  );
}
