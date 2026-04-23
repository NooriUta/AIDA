import { memo, useRef, useState, useCallback, useMemo, useEffect } from 'react';
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
import { ToolbarDivider }  from '../ui/ToolbarPrimitives';
import { useIsMobile }     from '../../hooks/useIsMobile';

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
  const isMobile = useIsMobile();

  const appBase = useMemo(
    () => (pathname.startsWith('/verdandi') ? '/verdandi' : ''),
    [pathname],
  );
  const go = useCallback(
    (route: string) => navigate(appBase + route),
    [navigate, appBase],
  );
  const localPath = pathname.slice(appBase.length) || '/';

  const activeNorn: NornId =
    localPath.startsWith('/urd')   ? 'URD'   :
    localPath.startsWith('/skuld') ? 'SKULD' :
    'VERDANDI';

  const activeSubModule: string =
    localPath.startsWith('/knot') ? 'KNOT' : 'LOOM';

  const currentNorn = NORNS.find((n) => n.id === activeNorn)!;

  const [profileOpen, setProfileOpen]         = useState(false);
  const [seerMenuOpen, setSeerMenuOpen]       = useState(false);
  const [paletteMenuOpen, setPaletteMenuOpen] = useState(false);
  const [cmdPaletteOpen, setCmdPaletteOpen]       = useState(false);
  const [searchPaletteOpen, setSearchPaletteOpen] = useState(false);
  const [mobileVernOpen, setMobileVernOpen]   = useState(false);

  const toggleCmdPalette    = useCallback(() => setCmdPaletteOpen((v) => !v), []);
  const toggleSearchPalette = useCallback(() => setSearchPaletteOpen((v) => !v), []);

  // Ctrl+K / Cmd+K opens command palette; / opens search palette
  useHotkeys([
    { key: 'k', ctrl: true, action: toggleCmdPalette, global: true },
    { key: '/', action: toggleSearchPalette },
  ]);

  const seerMenuRef    = useRef<HTMLDivElement>(null);
  const paletteMenuRef = useRef<HTMLDivElement>(null);
  const mobileVernRef  = useRef<HTMLDivElement>(null);

  const handleSeerMenuBlur = (e: React.FocusEvent) => {
    if (!seerMenuRef.current?.contains(e.relatedTarget as Node)) setSeerMenuOpen(false);
  };
  const handlePaletteMenuBlur = (e: React.FocusEvent) => {
    if (!paletteMenuRef.current?.contains(e.relatedTarget as Node)) setPaletteMenuOpen(false);
  };

  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';

  return (
    <header style={{
      height: '42px',
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
            borderRadius: 'var(--seer-radius-md)',
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
            borderRadius: 'var(--seer-radius-lg)',
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
                  onClick={() => { go(norn.route); setSeerMenuOpen(false); }}
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

      <ToolbarDivider />

      {/* ── Tenant picker (super-admin: dropdown; others: static badge) ──── */}
      {user?.activeTenantAlias && (
        <>
          <TenantPickerButton user={user} />
          <ToolbarDivider />
        </>
      )}

      {isMobile ? (
        /* ── Mobile: V dropdown + active tab only ──────────────────────── */
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1 }}>
          <div
            ref={mobileVernRef}
            style={{ position: 'relative', flexShrink: 0 }}
            onBlur={(e) => {
              if (!mobileVernRef.current?.contains(e.relatedTarget as Node))
                setMobileVernOpen(false);
            }}
          >
            <button
              onClick={() => setMobileVernOpen((v) => !v)}
              title={activeNorn}
              style={{
                display: 'flex', alignItems: 'center', gap: '4px',
                padding: '4px 7px',
                background: mobileVernOpen ? 'var(--bg2)' : 'transparent',
                border: '1px solid',
                borderColor: mobileVernOpen ? 'var(--bd)' : 'transparent',
                borderRadius: 'var(--seer-radius-md)',
                cursor: 'pointer',
                fontSize: '11px', fontWeight: 700, letterSpacing: '0.1em',
                color: 'var(--t1)',
                transition: 'background 0.12s, border-color 0.12s',
              }}
            >
              {activeNorn[0]}
              <ChevronDown size={10} style={{
                color: 'var(--t3)',
                transform: mobileVernOpen ? 'rotate(180deg)' : 'rotate(0)',
                transition: 'transform 0.15s',
              }} />
            </button>

            {mobileVernOpen && (
              <div style={{
                position: 'absolute', top: 'calc(100% + 4px)', left: 0,
                zIndex: 300, minWidth: '160px',
                background: 'var(--bg1)', border: '1px solid var(--bd)',
                borderRadius: 'var(--seer-radius-lg)',
                boxShadow: '0 8px 24px rgba(0,0,0,0.35)',
                overflow: 'hidden',
              }}>
                <div style={{
                  padding: '7px 12px 6px', fontSize: '10px', fontWeight: 600,
                  color: 'var(--t3)', letterSpacing: '0.08em',
                  borderBottom: '1px solid var(--bd)',
                }}>
                  {activeNorn}
                </div>
                {currentNorn.subModules.map((sub) => {
                  const isSub = sub.id === activeSubModule;
                  const isEnabled = sub.route !== null;
                  return (
                    <button
                      key={sub.id}
                      onClick={() => {
                        if (isEnabled && sub.route) { go(sub.route); setMobileVernOpen(false); }
                      }}
                      disabled={!isEnabled}
                      style={{
                        display: 'flex', alignItems: 'center', gap: '10px',
                        width: '100%', padding: '10px 12px',
                        background: isSub ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
                        border: 'none',
                        color: isSub ? 'var(--acc)' : isEnabled ? 'var(--t1)' : 'var(--t3)',
                        fontSize: '12px', fontWeight: 500,
                        cursor: isEnabled ? 'pointer' : 'not-allowed',
                        opacity: isEnabled ? 1 : 0.35,
                        textAlign: 'left',
                      }}
                    >
                      <div style={{
                        width: 5, height: 5, borderRadius: '50%', flexShrink: 0,
                        background: isSub ? 'var(--acc)' : 'transparent',
                      }} />
                      <span style={{ flex: 1 }}>{t(sub.key)}</span>
                      {!isEnabled && sub.horizon && (
                        <span style={{
                          fontSize: '9px', fontWeight: 600, padding: '1px 5px',
                          borderRadius: 3,
                          background: 'color-mix(in srgb, var(--t3) 15%, transparent)',
                          color: 'var(--t3)',
                        }}>{sub.horizon}</span>
                      )}
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          {/* Active sub-module badge */}
          <span style={{
            fontSize: '12px', fontWeight: 500, letterSpacing: '0.06em',
            color: 'var(--acc)',
            padding: '4px 8px',
            background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
            borderRadius: 'var(--seer-radius-sm)',
          }}>
            {t(`nav.${activeSubModule.toLowerCase()}`)}
          </span>

          <div style={{ flex: 1 }} />
        </div>
      ) : (
        /* ── Desktop: Norn name + separator + full tab nav ──────────────── */
        <>
          <button
            onClick={() => go(currentNorn.route)}
            title={`${currentNorn.id} — ${t(currentNorn.descKey)}`}
            style={{
              padding: '5px 10px',
              fontSize: '11px', fontWeight: 700, letterSpacing: '0.1em',
              color: 'var(--t1)',
              background: 'transparent', border: 'none',
              cursor: 'pointer', borderRadius: 'var(--seer-radius-sm)',
              transition: 'color 0.12s', flexShrink: 0,
            }}
            onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--acc)'; }}
            onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--t1)'; }}
          >
            {activeNorn}
          </button>

          <ToolbarDivider />

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
                    onClick={() => isEnabled && sub.route && go(sub.route)}
                    title={!isEnabled && sub.horizon ? t('nav.comingSoon', { horizon: sub.horizon }) : undefined}
                    style={{
                      padding: '6px 14px', fontSize: '12px',
                      fontWeight: isSub ? 500 : 400,
                      borderRadius: 'var(--seer-radius-sm)', border: 'none',
                      cursor: isEnabled ? 'pointer' : 'not-allowed',
                      background: isSub ? 'color-mix(in srgb, var(--acc) 12%, transparent)' : 'transparent',
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
                padding: '4px 12px', fontSize: '11px', fontWeight: 500,
                letterSpacing: '0.04em', color: 'var(--t3)',
              }}>
                {t('stub.underConstruction')}
              </span>
            )}
          </nav>
        </>
      )}

      {/* ── Right toolbar ────────────────────────────────────────────────── */}

      {/* Search palette */}
      <button
        onClick={toggleSearchPalette}
        style={{
          display: 'flex', alignItems: 'center', gap: '6px',
          padding: '5px 10px',
          background: 'var(--bg2)',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)',
          color: 'var(--t2)', fontSize: '11px',
          cursor: 'pointer',
        }}
        title={t('searchPalette.title') + ' ( / )'}
      >
        <Search size={12} />
        <span style={{ letterSpacing: '0.04em', color: 'var(--t3)' }}>/</span>
      </button>

      {!isMobile && <ToolbarDivider size="sm" />}

      {/* Command palette — hidden on mobile (accessible via search icon) */}
      {!isMobile && (
        <button
          onClick={toggleCmdPalette}
          style={{
            display: 'flex', alignItems: 'center', gap: '6px',
            padding: '5px 10px',
            background: 'var(--bg2)',
            border: '1px solid var(--bd)',
            borderRadius: 'var(--seer-radius-md)',
            color: 'var(--t2)', fontSize: '11px',
            cursor: 'pointer',
          }}
          title={t('commandPalette.title') + ' (⌘K)'}
        >
          <span style={{ letterSpacing: '0.04em' }}>⌘K</span>
        </button>
      )}

      {!isMobile && <LanguageSwitcher />}

      {/* Palette switcher */}
      {!isMobile && <div ref={paletteMenuRef} style={{ position: 'relative' }} onBlur={handlePaletteMenuBlur}>
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
            borderRadius: 'var(--seer-radius-lg)',
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
      </div>}

      {!isMobile && <LegendButton />}

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
            borderRadius: 'var(--seer-radius-md)',
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
          {!isMobile && <span style={{ fontSize: '12px', color: 'var(--t2)' }}>{user.username}</span>}
        </button>
      )}

      {profileOpen && <ProfileModal onClose={() => setProfileOpen(false)} />}
      <CommandPalette open={cmdPaletteOpen} onClose={() => setCmdPaletteOpen(false)} />
      <SearchPalette open={searchPaletteOpen} onClose={() => setSearchPaletteOpen(false)} />
    </header>
  );
});

Header.displayName = 'Header';

// ── TenantPickerButton ────────────────────────────────────────────────────────
// Super-admins see a dropdown to switch tenant context without re-login.
// All other roles see a plain read-only badge.

interface TenantItem { id: string; name: string }

function TenantPickerButton({ user }: { user: import('../../stores/authStore').AuthUser }) {
  const { t } = useTranslation();
  const isSuperAdmin = user.scopes?.includes('aida:admin');

  const [open,    setOpen]    = useState(false);
  const [tenants, setTenants] = useState<TenantItem[]>([]);
  const [loading, setLoading] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  // Lazy-load tenant list on first open
  useEffect(() => {
    if (!open || !isSuperAdmin || tenants.length > 0) return;
    setLoading(true);
    fetch('/admin/tenants', { credentials: 'include' })
      .then(r => r.ok ? r.json() : Promise.reject(r.status))
      .then((data: TenantItem[]) => setTenants(data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [open, isSuperAdmin, tenants.length]);

  async function pick(alias: string) {
    localStorage.setItem('seer-active-tenant', alias);
    window.dispatchEvent(new CustomEvent('seer-tenant-changed', { detail: { alias } }));
    try {
      await fetch('/auth/me/tenant', {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tenantAlias: alias }),
      });
      // Update authStore in-place so header badge refreshes immediately
      useAuthStore.setState((s) =>
        s.user ? { user: { ...s.user, activeTenantAlias: alias } } : {},
      );
    } catch { /* session patch is best-effort */ }
    setOpen(false);
  }

  const badgeStyle: React.CSSProperties = {
    display: 'inline-flex', alignItems: 'center', gap: '5px',
    padding: '3px 8px',
    fontSize: '10px', fontWeight: 600, letterSpacing: '0.06em',
    color: 'var(--acc)',
    background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
    border: '1px solid color-mix(in srgb, var(--acc) 35%, transparent)',
    borderRadius: 'var(--seer-radius-sm)',
    textTransform: 'uppercase', flexShrink: 0,
  };

  if (!isSuperAdmin) {
    return (
      <span title={t('tenant.activeHint', { defaultValue: 'Active tenant' })} style={badgeStyle}>
        <div style={{ width: 5, height: 5, borderRadius: '50%', background: 'var(--acc)' }} />
        {user.activeTenantAlias}
      </span>
    );
  }

  return (
    <div ref={ref} style={{ position: 'relative', flexShrink: 0 }}
      onBlur={(e) => { if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false); }}
    >
      <button
        onClick={() => setOpen((v) => !v)}
        title={t('tenant.switchHint', { defaultValue: 'Switch tenant' })}
        style={{ ...badgeStyle, cursor: 'pointer', border: 'none', gap: '5px' }}
      >
        <div style={{ width: 5, height: 5, borderRadius: '50%', background: 'var(--acc)' }} />
        {user.activeTenantAlias}
        <ChevronDown size={10} style={{
          color: 'var(--acc)', marginLeft: '1px',
          transform: open ? 'rotate(180deg)' : 'rotate(0)',
          transition: 'transform 0.15s',
        }} />
      </button>

      {open && (
        <div style={{
          position: 'absolute', top: 'calc(100% + 4px)', left: 0,
          zIndex: 300, minWidth: '180px',
          background: 'var(--bg1)', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-lg)',
          boxShadow: '0 8px 24px rgba(0,0,0,0.35)',
          overflow: 'hidden',
        }}>
          <div style={{
            padding: '6px 12px 5px', fontSize: '10px', fontWeight: 600,
            color: 'var(--t3)', letterSpacing: '0.08em',
            borderBottom: '1px solid var(--bd)',
          }}>
            {t('tenant.switchTitle', { defaultValue: 'SWITCH TENANT' })}
          </div>

          {loading && (
            <div style={{ padding: '10px 12px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('profile.loading')}
            </div>
          )}

          {!loading && tenants.map((ten) => {
            const isActive = ten.id === user.activeTenantAlias;
            return (
              <button key={ten.id} onClick={() => pick(ten.id)}
                style={{
                  display: 'flex', alignItems: 'center', gap: '8px',
                  width: '100%', padding: '9px 12px',
                  background: isActive ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
                  border: 'none',
                  color: isActive ? 'var(--acc)' : 'var(--t1)',
                  fontSize: '12px', fontWeight: isActive ? 600 : 400,
                  cursor: 'pointer', textAlign: 'left',
                  transition: 'background 0.1s',
                }}
                onMouseEnter={(e) => {
                  if (!isActive) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                }}
                onMouseLeave={(e) => {
                  if (!isActive) (e.currentTarget as HTMLElement).style.background = 'transparent';
                }}
              >
                <div style={{
                  width: 5, height: 5, borderRadius: '50%', flexShrink: 0,
                  background: isActive ? 'var(--acc)' : 'var(--t3)',
                }} />
                <span style={{ flex: 1, textTransform: 'uppercase', letterSpacing: '0.05em', fontSize: '11px' }}>
                  {ten.id}
                </span>
                {ten.name !== ten.id && (
                  <span style={{ fontSize: '10px', color: 'var(--t3)' }}>{ten.name}</span>
                )}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
