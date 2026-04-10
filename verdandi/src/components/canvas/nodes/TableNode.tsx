import { memo, useRef, useState, useEffect } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { Table2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData, ColumnInfo } from '../../../types/domain';
import { NodeExpandButtons } from './NodeExpandButtons';
import { useZoomLevel, LOD_COMPACT_ZOOM } from '../ZoomLevelContext';
import { LAYOUT, TRANSFORM, CANVAS } from '../../../utils/constants';

export type TableNodeType = Node<DaliNodeData>;

const { MAX_PARTIAL_COLS } = TRANSFORM;
const { COL_ROW_HEIGHT } = LAYOUT;

// ── Column row ────────────────────────────────────────────────────────────────

function ColumnRow({
  col,
  onClick,
}: {
  col: ColumnInfo;
  onClick?: () => void;
}) {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 'var(--seer-space-2)',
        padding: '3px 10px',
        borderTop: '1px solid var(--bd)',
        fontSize: '12px',
        position: 'relative',
        cursor: onClick ? 'pointer' : 'default',
      }}
      onClick={onClick ? (e) => { e.stopPropagation(); onClick(); } : undefined}
    >
      {/* Column-level lineage handles */}
      <Handle
        type="source"
        position={Position.Right}
        id={`src-${col.id}`}
        style={{ background: 'var(--acc)', width: '6px', height: '6px', right: '-4px' }}
      />
      <Handle
        type="target"
        position={Position.Left}
        id={`tgt-${col.id}`}
        style={{ background: 'var(--inf)', width: '6px', height: '6px', left: '-4px' }}
      />

      {/* PK filled dot · FK hollow dot · plain space */}
      <span style={{ flexShrink: 0, width: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        {col.isPrimaryKey ? (
          <span style={{ width: '7px', height: '7px', borderRadius: '50%', background: 'var(--wrn)', display: 'block' }} />
        ) : col.isForeignKey ? (
          <span style={{ width: '7px', height: '7px', borderRadius: '50%', border: '1.5px solid var(--inf)', display: 'block' }} />
        ) : (
          <span style={{ width: '7px' }} />
        )}
      </span>

      <span
        className="mono"
        style={{ color: 'var(--t1)', flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontSize: '12px' }}
      >
        {col.name}
      </span>

      <span className="mono" style={{ color: 'var(--t3)', flexShrink: 0, fontSize: '11px' }}>
        {col.type}
      </span>
    </div>
  );
}

// ── TableNode ─────────────────────────────────────────────────────────────────

export const TableNode = memo(({ data, selected, id }: NodeProps<TableNodeType>) => {
  const { drillDown, selectNode, nodeExpansionState, setNodeExpansion } = useLoomStore();
  const { t } = useTranslation();
  const [colFilter, setColFilter] = useState('');
  const headerRef = useRef<HTMLDivElement>(null);
  const zoomLevel = useZoomLevel();
  const isLodCompact = zoomLevel < LOD_COMPACT_ZOOM;

  // handleTop must NOT depend on isLodCompact — changing it at LOD boundary shifts edges.
  // Use stable values based only on static data props.
  const handleTop = data.schema ? '30px' : '22px';

  // Current expansion state — default is 'partial'. LOD forces collapsed at low zoom.
  const expState = isLodCompact ? 'collapsed' : (nodeExpansionState[id] ?? 'partial');
  const columns  = data.columns ?? [];

  // Client-side column filter (expanded state only)
  const filteredCols =
    expState === 'expanded' && colFilter.trim()
      ? columns.filter((c) => c.name.toLowerCase().includes(colFilter.toLowerCase()))
      : columns;

  const visibleCols =
    expState === 'collapsed' ? [] :
    expState === 'partial'   ? filteredCols.slice(0, MAX_PARTIAL_COLS) :
    filteredCols;

  const overflow = expState === 'partial' ? Math.max(0, columns.length - MAX_PARTIAL_COLS) : 0;

  // Columns are zoom-driven only (no hover): appear when zoomed in past LOD_COMPACT_ZOOM.
  // This avoids blink (no rapid hover toggle) and edge shift (no handle mount/unmount
  // caused by mouse movement — only zoom-triggered, which is deliberate and smooth).
  const showColumns = !isLodCompact;

  // Spacer height: reserves space matching ELK layout when columns are not in DOM.
  // Only needed when collapsed by LOD (isLodCompact) but the node has columns.
  const spacerHeight = !showColumns && visibleCols.length === 0 && columns.length > 0
    ? Math.min(columns.length, MAX_PARTIAL_COLS) * COL_ROW_HEIGHT
    : 0;

  // Delayed visibility for column area: mount immediately but fade in to avoid
  // the flash of "all columns appear at once" when crossing the LOD threshold.
  const [colsVisible, setColsVisible] = useState(!isLodCompact);
  useEffect(() => {
    if (!isLodCompact) {
      // Small delay so the mount happens first, then CSS transition kicks in
      const t = setTimeout(() => setColsVisible(true), CANVAS.LOD_TIMEOUT);
      return () => clearTimeout(t);
    } else {
      setColsVisible(false);
    }
  }, [isLodCompact]);

  // Double-click on header: toggle collapsed ↔ partial
  const handleHeaderDblClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (expState === 'collapsed') {
      setNodeExpansion(id, 'partial');
    } else {
      setNodeExpansion(id, 'collapsed');
    }
  };

  return (
    <div
      className={`loom-node${selected ? ' selected' : ''}`}
      style={{
        background:      'var(--bg2)',
        borderLeftWidth: '3px',
        borderLeftColor: 'var(--acc)',
        minWidth:        '220px',
        padding:         0,
        overflow:        'hidden',
      }}
      onClick={() => selectNode(id)}
    >
      <NodeExpandButtons nodeId={id} show={selected ?? false} />
      {/* Both default handles BEFORE column rows — React Flow picks the first handle of
          each type when sourceHandle/targetHandle is unspecified on an edge. Column handles
          (src-colId / tgt-colId) come AFTER so node-level WRITES_TO/READS_FROM route to header. */}
      <Handle type="target" position={Position.Left}  style={{ background: 'var(--inf)', zIndex: 5, top: handleTop }} />
      <Handle type="source" position={Position.Right} style={{ background: 'var(--acc)', zIndex: 5, top: handleTop }} />

      {/* ── Header ─────────────────────────────────────────────────────────── */}
      <div
        ref={headerRef}
        style={{
          padding:      'var(--seer-space-2) var(--seer-space-3)',
          display:      'flex',
          alignItems:   'center',
          gap:          'var(--seer-space-2)',
          background:   'var(--bg3)',
          borderBottom: (visibleCols.length > 0 || spacerHeight > 0) ? '1px solid var(--bd)' : 'none',
          boxSizing:    'border-box',
        }}
        onDoubleClick={handleHeaderDblClick}
      >
        <Table2 size={13} color="var(--acc)" strokeWidth={1.5} style={{ flexShrink: 0 }} />

        <div style={{ flex: 1, overflow: 'hidden' }}>
          {/* Schema label */}
          {data.schema && (
            <div style={{ fontSize: '9px', color: 'var(--t3)', opacity: 0.7, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', marginBottom: '1px', letterSpacing: '0.02em' }}>
              {data.schema}
            </div>
          )}
          <div
            title={data.label}
            style={{
              fontWeight:   600,
              fontSize:     '13px',
              color:        'var(--t1)',
              overflow:     'hidden',
              textOverflow: 'ellipsis',
              whiteSpace:   'nowrap',
            }}
          >
            {data.label}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '1px' }}>
            {columns.length} {t('nodes.columns')}
          </div>
        </div>

      </div>

      {/* ── Column filter input (expanded only) ────────────────────────────── */}
      {expState === 'expanded' && showColumns && colsVisible && (
        <div style={{
          padding:      '4px 10px',
          borderBottom: '1px solid var(--bd)',
          background:   'var(--bg2)',
        }}>
          <input
            type="text"
            placeholder={t('tableNode.filterPlaceholder')}
            value={colFilter}
            onChange={(e) => setColFilter(e.target.value)}
            onClick={(e) => e.stopPropagation()}
            onDoubleClick={(e) => e.stopPropagation()}
            style={{
              width:       '100%',
              background:  'var(--bg1)',
              border:      '1px solid var(--bd)',
              borderRadius:'4px',
              padding:     '3px 7px',
              fontSize:    '11px',
              color:       'var(--t1)',
              outline:     'none',
              boxSizing:   'border-box',
            }}
          />
        </div>
      )}

      {/* ── Column rows (zoom-driven) with CSS fade; spacer when LOD compact ── */}
      {showColumns ? (
        <div
          style={{
            ...(expState === 'expanded' ? { maxHeight: '320px', overflowY: 'auto' } : {}),
            opacity:    colsVisible ? 1 : 0,
            transition: 'opacity 0.15s ease',
          }}
        >
          {visibleCols.map((col) => (
            <ColumnRow
              key={col.id}
              col={col}
              onClick={data.childrenAvailable ? () => drillDown(col.id, col.name, 'DaliColumn') : undefined}
            />
          ))}
        </div>
      ) : spacerHeight > 0 ? (
        <div style={{ height: spacerHeight }} />
      ) : null}

      {/* ── "+N more" footer — click to expand ─────────────────────────────── */}
      {overflow > 0 && showColumns && (
        <div
          style={{
            padding:    '3px var(--seer-space-3)',
            borderTop:  '1px solid var(--bd)',
            fontSize:   '11px',
            color:      'var(--acc)',
            cursor:     'pointer',
            opacity:    colsVisible ? 1 : 0,
            transition: 'opacity 0.15s ease',
          }}
          onClick={(e) => { e.stopPropagation(); setNodeExpansion(id, 'expanded'); }}
        >
          {t('tableNode.moreColumns', { count: overflow })}
        </div>
      )}

    </div>
  );
});

TableNode.displayName = 'TableNode';
