import { memo, useRef, useState, useEffect } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { FileCode } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData, ColumnInfo } from '../../../types/domain';
import { NodeExpandButtons } from './NodeExpandButtons';
import { useZoomLevel, LOD_COMPACT_ZOOM } from '../ZoomLevelContext';

const WRITE_OPS = new Set(['INSERT', 'UPDATE', 'MERGE']);

export type StatementNodeType = Node<DaliNodeData>;

const COL_ROW_HEIGHT = 22; // must match layoutGraph.ts COLUMN_ROW_HEIGHT

// ─── Statement type → badge colour ──────────────────────────────────────────
const STMT_TYPE_COLORS: Record<string, string> = {
  INSERT:   '#D4922A',
  UPDATE:   '#D4922A',
  DELETE:   '#c85c5c',
  MERGE:    '#D4922A',
  SELECT:   '#88B8A8',
  CTE:      '#A8B860',
  WITH:     '#A8B860',
  CREATE:   '#7DBF78',
  DROP:     '#c85c5c',
  TRUNCATE: '#c85c5c',
  CALL:     '#665c48',
  SQ:       '#88B8A8',
  OPEN:           '#665c48',
  FETCH:          '#88B8A8',
  CLOSE:          '#665c48',
  CURSOR:         '#88B8A8',
  DINAMIC_CURSOR: '#88B8A8',
  DYNAMIC_CURSOR: '#88B8A8',
};

function OutputColRow({ col }: { col: ColumnInfo }) {
  return (
    <div style={{
      display:     'flex',
      alignItems:  'center',
      gap:         'var(--seer-space-2)',
      padding:     '3px 10px',
      borderTop:   '1px solid var(--bd)',
      fontSize:    '12px',
      position:    'relative',
    }}>
      <Handle
        type="source"
        position={Position.Right}
        id={`src-${col.id}`}
        style={{ background: 'var(--suc)', width: '6px', height: '6px', right: '-4px' }}
      />
      <Handle
        type="target"
        position={Position.Left}
        id={`tgt-${col.id}`}
        style={{ background: 'var(--suc)', width: '6px', height: '6px', left: '-4px' }}
      />
      <span className="mono" style={{
        color:        'var(--t1)',
        flex:         1,
        overflow:     'hidden',
        textOverflow: 'ellipsis',
        whiteSpace:   'nowrap',
        fontSize:     '12px',
      }}>
        {col.name}
      </span>
    </div>
  );
}

export const StatementNode = memo(({ data, selected, id }: NodeProps<StatementNodeType>) => {
  const { t } = useTranslation();
  const { selectNode } = useLoomStore();
  const zoomLevel = useZoomLevel();
  const isLodCompact = zoomLevel < LOD_COMPACT_ZOOM;
  const columns  = data.columns ?? [];
  const isCompact = isLodCompact || data.metadata?.compact === true;
  const visible  = isCompact ? [] : columns;
  const overflow = 0;

  // Columns are zoom-driven only (no hover): appear when zoomed past LOD_COMPACT_ZOOM.
  const showColumns = !isLodCompact;

  // Spacer height when in LOD compact mode but statement has output columns
  const spacerHeight = !showColumns && columns.length > 0
    ? columns.length * COL_ROW_HEIGHT
    : 0;

  // handleTop must NOT depend on isLodCompact — changing at LOD boundary shifts edges.
  // Use groupPath only (stable per node, not zoom-dependent).
  const groupPath = (Array.isArray(data.metadata?.groupPath))
    ? (data.metadata.groupPath as string[]) : [];
  const handleTop = groupPath.length > 0 ? `${22 + groupPath.length * 7}px` : '22px';

  const headerRef  = useRef<HTMLDivElement>(null);

  // Fade-in when columns appear at LOD threshold (avoids flash of content)
  const [colsVisible, setColsVisible] = useState(!isLodCompact);
  useEffect(() => {
    if (!isLodCompact) {
      const timer = setTimeout(() => setColsVisible(true), 16);
      return () => clearTimeout(timer);
    } else {
      setColsVisible(false);
    }
  }, [isLodCompact]);

  // ── Statement type badge ─────────────────────────────────────────────────
  const stmtType  = data.operation?.toUpperCase();
  const typeLabel  = stmtType ?? 'STMT';
  const typeColor  = (stmtType && STMT_TYPE_COLORS[stmtType]) || 'var(--suc)';

  return (
    <div
      className={`loom-node${selected ? ' selected' : ''}`}
      style={{
        background:      'var(--bg2)',
        borderLeftWidth: '3px',
        borderLeftColor: selected ? 'var(--acc)' : typeColor,
        minWidth:        isCompact ? '160px' : '220px',
        padding:         0,
        overflow:        'hidden',
      }}
      onClick={() => selectNode(id)}
    >
      {!isCompact && <NodeExpandButtons nodeId={id} show={selected ?? false} />}
      {/* Both default handles BEFORE column rows — React Flow picks the first handle of each
          type when sourceHandle/targetHandle is unspecified on an edge. Column handles
          (src-colId / tgt-colId) come AFTER so node-level WRITES_TO/READS_FROM
          edges always route to the header, not to a column row. */}
      <Handle type="target" position={Position.Left}  style={{ background: typeColor, zIndex: 5, top: handleTop }} />
      <Handle type="source" position={Position.Right} style={{ background: typeColor, zIndex: 5, top: handleTop }} />

      {/* Header */}
      <div ref={headerRef} style={{
        padding:      'var(--seer-space-2) var(--seer-space-3)',
        display:      'flex',
        alignItems:   'center',
        gap:          'var(--seer-space-2)',
        background:   'var(--bg3)',
        borderBottom: (visible.length > 0 || spacerHeight > 0) ? '1px solid var(--bd)' : 'none',
      }}>
        <FileCode size={13} color={typeColor} strokeWidth={1.5} />
        <div style={{ flex: 1, overflow: 'hidden' }}>
          {/* Hierarchy path (Schema / Package / Routine) — vertical */}
          {!isCompact && Array.isArray(data.metadata?.groupPath) && (data.metadata.groupPath as string[]).length > 0 && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 0, marginBottom: '2px' }}>
              {(data.metadata.groupPath as string[]).map((seg, i) => (
                <div key={i} style={{
                  fontSize:     '9px',
                  color:        'var(--t3)',
                  opacity:      0.6 + i * 0.15,
                  overflow:     'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace:   'nowrap',
                  lineHeight:   '12px',
                  letterSpacing: '0.02em',
                }}>
                  {seg}
                </div>
              ))}
            </div>
          )}
          <div
            title={data.label || 'Statement'}
            style={{
              fontWeight:   600,
              fontSize:     isCompact ? '11px' : '13px',
              color:        'var(--t1)',
              overflow:     'hidden',
              textOverflow: 'ellipsis',
              whiteSpace:   'nowrap',
            }}
          >
            {data.label || 'Statement'}
          </div>
          {!isCompact && columns.length > 0 && (
            <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '1px' }}>
              {columns.length} output col{columns.length !== 1 ? 's' : ''}
            </div>
          )}
        </div>
        {/* Statement type badge with colour */}
        <span style={{
          fontSize:      '8px',
          padding:       '1px 5px',
          borderRadius:  2,
          fontFamily:    'monospace',
          border:        `0.5px solid ${typeColor}`,
          color:         typeColor,
          opacity:       0.8,
          flexShrink:    0,
          letterSpacing: '0.03em',
          fontWeight:    600,
        }}>
          {typeLabel}
        </span>
      </div>

      {/* Output column rows (zoom-driven) with CSS fade; spacer when LOD compact */}
      {showColumns ? (
        <div style={{ opacity: colsVisible ? 1 : 0, transition: 'opacity 0.15s ease' }}>
          {visible.map((col) => (
            <OutputColRow key={col.id} col={col} />
          ))}
          {overflow > 0 && (
            <div style={{
              padding:   '3px var(--seer-space-3)',
              borderTop: '1px solid var(--bd)',
              fontSize:  '11px',
              color:     'var(--acc)',
            }}>
              +{overflow} more
            </div>
          )}
        </div>
      ) : spacerHeight > 0 ? (
        <div style={{ height: spacerHeight }} />
      ) : null}

      {/* "no col mapping" badge — write ops with no output columns mapped */}
      {!isCompact && stmtType && WRITE_OPS.has(stmtType) && columns.length === 0 && (
        <div style={{
          padding:   '3px var(--seer-space-3)',
          borderTop: '1px solid var(--bd)',
          display:   'flex',
          alignItems: 'center',
        }}>
          <span style={{
            fontSize:   '9px',
            color:      'var(--t3)',
            background: 'color-mix(in srgb, var(--t3) 10%, transparent)',
            border:     '1px solid color-mix(in srgb, var(--t3) 25%, transparent)',
            borderRadius: 3,
            padding:    '1px 5px',
            letterSpacing: '0.04em',
          }}>
            {t('statement.noColMapping')}
          </span>
        </div>
      )}

    </div>
  );
});

StatementNode.displayName = 'StatementNode';
