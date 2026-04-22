import { useState, useEffect, useCallback } from 'react';
import { LogOut, User, Palette, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuthStore }       from '../../stores/authStore';
import { useIsMobile }        from '../../hooks/useIsMobile';
import { sharedPrefsStore }   from '../../stores/sharedPrefsStore';

// ── Types ─────────────────────────────────────────────────────────────────────
type Tab = 'profile' | 'appearance';

const PALETTES: Array<{ id: string; key: string; colors: string[] }> = [
  { id: 'amber-forest', key: 'palette.amberForest', colors: ['#A8B860', '#1c1810', '#42382a'] },
  { id: 'slate',        key: 'palette.slate',        colors: ['#7B9EFF', '#1e2433', '#354060'] },
  { id: 'lichen',       key: 'palette.lichen',       colors: ['#B0C070', '#161814', '#363a2c'] },
  { id: 'juniper',      key: 'palette.juniper',       colors: ['#D4A050', '#171410', '#3c3228'] },
  { id: 'warm-dark',    key: 'palette.warmDark',      colors: ['#C4965A', '#241e14', '#4a3f2c'] },
];

const UI_FONTS = [
  { id: 'Manrope',       label: 'Manrope',       sub: 'Brand · Rounded sans' },
  { id: 'DM Sans',       label: 'DM Sans',        sub: 'Rounded humanist' },
  { id: 'Inter',         label: 'Inter',          sub: 'Neutral grotesque' },
  { id: 'IBM Plex Sans', label: 'IBM Plex Sans',  sub: 'Technical · IDE-like' },
  { id: 'Geist',         label: 'Geist ✦',        sub: 'Vercel · Ultra-clean' },
  { id: 'Oxanium',       label: 'Oxanium ✦',      sub: 'Sci-fi · Data terminal' },
  { id: 'Exo 2',         label: 'Exo 2 ✦',        sub: 'Geometric · Futuristic' },
];

const MONO_FONTS = [
  { id: 'IBM Plex Mono',   label: 'IBM Plex Mono',   sub: 'Brand mono · Clean' },
  { id: 'Fira Code',       label: 'Fira Code',        sub: 'Ligatures' },
  { id: 'JetBrains Mono',  label: 'JetBrains Mono',   sub: 'Ligatures · Wide' },
  { id: 'Geist Mono',      label: 'Geist Mono ✦',     sub: 'Vercel · Minimal' },
  { id: 'Source Code Pro', label: 'Source Code Pro',  sub: 'Geometric · Adobe' },
];

// ── ProfileModal ──────────────────────────────────────────────────────────────
export function ProfileModal({ onClose }: { onClose: () => void }) {
  const { t } = useTranslation();
  const isMobile = useIsMobile();
  const [tab, setTab]           = useState<Tab>('profile');
  const [theme, setThemeState]  = useState<string>(() => localStorage.getItem('seer-theme')   ?? 'dark');
  const [palette, setPaletteState] = useState<string>(() => localStorage.getItem('seer-palette') ?? 'amber-forest');

  const { user, logout } = useAuthStore();

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [onClose]);

  const applyTheme = useCallback((t: string) => {
    sharedPrefsStore.savePrefs({ theme: t });
    setThemeState(t);
  }, []);

  const applyPalette = useCallback((p: string) => {
    sharedPrefsStore.savePrefs({ palette: p });
    setPaletteState(p);
  }, []);

  const handleLogout = async () => {
    await logout();
    onClose();
  };

  const initials = user?.username.slice(0, 2).toUpperCase() ?? '??';
  const email    = `${user?.username ?? 'user'}@seer.internal`;

  const roleBadgeColor: Record<string, string> = {
    admin:  'var(--acc)',
    editor: 'var(--inf)',
    viewer: 'var(--t3)',
  };

  return (
    <>
      {/* Backdrop */}
      <div
        onClick={onClose}
        style={{
          position: 'fixed', inset: 0,
          background: 'rgba(0,0,0,0.6)',
          backdropFilter: 'blur(2px)',
          zIndex: 999,
        }}
      />

      {/* Modal */}
      <div style={{
        position:     'fixed',
        ...(isMobile
          ? { inset: 0, borderRadius: 0 }
          : { top: '50%', left: '50%', transform: 'translate(-50%, -50%)',
              width: '820px', height: '560px', borderRadius: 'var(--seer-radius-xl)',
              boxShadow: '0 24px 64px rgba(0,0,0,0.5)' }),
        zIndex:       1000,
        background:   'var(--bg1)',
        border:       '1px solid var(--bd)',
        display:      'flex',
        flexDirection: isMobile ? 'column' : 'row',
        overflow:     'hidden',
      }}>

        {/* ── Sidebar / Top nav ────────────────────────────────────────────── */}
        <div style={isMobile ? {
          background: 'var(--bg2)',
          borderBottom: '1px solid var(--bd)',
          display: 'flex', flexDirection: 'row', alignItems: 'center',
          padding: '10px 12px', gap: 8, flexShrink: 0,
        } : {
          width: '220px', flexShrink: 0,
          background: 'var(--bg2)', borderRight: '1px solid var(--bd)',
          display: 'flex', flexDirection: 'column', padding: '24px 0',
        }}>
          {/* User avatar + name — desktop only */}
          {!isMobile && (
            <div style={{ padding: '0 20px 20px', borderBottom: '1px solid var(--bd)' }}>
              <div style={{
                width: 40, height: 40, borderRadius: '50%',
                background: 'color-mix(in srgb, var(--acc) 20%, transparent)',
                border:     '1px solid color-mix(in srgb, var(--acc) 50%, transparent)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: '14px', fontWeight: 600, color: 'var(--acc)', marginBottom: '10px',
              }}>{initials}</div>
              <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--t1)', marginBottom: '3px' }}>
                {user?.username ?? '—'}
              </div>
              <div style={{
                display: 'inline-flex', alignItems: 'center',
                fontSize: '10px', fontWeight: 600, letterSpacing: '0.06em', textTransform: 'uppercase',
                padding: '2px 6px', borderRadius: 'var(--seer-radius-sm)',
                background: 'color-mix(in srgb, var(--acc) 15%, transparent)',
                color: roleBadgeColor[user?.role ?? 'viewer'] ?? 'var(--t3)',
              }}>
                {user?.role ?? 'viewer'}
              </div>
            </div>
          )}

          {/* Nav tabs */}
          <nav style={isMobile
            ? { display: 'flex', flexDirection: 'row', gap: 4, flex: 1 }
            : { flex: 1, padding: '12px 8px', display: 'flex', flexDirection: 'column', gap: '2px' }
          }>
            <SidebarItem icon={<User size={14} />}    label={t('profile.tabs.profile')}     active={tab === 'profile'}    onClick={() => setTab('profile')}    compact={isMobile} />
            <SidebarItem icon={<Palette size={14} />} label={t('profile.tabs.appearance')} active={tab === 'appearance'} onClick={() => setTab('appearance')} compact={isMobile} />
          </nav>

          {/* Logout + close */}
          <div style={{ display: 'flex', gap: 8, padding: isMobile ? '0' : '0 8px', flexShrink: 0 }}>
            <button
              onClick={handleLogout}
              title={t('auth.logout')}
              style={{
                display: 'flex', alignItems: 'center', gap: isMobile ? 0 : 8,
                padding: isMobile ? '6px 8px' : '8px 12px',
                background: 'transparent',
                border: '1px solid color-mix(in srgb, var(--danger) 40%, transparent)',
                borderRadius: 'var(--seer-radius-md)',
                color: 'var(--danger)', fontSize: '13px', cursor: 'pointer',
                ...(isMobile ? {} : { width: '100%' }),
              }}
            >
              <LogOut size={14} />
              {!isMobile && <span style={{ marginLeft: 8 }}>{t('auth.logout')}</span>}
            </button>
            {isMobile && (
              <button onClick={onClose} style={{
                display: 'flex', alignItems: 'center', padding: '6px 8px',
                background: 'transparent', border: '1px solid var(--bd)',
                borderRadius: 'var(--seer-radius-md)', color: 'var(--t3)', cursor: 'pointer',
              }}>
                <X size={16} />
              </button>
            )}
          </div>
        </div>

        {/* ── Content ──────────────────────────────────────────────────────── */}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          {/* Header — desktop only */}
          {!isMobile && (
            <div style={{
              display: 'flex', alignItems: 'center', justifyContent: 'space-between',
              padding: '20px 28px', borderBottom: '1px solid var(--bd)', flexShrink: 0,
            }}>
              <div style={{ fontSize: '15px', fontWeight: 600, color: 'var(--t1)' }}>
                {tab === 'profile' ? t('profile.tabs.profile') : t('profile.tabs.appearance')}
              </div>
              <button onClick={onClose} style={{
                background: 'transparent', border: 'none', color: 'var(--t3)', cursor: 'pointer',
                padding: '4px', borderRadius: 'var(--seer-radius-sm)', display: 'flex', alignItems: 'center',
              }}>
                <X size={16} />
              </button>
            </div>
          )}

          {/* Tab content */}
          <div style={{ flex: 1, overflowY: 'auto', padding: isMobile ? '16px' : '28px' }}>
            {tab === 'profile'    && <ProfileTab initials={initials} username={user?.username} role={user?.role} email={email} roleColor={roleBadgeColor[user?.role ?? 'viewer']} onClose={onClose} />}
            {tab === 'appearance' && <AppearanceTab theme={theme} palette={palette} onTheme={applyTheme} onPalette={applyPalette} />}
          </div>
        </div>
      </div>
    </>
  );
}

// ── Sidebar item ──────────────────────────────────────────────────────────────
function SidebarItem({ icon, label, active, onClick, compact }: {
  icon: React.ReactNode; label: string; active: boolean; onClick: () => void; compact?: boolean;
}) {
  return (
    <button
      onClick={onClick}
      style={{
        display:      'flex',
        alignItems:   'center',
        gap:          '6px',
        padding:      compact ? '7px 10px' : '8px 12px',
        borderRadius: 'var(--seer-radius-md)',
        background:   active ? 'color-mix(in srgb, var(--acc) 12%, transparent)' : 'transparent',
        color:        active ? 'var(--acc)' : 'var(--t2)',
        fontSize:     '13px',
        fontWeight:   active ? 600 : 400,
        border:       compact
          ? `1px solid ${active ? 'color-mix(in srgb, var(--acc) 30%, transparent)' : 'transparent'}`
          : 'none',
        cursor:       'pointer',
        width:        compact ? 'auto' : '100%',
        textAlign:    'left',
        whiteSpace:   'nowrap',
        transition:   'background 0.1s, color 0.1s',
      }}
    >
      {icon}<span>{label}</span>
    </button>
  );
}

// ── Profile tab ───────────────────────────────────────────────────────────────
function ProfileTab({ initials, username, role, email, roleColor, onClose }: {
  initials: string; username?: string; role?: string; email: string; roleColor: string;
  onClose: () => void;
}) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const goto = (path: string) => { onClose(); navigate(path); };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      {/* Avatar hero */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        <div style={{
          width: 64, height: 64, borderRadius: '50%',
          background: 'color-mix(in srgb, var(--acc) 18%, var(--bg3))',
          border:     '2px solid color-mix(in srgb, var(--acc) 30%, transparent)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '22px', fontWeight: 700, color: 'var(--acc)',
          flexShrink: 0,
        }}>{initials}</div>
        <div>
          <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--t1)', marginBottom: '6px' }}>
            {username ?? '—'}
          </div>
          <div style={{
            display: 'inline-flex', alignItems: 'center',
            fontSize: '11px', fontWeight: 600, letterSpacing: '0.06em', textTransform: 'uppercase',
            padding: '3px 8px', borderRadius: 'var(--seer-radius-sm)',
            background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
            color: roleColor,
          }}>
            {role ?? 'viewer'}
          </div>
        </div>
      </div>

      {/* Info fields */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <InfoField label={t('profile.fieldUsername')} value={username ?? '—'} mono />
        <InfoField label={t('profile.fieldEmail')}    value={email} mono />
        <InfoField label={t('profile.fieldRole')}     value={role ?? 'viewer'} />
        <InfoField label={t('profile.fieldPlatform')} value={t('profile.platform')} />
      </div>

      {/* Round 5 — self-service navigation shortcuts */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
        <span style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
          {t('profile.quickLinks', 'Быстрые ссылки')}
        </span>
        <button className="btn-secondary" onClick={() => goto('/me/profile')}>
          {t('nav.profile', 'Профиль')} →
        </button>
        <button className="btn-secondary" onClick={() => goto('/me/preferences')}>
          {t('nav.preferences', 'Настройки')} →
        </button>
        <button className="btn-secondary" onClick={() => goto('/me/notifications')}>
          {t('nav.notifications', 'Уведомления')} →
        </button>
      </div>
    </div>
  );
}

function InfoField({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
      <span style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
        {label}
      </span>
      <div style={{
        padding:      '9px 12px',
        background:   'var(--bg2)',
        border:       '1px solid var(--bd)',
        borderRadius: 'var(--seer-radius-sm)',
        fontSize:     '13px',
        color:        'var(--t1)',
        fontFamily:   mono ? 'var(--mono)' : 'var(--font)',
      }}>
        {value}
      </div>
    </div>
  );
}

// ── Appearance tab ────────────────────────────────────────────────────────────
function AppearanceTab({ theme, palette, onTheme, onPalette }: {
  theme: string; palette: string;
  onTheme: (t: string) => void; onPalette: (p: string) => void;
}) {
  const { t } = useTranslation();

  const [uiFont,   setUiFont]   = useState(() => localStorage.getItem('seer-ui-font')   ?? 'Manrope');
  const [monoFont, setMonoFont] = useState(() => localStorage.getItem('seer-mono-font') ?? 'IBM Plex Mono');
  const [fontSize, setFontSize] = useState(() => parseInt(localStorage.getItem('seer-font-size') ?? '13', 10));
  const [density,  setDensity]  = useState<'compact' | 'normal'>(() =>
    (localStorage.getItem('seer-density') as 'compact' | 'normal') ?? 'compact'
  );

  useEffect(() => {
    document.documentElement.style.setProperty('--font', `'${uiFont}', system-ui, sans-serif`);
    sharedPrefsStore.savePrefs({ uiFont });
  }, [uiFont]);

  useEffect(() => {
    document.documentElement.style.setProperty('--mono', `'${monoFont}', monospace`);
    sharedPrefsStore.savePrefs({ monoFont });
  }, [monoFont]);

  useEffect(() => {
    document.documentElement.style.fontSize = `${fontSize}px`;
    sharedPrefsStore.savePrefs({ fontSize: String(fontSize) });
  }, [fontSize]);

  useEffect(() => {
    document.documentElement.setAttribute('data-density', density);
    sharedPrefsStore.savePrefs({ density });
  }, [density]);

  return (
    <div>
      {/* Theme */}
      <FieldLabel>{t('profile.appearance.theme')}</FieldLabel>
      <div style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
        {(['dark', 'light'] as const).map(th => (
          <button
            key={th}
            onClick={() => onTheme(th)}
            style={{
              flex: 1, padding: '10px 0', borderRadius: 'var(--seer-radius-md)',
              border: `1.5px solid ${theme === th ? 'var(--acc)' : 'var(--bd)'}`,
              cursor: 'pointer',
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '6px',
              background: theme === th ? 'color-mix(in srgb, var(--acc) 8%, var(--bg2))' : 'var(--bg2)',
              color: theme === th ? 'var(--acc)' : 'var(--t2)',
              fontSize: '11px', fontWeight: 500, fontFamily: 'inherit',
              transition: 'all 0.12s',
            }}
          >
            <div style={{
              width: '40px', height: '26px', borderRadius: 'var(--seer-radius-sm)',
              border: '1px solid var(--bd)', overflow: 'hidden', display: 'flex', flexDirection: 'column',
            }}>
              <div style={{ height: '6px', background: th === 'dark' ? '#141108' : '#f5f3ee' }} />
              <div style={{ flex: 1, background: th === 'dark' ? '#1c1810' : '#faf8f3' }} />
            </div>
            {th === 'dark' ? t('profile.appearance.dark') : t('profile.appearance.light')}
          </button>
        ))}
      </div>

      {/* Palette */}
      <FieldLabel>{t('profile.appearance.palette')}</FieldLabel>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', marginBottom: '24px' }}>
        {PALETTES.map(p => (
          <div
            key={p.id}
            onClick={() => onPalette(p.id)}
            style={{
              borderRadius: 'var(--seer-radius-md)',
              border: `1.5px solid ${palette === p.id ? 'var(--acc)' : 'var(--bd)'}`,
              cursor: 'pointer', overflow: 'hidden', position: 'relative',
              transition: 'border-color 0.12s',
            }}
          >
            {palette === p.id && (
              <div style={{ position: 'absolute', top: '5px', right: '6px', fontSize: '10px', color: 'var(--acc)', fontWeight: 700 }}>✓</div>
            )}
            <div style={{ height: '36px', display: 'flex' }}>
              {p.colors.map((c, i) => <div key={i} style={{ flex: 1, background: c }} />)}
            </div>
            <div style={{ padding: '5px 8px', fontSize: '10px', fontWeight: 600, color: 'var(--t2)', background: 'var(--bg2)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              {t(p.key)}
            </div>
          </div>
        ))}
      </div>

      {/* UI Font */}
      <FieldLabel>{t('profile.appearance.uiFont')}</FieldLabel>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px', marginBottom: '20px' }}>
        {UI_FONTS.map(f => (
          <button
            key={f.id}
            onClick={() => setUiFont(f.id)}
            style={{
              padding: '10px 12px', borderRadius: 'var(--seer-radius-md)',
              border: `1.5px solid ${uiFont === f.id ? 'var(--acc)' : 'var(--bd)'}`,
              cursor: 'pointer',
              background: uiFont === f.id ? 'color-mix(in srgb, var(--acc) 6%, var(--bg2))' : 'var(--bg2)',
              textAlign: 'left', position: 'relative',
              fontFamily: 'inherit', transition: 'border-color 0.12s, background 0.12s',
            }}
          >
            {uiFont === f.id && <span style={{ position: 'absolute', top: '6px', right: '8px', fontSize: '11px', color: 'var(--acc)', fontWeight: 700 }}>✓</span>}
            <div style={{ fontSize: '11px', fontWeight: 600, color: uiFont === f.id ? 'var(--acc)' : 'var(--t2)', letterSpacing: '0.03em', marginBottom: '5px' }}>{f.label}</div>
            <div style={{ fontSize: '14px', fontWeight: 500, color: 'var(--t1)', fontFamily: `'${f.id}', sans-serif`, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              HM · events · L2
            </div>
            <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: '3px' }}>{f.sub}</div>
          </button>
        ))}
      </div>

      {/* Mono Font */}
      <FieldLabel>{t('profile.appearance.monoFont')}</FieldLabel>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', marginBottom: '24px' }}>
        {MONO_FONTS.map(f => (
          <button
            key={f.id}
            onClick={() => setMonoFont(f.id)}
            style={{
              padding: '10px 12px', borderRadius: 'var(--seer-radius-md)',
              border: `1.5px solid ${monoFont === f.id ? 'var(--acc)' : 'var(--bd)'}`,
              cursor: 'pointer',
              background: monoFont === f.id ? 'color-mix(in srgb, var(--acc) 6%, var(--bg2))' : 'var(--bg2)',
              textAlign: 'left', position: 'relative',
              fontFamily: 'inherit', transition: 'border-color 0.12s, background 0.12s',
            }}
          >
            {monoFont === f.id && <span style={{ position: 'absolute', top: '6px', right: '8px', fontSize: '11px', color: 'var(--acc)', fontWeight: 700 }}>✓</span>}
            <div style={{ fontSize: '11px', fontWeight: 600, color: monoFont === f.id ? 'var(--acc)' : 'var(--t2)', letterSpacing: '0.03em', marginBottom: '5px' }}>{f.label}</div>
            <div style={{ fontSize: '12px', color: 'var(--t1)', fontFamily: `'${f.id}', monospace` }}>session_id  uuid  →</div>
            <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: '3px' }}>{f.sub}</div>
          </button>
        ))}
      </div>

      {/* Font preview */}
      <FieldLabel style={{ marginBottom: '8px' }}>{t('profile.appearance.preview')}</FieldLabel>
      <div style={{ padding: '12px 14px', background: 'var(--bg0)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', marginBottom: '24px' }}>
        <div style={{ fontSize: '10px', color: 'var(--t3)', marginBottom: '4px', letterSpacing: '0.05em', textTransform: 'uppercase' }}>UI — event title</div>
        <div style={{ fontSize: `${fontSize}px`, fontWeight: 500, color: 'var(--t1)', marginBottom: '8px' }}>
          dali · parse · 14 atoms resolved
        </div>
        <div style={{ fontSize: '10px', color: 'var(--t3)', marginBottom: '4px', letterSpacing: '0.05em', textTransform: 'uppercase' }}>Mono — session ID &amp; type</div>
        <div style={{ fontSize: '12px', color: 'var(--acc)', fontFamily: `'${monoFont}', monospace` }}>
          session_id&nbsp;&nbsp;uuid&nbsp;&nbsp;NOT NULL
        </div>
      </div>

      {/* Font size */}
      <FieldLabel>{t('profile.appearance.fontSize')}</FieldLabel>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
        <span style={{ fontSize: '11px', color: 'var(--t3)' }}>A</span>
        <input
          type="range" min={11} max={16} value={fontSize}
          onChange={e => setFontSize(Number(e.target.value))}
          style={{ flex: 1, accentColor: 'var(--acc)' }}
        />
        <span style={{ fontSize: '15px', color: 'var(--t2)' }}>A</span>
        <span style={{ fontSize: '11px', color: 'var(--t2)', minWidth: '28px', textAlign: 'right' }}>{fontSize}px</span>
      </div>

      {/* Density */}
      <FieldLabel>{t('profile.appearance.density')}</FieldLabel>
      <div style={{ display: 'flex', gap: '8px', marginBottom: '16px' }}>
        {(['compact', 'normal'] as const).map(d => (
          <button
            key={d}
            onClick={() => setDensity(d)}
            style={{
              flex: 1, padding: '8px 0', borderRadius: 'var(--seer-radius-md)',
              border: `1.5px solid ${density === d ? 'var(--acc)' : 'var(--bd)'}`,
              cursor: 'pointer', fontFamily: 'inherit',
              background: density === d ? 'color-mix(in srgb, var(--acc) 8%, var(--bg2))' : 'var(--bg2)',
              color: density === d ? 'var(--acc)' : 'var(--t2)',
              fontSize: '11px', fontWeight: 500, transition: 'all 0.12s',
            }}
          >
            {t(`profile.appearance.${d}`)}
          </button>
        ))}
      </div>
    </div>
  );
}

function FieldLabel({ children, style }: { children: React.ReactNode; style?: React.CSSProperties }) {
  return (
    <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '8px', textTransform: 'uppercase', ...style }}>
      {children}
    </div>
  );
}
