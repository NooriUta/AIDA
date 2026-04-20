import { memo, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Header } from './Header';
import { FilterToolbar } from './FilterToolbar';
import { FilterToolbarL1 } from './FilterToolbarL1';
import { StatusBar } from './StatusBar';
import { ResizablePanel } from './ResizablePanel';
import { MobileInspectorDrawer } from './MobileInspectorDrawer';
import { LoomCanvas } from '../canvas/LoomCanvas';
import { InspectorPanel } from '../inspector/InspectorPanel';
import { useLoomStore } from '../../stores/loomStore';
import { useHotkeys } from '../../hooks/useHotkeys';
import { useIsMobile } from '../../hooks/useIsMobile';
import { useState } from 'react';

/** Inspector panel maximum width = 40% of the viewport (floored at 480px). */
function useInspectorMaxWidth(): number {
  const compute = () => Math.max(480, Math.round(window.innerWidth * 0.4));
  const [max, setMax] = useState<number>(() =>
    typeof window === 'undefined' ? 480 : compute(),
  );
  useEffect(() => {
    const onResize = () => setMax(compute());
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);
  return max;
}

export const Shell = memo(() => {
  const { t } = useTranslation();
  const {
    viewLevel, jumpTo, selectNode, requestFitView, undo, redo,
    selectedNodeId, inspectorOpen, setInspectorOpen,
  } = useLoomStore();
  const [searchParams, setSearchParams] = useSearchParams();
  const inspectorMaxWidth = useInspectorMaxWidth();
  const isMobile = useIsMobile();

  // Auto-open inspector drawer when a node is selected
  const prevNodeId = useRef<string | null>(null);
  useEffect(() => {
    if (selectedNodeId && selectedNodeId !== prevNodeId.current) {
      setInspectorOpen(true);
    }
    prevNodeId.current = selectedNodeId;
  }, [selectedNodeId, setInspectorOpen]);

  // KNOT → LOOM: auto-navigate when ?pkg= or ?schema= param is present
  useEffect(() => {
    const pkg    = searchParams.get('pkg');
    const schema = searchParams.get('schema');
    if (pkg) {
      setSearchParams({}, { replace: true });
      jumpTo('L2', `pkg-${pkg}`, pkg, 'DaliPackage');
    } else if (schema) {
      setSearchParams({}, { replace: true });
      jumpTo('L2', `schema-${schema}`, schema, 'DaliSchema');
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useHotkeys([
    { key: 'Escape', action: () => selectNode(null), global: true },
    { key: 'f',      action: () => requestFitView() },
    { key: 'z',      ctrl: true,  action: () => undo() },
    { key: 'z',      ctrl: true,  shift: true, action: () => redo() },
  ]);

  return (
    <div style={{
      display: 'grid',
      gridTemplateRows: '42px 1fr 28px',
      height: '100%',
      overflow: 'hidden',
      background: 'var(--seer-bg)',
    }}>
      {/* ── Row 1: Header ─────────────────────────────────────────────────── */}
      <Header />

      {/* ── Row 2: Workspace ──────────────────────────────────────────────── */}
      <div style={{ display: 'flex', overflow: 'hidden', position: 'relative' }}>

        {/* Canvas area = FilterToolbar (level-dependent) + LoomCanvas */}
        <div style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          {viewLevel === 'L1' ? <FilterToolbarL1 /> : <FilterToolbar />}
          <div style={{ flex: 1, overflow: 'hidden', position: 'relative' }}>
            <LoomCanvas />
          </div>
        </div>

        {/* Desktop: side panel always visible. Mobile: right overlay drawer. */}
        {isMobile ? (
          <MobileInspectorDrawer
            open={inspectorOpen}
            onClose={() => setInspectorOpen(false)}
          />
        ) : (
          <ResizablePanel
            side="right"
            defaultWidth={320}
            minWidth={240}
            maxWidth={inspectorMaxWidth}
            title={t('panel.inspector')}
          >
            <InspectorPanel />
          </ResizablePanel>
        )}

      </div>

      {/* ── Row 3: Status bar ─────────────────────────────────────────────── */}
      <StatusBar />
    </div>
  );
});

Shell.displayName = 'Shell';
