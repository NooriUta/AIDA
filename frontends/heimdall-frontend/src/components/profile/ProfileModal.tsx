import { useState, useEffect, useCallback } from 'react';
import { LogOut, User, Palette, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '../../stores/authStore';
import { useIsMobile }  from '../../hooks/useIsMobile';

// ── Types ─────────────────────────────────────────────────────────────────────
type Tab = 'profile' | 'appearance';

const PALETTES: Array<{ id: string; key: string; accent: string }> = [
  { id: 'amber-forest', key: 'palette.amberForest', accent: '#e6a817' },
  { id: 'lichen',       key: 'palette.lichen',       accent: '#6db38c' },
  { id: 'slate',        key: 'palette.slate',         accent: '#7b9ebf' },
  { id: 'juniper',      key: 'palette.juniper',       accent: '#8fbc8f' },
  { id: 'warm-dark',    key: 'palette.warmDark',      accent: '#d4882a' },
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
    document.documentElement.setAttribute('data-theme', t);
    localStorage.setItem('seer-theme', t);
    setThemeState(t);
  }, []);

  const applyPalette = useCallback((p: string) => {
    document.documentElement.setAttribute('data-palette', p);
    localStorage.setItem('seer-palette', p);
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
            <SidebarItem icon={<User size={14} />}    label={t('profile.title')}      active={tab === 'profile'}    onClick={() => setTab('profile')}    compact={isMobile} />
            <SidebarItem icon={<Palette size={14} />} label={t('profile.appearance')} active={tab === 'appearance'} onClick={() => setTab('appearance')} compact={isMobile} />
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
                {tab === 'profile' ? t('profile.title') : t('profile.appearance')}
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
            {tab === 'profile'    && <ProfileTab initials={initials} username={user?.username} role={user?.role} email={email} roleColor={roleBadgeColor[user?.role ?? 'viewer']} />}
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
function ProfileTab({ initials, username, role, email, roleColor }: {
  initials: string; username?: string; role?: string; email: string; roleColor: string;
}) {
  const { t } = useTranslation();
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
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      {/* Theme */}
      <div>
        <div style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: '12px' }}>
          {t('palette.title')}
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          {(['dark', 'light'] as const).map(th => (
            <button
              key={th}
              onClick={() => onTheme(th)}
              style={{
                flex:         1,
                padding:      '16px',
                background:   theme === th ? 'color-mix(in srgb, var(--acc) 12%, var(--bg2))' : 'var(--bg2)',
                border:       `2px solid ${theme === th ? 'var(--acc)' : 'var(--bd)'}`,
                borderRadius: 'var(--seer-radius-md)',
                color:        theme === th ? 'var(--acc)' : 'var(--t2)',
                fontSize:     '13px',
                fontWeight:   theme === th ? 600 : 400,
                cursor:       'pointer',
                transition:   'all 0.12s',
                display:      'flex',
                flexDirection: 'column',
                alignItems:   'center',
                gap:          '8px',
              }}
            >
              {/* Mini preview */}
              <div style={{
                width: '48px', height: '32px',
                borderRadius: 'var(--seer-radius-sm)',
                background: th === 'dark' ? '#0e0e0e' : '#fafaf8',
                border: '1px solid var(--bd)',
                display: 'flex', gap: '3px', alignItems: 'center', justifyContent: 'center',
              }}>
                <div style={{ width: 8, height: 8, borderRadius: '50%', background: th === 'dark' ? '#333' : '#ddd' }} />
                <div style={{ width: 20, height: 4, borderRadius: 2, background: th === 'dark' ? '#444' : '#ccc' }} />
              </div>
              {t(`theme.${th}`)}
            </button>
          ))}
        </div>
      </div>

      {/* Palette */}
      <div>
        <div style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: '12px' }}>
          {t('palette.title')}
        </div>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          {PALETTES.map(p => (
            <button
              key={p.id}
              onClick={() => onPalette(p.id)}
              title={t(p.key)}
              style={{
                width:        '44px',
                height:       '44px',
                borderRadius: '50%',
                background:   p.accent,
                border:       palette === p.id ? `3px solid var(--t1)` : '3px solid transparent',
                outline:      palette === p.id ? `2px solid ${p.accent}` : 'none',
                cursor:       'pointer',
                transition:   'transform 0.1s',
                transform:    palette === p.id ? 'scale(1.15)' : 'scale(1)',
              }}
            />
          ))}
        </div>
        <div style={{ fontSize: '12px', color: 'var(--t3)', marginTop: '8px' }}>
          {t(PALETTES.find(p => p.id === palette)?.key ?? '') || '—'}
        </div>
      </div>

      <div style={{ fontSize: '12px', color: 'var(--t3)', borderTop: '1px solid var(--bd)', paddingTop: '16px' }}>
        {t('profile.themeHint')}
      </div>
    </div>
  );
}
