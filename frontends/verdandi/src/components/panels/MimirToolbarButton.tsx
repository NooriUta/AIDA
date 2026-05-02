import { useMimirChatStore } from '../../stores/mimirChatStore';

/**
 * Toolbar trigger for the MIMIR sidebar (TIER2 MT-04).
 * Mount once in the app shell — clicking toggles the sidebar.
 */
export default function MimirToolbarButton() {
  const open   = useMimirChatStore((s) => s.open);
  const toggle = useMimirChatStore((s) => s.toggle);
  return (
    <button
      type="button"
      className="mimir-toolbar-button"
      aria-pressed={open}
      onClick={toggle}
      title={open ? 'Close MIMIR' : 'Ask MIMIR'}
    >
      <span className="mimir-dot" aria-hidden="true" />
      Ask MIMIR
    </button>
  );
}
