import { memo } from 'react';
import { NodeToolbar, Position } from '@xyflow/react';
import { ChevronLeft, ChevronRight, CheckCircle, Loader2 } from 'lucide-react';
import { useLoomStore } from '../../../stores/loomStore';

interface NodeExpandButtonsProps {
  nodeId: string;
  /** Pass `selected` prop from the parent node — buttons appear when node is selected */
  show: boolean;
}

// Shared keyframe already injected by SpinnerSVG in LoomCanvas; reuse it.
const SPIN_STYLE: React.CSSProperties = { animation: 'spin 0.8s linear infinite' };

export const NodeExpandButtons = memo(({ nodeId, show }: NodeExpandButtonsProps) => {
  const expandRequest        = useLoomStore((s) => s.expandRequest);
  const expandedUpstreamIds  = useLoomStore((s) => s.expandedUpstreamIds);
  const expandedDownstreamIds = useLoomStore((s) => s.expandedDownstreamIds);
  const requestExpand        = useLoomStore((s) => s.requestExpand);

  const isLoadingUp   = expandRequest?.nodeId === nodeId && expandRequest.direction === 'upstream';
  const isLoadingDown = expandRequest?.nodeId === nodeId && expandRequest.direction === 'downstream';
  const upDone        = expandedUpstreamIds.has(nodeId);
  const downDone      = expandedDownstreamIds.has(nodeId);

  const makeBtn = (
    done: boolean,
    loading: boolean,
    onClick: () => void,
    title: string,
    icon: React.ReactNode,
  ) => {
    const disabled = done || loading;
    return (
      <button
        title={title}
        disabled={disabled}
        onClick={(e) => { e.stopPropagation(); e.preventDefault(); if (!disabled) onClick(); }}
        onDoubleClick={(e) => { e.stopPropagation(); e.preventDefault(); }}
        style={{
          display:        'flex',
          alignItems:     'center',
          justifyContent: 'center',
          width:          '22px',
          height:         '22px',
          borderRadius:   '50%',
          border:         `1px solid ${done ? 'var(--suc)' : 'var(--bd)'}`,
          background:     'var(--bg2)',
          color:          done ? 'var(--suc)' : loading ? 'var(--t3)' : 'var(--acc)',
          cursor:         disabled ? 'default' : 'pointer',
          opacity:        done ? 0.6 : 1,
          padding:        0,
          flexShrink:     0,
          boxShadow:      '0 1px 4px rgba(0,0,0,0.35)',
          transition:     'background 0.12s, color 0.12s, border-color 0.12s',
        }}
      >
        {icon}
      </button>
    );
  };

  return (
    <>
      {/* ── Upstream: left side ─────────────────────────────────────────── */}
      <NodeToolbar position={Position.Left} isVisible={show} offset={6}>
        {makeBtn(
          upDone,
          isLoadingUp,
          () => requestExpand(nodeId, 'upstream'),
          upDone ? 'Upstream already expanded' : 'Expand upstream',
          isLoadingUp
            ? <Loader2 size={12} style={SPIN_STYLE} />
            : upDone
            ? <CheckCircle size={12} />
            : <ChevronLeft size={13} strokeWidth={2.5} />,
        )}
      </NodeToolbar>

      {/* ── Downstream: right side ──────────────────────────────────────── */}
      <NodeToolbar position={Position.Right} isVisible={show} offset={6}>
        {makeBtn(
          downDone,
          isLoadingDown,
          () => requestExpand(nodeId, 'downstream'),
          downDone ? 'Downstream already expanded' : 'Expand downstream',
          isLoadingDown
            ? <Loader2 size={12} style={SPIN_STYLE} />
            : downDone
            ? <CheckCircle size={12} />
            : <ChevronRight size={13} strokeWidth={2.5} />,
        )}
      </NodeToolbar>
    </>
  );
});

NodeExpandButtons.displayName = 'NodeExpandButtons';
