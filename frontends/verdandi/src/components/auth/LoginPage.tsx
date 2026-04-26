import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useTranslation } from 'react-i18next';
import { LogIn } from 'lucide-react';
import { useAuthStore } from '../../stores/authStore';

const schema = z.object({
  username: z.string().min(1, 'auth.error.required'),
  password: z.string().min(1, 'auth.error.required'),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { login, isLoading, error, isAuthenticated, clearError } = useAuthStore();

  // Pick a random slogan once — useRef persists across re-renders without setter overhead
  const sloganRef = useRef('');
  if (!sloganRef.current) {
    const list = t('app.slogans', { returnObjects: true }) as string[];
    sloganRef.current = Array.isArray(list) ? list[Math.floor(Math.random() * list.length)] : t('app.tagline');
  }
  const slogan = sloganRef.current;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  // Redirect if already authenticated.
  // NOTE: `navigate` is intentionally excluded from deps — React Router's
  // navigate function is stable across renders by design, but including it
  // causes an infinite loop in React Router v7 when the location changes
  // (new navigate ref → effect fires → navigate() → new location → repeat).
  // Navigate to '..' (parent) to stay within verdandi's routing scope so it
  // works correctly both standalone (/login → /) and inside Shell (/verdandi/login → /verdandi/).
  useEffect(() => {
    if (isAuthenticated) navigate('..', { replace: true, relative: 'path' });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  const onSubmit = async ({ username, password }: FormValues) => {
    await login(username, password);
  };

  return (
    <div style={{
      minHeight: '100%',
      background: 'var(--bg0)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '24px',
    }}>
      <div style={{
        width: '100%',
        maxWidth: '360px',
        display: 'flex',
        flexDirection: 'column',
        gap: '32px',
      }}>
        {/* Logo */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
          {/* Seiðr lettermark — amber square with S */}
          <div style={{
            width: '44px', height: '44px',
            borderRadius: 'var(--seer-radius-sm)',
            background: 'var(--acc)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            flexShrink: 0,
          }}>
            <span className="seer-logo-text" style={{ fontSize: '24px', color: 'var(--bg0)', lineHeight: 1 }}>
              S
            </span>
          </div>

          {/* Wordmark: • Seiðr Studio */}
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
            <div style={{ width: 7, height: 7, borderRadius: '50%', background: 'var(--acc)', flexShrink: 0, alignSelf: 'center' }} />
            <span className="seer-logo-text" style={{ fontSize: '26px', color: 'var(--t1)' }}>
              Seiðr
            </span>
            <span style={{ fontSize: '13px', fontWeight: 500, color: 'var(--t2)', letterSpacing: '0.06em' }}>
              Studio
            </span>
          </div>

          {/* Random slogan from library */}
          <div style={{ fontSize: '11px', color: 'var(--t3)', letterSpacing: '0.04em', textAlign: 'center', maxWidth: '280px' }}>
            {slogan}
          </div>

          {/* Three Norns — mono */}
          <div className="mono" style={{ fontSize: '10px', color: 'var(--t3)', letterSpacing: '0.07em', opacity: 0.7 }}>
            VERðANðI · URð · SKULð
          </div>
        </div>

        {/* Card — amber accent top border */}
        <div style={{
          background: 'var(--bg1)',
          border: '1px solid var(--bd)',
          borderTop: '2px solid var(--acc)',
          borderRadius: 'var(--seer-radius-lg)',
          padding: '28px',
          display: 'flex',
          flexDirection: 'column',
          gap: '20px',
        }}>
          <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {/* Username */}
            <Field label={t('auth.username')} error={errors.username ? t(errors.username.message as string) : undefined}>
              <input
                {...register('username')}
                autoComplete="username"
                autoFocus
                onChange={() => clearError()}
                style={inputStyle(!!errors.username)}
              />
            </Field>

            {/* Password */}
            <Field label={t('auth.password')} error={errors.password ? t(errors.password.message as string) : undefined}>
              <input
                {...register('password')}
                type="password"
                autoComplete="current-password"
                onChange={() => clearError()}
                style={inputStyle(!!errors.password)}
              />
            </Field>

            {/* Server error */}
            {error && (
              <div style={{
                fontSize: '12px',
                color: 'var(--wrn)',
                background: 'color-mix(in srgb, var(--wrn) 10%, transparent)',
                border: '1px solid color-mix(in srgb, var(--wrn) 25%, transparent)',
                borderRadius: 'var(--seer-radius-sm)',
                padding: '8px 10px',
              }}>
                {t(error)}
              </div>
            )}

            {/* Submit */}
            <button
              type="submit"
              disabled={isLoading}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                padding: '9px 16px',
                background: isLoading ? 'var(--bg3)' : 'var(--acc)',
                color: isLoading ? 'var(--t3)' : 'var(--bg0)',
                border: 'none',
                borderRadius: 'var(--seer-radius-md)',
                fontSize: '13px',
                fontWeight: 500,
                cursor: isLoading ? 'not-allowed' : 'pointer',
                transition: 'background 0.12s, color 0.12s',
                letterSpacing: '0.04em',
              }}
            >
              <LogIn size={14} />
              {isLoading ? t('auth.signingIn') : t('auth.login')}
            </button>
          </form>

          {/* Auth Code (SSO) — Phase 4 scaffold */}
          <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px dashed var(--bd)',
                        display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <div style={{ fontSize: '11px', color: 'var(--t3)', textAlign: 'center', letterSpacing: '0.04em' }}>
              {t('auth.or', 'or')}
            </div>
            <button
              type="button"
              onClick={() => {
                const returnTo = encodeURIComponent(window.location.origin + '/');
                window.location.href = `/auth/login?return_to=${returnTo}`;
              }}
              style={{
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px',
                padding: '9px 16px', background: 'var(--bg2)', color: 'var(--t1)',
                border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)',
                fontSize: '13px', fontWeight: 500, cursor: 'pointer',
                letterSpacing: '0.04em',
              }}
            >
              {t('auth.loginSso', '🔐 Войти через Seiðr SSO (Auth Code)')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      <label style={{ fontSize: '12px', color: 'var(--t2)', letterSpacing: '0.04em' }}>
        {label}
      </label>
      {children}
      {error && (
        <span style={{ fontSize: '11px', color: 'var(--wrn)' }}>{error}</span>
      )}
    </div>
  );
}

function inputStyle(hasError: boolean): React.CSSProperties {
  return {
    width: '100%',
    padding: '8px 10px',
    background: 'var(--bg2)',
    border: `1px solid ${hasError ? 'var(--wrn)' : 'var(--bd)'}`,
    borderRadius: 'var(--seer-radius-sm)',
    color: 'var(--t1)',
    fontSize: '13px',
    outline: 'none',
    boxSizing: 'border-box',
    fontFamily: 'inherit',
    transition: 'border-color 0.12s',
  };
}
