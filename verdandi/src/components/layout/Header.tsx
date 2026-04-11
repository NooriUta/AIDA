import { memo, useRef, useState, useCallback } from 'react';
import { Sun, Moon, Paintbrush, ChevronDown, Search } from 'lucide-react';
import { ProfileModal } from '../profile/ProfileModal';
import { CommandPalette } from '../CommandPalette';
import { SearchPalette } from '../SearchPalette';
import { useTranslation } from 'react-i18next';
import { useNavigate, useLocation } from 'react-router-dom';
import { useLoomStore } from '../../stores/loomStore';
import { useAuthStore } from '../../stores/authStore';
import { useHotkeys } from '../../hooks/useHotkeys';
import { LanguageSwitcher } from './LanguageSwitcher';
import { LegendButton }    from './LegendButton';

// ─── Navigation structure (SEER Studio architecture) ─────────────────────────
type NornId = 'VERDANDI' | 'URD' | 'SKULD';

interface SubModule {
  id: string;
  key: string;
  route: string | null;
  horizon?: string;
}

interface NornDef {
  id: NornId;
  descKey: string;
  route: string;
  subModules: SubModule[];
  horizon?: string;
}

const NORNS: NornDef[] = [
  {
    id: 'VERDANDI', descKey: 'nav.verdandiDesc', route: '/',
    subModules: [
      { id: 'LOOM',  key: 'nav.loom',  route: '/'     },
      { id: 'KNOT',  key: 'nav.knot',  route: '/knot' },
      { id: 'ANVIL', key: 'nav.anvil', route: null, horizon: 'H2' },
    ],
  },
  { id: 'URD',   descKey: 'nav.urdDesc',   route: '/urd',   subModules: [], horizon: 'H3' },
  { id: 'SKULD', descKey: 'nav.skuldDesc', route: '/skuld', subModules: [], horizon: 'H3' },
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
  const { user } = useAuthStore();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const activeNorn: NornId =
    pathname.startsWith('/urd')   ? 'URD'   :
    pathname.startsWith('/skuld') ? 'SKULD' :
    'VERDANDI';

  const activeSubModule: string =
    pathname.startsWith('/knot') ? 'KNOT' : 'LOOM';

  const currentNorn = NORNS.find((n) => n.id === activeNorn)!;

  const [profileOpen, setProfileOpen]         = useState(false);
  const [seerMenuOpen, setSeerMenuOpen]       = useState(false);
  const [paletteMenuOpen, setPaletteMenuOpen] = useState(false);
  const [cmdPaletteOpen, setCmdPaletteOpen]       = useState(false);
  const [searchPaletteOpen, setSearchPaletteOpen] = useState(false);

  const toggleCmdPalette    = useCallback(() => setCmdPaletteOpen((v) => !v), []);
  const toggleSearchPalette = useCallback(() => setSearchPaletteOpen((v) => !v), []);

  // Ctrl+K / Cmd+K opens command palette; / opens search palette
  useHotkeys([
    { key: 'k', ctrl: true, action: toggleCmdPalette, global: true },
    { key: '/', action: toggleSearchPalette },
  ]);

  const seerMenuRef    = useRef<HTMLDivElement>(null);
  const paletteMenuRef = useRef<HTMLDivElement>(null);

  const handleSeerMenuBlur = (e: React.FocusEvent) => {
    if (!seerMenuRef.current?.contains(e.relatedTarget as Node)) setSeerMenuOpen(false);
  };
  const handlePaletteMenuBlur = (e: React.FocusEvent) => {
    if (!paletteMenuRef.current?.contains(e.relatedTarget as Node)) setPaletteMenuOpen(false);
  };

  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';

  return (
    <header style={{
      height: '48px',
      background: 'var(--bg0)',
      borderBottom: '1px solid var(--bd)',
      display: 'flex',
      alignItems: 'center',
      padding: '0 12px',
      gap: '8px',
      flexShrink: 0,
      zIndex: 100,
    }}>

      {/* ── SEER logo + Norn switcher dropdown ──────────────────────────── */}
      <div ref={seerMenuRef} style={{ position: 'relative', flexShrink: 0 }} onBlur={handleSeerMenuBlur}>
        <button
          onClick={() => setSeerMenuOpen((v) => !v)}
          style={{
            display: 'flex', alignItems: 'center', gap: '6px',
            padding: '4px 8px 4px 4px',
            background: seerMenuOpen ? 'var(--bg2)' : 'transparent',
            border: '1px solid',
            borderColor: seerMenuOpen ? 'var(--bd)' : 'transparent',
            borderRadius: '6px',
            cursor: 'pointer',
            transition: 'background 0.12s, border-color 0.12s',
          }}
          onMouseEnter={(e) => {
            if (!seerMenuOpen) {
              (e.currentTarget as HTMLElement).style.background = 'var(--bg2)';
              (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)';
            }
          }}
          onMouseLeave={(e) => {
            if (!seerMenuOpen) {
              (e.currentTarget as HTMLElement).style.background = 'transparent';
              (e.currentTarget as HTMLElement).style.borderColor = 'transparent';
            }
          }}
        >
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--acc)', flexShrink: 0 }} />
          <span className="seer-logo-text" style={{ fontSize: '13px', color: 'var(--t1)' }}>
            Seiðr
          </span>
          <ChevronDown size={11} style={{
            color: 'var(--t3)', marginLeft: '1px',
            transform: seerMenuOpen ? 'rotate(180deg)' : 'rotate(0)',
            transition: 'transform 0.15s',
          }} />
        </button>

        {/* Norn switcher dropdown */}
        {seerMenuOpen && (
          <div style={{
            position: 'absolute', top: 'calc(100% + 4px)', left: 0,
            zIndex: 300, minWidth: '220px',
            background: 'var(--bg1)', border: '1px solid var(--bd)',
            borderRadius: '8px',
            boxShadow: '0 8px 24px rgba(0,0,0,0.35)',
            overflow: 'hidden',
          }}>
            <div style={{
              padding: '7px 12px 6px', fontSize: '10px', fontWeight: 600,
              color: 'var(--t3)', letterSpacing: '0.08em',
              borderBottom: '1px solid var(--bd)',
            }}>
              Seiðr Studio
            </div>
            {NORNS.map((norn) => {
              const isCurrent = norn.id === activeNorn;
              return (
                <button
                  key={norn.id}
                  onClick={() => { navigate(norn.route); setSeerMenuOpen(false); }}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '10px',
                    width: '100%', padding: '10px 12px',
                    background: isCurrent ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
                    border: 'none',
                    color: isCurrent ? 'var(--acc)' : 'var(--t1)',
                    fontSize: '12px', fontWeight: 500,
                    cursor: 'pointer', textAlign: 'left',
                    transition: 'background 0.1s',
                  }}
                  onMouseEnter={(e) => {
                    if (!isCurrent) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                  }}
                  onMouseLeave={(e) => {
                    if (!isCurrent) (e.currentTarget as HTMLElement).style.background = 'transparent';
                  }}
                >
                  <div style={{
                    width: 5, height: 5, borderRadius: '50%', flexShrink: 0,
                    background: isCurrent ? 'var(--acc)' : 'transparent',
                  }} />
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ fontWeight: 600, letterSpacing: '0.04em' }}>{norn.id}</span>
                      {norn.horizon && (
                        <span style={{
                          fontSize: '9px', fontWeight: 600, letterSpacing: '0.05em',
                          padding: '1px 5px', borderRadius: '3px',
                          background: 'color-mix(in srgb, var(--t3) 15%, transparent)',
                          color: 'var(--t3)',
                        }}>
                          {norn.horizon}
                        </span>
                      )}
                    </div>
                    <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: '2px' }}>
                      {t(norn.descKey)}
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        )}
      </div>

      {/* ── Separator ────────────────────────────────────────────────────── */}
      <div style={{ width: 1, height: 20, background: 'var(--bd)', flexShrink: 0 }} />

      {/* ── Active Norn name (home point) ────────────────────────────────── */}
      <button
        onClick={() => navigate(currentNorn.route)}
        title={`${currentNorn.id} — ${t(currentNorn.descKey)}`}
        style={{
          padding: '5px 10px',
          fontSize: '11px', fontWeight: 700, letterSpacing: '0.1em',
          color: 'var(--t1)',
          background: 'transparent', border: 'none',
          cursor: 'pointer', borderRadius: '4px',
          transition: 'color 0.12s',
          flexShrink: 0,
        }}
        onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--acc)'; }}
        onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--t1)'; }}
      >
        {activeNorn}
      </button>

      {/* ── Separator ────────────────────────────────────────────────────── */}
      <div style={{ width: 1, height: 20, background: 'var(--bd)', flexShrink: 0 }} />

      {/* ── Sub-module tabs ──────────────────────────────────────────────── */}
      <nav style={{ display: 'flex', gap: '2px', flex: 1, alignItems: 'center' }}>
        {currentNorn.subModules.length > 0 ? (
          currentNorn.subModules.map((sub) => {
            const isSub = sub.id === activeSubModule;
            const isEnabled = sub.route !== null;
            return (
              <button
                key={sub.id}
                disabled={!isEnabled}
                aria-disabled={!isEnabled}
                tabIndex={isEnabled ? 0 : -1}
                onClick={() => isEnabled && sub.route && navigate(sub.route)}
                title={!isEnabled && sub.horizon ? t('nav.comingSoon', { horizon: sub.horizon }) : undefined}
                style={{
                  padding: '6px 14px',
                  fontSize: '12px',
                  fontWeight: isSub ? 500 : 400,
                  borderRadius: '4px',
                  border: 'none',
                  cursor: isEnabled ? 'pointer' : 'not-allowed',
                  background: isSub
                    ? 'color-mix(in srgb, var(--acc) 12%, transparent)'
                    : 'transparent',
                  color: isSub ? 'var(--acc)' : isEnabled ? 'var(--t2)' : 'var(--t3)',
                  opacity: isEnabled ? 1 : 0.35,
                  transition: 'background 0.12s, color 0.12s',
                  letterSpacing: '0.06em',
                }}
              >
                {t(sub.key)}
              </button>
            );
          })
        ) : (
          <span style={{
            padding: '4px 12px',
            fontSize: '11px', fontWeight: 500, letterSpacing: '0.04em',
            color: 'var(--t3)',
          }}>
            {t('stub.underConstruction')}
          </span>
        )}
      </nav>

      {/* ── Right toolbar ────────────────────────────────────────────────── */}

      {/* Search palette */}
      <button
        onClick={toggleSearchPalette}
        style={{
          display: 'flex', alignItems: 'center', gap: '6px',
          padding: '5px 10px',
          background: 'var(--bg2)',
          border: '1px solid var(--bd)',
          borderRadius: '6px',
          color: 'var(--t2)', fontSize: '11px',
          cursor: 'pointer',
        }}
        title={t('searchPalette.title') + ' ( / )'}
      >
        <Search size={12} />
        <span style={{ letterSpacing: '0.04em', color: 'var(--t3)' }}>/</span>
      </button>

      {/* Visual separator between Search and Command palette */}
      <div style={{ width: 1, height: 16, background: 'var(--bd)', flexShrink: 0 }} />

      {/* Command palette */}
      <button
        onClick={toggleCmdPalette}
        style={{
          display: 'flex', alignItems: 'center', gap: '6px',
          padding: '5px 10px',
          background: 'var(--bg2)',
          border: '1px solid var(--bd)',
          borderRadius: '6px',
          color: 'var(--t2)', fontSize: '11px',
          cursor: 'pointer',
        }}
        title={t('commandPalette.title') + ' (⌘K)'}
      >
        <span style={{ letterSpacing: '0.04em' }}>⌘K</span>
      </button>

      <LanguageSwitcher />

      {/* Palette switcher */}
      <div ref={paletteMenuRef} style={{ position: 'relative' }} onBlur={handlePaletteMenuBlur}>
        <button
          onClick={() => setPaletteMenuOpen((v) => !v)}
          title={t('palette.title')}
          style={{
            background: 'transparent',
            border: '1px solid var(--bd)',
            borderRadius: '6px',
            padding: '5px 7px',
            cursor: 'pointer',
            color: paletteMenuOpen ? 'var(--acc)' : 'var(--t2)',
            display: 'flex', alignItems: 'center',
            transition: 'border-color 0.12s, color 0.12s',
          }}
        >
          <Paintbrush size={13} />
        </button>

        {paletteMenuOpen && (
          <div style={{
            position: 'absolute', top: 'calc(100% + 4px)', right: 0,
            zIndex: 300, minWidth: '160px',
            background: 'var(--bg1)', border: '1px solid var(--bd)',
            borderRadius: '8px',
            boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
            overflow: 'hidden',
          }}>
            <div style={{
              padding: '6px 12px 5px', fontSize: '10px',
              color: 'var(--t3)', letterSpacing: '0.07em',
              borderBottom: '1px solid var(--bd)',
            }}>
              {t('palette.title').toUpperCase()}
            </div>
            {PALETTES.map((p) => (
              <button
                key={p.id}
                onClick={() => { setPalette(p.id); setPaletteMenuOpen(false); }}
                style={{
                  display: 'flex', alignItems: 'center', gap: '8px',
                  width: '100%', padding: '8px 12px',
                  background: palette === p.id
                    ? 'color-mix(in srgb, var(--acc) 10%, transparent)'
                    : 'transparent',
                  border: 'none',
                  color: palette === p.id ? 'var(--acc)' : 'var(--t2)',
                  fontSize: '12px', cursor: 'pointer', textAlign: 'left',
                  transition: 'background 0.1s',
                }}
                onMouseEnter={(e) => {
                  if (palette !== p.id) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                }}
                onMouseLeave={(e) => {
                  if (palette !== p.id) (e.currentTarget as HTMLElement).style.background = 'transparent';
                }}
              >
                <div style={{
                  width: 5, height: 5, borderRadius: '50%', flexShrink: 0,
                  background: palette === p.id ? 'var(--acc)' : 'transparent',
                }} />
                {t(p.key)}
              </button>
            ))}
          </div>
        )}
      </div>

      <LegendButton />

      {/* Theme toggle */}
      <button
        onClick={toggleTheme}
        title={t(theme === 'dark' ? 'theme.light' : 'theme.dark')}
        style={{
          background: 'transparent',
          border: '1px solid var(--bd)',
          borderRadius: '6px',
          padding: '5px 7px',
          cursor: 'pointer',
          color: 'var(--t2)',
          display: 'flex', alignItems: 'center',
          transition: 'border-color 0.12s, color 0.12s',
        }}
      >
        {theme === 'dark' ? <Sun size={13} /> : <Moon size={13} />}
      </button>

      {/* User badge → ProfileModal */}
      {user && (
        <button
          onClick={() => setProfileOpen(true)}
          title={`${user.username} · ${user.role}`}
          style={{
            display: 'flex', alignItems: 'center', gap: '6px',
            marginLeft: '2px',
            padding: '4px 8px 4px 5px',
            background: 'transparent',
            border: '1px solid transparent',
            borderRadius: '6px',
            cursor: 'pointer',
            transition: 'background 0.12s, border-color 0.12s',
          }}
          onMouseEnter={(e) => {
            (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
            (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)';
          }}
          onMouseLeave={(e) => {
            (e.currentTarget as HTMLElement).style.background = 'transparent';
            (e.currentTarget as HTMLElement).style.borderColor = 'transparent';
          }}
        >
          <div style={{
            width: 24, height: 24, borderRadius: '50%',
            background: 'color-mix(in srgb, var(--acc) 20%, transparent)',
            border: '1px solid color-mix(in srgb, var(--acc) 50%, transparent)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '10px', fontWeight: 600, color: 'var(--acc)', flexShrink: 0,
          }}>
            {initials}
          </div>
          <span style={{ fontSize: '12px', color: 'var(--t2)' }}>{user.username}</span>
        </button>
      )}

      {profileOpen && <ProfileModal onClose={() => setProfileOpen(false)} />}
      <CommandPalette open={cmdPaletteOpen} onClose={() => setCmdPaletteOpen(false)} />
      <SearchPalette open={searchPaletteOpen} onClose={() => setSearchPaletteOpen(false)} />
    </header>
  );
});

Header.displayName = 'Header';
