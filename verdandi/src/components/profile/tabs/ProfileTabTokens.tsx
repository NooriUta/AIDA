import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';

const MOCK_TOKEN = 'seer_sk_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0';
const MASKED     = 'seer_sk_•••••••••••••••••••••••••••••••••';

export const ProfileTabTokens = memo(() => {
  const { t } = useTranslation();
  const [visible,   setVisible]   = useState(false);
  const [copied,    setCopied]    = useState(false);
  const [exportMeta, setExportMeta] = useState(true);

  function copyToken() {
    navigator.clipboard.writeText(MOCK_TOKEN).catch(() => {});
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.tokens')}
      </div>

      {/* API Token */}
      <SecurityBlock>
        <BlockTitle dotOn>{t('profile.tokens.apiToken')}</BlockTitle>
        <p style={{ fontSize: '12px', color: 'var(--t2)', marginBottom: '14px', lineHeight: 1.6 }}>
          {t('profile.tokens.apiTokenDesc')}
        </p>
        <div style={{ display: 'flex', gap: '8px', marginBottom: '12px', alignItems: 'center' }}>
          <div style={{ flex: 1, padding: '7px 10px', background: 'var(--bg0)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', fontFamily: 'var(--mono)', fontSize: '12px', color: 'var(--t2)', letterSpacing: '0.04em', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {visible ? MOCK_TOKEN : MASKED}
          </div>
          <button onClick={() => setVisible((v) => !v)} style={btnSm}>{visible ? t('profile.tokens.hide') : t('profile.tokens.show')}</button>
          <button onClick={copyToken} style={btnSm}>{copied ? `✓ ${t('profile.tokens.copied')}` : t('profile.tokens.copy')}</button>
        </div>
        <div style={{ display: 'flex', gap: '8px', marginBottom: '10px' }}>
          <button style={btnSecondary}>{t('profile.tokens.reissue')}</button>
          <button style={btnDanger}>{t('profile.tokens.revoke')}</button>
        </div>
        <div style={{ fontSize: '11px', color: 'var(--t3)' }}>
          {t('profile.tokens.issued')}: 2025-12-01 · {t('profile.tokens.lastUsed')}: today, 09:41
        </div>
      </SecurityBlock>

      {/* Export */}
      <SecurityBlock>
        <BlockTitle>{t('profile.tokens.exportGraph')}</BlockTitle>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', marginBottom: '12px' }}>
          {(['PNG (current view)', 'SVG (full graph)', 'JSON (lineage data)', 'CSV (nodes + edges)'] as const).map((label) => (
            <button key={label} style={{ ...btnSecondary, justifyContent: 'center' }}>{label}</button>
          ))}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--t2)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>{t('profile.tokens.includeMeta')}</span>
          <div
            onClick={() => setExportMeta((v) => !v)}
            style={{ width: '36px', height: '20px', borderRadius: '10px', background: exportMeta ? 'var(--acc)' : 'var(--bg3)', border: `1px solid ${exportMeta ? 'var(--acc)' : 'var(--bd)'}`, position: 'relative', cursor: 'pointer', transition: 'background 0.15s' }}
          >
            <div style={{ width: '14px', height: '14px', borderRadius: '50%', background: exportMeta ? 'var(--bg0)' : 'var(--t3)', position: 'absolute', top: '2px', left: '2px', transform: exportMeta ? 'translateX(16px)' : 'none', transition: 'transform 0.15s' }} />
          </div>
        </div>
      </SecurityBlock>

      {/* Webhook */}
      <SecurityBlock>
        <BlockTitle>{t('profile.tokens.webhook')}</BlockTitle>
        <p style={{ fontSize: '12px', color: 'var(--t2)', marginBottom: '12px', lineHeight: 1.6 }}>
          {t('profile.tokens.webhookDesc')}
        </p>
        <div style={{ marginBottom: '10px' }}>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '6px', textTransform: 'uppercase' }}>
            URL endpoint
          </label>
          <input type="url" placeholder="https://your-service.com/webhook"
            style={{ width: '100%', padding: '7px 10px', background: 'var(--bg0)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', color: 'var(--t1)', fontSize: '13px', fontFamily: 'inherit', outline: 'none' }} />
        </div>
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '3px 8px', borderRadius: '3px', background: 'color-mix(in srgb, var(--t3) 12%, transparent)', border: '1px solid var(--bd)', fontSize: '10px', color: 'var(--t3)', letterSpacing: '0.05em' }}>
          {t('profile.tokens.comingSoon')}
        </span>
      </SecurityBlock>
    </div>
  );
});

ProfileTabTokens.displayName = 'ProfileTabTokens';

function SecurityBlock({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: '10px', padding: '16px 20px', marginBottom: '16px' }}>
      {children}
    </div>
  );
}

function BlockTitle({ children, dotOn }: { children: React.ReactNode; dotOn?: boolean }) {
  return (
    <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--t1)', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
      {dotOn !== undefined && <span style={{ width: '7px', height: '7px', borderRadius: '50%', background: dotOn ? 'var(--suc)' : 'var(--t3)', flexShrink: 0, display: 'inline-block' }} />}
      {children}
    </div>
  );
}

const btnSm:        React.CSSProperties = { fontSize: '11px', padding: '5px 10px', borderRadius: 'var(--seer-radius-md)', border: '1px solid var(--bd)', background: 'transparent', color: 'var(--t2)', cursor: 'pointer', fontFamily: 'inherit', flexShrink: 0, whiteSpace: 'nowrap' };
const btnSecondary: React.CSSProperties = { display: 'inline-flex', alignItems: 'center', padding: '7px 14px', borderRadius: 'var(--seer-radius-md)', fontSize: '12px', fontWeight: 500, fontFamily: 'inherit', cursor: 'pointer', border: '1px solid var(--bd)', background: 'transparent', color: 'var(--t2)' };
const btnDanger:    React.CSSProperties = { display: 'inline-flex', alignItems: 'center', padding: '7px 14px', borderRadius: 'var(--seer-radius-md)', fontSize: '12px', fontWeight: 500, fontFamily: 'inherit', cursor: 'pointer', border: '1px solid color-mix(in srgb, var(--danger) 40%, transparent)', background: 'transparent', color: 'var(--danger)' };
