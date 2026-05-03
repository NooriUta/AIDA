import { memo, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Header } from './Header';
import { FilterToolbar } from './FilterToolbar';
import { FilterToolbarL1 } from './FilterToolbarL1';
import { StatusBar } from './StatusBar';
import { ResizablePanel } from './ResizablePanel';
import { MobileInspectorDrawer } from './MobileInspectorDrawer';
import { MimirChevronTab } from '../panels/MimirChevronTab';
import { LoomCanvas } from '../canvas/LoomCanvas';
import { InspectorPanel } from '../inspector/InspectorPanel';
import { EventStreamPanel } from '../loom/EventStreamPanel';
import { useLoomStore } from '../../stores/loomStore';
import { useHotkeys } from '../../hooks/useHotkeys';
import { useIsMobile } from '../../hooks/useIsMobile';

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
    selectedNodeId, inspectorOpen, setInspectorOpen, highlightedNodes,
  } = useLoomStore();
  const [searchParams, setSearchParams] = useSearchParams();
  const inspectorMaxWidth = useInspectorMaxWidth();
  const isMobile = useIsMobile();

  /** SD-03: EventStream bottom split toggle. Hidden by default + on mobile (<1200px). */
  const [showEvents, setShowEvents] = useState(false);

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
      gridTemplateRows: 'auto 1fr 28px',
      height: '100%',
      overflow: 'hidden',
      background: 'var(--seer-bg)',
    }}>
      {/* ── Row 1: Header ─────────────────────────────────────────────────── */}
      <Header />

      {/* ── Row 2: Workspace (SD-03: vertical grid split when events shown) ── */}
      {/*
        Outer grid splits the workspace vertically into:
          • top row  — canvas + inspector (always visible)
          • bottom row — EventStreamPanel (desktop ≥1200px, toggle-controlled)
        On mobile or when hidden, only the top row exists (gridTemplateRows: '1fr').
      */}
      <div style={{
        display:           'grid',
        gridTemplateRows:  showEvents && !isMobile ? '65fr 35fr' : '1fr',
        overflow:          'hidden',
        position:          'relative',
        minHeight:         0,
      }}>

        {/* ── Top row: canvas + inspector ──────────────────────────────── */}
        <div style={{ display: 'flex', overflow: 'hidden', position: 'relative', minHeight: 0 }}>

          {/* Canvas area = FilterToolbar (level-dependent) + LoomCanvas */}
          <div style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column', position: 'relative' }}>
            {viewLevel === 'L1' ? <FilterToolbarL1 /> : <FilterToolbar />}
            <div style={{ flex: 1, overflow: 'hidden', position: 'relative' }}>
              <LoomCanvas />

              {/* SD-03: Events toggle button — floating, bottom-left of canvas, desktop only */}
              {!isMobile && (
                <button
                  onClick={() => setShowEvents(v => !v)}
                  style={{
                    position:     'absolute',
                    bottom:       12,
                    left:         12,
                    zIndex:       10,
                    display:      'flex',
                    alignItems:   'center',
                    gap:          5,
                    padding:      '3px 10px',
                    fontSize:     11,
                    background:   'var(--bg1)',
                    border:       '1px solid var(--bd)',
                    borderRadius: 'var(--seer-radius-md)',
                    color:        showEvents ? 'var(--acc)' : 'var(--t3)',
                    cursor:       'pointer',
                    userSelect:   'none',
                    boxShadow:    '0 1px 4px color-mix(in srgb, var(--seer-bg) 60%, transparent)',
                  }}
                  title={showEvents
                    ? t('eventStream.hide', 'Hide event stream')
                    : t('eventStream.show', 'Show event stream')}
                >
                  <span style={{ fontSize: 9, lineHeight: 1 }}>{showEvents ? '▼' : '▲'}</span>
                  {t('eventStream.title', 'Events')}
                </button>
              )}
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
              toggleLabel="K"
            >
              <InspectorPanel />
            </ResizablePanel>
          )}

          {/* Stacked above the Inspector pull-tab — same chevron style, opens
              the MIMIR Copilot sidebar. Desktop only; mobile uses the header
              button. */}
          {!isMobile && <MimirChevronTab />}

        </div>

        {/* ── Bottom row: EventStreamPanel (desktop only, when showEvents) ── */}
        {showEvents && !isMobile && (
          <EventStreamPanel
            highlightNodeIds={highlightedNodes}
            onClose={() => setShowEvents(false)}
          />
        )}

      </div>

      {/* ── Row 3: Status bar ─────────────────────────────────────────────── */}
      <StatusBar />
    </div>
  );
});

Shell.displayName = 'Shell';
