import { useRef, useState, type ReactNode } from 'react';
import { Globe, Paintbrush, Sun, Moon } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { sharedPrefsStore } from '../../stores/sharedPrefsStore';
import { PALETTES } from './heimdallNavData';

// ── Primitives ─────────────────────────────────────────────────────────────────
export function HDivider() {
  return <div style={{ width: '1px', height: '16px', background: 'var(--bd)', flexShrink: 0 }} />;
}

export function LanguageSwitcher() {
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

export function PaletteDropdown() {
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

export function ThemeToggle() {
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
export function DropdownMenu({ children }: { children: ReactNode }) {
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

export function DropdownHeader({ label }: { label: string }) {
  return (
    <div style={{
      padding: '7px 12px 6px', fontSize: '10px', fontWeight: 600,
      color: 'var(--t3)', letterSpacing: '0.08em', borderBottom: '1px solid var(--bd)',
    }}>
      {label}
    </div>
  );
}

export function DropdownItem({
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
