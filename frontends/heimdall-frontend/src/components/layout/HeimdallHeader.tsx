import { memo, useRef, useState } from 'react';
import { Globe, Paintbrush, Sun, Moon, ChevronDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore }      from '../../stores/authStore';
import { useDashboardStore } from '../../stores/dashboardStore';
import { ProfileModal }      from '../profile/ProfileModal';
import { PresentationMode }  from '../PresentationMode';
import { useIsMobile }       from '../../hooks/useIsMobile';

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
      { id: 'JobRunr',  labelKey: 'nav.jobrunr',  route: '/dali/jobrunr'  },
    ],
  },
  {
    id: 'SAGA', descKey: 'nav.docsDesc', route: '/docs',
    subTabs: [
      { id: 'Docs', labelKey: 'nav.docs', route: '/docs' },
    ],
  },
  {
    id: 'FENRIR', descKey: 'nav.adminDesc', route: '/users',
    subTabs: [
      { id: 'Users', labelKey: 'nav.users', route: '/users' },
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
              style={{
                display: 'block', width: '100%', padding: '8px 12px', textAlign: 'left',
                background: current === lang ? 'var(--bg3)' : 'transparent', border: 'none',
                color: current === lang ? 'var(--t1)' : 'var(--t2)', fontSize: '12px', cursor: 'pointer',
              }}
              onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
              onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = current === lang ? 'var(--bg3)' : 'transparent'; }}
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
  const [palette, setPaletteState] = useState(() => localStorage.getItem('seer-palette') ?? 'amber-forest');
  const ref = useRef<HTMLDivElement>(null);
  const pick = (id: string) => {
    document.documentElement.setAttribute('data-palette', id);
    localStorage.setItem('seer-palette', id);
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
            <button key={p.id} onClick={() => pick(p.id)} style={{
              display: 'flex', alignItems: 'center', gap: '8px', width: '100%', padding: '8px 12px',
              background: palette === p.id ? 'color-mix(in srgb, var(--acc) 10%, transparent)' : 'transparent',
              border: 'none', color: palette === p.id ? 'var(--acc)' : 'var(--t2)', fontSize: '12px', cursor: 'pointer', textAlign: 'left',
            }}
              onMouseEnter={e => { if (palette !== p.id) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
              onMouseLeave={e => { if (palette !== p.id) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
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
  const [theme, setThemeState] = useState(() => localStorage.getItem('seer-theme') ?? 'dark');
  const toggle = () => {
    const next = theme === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('seer-theme', next);
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

// ── Main header ────────────────────────────────────────────────────────────────
export const HeimdallHeader = memo(() => {
  const { t }        = useTranslation();
  const navigate     = useNavigate();
  const { pathname } = useLocation();
  const user         = useAuthStore(s => s.user);
  const events       = useDashboardStore(s => s.events);
  const metrics      = useDashboardStore(s => s.metrics);
  const isMobile     = useIsMobile();

  // Mobile: two-step picker state
  const [sectionPickerOpen, setSectionPickerOpen] = useState(false);
  const [subPickerOpen,     setSubPickerOpen]     = useState(false);
  const sectionPickerRef = useRef<HTMLDivElement>(null);
  const subPickerRef     = useRef<HTMLDivElement>(null);

  // Desktop: per-section dropdown state
  const [openSectionId, setOpenSectionId] = useState<SectionId | null>(null);
  const menuRefs = useRef(new Map<SectionId, HTMLDivElement | null>());

  const [profileOpen,      setProfileOpen]      = useState(false);
  const [presentationMode, setPresentationMode] = useState(false);

  // Active section/subtab derived from pathname
  const activeSectionId: SectionId =
    pathname.includes('/dali') ? 'DALI' :
    pathname.includes('/docs') ? 'SAGA' :
    'BIFROST';

  const activeSubId: string =
    pathname.includes('/dali/sources')  ? 'Sources'   :
    pathname.includes('/dali/jobrunr')  ? 'JobRunr'   :
    pathname.includes('/dali')          ? 'Sessions'  :
    pathname.includes('/docs')          ? 'Docs'      :
    pathname.includes('/dashboard')     ? 'Dashboard' :
    pathname.includes('/events')        ? 'Events'    :
    'Services';

  const activeSection = SECTIONS.find(s => s.id === activeSectionId)!;
  const activeSubTab  = activeSection?.subTabs.find(s => s.id === activeSubId);

  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';

  // Navigate to a section (mobile section picker)
  const pickSection = (sec: Section) => {
    if (sec.horizon) return;
    navigate(sec.subTabs[0]?.route ?? sec.route);
    setSectionPickerOpen(false);
  };

  // Desktop: toggle per-section dropdown
  const toggleDesktopSection = (sec: Section) => {
    if (sec.horizon) return;
    if (sec.subTabs.length <= 1) {
      navigate(sec.subTabs[0]?.route ?? sec.route);
      setOpenSectionId(null);
      return;
    }
    setOpenSectionId(v => v === sec.id ? null : sec.id);
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
             MOBILE LAYOUT
             Step 1: HEIMÐALLR▾ → section picker (full names)
             Step 2: X.▾ → sub-page picker + current page label
             ══════════════════════════════════════════════════════ */
          <>
            {/* Step 1 — section picker */}
            <div
              ref={sectionPickerRef}
              style={{ position: 'relative', flexShrink: 0 }}
              onBlur={e => { if (!sectionPickerRef.current?.contains(e.relatedTarget as Node)) setSectionPickerOpen(false); }}
            >
              <button
                onClick={() => { setSectionPickerOpen(v => !v); setSubPickerOpen(false); }}
                style={{
                  display: 'flex', alignItems: 'center', gap: '4px',
                  padding: '4px 6px',
                  background: sectionPickerOpen ? 'var(--bg2)' : 'transparent',
                  border: '1px solid',
                  borderColor: sectionPickerOpen ? 'var(--bd)' : 'transparent',
                  borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                  transition: 'background 0.12s, border-color 0.12s',
                }}
              >
                <span style={{
                  fontFamily: 'var(--font-display)', fontSize: '12px',
                  letterSpacing: '0.08em', color: 'var(--aida-app-heimdall)',
                  textTransform: 'uppercase',
                }}>
                  HEIMÐALLR
                </span>
                <ChevronDown size={10} style={{
                  color: 'var(--t3)',
                  transform: sectionPickerOpen ? 'rotate(180deg)' : 'rotate(0)',
                  transition: 'transform 0.15s',
                }} />
              </button>

              {sectionPickerOpen && (
                <DropdownMenu>
                  <DropdownHeader label="HEIMÐALLR" />
                  {SECTIONS.map(sec => (
                    <DropdownItem
                      key={sec.id}
                      label={sec.id}
                      active={sec.id === activeSectionId}
                      disabled={!!sec.horizon}
                      horizon={sec.horizon}
                      onClick={() => pickSection(sec)}
                    />
                  ))}
                </DropdownMenu>
              )}
            </div>

            <HDivider />

            {/* Step 2 — section-letter dropdown (sub-pages of active section) */}
            <div
              ref={subPickerRef}
              style={{ position: 'relative', flexShrink: 0 }}
              onBlur={e => { if (!subPickerRef.current?.contains(e.relatedTarget as Node)) setSubPickerOpen(false); }}
            >
              <button
                onClick={() => {
                  if (activeSection.subTabs.length > 1) setSubPickerOpen(v => !v);
                }}
                title={activeSectionId}
                style={{
                  display: 'flex', alignItems: 'center', gap: '3px',
                  padding: '4px 6px',
                  background: subPickerOpen ? 'var(--bg2)' : 'transparent',
                  border: '1px solid',
                  borderColor: subPickerOpen ? 'var(--bd)' : 'transparent',
                  borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                  transition: 'background 0.12s, border-color 0.12s',
                }}
              >
                <span style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.06em', color: 'var(--acc)' }}>
                  {activeSectionId[0]}.
                </span>
                {activeSection.subTabs.length > 1 && (
                  <ChevronDown size={9} style={{
                    color: 'var(--t3)',
                    transform: subPickerOpen ? 'rotate(180deg)' : 'rotate(0)',
                    transition: 'transform 0.15s',
                  }} />
                )}
              </button>

              {subPickerOpen && activeSection.subTabs.length > 1 && (
                <DropdownMenu>
                  <DropdownHeader label={activeSectionId} />
                  {activeSection.subTabs.map(sub => (
                    <DropdownItem
                      key={sub.id}
                      label={t(sub.labelKey).toUpperCase()}
                      active={sub.id === activeSubId}
                      onClick={() => { navigate(sub.route); setSubPickerOpen(false); }}
                    />
                  ))}
                </DropdownMenu>
              )}
            </div>

            {/* Current sub-page name */}
            <span style={{
              fontSize: '12px', fontWeight: 600, letterSpacing: '0.06em',
              color: 'var(--t1)', whiteSpace: 'nowrap',
            }}>
              {activeSubTab ? t(activeSubTab.labelKey).toUpperCase() : activeSubId.toUpperCase()}
            </span>

            <div style={{ flex: 1 }} />

            {/* Always visible: theme + avatar */}
            <ThemeToggle />
            {user && (
              <button
                onClick={() => setProfileOpen(true)}
                title={`${user.username} · ${user.role}`}
                style={{
                  display: 'flex', alignItems: 'center',
                  padding: '4px 5px',
                  background: 'transparent', border: '1px solid transparent',
                  borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                  transition: 'background 0.12s, border-color 0.12s',
                }}
                onMouseEnter={e => {
                  (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                  (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)';
                }}
                onMouseLeave={e => {
                  (e.currentTarget as HTMLElement).style.background = 'transparent';
                  (e.currentTarget as HTMLElement).style.borderColor = 'transparent';
                }}
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
             All sections visible; each opens sub-page dropdown
             ══════════════════════════════════════════════════════ */
          <>
            {/* Logo */}
            <span style={{
              fontFamily: 'var(--font-display)', fontSize: '13px',
              letterSpacing: '0.08em', color: 'var(--aida-app-heimdall)',
              textTransform: 'uppercase', flexShrink: 0,
            }}>
              HEIMÐALLR
            </span>

            <HDivider />

            {/* Section nav */}
            <nav style={{ display: 'flex', gap: '2px', alignItems: 'center', flexShrink: 0 }}>
              {SECTIONS.map(sec => {
                const isActive    = sec.id === activeSectionId;
                const isDisabled  = !!sec.horizon;
                const isOpen      = openSectionId === sec.id;
                const hasDropdown = sec.subTabs.length > 1;

                return (
                  <div
                    key={sec.id}
                    ref={el => { menuRefs.current.set(sec.id, el); }}
                    style={{ position: 'relative' }}
                    onBlur={e => {
                      const container = menuRefs.current.get(sec.id);
                      if (!container?.contains(e.relatedTarget as Node))
                        setOpenSectionId(prev => prev === sec.id ? null : prev);
                    }}
                  >
                    <button
                      onClick={() => toggleDesktopSection(sec)}
                      disabled={isDisabled}
                      title={sec.id}
                      style={{
                        display: 'flex', alignItems: 'center', gap: '3px',
                        padding: '4px 8px',
                        background: isActive
                          ? 'color-mix(in srgb, var(--acc) 12%, transparent)'
                          : isOpen ? 'var(--bg2)' : 'transparent',
                        border: '1px solid',
                        borderColor: isActive
                          ? 'color-mix(in srgb, var(--acc) 30%, transparent)'
                          : isOpen ? 'var(--bd)' : 'transparent',
                        borderRadius: 'var(--seer-radius-md)',
                        cursor: isDisabled ? 'not-allowed' : 'pointer',
                        opacity: isDisabled ? 0.4 : 1,
                        transition: 'background 0.12s, border-color 0.12s',
                      }}
                      onMouseEnter={e => {
                        if (!isActive && !isDisabled && !isOpen)
                          (e.currentTarget as HTMLElement).style.background = 'var(--bg2)';
                      }}
                      onMouseLeave={e => {
                        if (!isActive && !isOpen)
                          (e.currentTarget as HTMLElement).style.background = 'transparent';
                      }}
                    >
                      <span style={{
                        fontSize: '11px', fontWeight: isActive ? 700 : 500,
                        letterSpacing: '0.08em',
                        color: isActive ? 'var(--acc)' : 'var(--t2)',
                      }}>
                        {sec.id}
                      </span>
                      {sec.horizon && (
                        <span style={{
                          fontSize: '9px', fontWeight: 600, letterSpacing: '0.05em',
                          padding: '1px 4px', borderRadius: '3px',
                          background: 'color-mix(in srgb, var(--t3) 15%, transparent)',
                          color: 'var(--t3)',
                        }}>
                          {sec.horizon}
                        </span>
                      )}
                      {hasDropdown && (
                        <ChevronDown size={9} style={{
                          color: isActive ? 'var(--acc)' : 'var(--t3)',
                          transform: isOpen ? 'rotate(180deg)' : 'rotate(0)',
                          transition: 'transform 0.15s', flexShrink: 0,
                        }} />
                      )}
                    </button>

                    {isOpen && hasDropdown && (
                      <DropdownMenu>
                        <DropdownHeader label={sec.id} />
                        {sec.subTabs.map(sub => (
                          <DropdownItem
                            key={sub.id}
                            label={t(sub.labelKey).toUpperCase()}
                            active={sub.id === activeSubId}
                            onClick={() => { navigate(sub.route); setOpenSectionId(null); }}
                          />
                        ))}
                      </DropdownMenu>
                    )}
                  </div>
                );
              })}
            </nav>

            <div style={{ flex: 1 }} />

            {/* Desktop secondary tools */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <button
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

              <button
                onClick={() => navigate('/users')}
                style={{
                  padding: '4px 8px', fontSize: '11px', color: 'var(--t2)',
                  background: 'transparent', border: '1px solid transparent',
                  borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                  letterSpacing: '0.06em', transition: 'color 0.12s, border-color 0.12s',
                }}
                onMouseEnter={e => {
                  (e.currentTarget as HTMLElement).style.color = 'var(--t1)';
                  (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)';
                }}
                onMouseLeave={e => {
                  (e.currentTarget as HTMLElement).style.color = 'var(--t2)';
                  (e.currentTarget as HTMLElement).style.borderColor = 'transparent';
                }}
              >
                {t('nav.users').toUpperCase()}
              </button>

              <HDivider />

              <ThemeToggle />

              {user && (
                <button
                  onClick={() => setProfileOpen(true)}
                  title={`${user.username} · ${user.role}`}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '6px',
                    padding: '4px 8px 4px 5px',
                    background: 'transparent', border: '1px solid transparent',
                    borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                    transition: 'background 0.12s, border-color 0.12s',
                  }}
                  onMouseEnter={e => {
                    (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                    (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)';
                  }}
                  onMouseLeave={e => {
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
            </div>
          </>
        )}
      </header>

      {profileOpen && <ProfileModal onClose={() => setProfileOpen(false)} />}
      {presentationMode && (
        <PresentationMode events={events} metrics={metrics} onExit={() => setPresentationMode(false)} />
      )}
    </>
  );
});

HeimdallHeader.displayName = 'HeimdallHeader';
