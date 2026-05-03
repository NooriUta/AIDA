import { memo, useCallback, useRef, useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface ResizablePanelProps {
  side: 'left' | 'right';
  defaultWidth?: number;
  minWidth?: number;
  maxWidth?: number;
  children?: React.ReactNode;
  title?: string;
  /** Vertical label rendered on the collapsed-state pull-tab. When omitted,
   *  the tab is the original 36 px chevron-only button. */
  toggleLabel?: string;
}

export const ResizablePanel = memo(({
  side,
  defaultWidth = 240,
  minWidth = 160,
  maxWidth = 480,
  children,
  title,
  toggleLabel,
}: ResizablePanelProps) => {
  const { t } = useTranslation();
  const [width, setWidth]         = useState(defaultWidth);
  const [collapsed, setCollapsed] = useState(true);
  const dragging = useRef(false);
  const startX   = useRef(0);
  const startW   = useRef(0);

  const onMouseDown = useCallback((e: React.MouseEvent) => {
    dragging.current = true;
    startX.current   = e.clientX;
    startW.current   = width;
    e.preventDefault();

    const onMove = (ev: MouseEvent) => {
      if (!dragging.current) return;
      const delta = side === 'left'
        ? ev.clientX - startX.current
        : startX.current - ev.clientX;
      const next = Math.max(minWidth, Math.min(maxWidth, startW.current + delta));
      setWidth(next);
    };
    const onUp = () => {
      dragging.current = false;
      window.removeEventListener('mousemove', onMove);
      window.removeEventListener('mouseup', onUp);
    };
    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onUp);
  }, [side, width, minWidth, maxWidth]);

  const isLeft = side === 'left';

  return (
    <div style={{
      display: 'flex',
      flexDirection: isLeft ? 'row' : 'row-reverse',
      flexShrink: 0,
      position: 'relative',
    }}>
      {/* Panel body */}
      <div style={{
        width: collapsed ? 0 : width,
        overflow: 'hidden',
        transition: collapsed ? 'width 0.2s ease' : undefined,
        background: 'var(--panel-bg)',
        borderRight:  isLeft  ? '1px solid var(--seer-border)' : 'none',
        borderLeft:   !isLeft ? '1px solid var(--seer-border)' : 'none',
        display: 'flex',
        flexDirection: 'column',
      }}>
        {/* Panel header */}
        {title && (
          <div style={{
            padding: '8px 12px',
            fontSize: '11px',
            fontWeight: 600,
            letterSpacing: '0.06em',
            color: 'var(--seer-text-muted)',
            borderBottom: '1px solid var(--seer-border)',
            textTransform: 'uppercase',
            flexShrink: 0,
          }}>
            {title}
          </div>
        )}
        <div style={{ flex: 1, overflow: 'auto', padding: '8px' }}>
          {!collapsed && children}
        </div>
      </div>

      {/* Resize handle */}
      {!collapsed && (
        <div
          onMouseDown={onMouseDown}
          style={{
            width: '4px',
            cursor: 'col-resize',
            background: 'transparent',
            flexShrink: 0,
            transition: 'background 0.1s',
            zIndex: 10,
          }}
          onMouseEnter={(e) => {
            (e.currentTarget as HTMLDivElement).style.background = 'var(--seer-accent)';
          }}
          onMouseLeave={(e) => {
            (e.currentTarget as HTMLDivElement).style.background = 'transparent';
          }}
        />
      )}

      {/* Collapse toggle — chevron-only tab, or chevron + vertical label
          when `toggleLabel` is provided (so two stacked tabs can be told apart). */}
      <button
        onClick={() => setCollapsed((c) => !c)}
        title={t(collapsed ? 'panel.expand' : 'panel.collapse')}
        aria-expanded={!collapsed}
        aria-label={toggleLabel ?? t(collapsed ? 'panel.expand' : 'panel.collapse')}
        style={{
          position: 'absolute',
          top: '50%',
          [isLeft ? 'right' : 'left']: collapsed ? '-18px' : '-14px',
          transform: 'translateY(-50%)',
          width: '18px',
          height: toggleLabel ? '60px' : '36px',
          background: 'var(--seer-surface-2)',
          border: '1px solid var(--seer-border)',
          borderRadius: isLeft ? '0 4px 4px 0' : '4px 0 0 4px',
          cursor: 'pointer',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 4,
          color: 'var(--seer-text-muted)',
          zIndex: 20,
          padding: 0,
        }}
      >
        {/* Chevron icon only when there is no letter label — labelled tabs
            (K, M, …) read better without the < / > arrow distraction. */}
        {!toggleLabel && (isLeft
          ? (collapsed ? <ChevronRight size={12} /> : <ChevronLeft size={12} />)
          : (collapsed ? <ChevronLeft  size={12} /> : <ChevronRight size={12} />)
        )}
        {toggleLabel && (
          <span style={{
            fontSize: 12,
            fontWeight: 700,
            letterSpacing: 0,
            textTransform: 'uppercase',
            lineHeight: 1,
          }}>
            {toggleLabel}
          </span>
        )}
      </button>
    </div>
  );
});

ResizablePanel.displayName = 'ResizablePanel';
