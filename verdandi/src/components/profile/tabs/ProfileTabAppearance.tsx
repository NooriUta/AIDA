import { memo, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../../stores/loomStore';

// ── Font definitions ──────────────────────────────────────────────────────────
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

const PALETTES = [
  { id: 'amber-forest', key: 'palette.amberForest', colors: ['#A8B860', '#1c1810', '#42382a'] },
  { id: 'slate',        key: 'palette.slate',        colors: ['#7B9EFF', '#1e2433', '#354060'] },
  { id: 'lichen',       key: 'palette.lichen',       colors: ['#B0C070', '#161814', '#363a2c'] },
  { id: 'juniper',      key: 'palette.juniper',      colors: ['#D4A050', '#171410', '#3c3228'] },
  { id: 'warm-dark',    key: 'palette.warmDark',     colors: ['#C4965A', '#241e14', '#4a3f2c'] },
];

export const ProfileTabAppearance = memo(() => {
  const { t } = useTranslation();
  const { theme, toggleTheme, palette, setPalette } = useLoomStore();

  const [uiFont,   setUiFont]   = useState(() => localStorage.getItem('seer-ui-font')   ?? 'Manrope');
  const [monoFont, setMonoFont] = useState(() => localStorage.getItem('seer-mono-font') ?? 'IBM Plex Mono');
  const [fontSize, setFontSize] = useState(() => parseInt(localStorage.getItem('seer-font-size') ?? '13', 10));
  const [density,  setDensity]  = useState<'compact' | 'normal'>(() =>
    (localStorage.getItem('seer-density') as 'compact' | 'normal') ?? 'compact'
  );

  // Apply font changes immediately
  useEffect(() => {
    document.documentElement.style.setProperty('--font', `'${uiFont}', system-ui, sans-serif`);
    localStorage.setItem('seer-ui-font', uiFont);
  }, [uiFont]);

  useEffect(() => {
    document.documentElement.style.setProperty('--mono', `'${monoFont}', monospace`);
    localStorage.setItem('seer-mono-font', monoFont);
  }, [monoFont]);

  useEffect(() => {
    document.documentElement.style.fontSize = `${fontSize}px`;
    localStorage.setItem('seer-font-size', String(fontSize));
  }, [fontSize]);

  useEffect(() => {
    document.documentElement.setAttribute('data-density', density);
    localStorage.setItem('seer-density', density);
  }, [density]);

  return (
    <div>
      <SectionTitle>{t('profile.tabs.appearance')}</SectionTitle>

      {/* Theme */}
      <FieldLabel>{t('profile.appearance.theme')}</FieldLabel>
      <div style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
        {(['dark', 'light'] as const).map((th) => (
          <button
            key={th}
            onClick={() => { if (theme !== th) toggleTheme(); }}
            style={{
              flex: 1, padding: '10px 0', borderRadius: '6px',
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
              width: '40px', height: '26px', borderRadius: '4px',
              border: '1px solid var(--bd)', overflow: 'hidden',
              display: 'flex', flexDirection: 'column',
            }}>
              <div style={{ height: '6px', background: th === 'dark' ? '#141108' : '#f5f3ee' }} />
              <div style={{ flex: 1, background: th === 'dark' ? '#1c1810' : '#faf8f3' }} />
            </div>
            {th === 'dark' ? t('theme.dark') : t('theme.light')}
          </button>
        ))}
      </div>

      {/* Palette */}
      <FieldLabel>{t('profile.appearance.palette')}</FieldLabel>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', marginBottom: '24px' }}>
        {PALETTES.map((p) => (
          <div
            key={p.id}
            onClick={() => setPalette(p.id)}
            style={{
              borderRadius: '6px',
              border: `1.5px solid ${palette === p.id ? 'var(--acc)' : 'var(--bd)'}`,
              cursor: 'pointer', overflow: 'hidden', position: 'relative',
              transition: 'border-color 0.12s',
            }}
          >
            {palette === p.id && (
              <div style={{ position: 'absolute', top: '5px', right: '6px', fontSize: '10px', color: 'var(--acc)', fontWeight: 700 }}>✓</div>
            )}
            <div style={{ height: '36px', display: 'flex' }}>
              {p.colors.map((c, i) => (
                <div key={i} style={{ flex: 1, background: c }} />
              ))}
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
        {UI_FONTS.map((f) => (
          <button
            key={f.id}
            onClick={() => setUiFont(f.id)}
            style={{
              padding: '10px 12px', borderRadius: '6px',
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
              LOOM · orders · L2
            </div>
            <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: '3px' }}>{f.sub}</div>
          </button>
        ))}
      </div>

      {/* Mono Font */}
      <FieldLabel>{t('profile.appearance.monoFont')}</FieldLabel>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', marginBottom: '24px' }}>
        {MONO_FONTS.map((f) => (
          <button
            key={f.id}
            onClick={() => setMonoFont(f.id)}
            style={{
              padding: '10px 12px', borderRadius: '6px',
              border: `1.5px solid ${monoFont === f.id ? 'var(--acc)' : 'var(--bd)'}`,
              cursor: 'pointer',
              background: monoFont === f.id ? 'color-mix(in srgb, var(--acc) 6%, var(--bg2))' : 'var(--bg2)',
              textAlign: 'left', position: 'relative',
              fontFamily: 'inherit', transition: 'border-color 0.12s, background 0.12s',
            }}
          >
            {monoFont === f.id && <span style={{ position: 'absolute', top: '6px', right: '8px', fontSize: '11px', color: 'var(--acc)', fontWeight: 700 }}>✓</span>}
            <div style={{ fontSize: '11px', fontWeight: 600, color: monoFont === f.id ? 'var(--acc)' : 'var(--t2)', letterSpacing: '0.03em', marginBottom: '5px' }}>{f.label}</div>
            <div style={{ fontSize: '12px', color: 'var(--t1)', fontFamily: `'${f.id}', monospace` }}>order_id  uuid  →</div>
            <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: '3px' }}>{f.sub}</div>
          </button>
        ))}
      </div>

      {/* Font preview */}
      <FieldLabel style={{ marginBottom: '8px' }}>{t('profile.appearance.preview')}</FieldLabel>
      <div style={{ padding: '12px 14px', background: 'var(--bg0)', border: '1px solid var(--bd)', borderRadius: '6px', marginBottom: '24px' }}>
        <div style={{ fontSize: '10px', color: 'var(--t3)', marginBottom: '4px', letterSpacing: '0.05em', textTransform: 'uppercase' }}>UI — node title</div>
        <div style={{ fontSize: `${fontSize}px`, fontWeight: 500, color: 'var(--t1)', marginBottom: '8px' }}>
          orders · public · 14 columns
        </div>
        <div style={{ fontSize: '10px', color: 'var(--t3)', marginBottom: '4px', letterSpacing: '0.05em', textTransform: 'uppercase' }}>Mono — column name &amp; type</div>
        <div style={{ fontSize: '12px', color: 'var(--acc)', fontFamily: `'${monoFont}', monospace` }}>
          customer_id&nbsp;&nbsp;int4&nbsp;&nbsp;NOT NULL
        </div>
      </div>

      {/* Font size */}
      <FieldLabel>{t('profile.appearance.fontSize')}</FieldLabel>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
        <span style={{ fontSize: '11px', color: 'var(--t3)' }}>A</span>
        <input
          type="range" min={11} max={16} value={fontSize}
          onChange={(e) => setFontSize(Number(e.target.value))}
          style={{ flex: 1, accentColor: 'var(--acc)' }}
        />
        <span style={{ fontSize: '15px', color: 'var(--t2)' }}>A</span>
        <span style={{ fontSize: '11px', color: 'var(--t2)', minWidth: '28px', textAlign: 'right' }}>{fontSize}px</span>
      </div>

      {/* Density */}
      <FieldLabel>{t('profile.appearance.density')}</FieldLabel>
      <div style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
        {(['compact', 'normal'] as const).map((d) => (
          <button
            key={d}
            onClick={() => setDensity(d)}
            style={{
              flex: 1, padding: '8px 0', borderRadius: '6px',
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
});

ProfileTabAppearance.displayName = 'ProfileTabAppearance';

// ── Shared sub-components ─────────────────────────────────────────────────────
function SectionTitle({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
      {children}
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
