import { useTranslation }    from 'react-i18next';
import { LogIn }             from 'lucide-react';
import { useShellAuthStore } from '../stores/authStore';

export function LoginPage() {
  const { t } = useTranslation();
  const { error } = useShellAuthStore();

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

        {/* ── Logo ─────────────────────────────────────────────────────────── */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '44px', height: '44px',
            borderRadius: 'var(--seer-radius-sm)',
            background:   'var(--acc)',
            display:      'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <span style={{
              fontFamily: 'var(--font-display)',
              fontSize:   '16px',
              fontWeight: 800,
              color:      'var(--bg0)',
              lineHeight: 1,
              letterSpacing: '0.02em',
            }}>AI</span>
          </div>

          <span style={{
            fontFamily:    'var(--font-display)',
            fontSize:      '22px',
            fontWeight:    800,
            color:         'var(--t1)',
            letterSpacing: '0.06em',
          }}>AIÐA</span>

          <span style={{
            fontSize:      '11px',
            color:         'var(--t3)',
            letterSpacing: '0.05em',
            textAlign:     'center',
          }}>
            {t('auth.tagline')}
          </span>
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

