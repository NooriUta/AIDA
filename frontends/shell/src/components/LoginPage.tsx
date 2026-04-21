import { useState }          from 'react';
import { useTranslation }    from 'react-i18next';
import { LogIn }             from 'lucide-react';
import { useShellAuthStore } from '../stores/authStore';
import type { ReactNode }    from 'react';

export function LoginPage() {
  const { t } = useTranslation();
  const { login, isLoading, error, clearError } = useShellAuthStore();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState({ username: '', password: '' });

  const validate = (): boolean => {
    const errs = { username: '', password: '' };
    if (!username.trim()) errs.username = t('auth.error.required');
    if (!password)        errs.password = t('auth.error.required');
    setFieldErrors(errs);
    return !errs.username && !errs.password;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
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
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>

            <Field label={t('auth.username')} error={fieldErrors.username}>
              <input
                value={username}
                onChange={e => { setUsername(e.target.value); clearError(); setFieldErrors(p => ({ ...p, username: '' })); }}
                autoComplete="username"
                autoFocus
                style={inputStyle(!!fieldErrors.username)}
              />
            </Field>

            <Field label={t('auth.password')} error={fieldErrors.password}>
              <input
                type="password"
                value={password}
                onChange={e => { setPassword(e.target.value); clearError(); setFieldErrors(p => ({ ...p, password: '' })); }}
                autoComplete="current-password"
                style={inputStyle(!!fieldErrors.password)}
              />
            </Field>

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
              type="submit"
              disabled={isLoading}
              style={{
                display:        'flex',
                alignItems:     'center',
                justifyContent: 'center',
                gap:            '8px',
                padding:        '9px 16px',
                background:     isLoading ? 'var(--bg3)' : 'var(--acc)',
                color:          isLoading ? 'var(--t3)'  : 'var(--bg0)',
                border:         'none',
                borderRadius:   'var(--seer-radius-md)',
                fontSize:       '13px',
                fontWeight:     500,
                cursor:         isLoading ? 'not-allowed' : 'pointer',
                transition:     'background 0.12s, color 0.12s',
                letterSpacing:  '0.04em',
              }}
            >
              <LogIn size={14} />
              {isLoading ? t('auth.signingIn') : t('auth.login')}
            </button>
          </form>
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
