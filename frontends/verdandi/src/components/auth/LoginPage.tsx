import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { LogIn } from 'lucide-react';
import { useAuthStore } from '../../stores/authStore';

export function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { error, isAuthenticated } = useAuthStore();

  // Pick a random slogan once — useRef persists across re-renders without setter overhead
  const sloganRef = useRef('');
  if (!sloganRef.current) {
    const list = t('app.slogans', { returnObjects: true }) as string[];
    sloganRef.current = Array.isArray(list) ? list[Math.floor(Math.random() * list.length)] : t('app.tagline');
  }
  const slogan = sloganRef.current;

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

