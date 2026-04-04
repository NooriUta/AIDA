import { memo } from 'react';
import { Header } from './Header';
import { FilterToolbar } from './FilterToolbar';
import { FilterToolbarL1 } from './FilterToolbarL1';
import { StatusBar } from './StatusBar';
import { ResizablePanel } from './ResizablePanel';
import { LoomCanvas } from '../canvas/LoomCanvas';
import { useLoomStore } from '../../stores/loomStore';
import { Search } from 'lucide-react';

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
        <ResizablePanel side="left" defaultWidth={240} minWidth={160} maxWidth={400} title="Explorer">
          <SearchPanelPlaceholder />
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

function SearchPanelPlaceholder() {
  return (
    <div style={{ padding: '4px' }}>
      {/* Search input stub */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '6px',
        padding: '6px 8px',
        background: 'var(--seer-surface-2)',
        border: '1px solid var(--seer-border)',
        borderRadius: '6px',
        marginBottom: '12px',
      }}>
        <Search size={13} color="var(--seer-text-muted)" />
        <span style={{ fontSize: '12px', color: 'var(--seer-text-muted)' }}>Search… (⌘F)</span>
      </div>

      <p style={{ fontSize: '11px', color: 'var(--seer-text-muted)', margin: 0 }}>
        Search panel — Phase 2
      </p>
    </div>
  );
}

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
