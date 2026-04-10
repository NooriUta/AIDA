import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';

export const ProfileTabSecurity = memo(() => {
  const { t } = useTranslation();
  const [current, setCurrent]   = useState('');
  const [next1,    setNext1]    = useState('');
  const [next2,    setNext2]    = useState('');
  const [msg,      setMsg]      = useState('');

  function handleChangePassword() {
    if (!current || !next1 || !next2) { setMsg(t('profile.security.fillAll')); return; }
    if (next1 !== next2) { setMsg(t('profile.security.mismatch')); return; }
    setMsg(`✓ ${t('profile.security.changed')}`);
    setCurrent(''); setNext1(''); setNext2('');
    setTimeout(() => setMsg(''), 3000);
  }

  return (
    <div>
      <SectionTitle>{t('profile.tabs.security')}</SectionTitle>

      {/* Password */}
      <SecurityBlock>
        <BlockTitle dotOn>{t('profile.security.password')}</BlockTitle>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <PasswordField label={t('profile.security.current')} value={current} onChange={setCurrent} />
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <PasswordField label={t('profile.security.new')}    value={next1}   onChange={setNext1} />
            <PasswordField label={t('profile.security.repeat')} value={next2}   onChange={setNext2} />
          </div>
        </div>
        {msg && <div style={{ marginTop: '10px', fontSize: '12px', color: msg.startsWith('✓') ? 'var(--suc)' : 'var(--danger)' }}>{msg}</div>}
        <div style={{ display: 'flex', gap: '10px', marginTop: '14px' }}>
          <button onClick={handleChangePassword} style={btnPrimary}>{t('profile.security.changeBtn')}</button>
        </div>
      </SecurityBlock>

      {/* 2FA */}
      <SecurityBlock>
        <BlockTitle dotOff>{t('profile.security.twoFa')}</BlockTitle>
        <p style={{ fontSize: '12px', color: 'var(--t2)', marginBottom: '14px', lineHeight: 1.6 }}>
          {t('profile.security.twoFaDesc')}
        </p>
        <button style={btnSecondary}>{t('profile.security.enable2fa')}</button>
      </SecurityBlock>

      {/* Danger zone */}
      <SecurityBlock danger>
        <BlockTitle danger>{t('profile.security.dangerZone')}</BlockTitle>
        <p style={{ fontSize: '12px', color: 'var(--t3)', marginBottom: '12px' }}>
          {t('profile.security.dangerDesc')}
        </p>
        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          <button style={btnDanger}>{t('profile.security.terminateSessions')}</button>
          <button style={btnDanger}>{t('profile.security.deleteAccount')}</button>
        </div>
      </SecurityBlock>
    </div>
  );
});

ProfileTabSecurity.displayName = 'ProfileTabSecurity';

function SecurityBlock({ children, danger }: { children: React.ReactNode; danger?: boolean }) {
  return (
    <div style={{
      background: 'var(--bg2)',
      border: `1px solid ${danger ? 'color-mix(in srgb, var(--danger) 30%, var(--bd))' : 'var(--bd)'}`,
      borderRadius: '10px', padding: '16px 20px', marginBottom: '16px',
    }}>
      {children}
    </div>
  );
}

function BlockTitle({ children, dotOn, dotOff, danger }: { children: React.ReactNode; dotOn?: boolean; dotOff?: boolean; danger?: boolean }) {
  return (
    <div style={{ fontSize: '12px', fontWeight: 600, color: danger ? 'var(--danger)' : 'var(--t1)', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
      {(dotOn || dotOff) && (
        <span style={{ width: '7px', height: '7px', borderRadius: '50%', background: dotOn ? 'var(--suc)' : 'var(--t3)', flexShrink: 0, display: 'inline-block' }} />
      )}
      {danger && <span style={{ width: '7px', height: '7px', borderRadius: '50%', background: 'var(--danger)', flexShrink: 0, display: 'inline-block' }} />}
      {children}
    </div>
  );
}

function PasswordField({ label, value, onChange }: { label: string; value: string; onChange: (v: string) => void }) {
  return (
    <div>
      <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '6px', textTransform: 'uppercase' }}>{label}</label>
      <input type="password" value={value} onChange={(e) => onChange(e.target.value)} placeholder="••••••••"
        style={{ width: '100%', padding: '7px 10px', background: 'var(--bg0)', border: '1px solid var(--bd)', borderRadius: '6px', color: 'var(--t1)', fontSize: '13px', fontFamily: 'inherit', outline: 'none' }} />
    </div>
  );
}

function SectionTitle({ children }: { children: React.ReactNode }) {
  return <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>{children}</div>;
}

const btnPrimary:   React.CSSProperties = { display: 'inline-flex', alignItems: 'center', padding: '7px 14px', borderRadius: '6px', fontSize: '12px', fontWeight: 500, fontFamily: 'inherit', cursor: 'pointer', border: '1px solid var(--acc)', background: 'var(--acc)', color: 'var(--bg0)' };
const btnSecondary: React.CSSProperties = { display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '7px 14px', borderRadius: '6px', fontSize: '12px', fontWeight: 500, fontFamily: 'inherit', cursor: 'pointer', border: '1px solid var(--bd)', background: 'transparent', color: 'var(--t2)' };
const btnDanger:    React.CSSProperties = { display: 'inline-flex', alignItems: 'center', padding: '7px 14px', borderRadius: '6px', fontSize: '12px', fontWeight: 500, fontFamily: 'inherit', cursor: 'pointer', border: '1px solid color-mix(in srgb, var(--danger) 40%, transparent)', background: 'transparent', color: 'var(--danger)' };
