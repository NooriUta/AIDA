/**
 * Round 5 — Self-service notifications preferences (MTN-63 FE).
 *
 * Five toggles persist into UserNotifications via /me/notifications.
 */
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { usePageTitle } from '../hooks/usePageTitle';
import { useMe } from '../hooks/useMe';

interface Notif extends Record<string, unknown> {
  email?:   boolean;
  browser?: boolean;
  harvest?: boolean;
  errors?:  boolean;
  digest?:  boolean;
}

export default function NotificationsPage() {
  const { t } = useTranslation();
  usePageTitle(t('nav.notifications', 'Notifications'));

  const { data, loading, error, conflict, save } = useMe<Notif>('/me/notifications');
  const [draft, setDraft] = useState<Notif>({});
  const [saving, setSaving] = useState(false);
  const [savedFlash, setSavedFlash] = useState(false);

  useEffect(() => {
    if (data) setDraft(data);
  }, [data]);

  const onSave = async () => {
    setSaving(true);
    const r = await save(draft);
    setSaving(false);
    if (r.ok) {
      setSavedFlash(true);
      setTimeout(() => setSavedFlash(false), 2000);
    }
  };

  const toggle = (key: keyof Notif) => (checked: boolean) =>
    setDraft(d => ({ ...d, [key]: checked }));

  return (
    <div style={{ padding: '24px', maxWidth: 680 }}>
      <h2 style={{ margin: 0, marginBottom: 8 }}>{t('nav.notifications', 'Notifications')}</h2>
      <p style={{ color: 'var(--t3)', marginTop: 0, marginBottom: 24 }}>
        {t('notifications.desc', 'Каналы и типы уведомлений. Email отправляется по адресу из Keycloak.')}
      </p>

      {loading && !data && <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>}
      {error && <p style={{ color: 'var(--danger)' }}>⚠ {error}</p>}
      {conflict && (
        <div style={{
          background: 'var(--wrn)', color: 'var(--bg0)',
          padding: '8px 12px', borderRadius: 'var(--seer-radius-sm)',
          marginBottom: 12,
        }}>
          {t('me.conflict', 'Данные изменены извне. Загружены свежие. Повторите изменения.')}
        </div>
      )}

      {!loading && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <Row label={t('notifications.email',   'Email')}           checked={!!draft.email}   onChange={toggle('email')} />
          <Row label={t('notifications.browser', 'Браузер (push)')}    checked={!!draft.browser} onChange={toggle('browser')} />
          <Row label={t('notifications.harvest', 'Завершение harvest')}  checked={!!draft.harvest} onChange={toggle('harvest')} />
          <Row label={t('notifications.errors',  'Критические ошибки')} checked={!!draft.errors}  onChange={toggle('errors')} />
          <Row label={t('notifications.digest',  'Ежедневный дайджест')}  checked={!!draft.digest}  onChange={toggle('digest')} />

          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginTop: 12 }}>
            <button className="btn-primary" onClick={onSave} disabled={saving || loading}>
              {saving ? t('status.saving', 'Saving…') : t('action.save', 'Сохранить')}
            </button>
            {savedFlash && (
              <span style={{ color: 'var(--suc)' }}>
                ✓ {t('status.saved', 'Сохранено')}
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function Row(props: { label: string; checked: boolean; onChange: (v: boolean) => void }) {
  return (
    <label style={{
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '8px 12px',
      background: 'var(--bg2)',
      borderRadius: 'var(--seer-radius-sm)',
      cursor: 'pointer',
    }}>
      <span className="field-label" style={{ margin: 0 }}>{props.label}</span>
      <input
        type="checkbox"
        checked={props.checked}
        onChange={e => props.onChange(e.target.checked)}
      />
    </label>
  );
}
