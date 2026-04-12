// i18n must be initialised before any component renders — importing here
// ensures it runs when App is loaded as an MF remote (main.tsx is not
// executed in that case).
import './i18n/config';
import React, { Suspense, useEffect, useRef, useState } from 'react';
import { Routes, Route, Navigate, NavLink } from 'react-router-dom';
import { Globe, Paintbrush, Sun, Moon } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { LoginPage }      from './components/auth/LoginPage';
import { ProtectedRoute } from './components/ProtectedRoute';
import { ProfileModal }   from './components/profile/ProfileModal';
import { useAuthStore }   from './stores/authStore';

const DashboardPage   = React.lazy(() => import('./pages/DashboardPage'));
const EventStreamPage = React.lazy(() => import('./pages/EventStreamPage'));
const ControlsPage    = React.lazy(() => import('./pages/ControlsPage'));

// ── Design token palettes (mirrors ProfileModal + verdandi) ───────────────────
const PALETTES: Array<{ id: string; key: string; accent: string }> = [
  { id: 'amber-forest', key: 'palette.amberForest', accent: '#e6a817' },
  { id: 'lichen',       key: 'palette.lichen',       accent: '#6db38c' },
  { id: 'slate',        key: 'palette.slate',         accent: '#7b9ebf' },
  { id: 'juniper',      key: 'palette.juniper',       accent: '#8fbc8f' },
  { id: 'warm-dark',    key: 'palette.warmDark',      accent: '#d4882a' },
];

// ── Toolbar divider ───────────────────────────────────────────────────────────
function ToolbarDivider() {
  return (
    <div style={{
      width:     '1px',
      height:    '16px',
      background: 'var(--bd)',
      flexShrink: 0,
    }} />
  );
}

// ── ⌘K button ─────────────────────────────────────────────────────────────────
function CmdKButton() {
  const { t } = useTranslation();
  return (
    <button
      title={t('commandPalette.title') + ' (⌘K)'}
      style={{
        display: 'flex', alignItems: 'center', gap: '6px',
        padding: '5px 10px',
        background: 'var(--bg2)',
        border: '1px solid var(--bd)',
        borderRadius: 'var(--seer-radius-md)',
        color: 'var(--t2)', fontSize: '11px',
        cursor: 'pointer',
        letterSpacing: '0.04em',
        transition: 'border-color 0.12s',
      }}
    >
      <span>⌘K</span>
    </button>
  );
}

// ── Language switcher ─────────────────────────────────────────────────────────
function LanguageSwitcher() {
  const { i18n, t } = useTranslation();
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const current = i18n.language.startsWith('ru') ? 'ru' : 'en';

  const pick = (lang: string) => {
    void i18n.changeLanguage(lang);
    setOpen(false);
  };

  const handleBlur = (e: React.FocusEvent) => {
    if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false);
  };

  return (
    <div ref={ref} style={{ position: 'relative' }} onBlur={handleBlur}>
      <button
        onClick={() => setOpen(v => !v)}
        aria-haspopup="listbox"
        aria-expanded={open}
        style={{
          display: 'flex', alignItems: 'center', gap: '5px',
          padding: '5px 8px',
          background: 'transparent',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)',
          color: 'var(--t2)', fontSize: '11px',
          cursor: 'pointer', letterSpacing: '0.06em',
          transition: 'border-color 0.12s, color 0.12s',
        }}
      >
        <Globe size={12} />
        {current.toUpperCase()}
      </button>

      {open && (
        <div
          role="listbox"
          style={{
            position: 'absolute', top: 'calc(100% + 4px)', right: 0,
            zIndex: 300, minWidth: '120px',
            background: 'var(--bg1)', border: '1px solid var(--bd)',
            borderRadius: 'var(--seer-radius-md)',
            boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
            overflow: 'hidden',
          }}
        >
          {(['en', 'ru'] as const).map(lang => (
            <button
              key={lang}
              role="option"
              aria-selected={current === lang}
              onClick={() => pick(lang)}
              style={{
                display: 'block', width: '100%',
                padding: '8px 12px', textAlign: 'left',
                background: current === lang ? 'var(--bg3)' : 'transparent',
                border: 'none',
                color: current === lang ? 'var(--t1)' : 'var(--t2)',
                fontSize: '12px', cursor: 'pointer',
                transition: 'background 0.1s',
              }}
              onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
              onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = current === lang ? 'var(--bg3)' : 'transparent'; }}
            >
              {t(`language.${lang}`)}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

// ── Palette dropdown ──────────────────────────────────────────────────────────
function PaletteDropdown() {
  const { t } = useTranslation();
  const [open, setOpen]       = useState(false);
  const [palette, setPaletteState] = useState(() => localStorage.getItem('seer-palette') ?? 'amber-forest');
  const ref = useRef<HTMLDivElement>(null);

  const pick = (id: string) => {
    document.documentElement.setAttribute('data-palette', id);
    localStorage.setItem('seer-palette', id);
    setPaletteState(id);
    setOpen(false);
  };

  const handleBlur = (e: React.FocusEvent) => {
    if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false);
  };

  return (
    <div ref={ref} style={{ position: 'relative' }} onBlur={handleBlur}>
      <button
        onClick={() => setOpen(v => !v)}
        title={t('palette.title')}
        style={{
          background: 'transparent',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)',
          padding: '5px 7px',
          cursor: 'pointer',
          color: open ? 'var(--acc)' : 'var(--t2)',
          display: 'flex', alignItems: 'center',
          transition: 'border-color 0.12s, color 0.12s',
        }}
      >
        <Paintbrush size={13} />
      </button>

      {open && (
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
          {PALETTES.map(p => (
            <button
              key={p.id}
              onClick={() => pick(p.id)}
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
              onMouseEnter={e => { if (palette !== p.id) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
              onMouseLeave={e => { if (palette !== p.id) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
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
  );
}

// ── Theme toggle ──────────────────────────────────────────────────────────────
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
    <button
      onClick={toggle}
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
  );
}

// ── Nav item ──────────────────────────────────────────────────────────────────
function NavItem({ to, label }: { to: string; label: string }) {
  return (
    <NavLink
      to={to}
      style={({ isActive }) => ({
        fontSize:       '13px',
        fontWeight:     500,
        color:          isActive ? 'var(--acc)' : 'var(--t2)',
        borderBottom:   isActive ? '2px solid var(--acc)' : '2px solid transparent',
        paddingBottom:  '2px',
        transition:     'color 0.15s, border-color 0.15s',
        textDecoration: 'none',
        whiteSpace:     'nowrap',
      })}
    >
      {label}
    </NavLink>
  );
}

// ── Avatar button ─────────────────────────────────────────────────────────────
function AvatarButton({ onClick }: { onClick: () => void }) {
  const user     = useAuthStore(s => s.user);
  const initials = user?.username.slice(0, 2).toUpperCase() ?? '?';
  return (
    <button
      onClick={onClick}
      title={user ? `${user.username} · ${user.role}` : 'Profile'}
      style={{
        display: 'flex', alignItems: 'center', gap: '6px',
        padding: '4px 8px 4px 5px',
        background: 'transparent',
        border: '1px solid transparent',
        borderRadius: 'var(--seer-radius-md)',
        cursor: 'pointer',
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
      {user && (
        <span style={{ fontSize: '12px', color: 'var(--t2)' }}>{user.username}</span>
      )}
    </button>
  );
}

// ── Protected layout ──────────────────────────────────────────────────────────
function AppLayout() {
  const { t } = useTranslation();
  const [profileOpen, setProfileOpen] = useState(false);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <nav style={{
        display:      'flex',
        alignItems:   'center',
        gap:          'var(--seer-space-4)',
        padding:      'var(--seer-space-3) var(--seer-space-6)',
        background:   'var(--bg1)',
        borderBottom: '1px solid var(--bd)',
        flexShrink:   0,
      }}>
        {/* Logo / wordmark */}
        <span style={{
          fontFamily:    'var(--font-display)',
          fontSize:      '13px',
          letterSpacing: '0.08em',
          color:         'var(--aida-app-heimdall)',
          marginRight:   'var(--seer-space-4)',
          textTransform: 'uppercase',
          flexShrink:    0,
        }}>
          HEIMDALL
        </span>

        {/* Nav links */}
        <NavItem to="dashboard" label={t('nav.dashboard')} />
        <NavItem to="events"    label={t('nav.events')} />
        <NavItem to="controls"  label={t('nav.controls')} />

        {/* Right-side toolbar (pushed to right) */}
        <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <CmdKButton />
          <ToolbarDivider />
          <LanguageSwitcher />
          <PaletteDropdown />
          <ThemeToggle />
          <ToolbarDivider />
          <AvatarButton onClick={() => setProfileOpen(true)} />
        </div>
      </nav>

      <main style={{ flex: 1, overflow: 'hidden' }}>
        <Suspense fallback={
          <div style={{ padding: 'var(--seer-space-8)', color: 'var(--t3)' }}>
            {t('status.loading')}
          </div>
        }>
          <Routes>
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="events"    element={<EventStreamPage />} />
            <Route path="controls"  element={<ControlsPage />} />
            <Route path="*"         element={<Navigate to="dashboard" replace />} />
          </Routes>
        </Suspense>
      </main>

      {profileOpen && <ProfileModal onClose={() => setProfileOpen(false)} />}
    </div>
  );
}

// ── Session check on mount ────────────────────────────────────────────────────
function SessionGuard({ children }: { children: React.ReactNode }) {
  const checkSession = useAuthStore(s => s.checkSession);
  useEffect(() => { void checkSession(); }, [checkSession]);
  return <>{children}</>;
}

/**
 * Heimdall-frontend root — exported as MF remote (heimdall-frontend/App).
 *
 * Does NOT include BrowserRouter — the Router context is provided by the
 * host (Shell) when running as a remote, or by main.tsx when running standalone.
 * Route paths are RELATIVE so they work under both "/" and "/heimdall/*".
 */
export default function App() {
  return (
    <SessionGuard>
      <Routes>
        <Route path="login" element={<LoginPage />} />
        <Route path="*" element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        } />
      </Routes>
    </SessionGuard>
  );
}
