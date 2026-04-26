import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useTranslation } from 'react-i18next';
import { LogIn } from 'lucide-react';
import { useAuthStore } from '../../stores/authStore';
import type { ReactNode } from 'react';

// ── Validation ────────────────────────────────────────────────────────────────
const schema = z.object({
  username: z.string().min(1, 'auth.error.required'),
  password: z.string().min(1, 'auth.error.required'),
});
type FormValues = z.infer<typeof schema>;

// ── Component ─────────────────────────────────────────────────────────────────
export function LoginPage() {
  const { t }  = useTranslation();
  const navigate = useNavigate();
  const { login, isLoading, error, isAuthenticated, clearError } = useAuthStore();

  // Pick random slogan once per mount
  const sloganRef = useRef('');
  if (!sloganRef.current) {
    const list = t('app.slogans', { returnObjects: true }) as string[];
    sloganRef.current = Array.isArray(list)
      ? list[Math.floor(Math.random() * list.length)]
      : t('app.tagline');
  }

  const { register, handleSubmit, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(schema),
  });

  useEffect(() => {
    if (isAuthenticated) navigate('/overview/services', { replace: true });
  }, [isAuthenticated, navigate]);

  const onSubmit = async ({ username, password }: FormValues) => {
    await login(username, password);
  };

  return (
    <div style={{
      minHeight:      '100vh',
      background:     'var(--bg0)',
      display:        'flex',
      alignItems:     'center',
      justifyContent: 'center',
      padding:        '24px',
    }}>
      <div style={{ width: '100%', maxWidth: '360px', display: 'flex', flexDirection: 'column', gap: '32px' }}>

        {/* ── Logo block ───────────────────────────────────────────────────── */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>

          {/* Amber square lettermark — "H" */}
          <div style={{
            width: '44px', height: '44px',
            borderRadius: 'var(--seer-radius-sm)',
            background:   'var(--acc)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            flexShrink: 0,
          }}>
            <span style={{
              fontFamily: 'var(--font-display)',
              fontSize:   '22px',
              fontWeight: 800,
              color:      'var(--bg0)',
              lineHeight: 1,
            }}>H</span>
          </div>

          {/* Wordmark: • HEIMDALL Control */}
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
            <div style={{
              width: 7, height: 7, borderRadius: '50%',
              background: 'var(--acc)', flexShrink: 0, alignSelf: 'center',
            }} />
            <span style={{
              fontFamily:    'var(--font-display)',
              fontSize:      '22px',
              fontWeight:    800,
              color:         'var(--t1)',
              letterSpacing: '0.04em',
            }}>HEIMÐALLR</span>
            <span style={{ fontSize: '12px', fontWeight: 500, color: 'var(--t2)', letterSpacing: '0.06em' }}>
              Control
            </span>
          </div>

          {/* Random slogan */}
          <div style={{
            fontSize: '11px', color: 'var(--t3)',
            letterSpacing: '0.04em', textAlign: 'center', maxWidth: '280px',
          }}>
            {sloganRef.current}
          </div>

          {/* AIDA platform mono tag */}
          <div style={{
            fontFamily:    'var(--mono)',
            fontSize:      '10px', color: 'var(--t3)',
            letterSpacing: '0.07em', opacity: 0.7,
          }}>
            AIDA · PLATFORM · OBSERVABILITY
          </div>
        </div>

        {/* ── Card ─────────────────────────────────────────────────────────── */}
        <div style={{
          background:    'var(--bg1)',
          border:        '1px solid var(--bd)',
          borderTop:     '2px solid var(--acc)',
          borderRadius:  'var(--seer-radius-lg)',
          padding:       '28px',
          display:       'flex',
          flexDirection: 'column',
          gap:           '20px',
        }}>
          {/* Server error (для случая если Auth Code callback вернулся с ошибкой) */}
          {error && (
            <div style={{
              fontSize:     '12px',
              color:        'var(--wrn)',
              background:   'color-mix(in srgb, var(--wrn) 10%, transparent)',
              border:       '1px solid color-mix(in srgb, var(--wrn) 25%, transparent)',
              borderRadius: 'var(--seer-radius-sm)',
              padding:      '8px 10px',
            }}>
              {t(error, error)}
            </div>
          )}

          {/* Auth Code (SSO) — единственный способ входа.
              ROPC форма удалена в sprint/auth-redesign-2026q2 — теперь только KC redirect flow. */}
          <button
            type="button"
            data-testid="login-sso-btn"
            onClick={() => {
              const returnTo = encodeURIComponent(window.location.origin + '/');
              window.location.href = `/auth/login?return_to=${returnTo}`;
            }}
            style={{
              display:        'flex',
              alignItems:     'center',
              justifyContent: 'center',
              gap:            '8px',
              padding:        '12px 20px',
              background:     'var(--acc)',
              color:          'var(--bg0)',
              border:         'none',
              borderRadius:   'var(--seer-radius-md)',
              fontSize:       '14px',
              fontWeight:     600,
              cursor:         'pointer',
              transition:     'background 0.12s, transform 0.12s',
              letterSpacing:  '0.04em',
            }}
          >
            <LogIn size={16} />
            {t('auth.loginSso', 'Войти через Seiðr SSO')}
          </button>

          <div style={{
            fontSize:      '11px',
            color:         'var(--t3)',
            textAlign:     'center',
            marginTop:     '4px',
            letterSpacing: '0.04em',
          }}>
            {t('auth.ssoNote', 'OAuth 2.0 Authorization Code + PKCE через Seiðr Studio')}
          </div>
        </div>

      </div>
    </div>
  );
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function Field({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      <label style={{ fontSize: '12px', color: 'var(--t2)', letterSpacing: '0.04em' }}>
        {label}
      </label>
      {children}
      {error && <span style={{ fontSize: '11px', color: 'var(--wrn)' }}>{error}</span>}
    </div>
  );
}

function inputStyle(hasError: boolean): React.CSSProperties {
  return {
    width:        '100%',
    padding:      '8px 10px',
    background:   'var(--bg2)',
    border:       `1px solid ${hasError ? 'var(--wrn)' : 'var(--bd)'}`,
    borderRadius: 'var(--seer-radius-sm)',
    color:        'var(--t1)',
    fontSize:     '13px',
    outline:      'none',
    boxSizing:    'border-box',
    fontFamily:   'inherit',
    transition:   'border-color 0.12s',
  };
}
