import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { usePageTitle } from '../hooks/usePageTitle';
import { useTenantDetails } from '../hooks/useTenantDetails';
import { useTenantContext } from '../hooks/useTenantContext';
import { TenantStatusBadge } from '../components/tenants/TenantStatusBadge';
import { TenantLifecycleActions } from '../components/tenants/TenantLifecycleActions';

function Row({ label, value }: { label: string; value?: string | number | null }) {
  if (value == null || value === '') return null;
  return (
    <tr style={{ borderBottom: '1px solid var(--border)' }}>
      <td style={{ padding: '6px 12px', color: 'var(--color-muted)', whiteSpace: 'nowrap' }}>{label}</td>
      <td style={{ padding: '6px 12px', fontFamily: 'monospace', wordBreak: 'break-all' }}>{String(value)}</td>
    </tr>
  );
}

export default function TenantDetailsPage() {
  const { alias } = useParams<{ alias: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  usePageTitle(alias ? `${t('tenants.tenant', 'Tenant')}: ${alias}` : t('tenants.tenant', 'Tenant'));

  const { tenant, loading, error, refresh } = useTenantDetails(alias);
  const { canManageUsers, isSuperAdmin }     = useTenantContext();

  return (
    <div style={{ padding: '24px', maxWidth: 720 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
        <button className="btn btn-secondary" onClick={() => navigate('/admin/tenants')}>← {t('tenants.back', 'Tenants')}</button>
        <h2 style={{ margin: 0, fontFamily: 'monospace' }}>{alias}</h2>
        {tenant && <TenantStatusBadge status={tenant.status} />}
        <div style={{ flex: 1 }} />
        {canManageUsers && (
          <button
            className="btn btn-secondary"
            onClick={() => navigate(`/admin/tenants/${alias}/members`)}
          >
            {t('members.heading', 'Members')}
          </button>
        )}
        {isSuperAdmin && (
          <button
            className="btn btn-secondary"
            onClick={() => navigate(`/admin/tenants/${alias}/config`)}
          >
            {t('tenants.config', 'Config')}
          </button>
        )}
        <button className="btn btn-secondary" onClick={refresh} disabled={loading}>
          {t('tenants.refresh', 'Refresh')}
        </button>
      </div>

      {error && <p style={{ color: 'var(--color-danger)' }}>{error}</p>}

      {loading && !tenant && (
        <p style={{ color: 'var(--color-muted)' }}>{t('status.loading', 'Loading…')}</p>
      )}

      {tenant && (
        <TenantLifecycleActions tenant={tenant} onRefresh={refresh} />
      )}

      {tenant && (
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.88rem' }}>
          <tbody>
            <Row label={t('tenants.alias', 'Alias')}            value={tenant.tenantAlias} />
            <Row label={t('tenants.status', 'Status')}          value={tenant.status} />
            <Row label={t('tenants.configVersion', 'Config v')} value={tenant.configVersion} />
            <Row label="keycloakOrgId"     value={tenant.keycloakOrgId} />
            <Row label="yggLineageDbName"  value={tenant.yggLineageDbName} />
            <Row label="yggSourceArchiveDbName" value={tenant.yggSourceArchiveDbName} />
            <Row label="friggDaliDbName"   value={tenant.friggDaliDbName} />
            <Row label="yggInstanceUrl"    value={tenant.yggInstanceUrl} />
            <Row label="harvestCron"       value={tenant.harvestCron} />
            <Row label="llmMode"           value={tenant.llmMode} />
            <Row label="dataRetentionDays" value={tenant.dataRetentionDays} />
            <Row label="maxParseSessions"  value={tenant.maxParseSessions} />
            <Row label="maxAtoms"          value={tenant.maxAtoms} />
            <Row label="maxSources"        value={tenant.maxSources} />
            <Row label="maxConcurrentJobs" value={tenant.maxConcurrentJobs} />
            <Row label="archiveS3Key"      value={tenant.archiveS3Key} />
            {tenant.archiveRetentionUntil && (
              <Row label="archiveRetentionUntil" value={new Date(tenant.archiveRetentionUntil).toISOString()} />
            )}
            {tenant.createdAt && (
              <Row label="createdAt" value={new Date(tenant.createdAt).toISOString()} />
            )}
            {tenant.updatedAt && (
              <Row label="updatedAt" value={new Date(tenant.updatedAt).toISOString()} />
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}
