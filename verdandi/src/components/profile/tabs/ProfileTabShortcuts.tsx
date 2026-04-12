import { memo } from 'react';
import { useTranslation } from 'react-i18next';

type ShortcutGroup = { labelKey: string; items: { action: string; keys: string[] }[] };

const SHORTCUT_GROUPS: ShortcutGroup[] = [
  {
    labelKey: 'profile.shortcuts.navigation',
    items: [
      { action: 'Drill down in node',  keys: ['Enter'] },
      { action: 'Go back / deselect', keys: ['Esc'] },
      { action: 'Switch to L1',        keys: ['1'] },
      { action: 'Switch to L2',        keys: ['2'] },
      { action: 'Cycle through nodes', keys: ['Tab'] },
    ],
  },
  {
    labelKey: 'profile.shortcuts.canvas',
    items: [
      { action: 'Command palette', keys: ['⌘', 'K'] },
      { action: 'Search',          keys: ['⌘', 'F'] },
      { action: 'Fit to screen',   keys: ['⌘', '0'] },
      { action: 'Zoom in',         keys: ['⌘', '+'] },
      { action: 'Zoom out',        keys: ['⌘', '−'] },
    ],
  },
  {
    labelKey: 'profile.shortcuts.panels',
    items: [
      { action: 'Toggle Search panel',    keys: ['⌘', 'B'] },
      { action: 'Toggle Inspector panel', keys: ['⌘', 'I'] },
      { action: 'Toggle theme',           keys: ['D'] },
    ],
  },
  {
    labelKey: 'profile.shortcuts.graph',
    items: [
      { action: 'Undo (hide/expand)',  keys: ['⌘', 'Z'] },
      { action: 'Redo',               keys: ['⌘', '⇧', 'Z'] },
      { action: 'Focus search input', keys: ['/'] },
      { action: 'Fit view',           keys: ['F'] },
    ],
  },
  {
    labelKey: 'profile.shortcuts.export',
    items: [
      { action: 'Export PNG', keys: ['⌘', 'E'] },
      { action: 'Export SVG', keys: ['⌘', '⇧', 'E'] },
    ],
  },
];

export const ProfileTabShortcuts = memo(() => {
  const { t } = useTranslation();

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.shortcuts')}
      </div>

      {SHORTCUT_GROUPS.map((group) => (
        <div key={group.labelKey} style={{ marginBottom: '20px' }}>
          <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '10px', textTransform: 'uppercase' }}>
            {t(group.labelKey)}
          </div>
          <div style={{ borderRadius: 'var(--seer-radius-md)', overflow: 'hidden', border: '1px solid var(--bd)' }}>
            {group.items.map((item, i) => (
              <div key={i} style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '8px 12px',
                background: i % 2 === 0 ? 'var(--bg2)' : 'color-mix(in srgb, var(--bg2) 60%, var(--bg3))',
                borderTop: i > 0 ? '1px solid var(--bd)' : 'none',
              }}>
                <span style={{ fontSize: '12px', color: 'var(--t1)' }}>{item.action}</span>
                <div style={{ display: 'flex', gap: '3px', alignItems: 'center' }}>
                  {item.keys.map((k, ki) => (
                    <kbd key={ki} style={{
                      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                      minWidth: '22px', height: '20px', padding: '0 5px',
                      background: 'var(--bg3)', border: '1px solid var(--bdh)',
                      borderRadius: 'var(--seer-radius-sm)', fontSize: '11px', fontFamily: 'var(--mono)',
                      color: 'var(--t2)', boxShadow: '0 1px 0 var(--bdh)',
                    }}>
                      {k}
                    </kbd>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}

      <div style={{ padding: '10px 14px', background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', marginTop: '4px' }}>
        <div style={{ fontSize: '11px', color: 'var(--t3)' }}>
          ⌘ = Ctrl on Windows/Linux · ⇧ = Shift
        </div>
      </div>
    </div>
  );
});

ProfileTabShortcuts.displayName = 'ProfileTabShortcuts';
