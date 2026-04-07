import { memo, useRef, useState } from 'react';
import { Sun, Moon, Command, LogOut, Paintbrush } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useLocation } from 'react-router-dom';
import { useLoomStore } from '../../stores/loomStore';
import { useAuthStore } from '../../stores/authStore';
import { LanguageSwitcher } from './LanguageSwitcher';
import { LegendButton }    from './LegendButton';

type TabId = 'LOOM' | 'ANVIL' | 'SHUTTLE' | 'KNOT';

const TABS: { id: TabId; key: string; route: string | null }[] = [
  { id: 'LOOM',    key: 'nav.loom',    route: '/'     },
  { id: 'ANVIL',   key: 'nav.anvil',   route: null    },
  { id: 'SHUTTLE', key: 'nav.shuttle', route: null    },
  { id: 'KNOT',    key: 'nav.knot',    route: '/knot' },
];

const PALETTES: { id: string; key: string }[] = [
  { id: 'amber-forest', key: 'palette.amberForest' },
  { id: 'lichen',       key: 'palette.lichen'      },
  { id: 'slate',        key: 'palette.slate'        },
  { id: 'juniper',      key: 'palette.juniper'      },
  { id: 'warm-dark',    key: 'palette.warmDark'     },
];

export const Header = memo(() => {
  const { theme, toggleTheme, palette, setPalette } = useLoomStore();
  const { user, logout } = useAuthStore();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const activeTab: TabId = pathname.startsWith('/knot') ? 'KNOT' : 'LOOM';

  const [userMenuOpen, setUserMenuOpen]       = useState(false);
  const [paletteMenuOpen, setPaletteMenuOpen] = useState(false);
  const userMenuRef    = useRef<HTMLDivElement>(null);
  const paletteMenuRef = useRef<HTMLDivElement>(null);

  const handleUserMenuBlur = (e: React.FocusEvent) => {
    if (!userMenuRef.current?.contains(e.relatedTarget as Node)) {
      setUserMenuOpen(false);
    }
  };

  const handlePaletteMenuBlur = (e: React.FocusEvent) => {
    if (!paletteMenuRef.current?.contains(e.relatedTarget as Node)) {
      setPaletteMenuOpen(false);
    }
  };

  const initials = user
    ? user.username.slice(0, 2).toUpperCase()
    : '??';

  return (
    <header style={{
      height: '48px',
      background: 'var(--bg0)',
      borderBottom: '1px solid var(--bd)',
      display: 'flex',
      alignItems: 'center',
      padding: '0 var(--seer-space-3)',
      gap: 'var(--seer-space-2)',
      flexShrink: 0,
      zIndex: 100,
    }}>
      {/* Logo — dot + SEER */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '6px',
        padding: '0 var(--seer-space-2) 0 4px',
        marginRight: 'var(--seer-space-2)',
      }}>
        <div style={{
          width: '8px',
          height: '8px',
          borderRadius: '50%',
          background: 'var(--acc)',
          flexShrink: 0,
        }} />
        <span style={{ fontWeight: 600, fontSize: '13px', letterSpacing: '0.08em', color: 'var(--t1)' }}>
          SEER
        </span>
      </div>

      {/* Separator */}
      <div style={{ width: '1px', height: '20px', background: 'var(--bd)', flexShrink: 0 }} />

      {/* Tabs */}
      <nav style={{ display: 'flex', gap: '2px', flex: 1 }}>
        {TABS.map((tab) => {
          const isActive = tab.id === activeTab;
          const isEnabled = tab.route !== null;
          return (
            <button
              key={tab.id}
              disabled={!isEnabled}
              onClick={() => isEnabled && tab.route && navigate(tab.route)}
              style={{
                padding: '6px 14px',
                fontSize: '12px',
                fontWeight: isActive ? 500 : 400,
                borderRadius: 'var(--seer-radius-sm)',
                border: 'none',
                cursor: isEnabled ? 'pointer' : 'not-allowed',
                background: isActive
                  ? 'color-mix(in srgb, var(--acc) 12%, transparent)'
                  : 'transparent',
                color: isActive ? 'var(--acc)' : isEnabled ? 'var(--t2)' : 'var(--t3)',
                opacity: isEnabled ? 1 : 0.4,
                transition: 'background 0.12s, color 0.12s',
                letterSpacing: '0.06em',
              }}
            >
              {t(tab.key)}
            </button>
          );
        })}
      </nav>

      {/* Command palette */}
      <button
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--seer-space-2)',
          padding: '5px 10px',
          background: 'var(--bg2)',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)',
          color: 'var(--t2)',
          fontSize: '11px',
          cursor: 'pointer',
          marginRight: 'var(--seer-space-2)',
        }}
        title="Command palette (⌘K)"
      >
        <Command size={12} />
        <span style={{ letterSpacing: '0.04em' }}>⌘K</span>
      </button>

      {/* Language switcher */}
      <LanguageSwitcher />

      {/* Palette switcher */}
      <div
        ref={paletteMenuRef}
        style={{ position: 'relative' }}
        onBlur={handlePaletteMenuBlur}
      >
        <button
          onClick={() => setPaletteMenuOpen((v) => !v)}
          title={t('palette.title')}
          style={{
            background: 'transparent',
            border: '1px solid var(--bd)',
            borderRadius: 'var(--seer-radius-md)',
            padding: '5px 7px',
            cursor: 'pointer',
            color: paletteMenuOpen ? 'var(--acc)' : 'var(--t2)',
            display: 'flex',
            alignItems: 'center',
            transition: 'border-color 0.12s, color 0.12s',
          }}
        >
          <Paintbrush size={13} />
        </button>

        {paletteMenuOpen && (
          <div style={{
            position: 'absolute',
            top: 'calc(100% + 4px)',
            right: 0,
            zIndex: 300,
            minWidth: '160px',
            background: 'var(--bg1)',
            border: '1px solid var(--bd)',
            borderRadius: 'var(--seer-radius-md)',
            boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
            overflow: 'hidden',
          }}>
            <div style={{
              padding: '6px 12px 5px',
              fontSize: '10px',
              color: 'var(--t3)',
              letterSpacing: '0.07em',
              borderBottom: '1px solid var(--bd)',
            }}>
              {t('palette.title').toUpperCase()}
            </div>
            {PALETTES.map((p) => (
              <button
                key={p.id}
                onClick={() => { setPalette(p.id); setPaletteMenuOpen(false); }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  width: '100%',
                  padding: '8px 12px',
                  background: palette === p.id
                    ? 'color-mix(in srgb, var(--acc) 10%, transparent)'
                    : 'transparent',
                  border: 'none',
                  color: palette === p.id ? 'var(--acc)' : 'var(--t2)',
                  fontSize: '12px',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'background 0.1s',
                }}
                onMouseEnter={(e) => {
                  if (palette !== p.id) {
                    (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                  }
                }}
                onMouseLeave={(e) => {
                  if (palette !== p.id) {
                    (e.currentTarget as HTMLElement).style.background = 'transparent';
                  }
                }}
              >
                {palette === p.id && (
                  <div style={{
                    width: '5px',
                    height: '5px',
                    borderRadius: '50%',
                    background: 'var(--acc)',
                    flexShrink: 0,
                  }} />
                )}
                {palette !== p.id && <div style={{ width: '5px', flexShrink: 0 }} />}
                {t(p.key)}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Legend */}
      <LegendButton />

      {/* Theme toggle */}
      <button
        onClick={toggleTheme}
        title={t(theme === 'dark' ? 'theme.light' : 'theme.dark')}
        style={{
          background: 'transparent',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)',
          padding: '5px 7px',
          cursor: 'pointer',
          color: 'var(--t2)',
          display: 'flex',
          alignItems: 'center',
          transition: 'border-color 0.12s, color 0.12s',
        }}
      >
        {theme === 'dark' ? <Sun size={13} /> : <Moon size={13} />}
      </button>

      {/* User badge — circular initials + dropdown */}
      {user && (
        <div
          ref={userMenuRef}
          style={{ position: 'relative', marginLeft: 'var(--seer-space-1)' }}
          onBlur={handleUserMenuBlur}
        >
          <button
            onClick={() => setUserMenuOpen((v) => !v)}
            style={{
              width: '28px',
              height: '28px',
              borderRadius: '50%',
              background: 'color-mix(in srgb, var(--acc) 20%, transparent)',
              border: '1px solid var(--acc)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '11px',
              fontWeight: 600,
              color: 'var(--acc)',
              cursor: 'pointer',
              flexShrink: 0,
              transition: 'background 0.12s',
            }}
            title={`${user.username} (${user.role})`}
          >
            {initials}
          </button>

          {userMenuOpen && (
            <div style={{
              position: 'absolute',
              top: 'calc(100% + 4px)',
              right: 0,
              zIndex: 300,
              minWidth: '160px',
              background: 'var(--bg1)',
              border: '1px solid var(--bd)',
              borderRadius: 'var(--seer-radius-md)',
              boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
              overflow: 'hidden',
            }}>
              {/* User info */}
              <div style={{
                padding: '8px 12px 6px',
                borderBottom: '1px solid var(--bd)',
              }}>
                <div style={{ fontSize: '12px', color: 'var(--t1)', fontWeight: 500 }}>
                  {user.username}
                </div>
                <div style={{ fontSize: '10px', color: 'var(--t3)', letterSpacing: '0.05em', marginTop: '2px' }}>
                  {user.role}
                </div>
              </div>
              {/* Logout */}
              <button
                onClick={() => { logout(); setUserMenuOpen(false); }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  width: '100%',
                  padding: '9px 12px',
                  background: 'transparent',
                  border: 'none',
                  color: 'var(--t2)',
                  fontSize: '12px',
                  cursor: 'pointer',
                  textAlign: 'left',
                  transition: 'background 0.1s',
                }}
                onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
                onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
              >
                <LogOut size={12} />
                {t('auth.logout')}
              </button>
            </div>
          )}
        </div>
      )}
    </header>
  );
});

Header.displayName = 'Header';
