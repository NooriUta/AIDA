import { memo, useRef, useState } from 'react';
import { Globe, Paintbrush, Sun, Moon, ChevronDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore }      from '../../stores/authStore';
import { useDashboardStore } from '../../stores/dashboardStore';
import { ProfileModal }      from '../profile/ProfileModal';
import { PresentationMode }  from '../PresentationMode';

// ── Navigation data ────────────────────────────────────────────────────────────
type SectionId = 'ОБЗОР' | 'DALI' | 'ADMIN';

interface SubTab { id: string; labelKey: string; route: string }
interface Section { id: SectionId; descKey: string; route: string; subTabs: SubTab[]; horizon?: string }

const SECTIONS: Section[] = [
  {
    id: 'ОБЗОР', descKey: 'nav.overviewDesc', route: '/overview',
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
  { id: 'ADMIN', descKey: 'nav.adminDesc', route: '/admin', subTabs: [], horizon: 'H2' },
];

const PALETTES: Array<{ id: string; key: string }> = [
  { id: 'amber-forest', key: 'palette.amberForest' },
  { id: 'lichen',       key: 'palette.lichen'      },
  { id: 'slate',        key: 'palette.slate'        },
  { id: 'juniper',      key: 'palette.juniper'      },
  { id: 'warm-dark',    key: 'palette.warmDark'     },
];

// ── Toolbar primitives ─────────────────────────────────────────────────────────
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
        cursor: 'pointer', color: open ? 'var(--acc)' : 'var(--t2)',
        display: 'flex', alignItems: 'center',
      }}>
        <Paintbrush size={13} />
      </button>
      {open && (
        <div style={{
          position: 'absolute', top: 'calc(100% + 4px)', right: 0, zIndex: 300,
          minWidth: '160px', background: 'var(--bg1)', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-lg)', boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
          overflow: 'hidden',
        }}>
          <div style={{ padding: '6px 12px 5px', fontSize: '10px', color: 'var(--t3)', letterSpacing: '0.07em', borderBottom: '1px solid var(--bd)' }}>
            {t('palette.title').toUpperCase()}
          </div>
          {PALETTES.map(p => (
            <button key={p.id} onClick={() => pick(p.id)} style={{
              display: 'flex', alignItems: 'center', gap: '8px', width: '100%', padding: '8px 12px',
              background: palette === p.id ? 'color-mix(in srgb, var(--acc) 10%, transparent)' : 'transparent',
              border: 'none', color: palette === p.id ? 'var(--acc)' : 'var(--t2)',
              fontSize: '12px', cursor: 'pointer', textAlign: 'left',
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

// ── Main header component ──────────────────────────────────────────────────────
export const HeimdallHeader = memo(() => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const user    = useAuthStore(s => s.user);
  const events  = useDashboardStore(s => s.events);
  const metrics = useDashboardStore(s => s.metrics);

  const [sectionMenuOpen, setSectionMenuOpen] = useState(false);
  const [profileOpen,     setProfileOpen]     = useState(false);
  const [presentationMode, setPresentationMode] = useState(false);
  const sectionMenuRef = useRef<HTMLDivElement>(null);

  const activeSectionId: SectionId =
    pathname.includes('/dali') ? 'DALI' : 'ОБЗОР';

  const activeSubId: string =
    pathname.includes('/dali/sources')  ? 'Sources'   :
    pathname.includes('/dali/jobrunr')  ? 'JobRunr'   :
    pathname.includes('/dali')          ? 'Sessions'  :
    pathname.includes('/dashboard')     ? 'Dashboard' :
    pathname.includes('/events')        ? 'Events'    :
    'Services';

  const currentSection = SECTIONS.find(s => s.id === activeSectionId)!;
  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';

  return (
    <>
      <header style={{
        height: '42px', background: 'var(--bg0)',
        borderBottom: '1px solid var(--bd)',
        display: 'flex', alignItems: 'center',
        padding: '0 12px', gap: '8px', flexShrink: 0, zIndex: 100,
      }}>

        {/* ── Logo ─────────────────────────────────────────────────────── */}
        <span style={{
          fontFamily: 'var(--font-display)', fontSize: '13px',
          letterSpacing: '0.08em', color: 'var(--aida-app-heimdall)',
          textTransform: 'uppercase', flexShrink: 0,
        }}>
          HEIMÐALLR
        </span>

        <HDivider />

        {/* ── Section dropdown ─────────────────────────────────────────── */}
        <div
          ref={sectionMenuRef}
          style={{ position: 'relative', flexShrink: 0 }}
          onBlur={e => { if (!sectionMenuRef.current?.contains(e.relatedTarget as Node)) setSectionMenuOpen(false); }}
        >
          <button
            onClick={() => setSectionMenuOpen(v => !v)}
            style={{
              display: 'flex', alignItems: 'center', gap: '6px',
              padding: '4px 8px 4px 4px',
              background: sectionMenuOpen ? 'var(--bg2)' : 'transparent',
              border: '1px solid',
              borderColor: sectionMenuOpen ? 'var(--bd)' : 'transparent',
              borderRadius: 'var(--seer-radius-md)',
              cursor: 'pointer', transition: 'background 0.12s, border-color 0.12s',
            }}
            onMouseEnter={e => {
              if (!sectionMenuOpen) {
                (e.currentTarget as HTMLElement).style.background = 'var(--bg2)';
                (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)';
              }
            }}
            onMouseLeave={e => {
              if (!sectionMenuOpen) {
                (e.currentTarget as HTMLElement).style.background = 'transparent';
                (e.currentTarget as HTMLElement).style.borderColor = 'transparent';
              }
            }}
          >
            <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--acc)', flexShrink: 0 }} />
            <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--t1)', letterSpacing: '0.04em' }}>
              {activeSectionId}
            </span>
            <ChevronDown size={11} style={{
              color: 'var(--t3)',
              transform: sectionMenuOpen ? 'rotate(180deg)' : 'rotate(0)',
              transition: 'transform 0.15s',
            }} />
          </button>

          {sectionMenuOpen && (
            <div style={{
              position: 'absolute', top: 'calc(100% + 4px)', left: 0, zIndex: 300,
              minWidth: '240px', background: 'var(--bg1)', border: '1px solid var(--bd)',
              borderRadius: 'var(--seer-radius-lg)', boxShadow: '0 8px 24px rgba(0,0,0,0.35)',
              overflow: 'hidden',
            }}>
              <div style={{
                padding: '7px 12px 6px', fontSize: '10px', fontWeight: 600,
                color: 'var(--t3)', letterSpacing: '0.08em', borderBottom: '1px solid var(--bd)',
              }}>
                HEIMÐALLR
              </div>
              {SECTIONS.map(sec => {
                const isCurrent  = sec.id === activeSectionId;
                const isDisabled = !!sec.horizon;
                return (
                  <button
                    key={sec.id}
                    disabled={isDisabled}
                    onClick={() => {
                      if (!isDisabled) {
                        navigate(sec.subTabs[0]?.route ?? sec.route);
                        setSectionMenuOpen(false);
                      }
                    }}
                    style={{
                      display: 'flex', alignItems: 'center', gap: '10px',
                      width: '100%', padding: '10px 12px',
                      background: isCurrent ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
                      border: 'none',
                      color: isCurrent ? 'var(--acc)' : isDisabled ? 'var(--t3)' : 'var(--t1)',
                      fontSize: '12px', fontWeight: 500,
                      cursor: isDisabled ? 'not-allowed' : 'pointer',
                      textAlign: 'left', opacity: isDisabled ? 0.5 : 1,
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
                        {isDisabled ? 'RBAC Sprint 4 — июнь 2026' : t(sec.descKey)}
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </div>

        <HDivider />

        {/* ── Active section name (home point) ─────────────────────────── */}
        <button
          onClick={() => navigate(currentSection.subTabs[0]?.route ?? currentSection.route)}
          title={t(currentSection.descKey)}
          style={{
            padding: '5px 10px', fontSize: '11px', fontWeight: 700,
            letterSpacing: '0.1em', color: 'var(--t1)',
            background: 'transparent', border: 'none',
            cursor: 'pointer', borderRadius: 'var(--seer-radius-sm)', flexShrink: 0,
            transition: 'color 0.12s',
          }}
          onMouseEnter={e => { (e.currentTarget as HTMLElement).style.color = 'var(--acc)'; }}
          onMouseLeave={e => { (e.currentTarget as HTMLElement).style.color = 'var(--t1)'; }}
        >
          {activeSectionId}
        </button>

        <HDivider />

        {/* ── Sub-tabs ──────────────────────────────────────────────────── */}
        <nav style={{ display: 'flex', gap: '2px', flex: 1, alignItems: 'center' }}>
          {currentSection.subTabs.map(sub => {
            const isActive = sub.id === activeSubId;
            const label = sub.id === 'JobRunr' ? `${t(sub.labelKey)} ↗` : t(sub.labelKey);
            return (
              <button
                key={sub.id}
                onClick={() => navigate(sub.route)}
                style={{
                  padding: '6px 14px', fontSize: '12px',
                  fontWeight: isActive ? 500 : 400,
                  borderRadius: 'var(--seer-radius-sm)', border: 'none',
                  cursor: 'pointer',
                  background: isActive
                    ? 'color-mix(in srgb, var(--acc) 12%, transparent)'
                    : 'transparent',
                  color: isActive ? 'var(--acc)' : 'var(--t2)',
                  transition: 'background 0.12s, color 0.12s',
                  letterSpacing: '0.06em', whiteSpace: 'nowrap',
                }}
                onMouseEnter={e => { if (!isActive) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
                onMouseLeave={e => { if (!isActive) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
              >
                {label}
              </button>
            );
          })}
        </nav>

        {/* ── Docs standalone ────────────────────────────────────────────── */}
        <button
          onClick={() => navigate('/docs')}
          style={{
            padding: '5px 10px', fontSize: '12px', color: 'var(--t3)',
            background: 'transparent', border: 'none',
            cursor: 'pointer', borderRadius: 'var(--seer-radius-sm)',
            letterSpacing: '0.04em', whiteSpace: 'nowrap', transition: 'color 0.12s',
          }}
          onMouseEnter={e => { (e.currentTarget as HTMLElement).style.color = 'var(--t1)'; }}
          onMouseLeave={e => { (e.currentTarget as HTMLElement).style.color = 'var(--t3)'; }}
        >
          Docs
        </button>

        {/* ── Right toolbar ──────────────────────────────────────────────── */}
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
          <ThemeToggle />

          <button
            onClick={() => setPresentationMode(true)}
            title="Presentation mode (fullscreen)"
            style={{
              background: 'transparent', border: '1px solid var(--bd)',
              borderRadius: 'var(--seer-radius-md)', padding: '5px 7px',
              cursor: 'pointer', color: 'var(--t2)',
              display: 'flex', alignItems: 'center', fontSize: '13px',
            }}
          >
            ⛶
          </button>

          <HDivider />

          {/* Users link */}
          <button
            onClick={() => navigate('/users')}
            style={{
              padding: '4px 8px', fontSize: '12px', color: 'var(--t2)',
              background: 'transparent', border: '1px solid transparent',
              borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
              letterSpacing: '0.04em', transition: 'color 0.12s, border-color 0.12s',
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
            {t('nav.users')}
          </button>

          {/* Avatar */}
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
      </header>

      {profileOpen && <ProfileModal onClose={() => setProfileOpen(false)} />}
      {presentationMode && (
        <PresentationMode
          events={events}
          metrics={metrics}
          onExit={() => setPresentationMode(false)}
        />
      )}
    </>
  );
});

HeimdallHeader.displayName = 'HeimdallHeader';
