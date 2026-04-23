/**
 * Round 5 — Self-service profile page (MTN-63 FE).
 *
 * Reads / writes `UserProfile` vertex via /me/profile with configVersion
 * CAS. Avatar upload placeholder only renders a URL field for now —
 * binary upload endpoint lands in a follow-up commit (S3 phase 2).
 */
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { usePageTitle } from '../hooks/usePageTitle';
import { useMe } from '../hooks/useMe';

interface ProfileData extends Record<string, unknown> {
  firstName?: string;
  lastName?:  string;
  title?:     string;
  dept?:      string;
  phone?:     string;
  avatarUrl?: string;
}

export default function ProfilePage() {
  const { t } = useTranslation();
  usePageTitle(t('nav.profile', 'Profile'));

  const { data, loading, error, conflict, save } = useMe<ProfileData>('/me/profile');

  const [draft, setDraft] = useState<ProfileData>({});
  const [saving, setSaving] = useState(false);
  const [savedFlash, setSavedFlash] = useState(false);

  // Rehydrate draft when fresh data arrives
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

  return (
    <div style={{ padding: '24px', maxWidth: 680 }}>
      <h2 style={{ margin: 0, marginBottom: 8 }}>{t('nav.profile', 'Profile')}</h2>
      <p style={{ color: 'var(--t3)', marginTop: 0, marginBottom: 24 }}>
        {t('profile.desc', 'Ваши данные в каталоге платформы. Видны администратору тенанта.')}
      </p>

      {loading && !data && (
        <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>
      )}

      {error && (
        <p style={{ color: 'var(--danger)' }}>
          ⚠ {error}
        </p>
      )}

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
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <Field
            label={t('profile.firstName', 'Имя')}
            value={draft.firstName ?? ''}
            onChange={v => setDraft(d => ({ ...d, firstName: v }))}
          />
          <Field
            label={t('profile.lastName', 'Фамилия')}
            value={draft.lastName ?? ''}
            onChange={v => setDraft(d => ({ ...d, lastName: v }))}
          />
          <Field
            label={t('profile.title', 'Должность')}
            value={draft.title ?? ''}
            onChange={v => setDraft(d => ({ ...d, title: v }))}
          />
          <Field
            label={t('profile.dept', 'Подразделение')}
            value={draft.dept ?? ''}
            onChange={v => setDraft(d => ({ ...d, dept: v }))}
          />
          <Field
            label={t('profile.phone', 'Телефон')}
            value={draft.phone ?? ''}
            onChange={v => setDraft(d => ({ ...d, phone: v }))}
          />
          <Field
            label={t('profile.avatarUrl', 'URL аватара')}
            value={draft.avatarUrl ?? ''}
            onChange={v => setDraft(d => ({ ...d, avatarUrl: v }))}
            placeholder="https://…"
          />

          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginTop: 8 }}>
            <button
              className="btn btn-primary"
              onClick={onSave}
              disabled={saving || loading}
            >
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

function Field(props: {
  label:        string;
  value:        string;
  onChange:     (v: string) => void;
  placeholder?: string;
}) {
  return (
    <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <span className="field-label">{props.label}</span>
      <input
        className="field-input"
        type="text"
        value={props.value}
        placeholder={props.placeholder}
        onChange={e => props.onChange(e.target.value)}
      />
    </label>
  );
}
