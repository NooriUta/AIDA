import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { usePageTitle } from '../hooks/usePageTitle';
import { useTenantDetails } from '../hooks/useTenantDetails';
import { TenantConfigEditor } from '../components/tenants/TenantConfigEditor';
import { updateTenantConfig, type TenantConfigPatch } from '../api/admin';

export default function TenantConfigPage() {
  const { alias } = useParams<{ alias: string }>();
  const { t }     = useTranslation();
  const navigate  = useNavigate();
  usePageTitle(
    alias
      ? `${t('config.pageTitle', 'Tenant config')}: ${alias}`
      : t('config.pageTitle', 'Tenant config')
  );

  const { tenant, loading, error, refresh } = useTenantDetails(alias);
  const [saving, setSaving] = useState(false);

  async function handleSave(patch: TenantConfigPatch) {
    if (!alias) return;
    setSaving(true);
    try {
      await updateTenantConfig(alias, patch);
      refresh();
    } finally {
      setSaving(false);
    }
  }

  return (
    <div style={{ padding: '24px', maxWidth: 720 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
        <button className="btn btn-secondary" onClick={() => navigate(`/admin/tenants/${alias}`)}>
          ← {t('members.backToTenant', 'Tenant')}
        </button>
        <h2 style={{ margin: 0, fontFamily: 'monospace' }}>
          {alias} · {t('config.heading', 'Config')}
        </h2>
      </div>

      {error && <p style={{ color: 'var(--danger)' }}>{error}</p>}

      {loading && !tenant && (
        <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>
      )}

      {tenant && <TenantConfigEditor tenant={tenant} onSave={handleSave} saving={saving} />}
    </div>
  );
}
