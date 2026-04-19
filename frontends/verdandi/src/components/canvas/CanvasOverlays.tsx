import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { SpinnerSVG } from '../ui/SpinnerSVG';
import { LoadingDots } from '../ui/LoadingDots';

interface Props {
  isLoading:       boolean;
  isLargeGraph:    boolean;
  layouting:       boolean;
  pendingNodeCount: number;
  statusKey:       string | null;
  layoutWarning:   string | null;
  triggerFullLayout: () => void;
  activeQueryLoading: boolean;
}

export const CanvasOverlays = memo(({
  isLoading, isLargeGraph, layouting, pendingNodeCount,
  statusKey, layoutWarning, triggerFullLayout, activeQueryLoading,
}: Props) => {
  const { t } = useTranslation();
  return (
    <>
      {isLoading && (
        <div style={{
          position: 'absolute', inset: 0, zIndex: 200,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: 'color-mix(in srgb, var(--bg0) 85%, transparent)',
          backdropFilter: 'blur(3px)', pointerEvents: 'none',
        }}>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
            <SpinnerSVG size={isLargeGraph ? 36 : 28} />
            <span style={{ fontSize: '11px', color: 'var(--t3)', letterSpacing: '0.07em' }}>
              {t(activeQueryLoading ? 'status.loading' : 'canvas.computingLayout')}
            </span>
            {isLargeGraph && layouting && (
              <>
                <span style={{ fontSize: '10px', color: 'var(--t3)', letterSpacing: '0.05em' }}>
                  {t('canvas.nodeCount', { count: pendingNodeCount })}
                </span>
                <LoadingDots />
              </>
            )}
          </div>
        </div>
      )}

      {statusKey && !isLoading && (
        <div style={{
          position: 'absolute', inset: 0, zIndex: 100,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          pointerEvents: 'none',
        }}>
          <span style={{ fontSize: '13px', color: 'var(--t3)', letterSpacing: '0.04em' }}>
            {t(statusKey)}
          </span>
        </div>
      )}

      {layoutWarning && !isLoading && (
        <div style={{
          position: 'absolute', top: 8, left: '50%', transform: 'translateX(-50%)',
          background: 'color-mix(in srgb, var(--wrn) 12%, transparent)',
          border: '1px solid color-mix(in srgb, var(--wrn) 35%, transparent)',
          borderRadius: 6, padding: '5px 12px',
          fontSize: 12, color: 'var(--wrn)',
          display: 'flex', alignItems: 'center', gap: 8, zIndex: 10,
          whiteSpace: 'nowrap',
        }}>
          ⚠ {layoutWarning}
          <button
            onClick={triggerFullLayout}
            style={{ fontSize: 11, color: 'var(--acc)', background: 'none',
                     border: 'none', cursor: 'pointer', padding: 0 }}
          >
            {t('canvas.computeFullLayout')}
          </button>
        </div>
      )}
    </>
  );
});

CanvasOverlays.displayName = 'CanvasOverlays';
