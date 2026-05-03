import { useTranslation } from 'react-i18next';
import { useMimirChatStore } from '../../stores/mimirChatStore';

/**
 * Toolbar trigger for the MIMIR sidebar (TIER2 MT-04).
 * Mount once in the app shell — clicking toggles the sidebar.
 */
export default function MimirToolbarButton() {
  const { t } = useTranslation();
  const open   = useMimirChatStore((s) => s.open);
  const toggle = useMimirChatStore((s) => s.toggle);
  return (
    <button
      type="button"
      className="mimir-toolbar-button"
      aria-pressed={open}
      onClick={toggle}
      title={t('mimir.askTitle')}
    >
      <span className="mimir-dot" aria-hidden="true" />
      {t('mimir.ask')}
    </button>
  );
}
