import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '../../../stores/authStore';

export const ProfileTabProfile = memo(() => {
  const { t } = useTranslation();
  const { user } = useAuthStore();

  const [firstName, setFirstName] = useState('');
  const [lastName,  setLastName]  = useState('');
  const [email,     setEmail]     = useState(user ? `${user.username}@seer.internal` : '');
  const [display,   setDisplay]   = useState(user?.username ?? '');
  const [saved,     setSaved]     = useState(false);

  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';

  function handleSave() {
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.profile')}
      </div>

      {/* Hero */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '28px',
        padding: '16px 20px', background: 'var(--bg2)', border: '1px solid var(--bd)',
        borderRadius: '10px',
      }}>
        <div style={{
          width: '52px', height: '52px', borderRadius: '50%', flexShrink: 0,
          background: 'color-mix(in srgb, var(--acc) 18%, var(--bg3))',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '18px', fontWeight: 600, color: 'var(--acc)',
          border: '2px solid color-mix(in srgb, var(--acc) 30%, transparent)',
        }}>
          {initials}
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: '15px', fontWeight: 600, color: 'var(--t1)', marginBottom: '4px' }}>
            {display || user?.username || '—'}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{
              display: 'inline-flex', alignItems: 'center', gap: '4px',
              padding: '2px 8px', borderRadius: '3px', fontSize: '10px', fontWeight: 600,
              letterSpacing: '0.07em', textTransform: 'uppercase',
              background: 'color-mix(in srgb, var(--acc) 14%, transparent)',
              color: 'var(--acc)', border: '1px solid color-mix(in srgb, var(--acc) 30%, transparent)',
            }}>
              {user?.role ?? 'viewer'}
            </span>
            <span style={{ fontSize: '11px', color: 'var(--t3)' }}>SEER Studio · Production</span>
          </div>
        </div>
      </div>

      {/* Fields */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '6px', textTransform: 'uppercase' }}>
            {t('profile.firstName')}
          </label>
          <input
            className="seer-input"
            type="text"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            placeholder={t('profile.firstNamePlaceholder')}
            style={inputStyle}
          />
        </div>
        <div>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '6px', textTransform: 'uppercase' }}>
            {t('profile.lastName')}
          </label>
          <input
            type="text"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            placeholder={t('profile.lastNamePlaceholder')}
            style={inputStyle}
          />
        </div>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label style={labelStyle}>{t('profile.email')}</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} style={inputStyle} />
        <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '5px' }}>{t('profile.emailHint')}</div>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label style={labelStyle}>{t('profile.displayName')}</label>
        <input type="text" value={display} onChange={(e) => setDisplay(e.target.value)} style={inputStyle} />
        <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '5px' }}>{t('profile.displayNameHint')}</div>
      </div>

      <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
        <button onClick={handleSave} style={btnPrimary}>
          {saved ? `✓ ${t('profile.saved')}` : t('profile.save')}
        </button>
        <button style={btnSecondary}>{t('profile.cancel')}</button>
      </div>
    </div>
  );
});

ProfileTabProfile.displayName = 'ProfileTabProfile';

const inputStyle: React.CSSProperties = {
  width: '100%', padding: '7px 10px',
  background: 'var(--bg2)', border: '1px solid var(--bd)',
  borderRadius: '6px', color: 'var(--t1)', fontSize: '13px',
  fontFamily: 'inherit', outline: 'none',
};
const labelStyle: React.CSSProperties = {
  display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--t2)',
  letterSpacing: '0.04em', marginBottom: '6px', textTransform: 'uppercase',
};
const btnPrimary: React.CSSProperties = {
  display: 'inline-flex', alignItems: 'center', gap: '6px',
  padding: '7px 14px', borderRadius: '6px',
  fontSize: '12px', fontWeight: 500, fontFamily: 'inherit',
  cursor: 'pointer', border: '1px solid var(--acc)',
  background: 'var(--acc)', color: 'var(--bg0)',
};
const btnSecondary: React.CSSProperties = {
  display: 'inline-flex', alignItems: 'center', gap: '6px',
  padding: '7px 14px', borderRadius: '6px',
  fontSize: '12px', fontWeight: 500, fontFamily: 'inherit',
  cursor: 'pointer', border: '1px solid var(--bd)',
  background: 'transparent', color: 'var(--t2)',
};
