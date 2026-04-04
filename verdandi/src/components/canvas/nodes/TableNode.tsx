import { memo, useState } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { Table2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData, ColumnInfo } from '../../../types/domain';

export type TableNodeType = Node<DaliNodeData>;

const MAX_PARTIAL_COLS = 7;

// ── Window button styles ──────────────────────────────────────────────────────

function WinBtn({
  color,
  title,
  active,
  onClick,
}: {
  color: string;
  title: string;
  active?: boolean;
  onClick: () => void;
}) {
  return (
    <button
      title={title}
      onClick={(e) => { e.stopPropagation(); e.preventDefault(); onClick(); }}
      onDoubleClick={(e) => { e.stopPropagation(); e.preventDefault(); }}
      style={{
        width: '10px',
        height: '10px',
        borderRadius: '50%',
        background: color,
        border: 'none',
        cursor: 'pointer',
        padding: 0,
        opacity: active === undefined ? 1 : active ? 1 : 0.3,
        transition: 'opacity 0.12s',
        flexShrink: 0,
      }}
    />
  );
}

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
  const { drillDown, selectNode, nodeExpansionState, setNodeExpansion, hideNode } = useLoomStore();
  const { t } = useTranslation();
  const [colFilter, setColFilter] = useState('');

  // Current expansion state — default is 'partial'
  const expState = nodeExpansionState[id] ?? 'partial';
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
        // Collapsed: clamp to header height only
        maxHeight:       expState === 'collapsed' ? '44px' : 'none',
      }}
      onClick={() => selectNode(id)}
    >
      <Handle type="target" position={Position.Left} style={{ background: 'var(--inf)', zIndex: 5 }} />

      {/* ── Header ─────────────────────────────────────────────────────────── */}
      <div
        style={{
          padding:      'var(--seer-space-2) var(--seer-space-3)',
          display:      'flex',
          alignItems:   'center',
          gap:          'var(--seer-space-2)',
          background:   'var(--bg3)',
          borderBottom: expState !== 'collapsed' ? '1px solid var(--bd)' : 'none',
          height:       '44px',
          boxSizing:    'border-box',
        }}
        onDoubleClick={handleHeaderDblClick}
      >
        <Table2 size={13} color="var(--acc)" strokeWidth={1.5} style={{ flexShrink: 0 }} />

        <div style={{ flex: 1, overflow: 'hidden' }}>
          <div style={{
            fontWeight:   600,
            fontSize:     '13px',
            color:        'var(--t1)',
            overflow:     'hidden',
            textOverflow: 'ellipsis',
            whiteSpace:   'nowrap',
          }}>
            {data.label}
          </div>
          <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '1px' }}>
            {data.schema ?? 'public'} · {columns.length} {t('nodes.columns')}
          </div>
        </div>

        {/* 🔴 hide · 🟡 partial · 🟢 expanded */}
        <div style={{ display: 'flex', gap: '4px', flexShrink: 0, alignItems: 'center' }}>
          <WinBtn
            color="#C85848"
            title={t('tableNode.hide')}
            onClick={() => hideNode(id)}
          />
          <WinBtn
            color="#D4922A"
            title={t('tableNode.collapse') + ' / partial'}
            active={expState === 'partial'}
            onClick={() => setNodeExpansion(id, 'partial')}
          />
          <WinBtn
            color="#7DBF78"
            title={t('tableNode.expand')}
            active={expState === 'expanded'}
            onClick={() => setNodeExpansion(id, 'expanded')}
          />
        </div>
      </div>

      {/* ── Column filter input (expanded only) ────────────────────────────── */}
      {expState === 'expanded' && (
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

      {/* ── Column rows ─────────────────────────────────────────────────────── */}
      <div style={expState === 'expanded' ? { maxHeight: '320px', overflowY: 'auto' } : {}}>
        {visibleCols.map((col) => (
          <ColumnRow
            key={col.id}
            col={col}
            onClick={data.childrenAvailable ? () => drillDown(col.id, col.name, 'DaliColumn') : undefined}
          />
        ))}
      </div>

      {/* ── "+N more" footer — click to expand ─────────────────────────────── */}
      {overflow > 0 && (
        <div
          style={{
            padding:   '3px var(--seer-space-3)',
            borderTop: '1px solid var(--bd)',
            fontSize:  '11px',
            color:     'var(--acc)',
            cursor:    'pointer',
          }}
          onClick={(e) => { e.stopPropagation(); setNodeExpansion(id, 'expanded'); }}
        >
          {t('tableNode.moreColumns', { count: overflow })}
        </div>
      )}

      <Handle type="source" position={Position.Right} style={{ background: 'var(--acc)', zIndex: 5 }} />
    </div>
  );
});

TableNode.displayName = 'TableNode';
