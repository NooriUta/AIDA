import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { provisionTenant } from '../../api/admin';

interface Props {
  onClose:   () => void;
  onCreated: (alias: string) => void;
}

const ALIAS_REGEX = /^[a-z][a-z0-9-]{2,30}[a-z0-9]$/;

export function TenantCreateModal({ onClose, onCreated }: Props) {
  const { t } = useTranslation();
  const [alias, setAlias] = useState('');
  const [busy,  setBusy]  = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isValid  = ALIAS_REGEX.test(alias);
  const canSubmit = isValid && !busy;

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!canSubmit) return;
    setBusy(true);
    setError(null);
    try {
      await provisionTenant(alias);
      onCreated(alias);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div
      style={{
        position:       'fixed', inset: 0,
        background:     'rgba(0,0,0,.65)',
        display:        'flex', alignItems: 'center', justifyContent: 'center',
        zIndex:         200,
      }}
      onClick={e => { if (e.target === e.currentTarget) onClose(); }}
    >
      <form
        onSubmit={submit}
        className="confirm-box"
        style={{ minWidth: 360, display: 'flex', flexDirection: 'column', gap: 12 }}
      >
        <div style={{ fontSize: 14, fontWeight: 600 }}>
          {t('tenants.createTitle', 'Create tenant')}
        </div>

        <div style={{ fontSize: 11, color: 'var(--t3)' }}>
          {t('tenants.createHint', 'Alias: 4–32 lowercase letters/digits/hyphens, starting with a letter.')}
        </div>

        <label style={{ fontSize: 12, display: 'flex', flexDirection: 'column', gap: 4 }}>
          <span style={{ color: 'var(--t3)' }}>
            {t('tenants.aliasField', 'Alias')}
          </span>
          <input
            type="text"
            value={alias}
            onChange={e => setAlias(e.target.value.toLowerCase())}
            placeholder="acme"
            required
            autoFocus
            style={{ fontFamily: 'var(--mono, monospace)' }}
          />
        </label>

        {error && <div style={{ color: 'var(--danger)', fontSize: 12 }}>{error}</div>}

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 8 }}>
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={busy}>
            {t('users.cancel', 'Cancel')}
          </button>
          <button type="submit" className="btn btn-primary" disabled={!canSubmit}>
            {busy
              ? t('config.saving', 'Saving…')
              : t('tenants.createSubmit', 'Create')}
          </button>
        </div>
      </form>
    </div>
  );
}
