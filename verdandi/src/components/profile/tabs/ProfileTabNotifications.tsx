import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';

type NotifSettings = {
  inApp:        boolean;
  emailDigest:  boolean;
  lineageChange: boolean;
  brokenLineage: boolean;
  myTablesImpact: boolean;
  shuttleSync:   boolean;
  anvilComments: boolean;
};

const DEFAULTS: NotifSettings = {
  inApp: true, emailDigest: false,
  lineageChange: true, brokenLineage: true, myTablesImpact: true,
  shuttleSync: false, anvilComments: false,
};

export const ProfileTabNotifications = memo(() => {
  const { t } = useTranslation();
  const [s, setS] = useState<NotifSettings>(DEFAULTS);
  const [saved, setSaved] = useState(false);

  const toggle = (key: keyof NotifSettings) => setS((p) => ({ ...p, [key]: !p[key] }));

  function handleSave() {
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  const CHANNELS = [
    { key: 'inApp',       label: t('profile.notif.inApp'),       desc: t('profile.notif.inAppDesc') },
    { key: 'emailDigest', label: t('profile.notif.emailDigest'), desc: t('profile.notif.emailDigestDesc') },
  ] as const;

  const EVENTS = [
    { key: 'lineageChange',   label: t('profile.notif.lineageChange'),   desc: t('profile.notif.lineageChangeDesc') },
    { key: 'brokenLineage',   label: t('profile.notif.brokenLineage'),   desc: t('profile.notif.brokenLineageDesc') },
    { key: 'myTablesImpact',  label: t('profile.notif.myTablesImpact'),  desc: t('profile.notif.myTablesImpactDesc') },
    { key: 'shuttleSync',     label: t('profile.notif.shuttleSync'),     desc: t('profile.notif.shuttleSyncDesc') },
    { key: 'anvilComments',   label: t('profile.notif.anvilComments'),   desc: t('profile.notif.anvilCommentsDesc') },
  ] as const;

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.notifications')}
      </div>

      <SectionLabel>{t('profile.notif.channels')}</SectionLabel>
      {CHANNELS.map(({ key, label, desc }) => (
        <SettingRow key={key} label={label} desc={desc} on={s[key]} onToggle={() => toggle(key)} />
      ))}

      <SectionLabel style={{ marginTop: '20px' }}>{t('profile.notif.loomEvents')}</SectionLabel>
      {EVENTS.map(({ key, label, desc }) => (
        <SettingRow key={key} label={label} desc={desc} on={s[key]} onToggle={() => toggle(key)} />
      ))}

      <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
        <button onClick={handleSave} style={btnPrimary}>{saved ? `✓ ${t('profile.saved')}` : t('profile.save')}</button>
        <button onClick={() => setS(DEFAULTS)} style={btnSecondary}>{t('profile.graph.reset')}</button>
      </div>
    </div>
  );
});

ProfileTabNotifications.displayName = 'ProfileTabNotifications';

function SectionLabel({ children, style }: { children: React.ReactNode; style?: React.CSSProperties }) {
  return <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '14px', textTransform: 'uppercase', ...style }}>{children}</div>;
}

function SettingRow({ label, desc, on, onToggle }: { label: string; desc: string; on: boolean; onToggle: () => void }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--bd)' }}>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: '12px', fontWeight: 500, color: 'var(--t1)', marginBottom: '2px' }}>{label}</div>
        <div style={{ fontSize: '11px', color: 'var(--t3)' }}>{desc}</div>
      </div>
      <div onClick={onToggle} style={{ width: '36px', height: '20px', borderRadius: '10px', flexShrink: 0, background: on ? 'var(--acc)' : 'var(--bg3)', border: `1px solid ${on ? 'var(--acc)' : 'var(--bd)'}`, position: 'relative', cursor: 'pointer', transition: 'background 0.15s' }}>
        <div style={{ width: '14px', height: '14px', borderRadius: '50%', background: on ? 'var(--bg0)' : 'var(--t3)', position: 'absolute', top: '2px', left: '2px', transform: on ? 'translateX(16px)' : 'none', transition: 'transform 0.15s, background 0.15s' }} />
      </div>
    </div>
  );
}

const btnPrimary:   React.CSSProperties = { display: 'inline-flex', alignItems: 'center', padding: '7px 14px', borderRadius: '6px', fontSize: '12px', fontWeight: 500, fontFamily: 'inherit', cursor: 'pointer', border: '1px solid var(--acc)', background: 'var(--acc)', color: 'var(--bg0)' };
const btnSecondary: React.CSSProperties = { display: 'inline-flex', alignItems: 'center', padding: '7px 14px', borderRadius: '6px', fontSize: '12px', fontWeight: 500, fontFamily: 'inherit', cursor: 'pointer', border: '1px solid var(--bd)', background: 'transparent', color: 'var(--t2)' };
