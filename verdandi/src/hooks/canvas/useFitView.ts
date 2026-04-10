import { useCallback, useEffect } from 'react';
import { useReactFlow, type OnMoveEnd } from '@xyflow/react';

import { useLoomStore } from '../../stores/loomStore';
import { CANVAS } from '../../utils/constants';

/**
 * Handles programmatic fitView requests (search focus, L1 return)
 * and zoom tracking for the status bar.
 *
 * Must be used inside ReactFlowProvider (calls useReactFlow).
 */
export function useFitView(layouting: boolean) {
  const { fitView } = useReactFlow();
  const { fitViewRequest, clearFitViewRequest, setZoom } = useLoomStore();

  // ── Programmatic fitView: triggered by search (focus node) or L1 return ──
  useEffect(() => {
    if (!fitViewRequest || layouting) return;
    const req = fitViewRequest;
    // Small delay: let React Flow finish painting the new nodes before fitting
    const timer = setTimeout(() => {
      if (req.type === 'full') {
        fitView({ duration: CANVAS.FIT_VIEW_DURATION, padding: CANVAS.FIT_VIEW_PADDING });
      } else {
        fitView({
          nodes:   [{ id: req.nodeId }],
          duration: 600,
          padding:  0.08,
          maxZoom:  1.8,
          minZoom:  0.15,
        });
      }
      clearFitViewRequest();
    }, 200);
    return () => clearTimeout(timer);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fitViewRequest, layouting]);

  // ── Zoom tracking for status bar ────────────────────────────────────────
  const onMoveEnd: OnMoveEnd = useCallback((_: unknown, viewport) => {
    setZoom(viewport.zoom);
  }, [setZoom]);

  return { onMoveEnd };
}
