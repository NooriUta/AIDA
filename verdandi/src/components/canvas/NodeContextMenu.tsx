import { memo, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useReactFlow } from '@xyflow/react';
import { useLoomStore } from '../../stores/loomStore';
import type { DaliNodeData } from '../../types/domain';
import { CANVAS } from '../../utils/constants';

/** Extract package name from a DaliStatement fullLabel.
 *  "DWH.PKG_ETL_CRM_STAGING:PROCEDURE:COMPUTE_CUSTOMER_RFM:INSERT:4343"
 *  → "PKG_ETL_CRM_STAGING"
 */
function extractPkgFromLabel(fullLabel: string | undefined): string | null {
  if (!fullLabel) return null;
  const firstSegment = fullLabel.split(':')[0];           // "DWH.PKG_ETL_CRM_STAGING"
  const parts = firstSegment.split('.');
  return parts[parts.length - 1] || null;                 // "PKG_ETL_CRM_STAGING"
}

// ─── Public type consumed by LoomCanvas ──────────────────────────────────────
export type ContextMenuState = {
  nodeId: string;
  data: DaliNodeData;
  x: number;   // screen clientX
  y: number;   // screen clientY
} | null;

interface Props {
  menu: ContextMenuState;
  onClose: () => void;
}

const MENU_W        = 214;
const MENU_APPROX_H = 290;   // used for bottom-edge clamping

export const NodeContextMenu = memo(({ menu, onClose }: Props) => {
  const { t } = useTranslation();
  const { fitView } = useReactFlow();
  const navigate = useNavigate();
  const {
    viewLevel,
    drillDown,
    setStartObject,
    requestExpand,
    expandedUpstreamIds,
    expandedDownstreamIds,
    hideNode,
  } = useLoomStore();

  const ref = useRef<HTMLDivElement>(null);

  // ── Close on outside click or Escape ─────────────────────────────────────
  useEffect(() => {
    if (!menu) return;
    const onMouseDown = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) onClose();
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('mousedown', onMouseDown);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onMouseDown);
      document.removeEventListener('keydown', onKey);
    };
  }, [menu, onClose]);

  if (!menu) return null;

  const { nodeId, data, x, y } = menu;

  // Clamp position so menu never overflows the viewport
  const left = Math.min(x, Math.max(0, window.innerWidth  - MENU_W        - 8));
  const top  = Math.min(y, Math.max(0, window.innerHeight - MENU_APPROX_H - 8));

  const isL2orL3    = viewLevel !== 'L1';
  const canDrillDown = !!data.childrenAvailable && viewLevel !== 'L3';
  const isStatement  = data.nodeType === 'DaliStatement';
  const pkgName      = isStatement
    ? extractPkgFromLabel(data.metadata?.fullLabel as string | undefined)
    : null;
  const upDone      = expandedUpstreamIds.has(nodeId);
  const downDone    = expandedDownstreamIds.has(nodeId);

  // Close and run an action
  const run = (fn: () => void) => () => { fn(); onClose(); };

  // ── Hover helpers (direct DOM — no re-render) ─────────────────────────────
  const hoverOn  = (e: React.MouseEvent) => {
    (e.currentTarget as HTMLElement).style.background = 'var(--bg1)';
  };
  const hoverOff = (e: React.MouseEvent) => {
    (e.currentTarget as HTMLElement).style.background = 'transparent';
  };

  // ── Shared styles ─────────────────────────────────────────────────────────
  const sep = (
    <div style={{ height: 1, margin: '3px 0', background: 'var(--bd)' }} />
  );

  const Item = ({
    icon, label, onClick, disabled = false, danger = false,
  }: {
    icon: string; label: string; onClick?: () => void;
    disabled?: boolean; danger?: boolean;
  }) => (
    <div
      role="menuitem"
      onClick={disabled ? undefined : onClick}
      onMouseEnter={disabled ? undefined : hoverOn}
      onMouseLeave={disabled ? undefined : hoverOff}
      style={{
        display: 'flex', alignItems: 'center', gap: 7,
        padding: '5px 12px',
        cursor: disabled ? 'default' : 'pointer',
        opacity: disabled ? 0.38 : 1,
        color: danger ? 'var(--wrn, #c0392b)' : 'inherit',
        background: 'transparent',
        transition: 'background 0.08s',
      }}
    >
      <span style={{ width: 14, textAlign: 'center', flexShrink: 0, opacity: 0.65 }}>
        {icon}
      </span>
      <span style={{ flex: 1 }}>{label}</span>
      {disabled && (
        <span style={{ fontSize: 10, color: 'var(--t3)', marginLeft: 'auto' }}>✓</span>
      )}
    </div>
  );

  return (
    <div
      ref={ref}
      onContextMenu={(e) => e.preventDefault()}
      style={{
        position: 'fixed',
        left,
        top,
        zIndex: 9999,
        width: MENU_W,
        background: 'var(--bg2)',
        border: '1px solid var(--bd)',
        borderRadius: 'var(--seer-radius-md, 6px)',
        boxShadow: '0 6px 28px rgba(0,0,0,0.50)',
        padding: '4px 0',
        fontSize: '12px',
        color: 'var(--t1)',
        userSelect: 'none',
      }}
    >
      {/* ── Header: node type + label ───────────────────────────────────── */}
      <div style={{
        padding: '5px 12px 5px',
        borderBottom: '1px solid var(--bd)',
        marginBottom: 2,
      }}>
        <div style={{
          fontSize: 10, color: 'var(--t3)', letterSpacing: '0.06em',
          textTransform: 'uppercase', marginBottom: 2,
        }}>
          {t(`nodeTypes.${data.nodeType}`, data.nodeType)}
        </div>
        <div style={{
          fontSize: 11, fontWeight: 600,
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
          color: 'var(--acc)',
        }}>
          {data.label}
        </div>
      </div>

      {/* ── Navigation ──────────────────────────────────────────────────── */}
      {canDrillDown && (
        <Item
          icon="⇲"
          label={t('contextMenu.drillDown')}
          onClick={run(() => {
            // Mirror scope-building logic from LoomCanvas.onNodeDoubleClick so that
            // schema/package nodes route to the correct exploreSchema / explorePackage
            // handler on SHUTTLE instead of the generic exploreByRid fallback.
            let scope: string;
            if (data.nodeType === 'DaliSchema') {
              const dbName = data.metadata?.databaseName as string | null | undefined;
              scope = dbName ? `schema-${data.label}|${dbName}` : `schema-${data.label}`;
            } else if (data.nodeType === 'DaliPackage') {
              scope = `pkg-${data.label}`;
            } else if (data.nodeType === 'DaliDatabase') {
              scope = `db-${data.label}`;
            } else {
              scope = nodeId;
            }
            drillDown(scope, data.label, data.nodeType);
          })}
        />
      )}
      {isL2orL3 && (
        <Item
          icon="⊕"
          label={t('actions.setAsStart')}
          onClick={run(() => setStartObject(nodeId, data.nodeType, data.label))}
        />
      )}

      {/* ── Expand (L2 / L3 only) ───────────────────────────────────────── */}
      {isL2orL3 && (
        <>
          {sep}
          <Item
            icon="←"
            label={t('expand.upstream')}
            onClick={run(() => requestExpand(nodeId, 'upstream'))}
            disabled={upDone}
          />
          <Item
            icon="→"
            label={t('expand.downstream')}
            onClick={run(() => requestExpand(nodeId, 'downstream'))}
            disabled={downDone}
          />
        </>
      )}

      {/* ── Open in KNOT (Statement nodes only) ─────────────────────────── */}
      {isStatement && (
        <>
          {sep}
          <Item
            icon="◈"
            label={t('contextMenu.openInKnot')}
            onClick={run(() => {
              const params = new URLSearchParams();
              if (pkgName) params.set('pkg', pkgName);
              params.set('stmt', data.label);
              navigate(`/knot?${params.toString()}`);
            })}
          />
        </>
      )}

      {sep}

      {/* ── Copy ────────────────────────────────────────────────────────── */}
      <Item
        icon="⎘"
        label={t('contextMenu.copyName')}
        onClick={run(() => navigator.clipboard.writeText(data.label))}
      />
      <Item
        icon="⌗"
        label={t('actions.copyId')}
        onClick={run(() => navigator.clipboard.writeText(nodeId))}
      />

      {sep}

      {/* ── View / visibility ───────────────────────────────────────────── */}
      <Item
        icon="⊡"
        label={t('actions.fitView')}
        onClick={run(() =>
          fitView({ nodes: [{ id: nodeId }], duration: CANVAS.FIT_VIEW_DURATION, padding: CANVAS.FIT_VIEW_PADDING, maxZoom: CANVAS.FIT_VIEW_MAX_ZOOM })
        )}
      />
      <Item
        icon="✕"
        label={t('tableNode.hide')}
        onClick={run(() => hideNode(nodeId))}
        danger
      />
    </div>
  );
});

NodeContextMenu.displayName = 'NodeContextMenu';
