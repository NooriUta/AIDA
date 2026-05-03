import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useMimirChatStore } from '../../stores/mimirChatStore';

/**
 * Small chevron pull-tab that toggles the MIMIR Copilot sidebar.
 *
 * Visually mirrors the ResizablePanel collapse chevron — same width, same
 * vertical label — and is positioned just above it on the right edge of the
 * canvas area, so the user reads two stacked tabs: "INSPECTOR" + "MIMIR".
 *
 * Desktop only. Mobile uses the header MimirToolbarButton.
 */
export const MimirChevronTab = memo(() => {
  const { t } = useTranslation();
  const open    = useMimirChatStore((s) => s.open);
  const toggle  = useMimirChatStore((s) => s.toggle);

  return (
    <button
      type="button"
      onClick={toggle}
      title={t('mimir.askTitle')}
      aria-pressed={open}
      aria-label={t('mimir.ask')}
      style={{
        position: 'absolute',
        // Stack above the Inspector pull-tab. Inspector sits at top: 50%; we
        // sit ~100px higher so both tabs are clearly separate touch targets.
        top: 'calc(50% - 70px)',
        right: open ? '-14px' : '-18px',
        transform: 'translateY(-50%)',
        width: '18px',
        height: '60px',
        background: open
          ? 'color-mix(in srgb, var(--acc) 14%, var(--seer-surface-2))'
          : 'var(--seer-surface-2)',
        border: '1px solid',
        borderColor: open
          ? 'color-mix(in srgb, var(--acc) 50%, var(--seer-border))'
          : 'var(--seer-border)',
        borderRadius: '4px 0 0 4px',
        cursor: 'pointer',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 4,
        color: open ? 'var(--acc)' : 'var(--seer-text-muted)',
        zIndex: 20,
        padding: 0,
      }}
    >
      <span style={{
        fontSize: 12,
        fontWeight: 700,
        textTransform: 'uppercase',
        lineHeight: 1,
      }}>
        M
      </span>
    </button>
  );
});

MimirChevronTab.displayName = 'MimirChevronTab';
