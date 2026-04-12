import { Globe, Paintbrush, Sun, Moon } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useRef, useState } from 'react';
import { useShellStore }   from '../stores/shellStore';

// ── Design tokens: palettes (matches verdandi + heimdall) ─────────────────────
const PALETTES: Array<{ id: string; key: string; accent: string }> = [
  { id: 'amber-forest', key: 'palette.amberForest', accent: '#e6a817' },
  { id: 'lichen',       key: 'palette.lichen',       accent: '#6db38c' },
  { id: 'slate',        key: 'palette.slate',         accent: '#7b9ebf' },
  { id: 'juniper',      key: 'palette.juniper',       accent: '#8fbc8f' },
  { id: 'warm-dark',    key: 'palette.warmDark',      accent: '#d4882a' },
];

// ── Toolbar helpers ───────────────────────────────────────────────────────────
function Divider() {
  return (
    <div style={{ width: 1, height: 16, background: 'var(--bd)', flexShrink: 0 }} />
  );
}

// ── App tab ───────────────────────────────────────────────────────────────────
function AppTab({
  app, label, color, active, onClick,
}: {
  app: string; label: string; color: string; active: boolean; onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      style={{
        display:        'flex',
        alignItems:     'center',
        gap:            '7px',
        padding:        '5px 12px',
        background:     active ? `color-mix(in srgb, ${color} 10%, transparent)` : 'transparent',
        border:         `1px solid ${active ? color : 'transparent'}`,
        borderRadius:   'var(--seer-radius-md)',
        color:          active ? color : 'var(--t2)',
        fontSize:       '13px',
        fontWeight:     active ? 600 : 400,
        cursor:         'pointer',
        transition:     'all 0.12s',
        whiteSpace:     'nowrap',
      }}
      onMouseEnter={e => {
        if (!active) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
      }}
      onMouseLeave={e => {
        if (!active) (e.currentTarget as HTMLElement).style.background = 'transparent';
      }}
    >
      {/* App colour dot */}
      <div style={{
        width: 6, height: 6, borderRadius: '50%',
        background: color, flexShrink: 0,
      }} />
      {label}
    </button>
  );
}

// ── Language switcher ─────────────────────────────────────────────────────────
function LanguageSwitcher() {
  const { i18n, t } = useTranslation();
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const current = i18n.language.startsWith('ru') ? 'ru' : 'en';

  const handleBlur = (e: React.FocusEvent) => {
    if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false);
  };

  return (
    <div ref={ref} style={{ position: 'relative' }} onBlur={handleBlur}>
      <button
        onClick={() => setOpen(v => !v)}
        aria-haspopup="listbox" aria-expanded={open}
        style={{
          display: 'flex', alignItems: 'center', gap: '5px',
          padding: '5px 8px', background: 'transparent',
          border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)',
          color: 'var(--t2)', fontSize: '11px', cursor: 'pointer',
          letterSpacing: '0.06em', transition: 'border-color 0.12s',
        }}
      >
        <Globe size={12} />
        {current.toUpperCase()}
      </button>
      {open && (
        <div role="listbox" style={{
          position: 'absolute', top: 'calc(100% + 4px)', right: 0, zIndex: 300,
          minWidth: '120px', background: 'var(--bg1)', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)', boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
          overflow: 'hidden',
        }}>
          {(['en', 'ru'] as const).map(lang => (
            <button
              key={lang}
              role="option" aria-selected={current === lang}
              onClick={() => { void i18n.changeLanguage(lang); setOpen(false); }}
              style={{
                display: 'block', width: '100%', padding: '8px 12px', textAlign: 'left',
                background: current === lang ? 'var(--bg3)' : 'transparent',
                border: 'none', color: current === lang ? 'var(--t1)' : 'var(--t2)',
                fontSize: '12px', cursor: 'pointer', transition: 'background 0.1s',
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
  const [open, setOpen]         = useState(false);
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
          background: 'transparent', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)', padding: '5px 7px',
          cursor: 'pointer', color: open ? 'var(--acc)' : 'var(--t2)',
          display: 'flex', alignItems: 'center', transition: 'border-color 0.12s, color 0.12s',
        }}
      >
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
            <button
              key={p.id}
              onClick={() => pick(p.id)}
              style={{
                display: 'flex', alignItems: 'center', gap: '8px',
                width: '100%', padding: '8px 12px',
                background: palette === p.id ? 'color-mix(in srgb, var(--acc) 10%, transparent)' : 'transparent',
                border: 'none', color: palette === p.id ? 'var(--acc)' : 'var(--t2)',
                fontSize: '12px', cursor: 'pointer', textAlign: 'left', transition: 'background 0.1s',
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
        background: 'transparent', border: '1px solid var(--bd)',
        borderRadius: 'var(--seer-radius-md)', padding: '5px 7px',
        cursor: 'pointer', color: 'var(--t2)', display: 'flex', alignItems: 'center',
        transition: 'border-color 0.12s, color 0.12s',
      }}
    >
      {theme === 'dark' ? <Sun size={13} /> : <Moon size={13} />}
    </button>
  );
}

// ── AidaNav ───────────────────────────────────────────────────────────────────
export function AidaNav() {
  const { t }      = useTranslation();
  const currentApp = useShellStore(s => s.currentApp);
  const navigateTo = useShellStore(s => s.navigateTo);

  return (
    <nav style={{
      display:      'flex',
      alignItems:   'center',
      gap:          '6px',
      padding:      'var(--seer-space-3) var(--seer-space-6)',
      background:   'var(--bg1)',
      borderBottom: '1px solid var(--bd)',
      flexShrink:   0,
    }}>
      {/* Platform wordmark */}
      <span style={{
        fontFamily:    'var(--font-display)',
        fontSize:      '11px',
        letterSpacing: '0.1em',
        color:         'var(--t3)',
        marginRight:   'var(--seer-space-2)',
        textTransform: 'uppercase',
        flexShrink:    0,
      }}>
        AIDA
      </span>

      <Divider />

      {/* App tabs */}
      <AppTab
        app="verdandi"
        label={t('nav.verdandi')}
        color="var(--aida-app-verdandi)"
        active={currentApp === 'verdandi'}
        onClick={() => navigateTo('verdandi')}
      />
      <AppTab
        app="heimdall"
        label={t('nav.heimdall')}
        color="var(--aida-app-heimdall)"
        active={currentApp === 'heimdall'}
        onClick={() => navigateTo('heimdall')}
      />

      {/* Right toolbar */}
      <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '6px' }}>
        <LanguageSwitcher />
        <PaletteDropdown />
        <ThemeToggle />
      </div>
    </nav>
  );
}
