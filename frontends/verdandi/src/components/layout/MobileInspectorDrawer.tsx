import { memo, useRef } from 'react';
import { X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { InspectorPanel } from '../inspector/InspectorPanel';

interface Props {
  open: boolean;
  onClose: () => void;
}

/** Bottom-sheet inspector for mobile (< 640 px).
 *  Slides up from the bottom over the canvas — does NOT push the canvas. */
export const MobileInspectorDrawer = memo(({ open, onClose }: Props) => {
  const { t } = useTranslation();
  const panelRef   = useRef<HTMLDivElement>(null);
  const touchStartY = useRef(0);

  const handleTouchStart = (e: React.TouchEvent) => {
    touchStartY.current = e.touches[0].clientY;
  };
  const handleTouchMove = (e: React.TouchEvent) => {
    const delta = e.touches[0].clientY - touchStartY.current;
    if (delta > 0 && panelRef.current) {
      panelRef.current.style.transition = 'none';
      panelRef.current.style.transform  = `translateY(${delta}px)`;
    }
  };
  const handleTouchEnd = (e: React.TouchEvent) => {
    const delta = e.changedTouches[0].clientY - touchStartY.current;
    if (panelRef.current) {
      panelRef.current.style.transition = 'transform 0.25s ease';
      if (delta > 80) {
        panelRef.current.style.transform = 'translateY(100%)';
        onClose();
      } else {
        panelRef.current.style.transform = 'translateY(0)';
      }
    }
  };

  return (
    <>
      {/* Backdrop — tap to close */}
      <div
        onClick={onClose}
        style={{
          position: 'absolute', inset: 0,
          background: 'rgba(0,0,0,0.35)',
          zIndex: 40,
          opacity: open ? 1 : 0,
          pointerEvents: open ? 'auto' : 'none',
          transition: 'opacity 0.22s ease',
        }}
      />

      {/* Panel — slides up from bottom */}
      <div
        ref={panelRef}
        role="complementary"
        aria-label={t('panel.inspector')}
        style={{
          position:    'absolute',
          left:        0,
          right:       0,
          bottom:      0,
          height:      '60vh',
          zIndex:      50,
          background:  'var(--panel-bg, var(--bg1))',
          borderTop:   '1px solid var(--seer-border, var(--bd))',
          borderRadius: '12px 12px 0 0',
          display:     'flex',
          flexDirection: 'column',
          transform:   open ? 'translateY(0)' : 'translateY(100%)',
          transition:  'transform 0.25s ease',
          boxShadow:   open ? '0 -4px 24px rgba(0,0,0,0.28)' : 'none',
        }}
      >
        {/* Drag handle + title bar — touch here to swipe-down-close */}
        <div
          onTouchStart={handleTouchStart}
          onTouchMove={handleTouchMove}
          onTouchEnd={handleTouchEnd}
          style={{
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            padding: '0 12px',
            height: 44,
            borderBottom: '1px solid var(--seer-border, var(--bd))',
            flexShrink: 0,
            background: 'var(--bg0)',
            borderRadius: '12px 12px 0 0',
            touchAction: 'none',
          }}>
          {/* Drag handle pill */}
          <div style={{
            position: 'absolute', top: 8, left: '50%', transform: 'translateX(-50%)',
            width: 36, height: 4, borderRadius: 2,
            background: 'var(--bd)',
          }} />
          <span style={{
            fontSize: '11px', fontWeight: 600,
            color: 'var(--seer-text-muted, var(--t3))',
            letterSpacing: '0.06em', textTransform: 'uppercase',
          }}>
            {t('panel.inspector')}
          </span>
          <button
            onClick={onClose}
            aria-label={t('panel.collapse')}
            style={{
              background: 'transparent', border: 'none',
              color: 'var(--t3)', cursor: 'pointer',
              padding: '6px', display: 'flex', alignItems: 'center',
              borderRadius: 4,
            }}
          >
            <X size={15} />
          </button>
        </div>

        {/* Content */}
        <div style={{ flex: 1, overflow: 'auto', padding: '8px' }}>
          {open && <InspectorPanel />}
        </div>
      </div>
    </>
  );
});

MobileInspectorDrawer.displayName = 'MobileInspectorDrawer';
