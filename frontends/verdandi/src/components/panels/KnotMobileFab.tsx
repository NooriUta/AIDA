import { memo } from 'react';
import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useIsMobile } from '../../hooks/useIsMobile';
import { useAuthStore } from '../../stores/authStore';
import { useLoomStore } from '../../stores/loomStore';

/**
 * Floating "K" action button for the KNOT Inspector (mobile). Toggles the
 * MobileInspectorDrawer that holds <InspectorPanel />. Stacks above the MIMIR
 * "M" FAB. Hidden on desktop and on /login.
 *
 * Note: this is the KNOT *Inspector* — the per-node context panel — not the
 * /knot route navigation. KNOT-page nav lives in the header sub-module nav.
 */
export const KnotMobileFab = memo(() => {
  const { t } = useTranslation();
  const { pathname } = useLocation();
  const isMobile = useIsMobile();
  const user = useAuthStore((s) => s.user);
  const inspectorOpen    = useLoomStore((s) => s.inspectorOpen);
  const setInspectorOpen = useLoomStore((s) => s.setInspectorOpen);

  if (!isMobile) return null;
  if (!user) return null;
  if (pathname.endsWith('/login') || pathname === '/login') return null;

  return (
    <button
      type="button"
      onClick={() => setInspectorOpen(!inspectorOpen)}
      title={t('panel.inspector')}
      aria-pressed={inspectorOpen}
      aria-label={t('panel.inspector')}
      style={{
        position: 'fixed',
        right: 14,
        // Stack above the MIMIR FAB (which sits at bottom 14 with 40 px height).
        bottom: 14 + 40 + 10,
        width: 40,
        height: 40,
        borderRadius: '50%',
        background: inspectorOpen
          ? 'color-mix(in srgb, var(--acc) 30%, var(--bg1))'
          : 'var(--bg1)',
        border: '1px solid',
        borderColor: inspectorOpen
          ? 'color-mix(in srgb, var(--acc) 60%, var(--bd))'
          : 'var(--bd)',
        color: inspectorOpen ? 'var(--acc)' : 'var(--t1)',
        fontSize: 15,
        fontWeight: 700,
        cursor: 'pointer',
        boxShadow: '0 2px 8px rgba(0,0,0,0.28)',
        zIndex: 95,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 0,
      }}
    >
      K
    </button>
  );
});

KnotMobileFab.displayName = 'KnotMobileFab';
