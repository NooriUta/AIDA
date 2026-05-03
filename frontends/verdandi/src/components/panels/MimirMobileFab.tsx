import { memo } from 'react';
import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useMimirChatStore } from '../../stores/mimirChatStore';
import { useIsMobile } from '../../hooks/useIsMobile';
import { useAuthStore } from '../../stores/authStore';

/**
 * Floating "M" action button for MIMIR on mobile, anchored to the bottom-right
 * corner — matches the bottom-anchored KNOT mobile entry point. Hidden on
 * desktop (the right-edge MimirChevronTab covers that case) and on /login.
 */
export const MimirMobileFab = memo(() => {
  const { t } = useTranslation();
  const { pathname } = useLocation();
  const isMobile = useIsMobile();
  const user = useAuthStore((s) => s.user);
  const open    = useMimirChatStore((s) => s.open);
  const toggle  = useMimirChatStore((s) => s.toggle);

  if (!isMobile) return null;
  if (!user) return null;
  if (pathname.endsWith('/login') || pathname === '/login') return null;

  return (
    <button
      type="button"
      onClick={toggle}
      title={t('mimir.askTitle')}
      aria-pressed={open}
      aria-label={t('mimir.ask')}
      style={{
        position: 'fixed',
        right: 14,
        bottom: 14,
        width: 40,
        height: 40,
        borderRadius: '50%',
        background: open
          ? 'color-mix(in srgb, var(--acc) 30%, var(--bg1))'
          : 'var(--bg1)',
        border: '1px solid',
        borderColor: open
          ? 'color-mix(in srgb, var(--acc) 60%, var(--bd))'
          : 'var(--bd)',
        color: open ? 'var(--acc)' : 'var(--t1)',
        fontSize: 15,
        fontWeight: 700,
        letterSpacing: 0,
        cursor: 'pointer',
        boxShadow: '0 2px 8px rgba(0,0,0,0.28)',
        zIndex: 95,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 0,
      }}
    >
      M
    </button>
  );
});

MimirMobileFab.displayName = 'MimirMobileFab';
