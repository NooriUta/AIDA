/**
 * Round 5 — Self-service preferences page (MTN-63 FE).
 *
 * Reads/writes UserPreferences via /me/preferences with CAS.
 * On save, the hook emits `window.aida:prefs` event so the existing
 * `sharedPrefsStore` applies theme/lang across all MFs instantly.
 */
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { usePageTitle } from '../hooks/usePageTitle';
import { useMe } from '../hooks/useMe';

interface Prefs extends Record<string, unknown> {
  theme?:     'light' | 'dark' | 'auto';
  lang?:      'ru' | 'en';
  tz?:        string;
  dateFmt?:   string;
  startPage?: string;
  density?:   'comfortable' | 'compact';
  fontSize?:  'sm' | 'md' | 'lg';
}

const THEMES: Array<Prefs['theme']>   = ['light', 'dark', 'auto'];
const LANGS:  Array<Prefs['lang']>    = ['ru', 'en'];
const DENSITIES: Array<Prefs['density']> = ['comfortable', 'compact'];
const FONT_SIZES: Array<Prefs['fontSize']> = ['sm', 'md', 'lg'];

export default function PreferencesPage() {
  const { t, i18n } = useTranslation();
  usePageTitle(t('nav.preferences', 'Preferences'));

  const { data, loading, error, conflict, save } = useMe<Prefs>('/me/preferences');
  const [draft, setDraft] = useState<Prefs>({});
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
      // Apply locally immediately (the event handler will also fire)
      if (draft.lang && draft.lang !== i18n.language) {
        void i18n.changeLanguage(draft.lang);
      }
      setSavedFlash(true);
      setTimeout(() => setSavedFlash(false), 2000);
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: 680 }}>
      <h2 style={{ margin: 0, marginBottom: 8 }}>{t('nav.preferences', 'Preferences')}</h2>
      <p style={{ color: 'var(--t3)', marginTop: 0, marginBottom: 24 }}>
        {t('preferences.desc', 'Персональные настройки UI. Применяются во всех модулях платформы.')}
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
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <Select
            label={t('preferences.theme', 'Тема')}
            value={draft.theme ?? 'auto'}
            options={THEMES.map(v => ({
              value: v!, label: t(`preferences.themeOpts.${v}`, v!),
            }))}
            onChange={v => setDraft(d => ({ ...d, theme: v as Prefs['theme'] }))}
          />
          <Select
            label={t('preferences.lang', 'Язык')}
            value={draft.lang ?? 'ru'}
            options={LANGS.map(v => ({ value: v!, label: t(`preferences.langOpts.${v}`, v!) }))}
            onChange={v => setDraft(d => ({ ...d, lang: v as Prefs['lang'] }))}
          />
          <Field
            label={t('preferences.tz', 'Timezone')}
            value={draft.tz ?? 'Europe/Moscow'}
            onChange={v => setDraft(d => ({ ...d, tz: v }))}
            placeholder="Europe/Moscow"
          />
          <Field
            label={t('preferences.dateFmt', 'Формат даты')}
            value={draft.dateFmt ?? 'DD.MM.YYYY'}
            onChange={v => setDraft(d => ({ ...d, dateFmt: v }))}
          />
          <Field
            label={t('preferences.startPage', 'Страница при входе')}
            value={draft.startPage ?? 'dashboard'}
            onChange={v => setDraft(d => ({ ...d, startPage: v }))}
          />
          <Select
            label={t('preferences.density', 'Плотность')}
            value={draft.density ?? 'comfortable'}
            options={DENSITIES.map(v => ({
              value: v!, label: t(`preferences.densityOpts.${v}`, v!),
            }))}
            onChange={v => setDraft(d => ({ ...d, density: v as Prefs['density'] }))}
          />
          <Select
            label={t('preferences.fontSize', 'Размер шрифта')}
            value={draft.fontSize ?? 'md'}
            options={FONT_SIZES.map(v => ({ value: v!, label: t(`preferences.fontSizeOpts.${v}`, v!) }))}
            onChange={v => setDraft(d => ({ ...d, fontSize: v as Prefs['fontSize'] }))}
          />

          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginTop: 8 }}>
            <button className="btn btn-primary" onClick={onSave} disabled={saving || loading}>
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
  label: string; value: string; onChange: (v: string) => void; placeholder?: string;
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

function Select(props: {
  label: string; value: string;
  options: Array<{ value: string; label: string }>;
  onChange: (v: string) => void;
}) {
  return (
    <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <span className="field-label">{props.label}</span>
      <select
        className="field-input"
        value={props.value}
        onChange={e => props.onChange(e.target.value)}
      >
        {props.options.map(opt => (
          <option key={opt.value} value={opt.value}>{opt.label}</option>
        ))}
      </select>
    </label>
  );
}
