import { memo, useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '../../stores/authStore';
import { ProfileTabProfile }       from './tabs/ProfileTabProfile';
import { ProfileTabSecurity }      from './tabs/ProfileTabSecurity';
import { ProfileTabAccess }        from './tabs/ProfileTabAccess';
import { ProfileTabAppearance }    from './tabs/ProfileTabAppearance';
import { ProfileTabGraph }         from './tabs/ProfileTabGraph';
import { ProfileTabActivity }      from './tabs/ProfileTabActivity';
import { ProfileTabNotifications } from './tabs/ProfileTabNotifications';
import { ProfileTabFavorites }     from './tabs/ProfileTabFavorites';
import { ProfileTabShortcuts }     from './tabs/ProfileTabShortcuts';
import { ProfileTabTokens }        from './tabs/ProfileTabTokens';

type TabId =
  | 'profile' | 'security' | 'access'
  | 'appearance' | 'graph'
  | 'activity' | 'notifications' | 'favorites' | 'shortcuts' | 'tokens';

interface NavSection {
  labelKey: string;
  items: { id: TabId; labelKey: string; icon: string }[];
}

const NAV_SECTIONS: NavSection[] = [
  {
    labelKey: 'profile.account',
    items: [
      { id: 'profile',       labelKey: 'profile.tabs.profile',       icon: '○' },
      { id: 'security',      labelKey: 'profile.tabs.security',      icon: '□' },
      { id: 'access',        labelKey: 'profile.tabs.access',        icon: '◇' },
    ],
  },
  {
    labelKey: 'profile.interface',
    items: [
      { id: 'appearance',    labelKey: 'profile.tabs.appearance',    icon: '◎' },
      { id: 'graph',         labelKey: 'profile.tabs.graph',         icon: '◈' },
    ],
  },
  {
    labelKey: 'profile.system',
    items: [
      { id: 'activity',      labelKey: 'profile.tabs.activity',      icon: '◻' },
      { id: 'notifications', labelKey: 'profile.tabs.notifications', icon: '△' },
      { id: 'favorites',     labelKey: 'profile.tabs.favorites',     icon: '★' },
      { id: 'shortcuts',     labelKey: 'profile.tabs.shortcuts',     icon: '⌨' },
      { id: 'tokens',        labelKey: 'profile.tabs.tokens',        icon: '⊡' },
    ],
  },
];

interface Props { onClose: () => void }

export const ProfileModal = memo(({ onClose }: Props) => {
  const { t } = useTranslation();
  const { user, logout } = useAuthStore();
  const [activeTab, setActiveTab] = useState<TabId>('profile');
  const [visible, setVisible] = useState(false);

  // mount animation
  useEffect(() => { requestAnimationFrame(() => setVisible(true)); }, []);

  // close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') handleClose(); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleClose() {
    setVisible(false);
    setTimeout(onClose, 200);
  }

  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';

  function renderTab() {
    switch (activeTab) {
      case 'profile':       return <ProfileTabProfile />;
      case 'security':      return <ProfileTabSecurity />;
      case 'access':        return <ProfileTabAccess />;
      case 'appearance':    return <ProfileTabAppearance />;
      case 'graph':         return <ProfileTabGraph />;
      case 'activity':      return <ProfileTabActivity />;
      case 'notifications': return <ProfileTabNotifications />;
      case 'favorites':     return <ProfileTabFavorites />;
      case 'shortcuts':     return <ProfileTabShortcuts />;
      case 'tokens':        return <ProfileTabTokens />;
    }
  }

  return (
    <div
      onClick={(e) => { if (e.target === e.currentTarget) handleClose(); }}
      style={{
        position: 'fixed', inset: 0,
        background: 'rgba(0,0,0,0.55)',
        backdropFilter: 'blur(3px)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        zIndex: 200,
        opacity: visible ? 1 : 0,
        transition: 'opacity 0.2s ease',
      }}
    >
      {/* Modal window */}
      <div style={{
        width: '820px', maxWidth: '95vw',
        height: '580px', maxHeight: '90vh',
        background: 'var(--bg1)',
        border: '1px solid var(--bd)',
        borderRadius: 'var(--seer-radius-xl)',
        display: 'flex', flexDirection: 'column',
        overflow: 'hidden',
        transform: visible ? 'translateY(0) scale(1)' : 'translateY(12px) scale(0.98)',
        transition: 'transform 0.22s cubic-bezier(0.16,1,0.3,1), opacity 0.18s ease',
        opacity: visible ? 1 : 0,
        boxShadow: '0 24px 80px rgba(0,0,0,0.45), 0 0 0 0.5px var(--bd)',
      }}>

        {/* ── Modal Header ───────────────────────────────────────────────── */}
        <div style={{
          height: '48px', padding: '0 20px',
          display: 'flex', alignItems: 'center', gap: '12px',
          borderBottom: '1px solid var(--bd)', flexShrink: 0,
          background: 'var(--bg0)',
        }}>
          <div style={{
            width: '24px', height: '24px', borderRadius: '50%', flexShrink: 0,
            background: 'color-mix(in srgb, var(--acc) 20%, transparent)',
            border: '1px solid color-mix(in srgb, var(--acc) 40%, transparent)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '10px', fontWeight: 600, color: 'var(--acc)',
          }}>
            {initials}
          </div>
          <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--t1)' }}>
            {t('profile.title')}
          </span>
          {user && (
            <span style={{ fontSize: '11px', color: 'var(--t3)', marginLeft: '2px' }}>
              {user.username} · Seiðr Studio
            </span>
          )}
          <button
            onClick={handleClose}
            title={t('profile.close')}
            style={{
              marginLeft: 'auto',
              width: '26px', height: '26px',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              borderRadius: 'var(--seer-radius-sm)', cursor: 'pointer',
              color: 'var(--t3)',
              border: 'none', background: 'transparent',
              transition: 'background 0.12s, color 0.12s',
            }}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
              (e.currentTarget as HTMLElement).style.color = 'var(--t1)';
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = 'transparent';
              (e.currentTarget as HTMLElement).style.color = 'var(--t3)';
            }}
          >
            <X size={14} />
          </button>
        </div>

        {/* ── Modal Body ─────────────────────────────────────────────────── */}
        <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>

          {/* Sidebar nav */}
          <nav style={{
            width: '192px', flexShrink: 0,
            background: 'var(--bg0)', borderRight: '1px solid var(--bd)',
            padding: '12px 8px', display: 'flex', flexDirection: 'column', gap: '2px',
            overflowY: 'auto',
          }}>
            {NAV_SECTIONS.map((section) => (
              <div key={section.labelKey}>
                <div style={{
                  fontSize: '10px', fontWeight: 600, letterSpacing: '0.08em',
                  color: 'var(--t3)', textTransform: 'uppercase',
                  padding: '8px 8px 4px', marginTop: '4px',
                }}>
                  {t(section.labelKey)}
                </div>
                {section.items.map((item) => (
                  <button
                    key={item.id}
                    onClick={() => setActiveTab(item.id)}
                    style={{
                      display: 'flex', alignItems: 'center', gap: '9px',
                      padding: '6px 10px', borderRadius: 'var(--seer-radius-md)',
                      cursor: 'pointer',
                      color: activeTab === item.id ? 'var(--acc)' : 'var(--t2)',
                      fontSize: '12px', fontWeight: 500,
                      border: 'none', width: '100%', textAlign: 'left',
                      background: activeTab === item.id
                        ? 'color-mix(in srgb, var(--acc) 12%, transparent)'
                        : 'transparent',
                      transition: 'background 0.1s, color 0.1s',
                    }}
                    onMouseEnter={(e) => {
                      if (activeTab !== item.id) {
                        (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                        (e.currentTarget as HTMLElement).style.color = 'var(--t1)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (activeTab !== item.id) {
                        (e.currentTarget as HTMLElement).style.background = 'transparent';
                        (e.currentTarget as HTMLElement).style.color = 'var(--t2)';
                      }
                    }}
                  >
                    <span style={{ fontSize: '13px', opacity: 0.7, flexShrink: 0 }}>{item.icon}</span>
                    {t(item.labelKey)}
                  </button>
                ))}
              </div>
            ))}

            {/* Logout button at bottom */}
            <div style={{ flex: 1, minHeight: '16px' }} />
            <button
              onClick={async () => { await logout(); handleClose(); }}
              style={{
                display: 'flex', alignItems: 'center', gap: '9px',
                padding: '6px 10px', borderRadius: 'var(--seer-radius-md)',
                cursor: 'pointer',
                color: 'var(--danger)',
                fontSize: '12px', fontWeight: 500,
                border: 'none', width: '100%', textAlign: 'left',
                background: 'transparent',
                transition: 'background 0.1s',
              }}
              onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'color-mix(in srgb, var(--danger) 10%, transparent)'; }}
              onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
            >
              <span style={{ fontSize: '13px', opacity: 0.7 }}>→</span>
              {t('auth.logout')}
            </button>
          </nav>

          {/* Content area */}
          <div style={{
            flex: 1, overflowY: 'auto', padding: '24px 28px',
            scrollbarWidth: 'thin', scrollbarColor: 'var(--bd) transparent',
          }}>
            {renderTab()}
          </div>
        </div>
      </div>
    </div>
  );
});

ProfileModal.displayName = 'ProfileModal';
