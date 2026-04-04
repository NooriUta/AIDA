import { memo } from 'react';
import { Header } from './Header';
import { FilterToolbar } from './FilterToolbar';
import { FilterToolbarL1 } from './FilterToolbarL1';
import { StatusBar } from './StatusBar';
import { ResizablePanel } from './ResizablePanel';
import { LoomCanvas } from '../canvas/LoomCanvas';
import { SearchPanel } from '../panels/SearchPanel';
import { useLoomStore } from '../../stores/loomStore';

export const Shell = memo(() => {
  const { viewLevel } = useLoomStore();
  return (
    <div style={{
      display: 'grid',
      gridTemplateRows: '48px 1fr 28px',
      height: '100vh',
      overflow: 'hidden',
      background: 'var(--seer-bg)',
    }}>
      {/* ── Row 1: Header ─────────────────────────────────────────────────── */}
      <Header />

      {/* ── Row 2: Workspace ──────────────────────────────────────────────── */}
      <div style={{ display: 'flex', overflow: 'hidden', position: 'relative' }}>

        {/* Left panel — Search / Explorer */}
        <ResizablePanel side="left" defaultWidth={240} minWidth={160} maxWidth={600} title="Explorer">
          <SearchPanel />
        </ResizablePanel>

        {/* Canvas area = FilterToolbar (level-dependent) + LoomCanvas */}
        <div style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          {viewLevel === 'L1' ? <FilterToolbarL1 /> : <FilterToolbar />}
          <div style={{ flex: 1, overflow: 'hidden', position: 'relative' }}>
            <LoomCanvas />
          </div>
        </div>

        {/* Right panel — KNOT Inspector */}
        <ResizablePanel side="right" defaultWidth={300} minWidth={200} maxWidth={480} title="KNOT Inspector">
          <KnotPanelPlaceholder />
        </ResizablePanel>

      </div>

      {/* ── Row 3: Status bar ─────────────────────────────────────────────── */}
      <StatusBar />
    </div>
  );
});

Shell.displayName = 'Shell';

// ── Placeholder panels ────────────────────────────────────────────────────────

function KnotPanelPlaceholder() {
  return (
    <div style={{ padding: '4px' }}>
      <p style={{ fontSize: '11px', color: 'var(--seer-text-muted)', margin: 0 }}>
        Select a node to inspect its details.
      </p>
      <p style={{ fontSize: '11px', color: 'var(--seer-border-2)', margin: '8px 0 0' }}>
        KNOT Inspector — Phase 2
      </p>
    </div>
  );
}
