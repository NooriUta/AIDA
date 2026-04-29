import { memo, useRef, useState, useCallback, useEffect } from 'react';
import { Globe, Paintbrush, Sun, Moon, ChevronDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore }        from '../../stores/authStore';
import { useDashboardStore }   from '../../stores/dashboardStore';
import { ProfileModal }        from '../profile/ProfileModal';
import { PresentationMode }    from '../PresentationMode';
import { useIsMobile }         from '../../hooks/useIsMobile';
import { useTenantContext }    from '../../hooks/useTenantContext';
import { useHotkeys }          from '../../hooks/useHotkeys';
import { HeimdallCommandPalette } from '../HeimdallCommandPalette';
import { sharedPrefsStore }   from '../../stores/sharedPrefsStore';

// ── Navigation data ────────────────────────────────────────────────────────────
type SectionId = 'BIFROST' | 'DALI' | 'SAGA' | 'FENRIR';

interface SubTab { id: string; labelKey: string; route: string }
interface Section { id: SectionId; descKey: string; route: string; subTabs: SubTab[]; horizon?: string }

const SECTIONS: Section[] = [
  {
    id: 'BIFROST', descKey: 'nav.overviewDesc', route: '/overview',
    subTabs: [
      { id: 'Services',  labelKey: 'nav.services',  route: '/overview/services'  },
      { id: 'Dashboard', labelKey: 'nav.dashboard', route: '/overview/dashboard' },
      { id: 'Events',    labelKey: 'nav.events',    route: '/overview/events'    },
    ],
  },
  {
    id: 'DALI', descKey: 'nav.daliDesc', route: '/dali',
    subTabs: [
      { id: 'Sessions', labelKey: 'nav.sessions', route: '/dali/sessions' },
      { id: 'Sources',  labelKey: 'nav.sources',  route: '/dali/sources'  },
    ],
  },
  {
    id: 'SAGA', descKey: 'nav.docsDesc', route: '/docs',
    subTabs: [
      { id: 'Docs', labelKey: 'nav.docs', route: '/docs' },
    ],
  },
  {
    id: 'FENRIR', descKey: 'nav.adminDesc', route: '/admin/tenants',
    subTabs: [
      { id: 'Tenants',   labelKey: 'nav.tenants',   route: '/admin/tenants' },
      { id: 'Users',     labelKey: 'nav.users',     route: '/users'         },
      { id: 'Analytics', labelKey: 'nav.analytics', route: '/analytics'     },
    ],
  },
];

const PALETTES: Array<{ id: string; key: string }> = [
  { id: 'amber-forest', key: 'palette.amberForest' },
  { id: 'lichen',       key: 'palette.lichen'      },
  { id: 'slate',        key: 'palette.slate'        },
  { id: 'juniper',      key: 'palette.juniper'      },
  { id: 'warm-dark',    key: 'palette.warmDark'     },
];

// ── Primitives ─────────────────────────────────────────────────────────────────
function HDivider() {
  return <div style={{ width: '1px', height: '16px', background: 'var(--bd)', flexShrink: 0 }} />;
}

function LanguageSwitcher() {
  const { i18n, t } = useTranslation();
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const current = i18n.language.startsWith('ru') ? 'ru' : 'en';
  return (
    <div ref={ref} style={{ position: 'relative' }}
      onBlur={e => { if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false); }}>
      <button onClick={() => setOpen(v => !v)} style={{
        display: 'flex', alignItems: 'center', gap: '5px', padding: '5px 8px',
        background: 'transparent', border: '1px solid var(--bd)',
        borderRadius: 'var(--seer-radius-md)', color: 'var(--t2)',
        fontSize: '11px', cursor: 'pointer', letterSpacing: '0.06em',
      }}>
        <Globe size={12} />{current.toUpperCase()}
      </button>
      {open && (
        <div style={{
          position: 'absolute', top: 'calc(100% + 4px)', right: 0, zIndex: 300,
          minWidth: '120px', background: 'var(--bg1)', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)', boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
          overflow: 'hidden',
        }}>
          {(['en', 'ru'] as const).map(lang => (
            <button key={lang} onClick={() => { void i18n.changeLanguage(lang); setOpen(false); }}
              className="hh-dd-item"
              style={{
                display: 'block', width: '100%', padding: '8px 12px', textAlign: 'left',
                background: current === lang ? 'var(--bg3)' : undefined,
                border: 'none',
                color: current === lang ? 'var(--t1)' : 'var(--t2)', fontSize: '12px', cursor: 'pointer',
              }}
            >{t(`language.${lang}`)}</button>
          ))}
        </div>
      )}
    </div>
  );
}

function PaletteDropdown() {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const [palette, setPaletteState] = useState(() => sharedPrefsStore.getPrefs().palette);
  const ref = useRef<HTMLDivElement>(null);
  const pick = (id: string) => {
    sharedPrefsStore.savePrefs({ palette: id });
    setPaletteState(id);
    setOpen(false);
  };
  return (
    <div ref={ref} style={{ position: 'relative' }}
      onBlur={e => { if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false); }}>
      <button onClick={() => setOpen(v => !v)} title={t('palette.title')} style={{
        background: 'transparent', border: '1px solid var(--bd)',
        borderRadius: 'var(--seer-radius-md)', padding: '5px 7px',
        cursor: 'pointer', color: open ? 'var(--acc)' : 'var(--t2)', display: 'flex', alignItems: 'center',
      }}>
        <Paintbrush size={13} />
      </button>
      {open && (
        <div style={{
          position: 'absolute', top: 'calc(100% + 4px)', right: 0, zIndex: 300,
          minWidth: '160px', background: 'var(--bg1)', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-lg)', boxShadow: '0 4px 16px rgba(0,0,0,0.4)', overflow: 'hidden',
        }}>
          <div style={{ padding: '6px 12px 5px', fontSize: '10px', color: 'var(--t3)', letterSpacing: '0.07em', borderBottom: '1px solid var(--bd)' }}>
            {t('palette.title').toUpperCase()}
          </div>
          {PALETTES.map(p => (
            <button key={p.id} onClick={() => pick(p.id)}
              className="hh-dd-item"
              style={{
                display: 'flex', alignItems: 'center', gap: '8px', width: '100%', padding: '8px 12px',
                background: palette === p.id ? 'color-mix(in srgb, var(--acc) 10%, transparent)' : undefined,
                border: 'none', color: palette === p.id ? 'var(--acc)' : 'var(--t2)', fontSize: '12px', cursor: 'pointer', textAlign: 'left',
              }}
            >
              <div style={{ width: 5, height: 5, borderRadius: '50%', flexShrink: 0, background: palette === p.id ? 'var(--acc)' : 'transparent' }} />
              {t(p.key)}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

function ThemeToggle() {
  const { t } = useTranslation();
  const [theme, setThemeState] = useState(() => sharedPrefsStore.getPrefs().theme);
  const toggle = () => {
    const next = theme === 'dark' ? 'light' : 'dark';
    sharedPrefsStore.savePrefs({ theme: next });
    setThemeState(next);
  };
  return (
    <button onClick={toggle} title={t(theme === 'dark' ? 'theme.light' : 'theme.dark')} style={{
      background: 'transparent', border: '1px solid var(--bd)',
      borderRadius: 'var(--seer-radius-md)', padding: '5px 7px',
      cursor: 'pointer', color: 'var(--t2)', display: 'flex', alignItems: 'center',
    }}>
      {theme === 'dark' ? <Sun size={13} /> : <Moon size={13} />}
    </button>
  );
}

// ── Dropdown shell ─────────────────────────────────────────────────────────────
function DropdownMenu({ children }: { children: React.ReactNode }) {
  return (
    <div style={{
      position: 'absolute', top: 'calc(100% + 4px)', left: 0, zIndex: 300,
      minWidth: '180px', background: 'var(--bg1)', border: '1px solid var(--bd)',
      borderRadius: 'var(--seer-radius-lg)', boxShadow: '0 8px 24px rgba(0,0,0,0.35)',
      overflow: 'hidden',
    }}>
      {children}
    </div>
  );
}

function DropdownHeader({ label }: { label: string }) {
  return (
    <div style={{
      padding: '7px 12px 6px', fontSize: '10px', fontWeight: 600,
      color: 'var(--t3)', letterSpacing: '0.08em', borderBottom: '1px solid var(--bd)',
    }}>
      {label}
    </div>
  );
}

function DropdownItem({
  label, active, disabled, horizon, onClick,
}: {
  label: string; active?: boolean; disabled?: boolean; horizon?: string; onClick?: () => void;
}) {
  return (
    <button
      onClick={disabled ? undefined : onClick}
      disabled={disabled}
      style={{
        display: 'flex', alignItems: 'center', gap: '10px',
        width: '100%', padding: '10px 12px',
        background: active ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
        border: 'none',
        color: active ? 'var(--acc)' : disabled ? 'var(--t3)' : 'var(--t1)',
        fontSize: '12px', fontWeight: 500,
        cursor: disabled ? 'not-allowed' : 'pointer',
        textAlign: 'left', opacity: disabled ? 0.45 : 1,
        transition: 'background 0.1s',
      }}
      onMouseEnter={e => { if (!active && !disabled) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
      onMouseLeave={e => { if (!active && !disabled) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
    >
      <div style={{ width: 5, height: 5, borderRadius: '50%', flexShrink: 0, background: active ? 'var(--acc)' : 'transparent' }} />
      <span style={{ flex: 1 }}>{label}</span>
      {horizon && (
        <span style={{
          fontSize: '9px', fontWeight: 600, padding: '1px 4px', borderRadius: 3,
          background: 'color-mix(in srgb, var(--t3) 15%, transparent)', color: 'var(--t3)',
          letterSpacing: '0.05em',
        }}>{horizon}</span>
      )}
    </button>
  );
}

// ── Tenant picker ──────────────────────────────────────────────────────────────
function TenantPickerButton() {
  const user = useAuthStore(s => s.user);
  const [open, setOpen]     = useState(false);
  const [tenants, setTenants] = useState<{ tenantAlias: string }[]>([]);
  const [active, setActive]  = useState<string>(() =>
    localStorage.getItem('seer-active-tenant') ?? user?.activeTenantAlias ?? 'default',
  );
  const ref = useRef<HTMLDivElement>(null);

  const isSuperAdmin = user?.role === 'super-admin';
  const isAdmin      = user?.role === 'admin' || isSuperAdmin;
  const canFetch     = isAdmin;

  useEffect(() => {
    if (!canFetch) return;
    fetch('/chur/api/admin/tenants', { credentials: 'include' })
      .then(r => r.ok ? r.json() : [])
      .then((body: unknown) => {
        const arr = Array.isArray(body) ? (body as { tenantAlias: string; status: string }[]) : [];
        setTenants(arr.filter(t => t.status === 'ACTIVE').map(t => ({ tenantAlias: t.tenantAlias })));
      })
      .catch(() => {});
  }, [canFetch]);

  useEffect(() => {
    const h = (e: Event) => {
      const alias = (e as CustomEvent<{ activeTenant: string }>).detail?.activeTenant;
      if (alias && alias !== '__all__') setActive(alias);
    };
    window.addEventListener('aida:tenant', h);
    return () => window.removeEventListener('aida:tenant', h);
  }, []);

  // `active` is initialized from localStorage/user and updated immediately on pick().
  // Use it directly — it's always current, unlike user.activeTenantAlias which
  // lags behind until the async PATCH /auth/me/tenant response comes back.
  const displayActive = active;

  if (!isAdmin) return null;

  const displayTenants = tenants;
  const canSwitch = isAdmin && displayTenants.length > 1;

  const pick = async (alias: string) => {
    setActive(alias);
    localStorage.setItem('seer-active-tenant', alias);
    window.dispatchEvent(new CustomEvent('aida:tenant', { detail: { activeTenant: alias } }));
    setOpen(false);
    try {
      await fetch('/auth/me/tenant', {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tenantAlias: alias }),
      });
      useAuthStore.setState(s => s.user ? { user: { ...s.user, activeTenantAlias: alias } } : {});
    } catch { /* best-effort */ }
  };

  return (
    <div
      ref={ref}
      style={{ position: 'relative' }}
      onBlur={e => { if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false); }}
    >
      <button
        onClick={() => canSwitch && setOpen(v => !v)}
        title={`Tenant: ${displayActive}`}
        style={{
          display: 'flex', alignItems: 'center', gap: '5px',
          padding: '4px 8px',
          background: open
            ? 'color-mix(in srgb, var(--acc) 14%, transparent)'
            : 'color-mix(in srgb, var(--acc) 8%, transparent)',
          border: `1px solid color-mix(in srgb, var(--acc) ${open ? '50%' : '30%'}, transparent)`,
          borderRadius: 'var(--seer-radius-md)',
          color: 'var(--acc)', cursor: canSwitch ? 'pointer' : 'default',
          fontSize: '11px', fontWeight: 700, letterSpacing: '0.06em',
        }}
      >
        <span>T</span>
        <span style={{ fontWeight: 400, maxWidth: 88, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {displayActive}
        </span>
        {canSwitch && (
          <ChevronDown size={9} style={{
            color: 'var(--acc)', opacity: 0.7,
            transform: open ? 'rotate(180deg)' : undefined, transition: 'transform 0.15s',
          }} />
        )}
      </button>

      {open && displayTenants.length > 0 && (
        <div style={{
          position: 'absolute', top: 'calc(100% + 4px)', right: 0, zIndex: 300,
          minWidth: '160px', background: 'var(--bg1)', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-lg)', boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
          overflow: 'hidden',
        }}>
          <div style={{ padding: '6px 12px 5px', fontSize: '10px', color: 'var(--t3)', letterSpacing: '0.07em', borderBottom: '1px solid var(--bd)' }}>
            TENANT
          </div>
          {displayTenants.map(t => (
            <button
              key={t.tenantAlias}
              onClick={() => void pick(t.tenantAlias)}
              style={{
                display: 'flex', alignItems: 'center', gap: '8px', width: '100%', padding: '8px 12px',
                background: active === t.tenantAlias ? 'color-mix(in srgb, var(--acc) 10%, transparent)' : 'transparent',
                border: 'none', color: active === t.tenantAlias ? 'var(--acc)' : 'var(--t2)',
                fontSize: '12px', cursor: 'pointer', textAlign: 'left',
              }}
              onMouseEnter={e => { if (active !== t.tenantAlias) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
              onMouseLeave={e => { if (active !== t.tenantAlias) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
            >
              <div style={{ width: 5, height: 5, borderRadius: '50%', flexShrink: 0, background: active === t.tenantAlias ? 'var(--acc)' : 'transparent', border: `1px solid ${active === t.tenantAlias ? 'var(--acc)' : 'var(--t3)'}` }} />
              {t.tenantAlias}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

// ── Main header ────────────────────────────────────────────────────────────────
export const HeimdallHeader = memo(() => {
  const { t }        = useTranslation();
  const navigate     = useNavigate();
  const { pathname } = useLocation();
  const user         = useAuthStore(s => s.user);
  const events       = useDashboardStore(s => s.events);
  const metrics      = useDashboardStore(s => s.metrics);
  const isMobile     = useIsMobile();

  // Mobile: one-step combined nav picker (HX-01)
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const mobileNavRef = useRef<HTMLDivElement>(null);

  // Desktop: logo dropdown state (section switcher — Studio-style)
  const [seerMenuOpen, setSeerMenuOpen] = useState(false);
  const seerMenuRef   = useRef<HTMLDivElement>(null);

  const [cmdOpen,          setCmdOpen]          = useState(false);
  const [profileOpen,      setProfileOpen]      = useState(false);
  const [presentationMode, setPresentationMode] = useState(false);

  useHotkeys([{ key: 'k', ctrl: true, action: () => setCmdOpen(v => !v), global: true }]);

  const { canManageUsers } = useTenantContext();

  // React Router basename ('/heimdall' in standalone, provided by Shell in MF
   // mode) already prepends the prefix, so pass absolute app-internal routes.
  const go = useCallback(
    (route: string) => navigate(route),
    [navigate],
  );

  // Only show FENRIR section when user can manage users
  const visibleSections = SECTIONS.filter(
    sec => sec.id !== 'FENRIR' || canManageUsers,
  );

  // Active section/subtab derived from pathname
  const activeSectionId: SectionId =
    pathname.includes('/admin/tenants') ? 'FENRIR' :
    pathname.includes('/users')         ? 'FENRIR' :
    pathname.includes('/analytics')     ? 'FENRIR' :
    pathname.includes('/dali')          ? 'DALI'   :
    pathname.includes('/docs')          ? 'SAGA'   :
    'BIFROST';

  const activeSubId: string =
    pathname.includes('/admin/tenants')  ? 'Tenants'   :
    pathname.includes('/users')          ? 'Users'     :
    pathname.includes('/analytics')      ? 'Analytics' :
    pathname.includes('/dali/sources')   ? 'Sources'   :
    pathname.includes('/dali')           ? 'Sessions'  :
    pathname.includes('/docs')           ? 'Docs'      :
    pathname.includes('/dashboard')      ? 'Dashboard' :
    pathname.includes('/events')         ? 'Events'    :
    'Services';

  const activeSection = visibleSections.find(s => s.id === activeSectionId)
    ?? visibleSections[0]!;
  const activeSubTab  = activeSection?.subTabs.find(s => s.id === activeSubId);

  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';

  // Desktop: pick a section from logo dropdown
  const pickDesktopSection = (sec: Section) => {
    if (sec.horizon) return;
    go(sec.subTabs[0]?.route ?? sec.route);
    setSeerMenuOpen(false);
  };

  return (
    <>
      <header style={{
        height: '42px', background: 'var(--bg0)',
        borderBottom: '1px solid var(--bd)',
        display: 'flex', alignItems: 'center',
        padding: '0 12px', gap: '6px', flexShrink: 0, zIndex: 100,
      }}>

        {isMobile ? (
          /* ══════════════════════════════════════════════════════
             MOBILE LAYOUT (HX-01: 1-step nav)
             Single button: "B. Dashboard▾" opens combined dropdown:
               • sub-tabs of active section (top)
               • divider
               • other sections (bottom, section-switching)
             ══════════════════════════════════════════════════════ */
          <>
            {/* Combined 1-step nav picker */}
            <div
              ref={mobileNavRef}
              style={{ position: 'relative', flexShrink: 0 }}
              onBlur={e => {
                if (!mobileNavRef.current?.contains(e.relatedTarget as Node))
                  setMobileNavOpen(false);
              }}
            >
              <button
                onClick={() => setMobileNavOpen(v => !v)}
                style={{
                  display: 'flex', alignItems: 'center', gap: '4px',
                  padding: '4px 8px',
                  background: mobileNavOpen ? 'var(--bg2)' : 'transparent',
                  border: '1px solid',
                  borderColor: mobileNavOpen ? 'var(--bd)' : 'transparent',
                  borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                  transition: 'background 0.12s, border-color 0.12s',
                }}
              >
                <span style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.06em', color: 'var(--acc)' }}>
                  {activeSectionId[0]}.
                </span>
                <span style={{
                  fontSize: '11px', fontWeight: 500, color: 'var(--t2)',
                  maxWidth: 70, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                }}>
                  {activeSubTab ? t(activeSubTab.labelKey) : activeSubId}
                </span>
                <ChevronDown size={9} style={{
                  color: 'var(--t3)',
                  transform: mobileNavOpen ? 'rotate(180deg)' : 'rotate(0)',
                  transition: 'transform 0.15s',
                }} />
              </button>

              {mobileNavOpen && (
                <DropdownMenu>
                  {/* Sub-tabs of active section */}
                  <DropdownHeader label={activeSectionId} />
                  {activeSection.subTabs.map(sub => (
                    <DropdownItem
                      key={sub.id}
                      label={t(sub.labelKey).toUpperCase()}
                      active={sub.id === activeSubId}
                      onClick={() => { go(sub.route); setMobileNavOpen(false); }}
                    />
                  ))}

                  {/* Divider + other sections for switching */}
                  {visibleSections.filter(s => s.id !== activeSectionId).length > 0 && (
                    <>
                      <div style={{ height: '1px', background: 'var(--bd)', margin: '4px 0' }} />
                      {visibleSections
                        .filter(s => s.id !== activeSectionId)
                        .map(sec => (
                          <DropdownItem
                            key={sec.id}
                            label={sec.id}
                            disabled={!!sec.horizon}
                            horizon={sec.horizon}
                            onClick={() => {
                              if (!sec.horizon) {
                                go(sec.subTabs[0]?.route ?? sec.route);
                                setMobileNavOpen(false);
                              }
                            }}
                          />
                        ))}
                    </>
                  )}
                </DropdownMenu>
              )}
            </div>

            {(user?.role === 'admin' || user?.role === 'super-admin') && (
              <TenantPickerButton />
            )}

            <div style={{ flex: 1 }} />

            {/* Always visible: theme + avatar */}
            <ThemeToggle />
            {user && (
              <button
                onClick={() => setProfileOpen(true)}
                title={`${user.username} · ${user.role}`}
                className="hh-btn"
                style={{ display: 'flex', alignItems: 'center', padding: '4px 5px' }}
              >
                <div style={{
                  width: 24, height: 24, borderRadius: '50%',
                  background: 'color-mix(in srgb, var(--acc) 20%, transparent)',
                  border: '1px solid color-mix(in srgb, var(--acc) 50%, transparent)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '10px', fontWeight: 600, color: 'var(--acc)',
                }}>
                  {initials}
                </div>
              </button>
            )}
          </>
        ) : (
          /* ══════════════════════════════════════════════════════
             DESKTOP LAYOUT
             HEIMÐALLR▾ (section switcher) │ ACTIVE │ sub-tabs inline
             ══════════════════════════════════════════════════════ */
          <>
            {/* Logo + section switcher dropdown */}
            <div
              ref={seerMenuRef}
              style={{ position: 'relative', flexShrink: 0 }}
              onBlur={e => { if (!seerMenuRef.current?.contains(e.relatedTarget as Node)) setSeerMenuOpen(false); }}
            >
              <button
                onClick={() => setSeerMenuOpen(v => !v)}
                style={{
                  display: 'flex', alignItems: 'center', gap: '6px',
                  padding: '4px 8px 4px 6px',
                  background: seerMenuOpen ? 'var(--bg2)' : 'transparent',
                  border: '1px solid',
                  borderColor: seerMenuOpen ? 'var(--bd)' : 'transparent',
                  borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                  transition: 'background 0.12s, border-color 0.12s',
                }}
                onMouseEnter={e => {
                  if (!seerMenuOpen) {
                    (e.currentTarget as HTMLElement).style.background = 'var(--bg2)';
                    (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)';
                  }
                }}
                onMouseLeave={e => {
                  if (!seerMenuOpen) {
                    (e.currentTarget as HTMLElement).style.background = 'transparent';
                    (e.currentTarget as HTMLElement).style.borderColor = 'transparent';
                  }
                }}
              >
                <span style={{
                  fontFamily: 'var(--font-display)', fontSize: '13px',
                  letterSpacing: '0.08em', color: 'var(--aida-app-heimdall)',
                  textTransform: 'uppercase',
                }}>
                  HEIMÐALLR
                </span>
                <ChevronDown size={11} style={{
                  color: 'var(--t3)',
                  transform: seerMenuOpen ? 'rotate(180deg)' : 'rotate(0)',
                  transition: 'transform 0.15s',
                }} />
              </button>

              {seerMenuOpen && (
                <div style={{
                  position: 'absolute', top: 'calc(100% + 4px)', left: 0,
                  zIndex: 300, minWidth: '260px',
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
                    HEIMÐALLR ADMIN
                  </div>
                  {visibleSections.map(sec => {
                    const isCurrent = sec.id === activeSectionId;
                    const isDisabled = !!sec.horizon;
                    return (
                      <button
                        key={sec.id}
                        onClick={() => pickDesktopSection(sec)}
                        disabled={isDisabled}
                        style={{
                          display: 'flex', alignItems: 'center', gap: '10px',
                          width: '100%', padding: '10px 12px',
                          background: isCurrent ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
                          border: 'none',
                          color: isCurrent ? 'var(--acc)' : isDisabled ? 'var(--t3)' : 'var(--t1)',
                          fontSize: '12px', fontWeight: 500,
                          cursor: isDisabled ? 'not-allowed' : 'pointer',
                          opacity: isDisabled ? 0.45 : 1,
                          textAlign: 'left',
                          transition: 'background 0.1s',
                        }}
                        onMouseEnter={e => {
                          if (!isCurrent && !isDisabled) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                        }}
                        onMouseLeave={e => {
                          if (!isCurrent && !isDisabled) (e.currentTarget as HTMLElement).style.background = 'transparent';
                        }}
                      >
                        <div style={{
                          width: 5, height: 5, borderRadius: '50%', flexShrink: 0,
                          background: isCurrent ? 'var(--acc)' : 'transparent',
                        }} />
                        <div style={{ flex: 1 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <span style={{ fontWeight: 600, letterSpacing: '0.04em' }}>{sec.id}</span>
                            {sec.horizon && (
                              <span style={{
                                fontSize: '9px', fontWeight: 600, letterSpacing: '0.05em',
                                padding: '1px 5px', borderRadius: '3px',
                                background: 'color-mix(in srgb, var(--t3) 15%, transparent)',
                                color: 'var(--t3)',
                              }}>
                                {sec.horizon}
                              </span>
                            )}
                          </div>
                          <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: '2px' }}>
                            {t(sec.descKey)}
                          </div>
                        </div>
                      </button>
                    );
                  })}
                </div>
              )}
            </div>

            <HDivider />

            {/* Active section name */}
            <button
              onClick={() => go(activeSection.subTabs[0]?.route ?? activeSection.route)}
              title={`${activeSection.id} — ${t(activeSection.descKey)}`}
              style={{
                padding: '5px 10px',
                fontSize: '11px', fontWeight: 700, letterSpacing: '0.1em',
                color: 'var(--t1)',
                background: 'transparent', border: 'none',
                cursor: 'pointer', borderRadius: 'var(--seer-radius-sm)',
                transition: 'color 0.12s', flexShrink: 0,
              }}
              onMouseEnter={e => { (e.currentTarget as HTMLElement).style.color = 'var(--acc)'; }}
              onMouseLeave={e => { (e.currentTarget as HTMLElement).style.color = 'var(--t1)'; }}
            >
              {activeSection.id}
            </button>

            {/* Tenant picker (admin + super-admin) */}
            {(user?.role === 'admin' || user?.role === 'super-admin') && (
              <>
                <HDivider />
                <TenantPickerButton />
              </>
            )}

            <HDivider />

            {/* Active section sub-tabs, inline */}
            <nav style={{ display: 'flex', gap: '2px', flex: 1, alignItems: 'center' }}>
              {activeSection.subTabs.map(sub => {
                const isSub = sub.id === activeSubId;
                return (
                  <button
                    key={sub.id}
                    onClick={() => go(sub.route)}
                    style={{
                      padding: '6px 14px', fontSize: '12px',
                      fontWeight: isSub ? 500 : 400,
                      borderRadius: 'var(--seer-radius-sm)', border: 'none',
                      cursor: 'pointer',
                      background: isSub ? 'color-mix(in srgb, var(--acc) 12%, transparent)' : 'transparent',
                      color: isSub ? 'var(--acc)' : 'var(--t2)',
                      transition: 'background 0.12s, color 0.12s',
                      letterSpacing: '0.06em',
                    }}
                    onMouseEnter={e => { if (!isSub) (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
                    onMouseLeave={e => { if (!isSub) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                  >
                    {t(sub.labelKey)}
                  </button>
                );
              })}
            </nav>

            {/* Desktop secondary tools */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <button
                onClick={() => setCmdOpen(v => !v)}
                title={t('commandPalette.title') + ' (⌘K)'}
                style={{
                  display: 'flex', alignItems: 'center', gap: '6px', padding: '5px 10px',
                  background: 'var(--bg2)', border: '1px solid var(--bd)',
                  borderRadius: 'var(--seer-radius-md)', color: 'var(--t2)',
                  fontSize: '11px', cursor: 'pointer', letterSpacing: '0.04em',
                }}
              >
                <span>⌘K</span>
              </button>

              <HDivider />
              <LanguageSwitcher />
              <PaletteDropdown />

              <button
                onClick={() => setPresentationMode(true)}
                title="Presentation mode (fullscreen)"
                style={{
                  background: 'transparent', border: '1px solid var(--bd)',
                  borderRadius: 'var(--seer-radius-md)', padding: '5px 7px',
                  cursor: 'pointer', color: 'var(--t2)', display: 'flex', alignItems: 'center', fontSize: '13px',
                }}
              >
                ⛶
              </button>

              <HDivider />

              <ThemeToggle />

              {user && (
                <button
                  onClick={() => setProfileOpen(true)}
                  title={`${user.username} · ${user.role}`}
                  className="hh-btn"
                  style={{
                    display: 'flex', alignItems: 'center', gap: '6px',
                    padding: '4px 8px 4px 5px',
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
            </div>
          </>
        )}
      </header>

      {cmdOpen && (
        <HeimdallCommandPalette
          open={cmdOpen}
          onClose={() => setCmdOpen(false)}
          sections={visibleSections}
          onNavigate={go}
        />
      )}
      {profileOpen && <ProfileModal onClose={() => setProfileOpen(false)} />}
      {presentationMode && (
        <PresentationMode events={events} metrics={metrics} onExit={() => setPresentationMode(false)} />
      )}
    </>
  );
});

HeimdallHeader.displayName = 'HeimdallHeader';
