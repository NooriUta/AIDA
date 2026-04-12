import { memo, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Header } from './Header';
import { FilterToolbar } from './FilterToolbar';
import { FilterToolbarL1 } from './FilterToolbarL1';
import { StatusBar } from './StatusBar';
import { ResizablePanel } from './ResizablePanel';
import { LoomCanvas } from '../canvas/LoomCanvas';
// SearchPanel removed — search is now via SearchPalette (/)
import { InspectorPanel } from '../inspector/InspectorPanel';
import { useLoomStore } from '../../stores/loomStore';
import { useHotkeys } from '../../hooks/useHotkeys';

export const Shell = memo(() => {
  const { t } = useTranslation();
  const { viewLevel, jumpTo, selectNode, requestFitView, undo, redo } = useLoomStore();
  const [searchParams, setSearchParams] = useSearchParams();

  // KNOT → LOOM: auto-navigate to package when ?pkg= param is present
  useEffect(() => {
    const pkg = searchParams.get('pkg');
    if (!pkg) return;
    setSearchParams({}, { replace: true });
    jumpTo('L2', `pkg-${pkg}`, pkg, 'DaliPackage');
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ── Keyboard shortcuts (canvas-level) ────────────────────────────────────────
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

        {/* Right panel — KNOT Inspector */}
        <ResizablePanel side="right" defaultWidth={300} minWidth={200} maxWidth={480} title={t('panel.inspector')}>
          <InspectorPanel />
        </ResizablePanel>

      </div>

      {/* ── Row 3: Status bar ─────────────────────────────────────────────── */}
      <StatusBar />
    </div>
  );
});

Shell.displayName = 'Shell';
