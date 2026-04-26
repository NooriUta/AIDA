import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { usePageTitle } from '../hooks/usePageTitle';
import { useTenantDetails } from '../hooks/useTenantDetails';
import { useTenantContext } from '../hooks/useTenantContext';
import { TenantStatusBadge } from '../components/tenants/TenantStatusBadge';
import { TenantLifecycleActions } from '../components/tenants/TenantLifecycleActions';

function Row({ label, value, hint }: {
  label: string;
  value?: string | number | null;
  hint?:  string;
}) {
  if (value == null || value === '') return null;
  return (
    <tr style={{ borderBottom: '1px solid var(--bd)' }}>
      <td
        title={hint}
        style={{
          padding:        '6px 12px',
          color:          'var(--t3)',
          whiteSpace:     'nowrap',
          cursor:         hint ? 'help' : 'default',
          textDecoration: hint ? 'underline dotted var(--bd)' : 'none',
          width:          180,
        }}
      >
        {label}
      </td>
      <td style={{ padding: '6px 12px', fontFamily: 'monospace', wordBreak: 'break-all', width: 260 }}>
        {String(value)}
      </td>
      <td style={{ padding: '6px 12px', color: 'var(--t3)', fontSize: '0.78rem', fontStyle: 'italic' }}>
        {hint ?? ''}
      </td>
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
    <div style={{ padding: '24px', maxWidth: 960 }}>
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
            {t('tenants.editConfig', 'Edit Config')}
          </button>
        )}
        <button className="btn btn-secondary" onClick={refresh} disabled={loading}>
          {t('tenants.refresh', 'Refresh')}
        </button>
      </div>

      {error && <p style={{ color: 'var(--danger)' }}>{error}</p>}

      {loading && !tenant && (
        <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>
      )}

      {tenant && (
        <TenantLifecycleActions tenant={tenant} onRefresh={refresh} />
      )}

      {tenant && (
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.88rem' }}>
          <tbody>
            <Row
              label={t('tenants.alias', 'Alias')}
              value={tenant.tenantAlias}
              hint={t('tenants.hints.alias', 'Уникальный псевдоним тенанта; используется в именах БД и scope claim')}
            />
            <Row
              label={t('tenants.status', 'Status')}
              value={t(`tenants.statusValues.${tenant.status}`, tenant.status)}
              hint={t('tenants.hints.status', 'Lifecycle: ACTIVE → SUSPENDED → ARCHIVED → PURGED')}
            />
            <Row
              label={t('tenants.configVersion', 'Config v')}
              value={tenant.configVersion}
              hint={t('tenants.hints.configVersion', 'Инкрементится при каждом обновлении конфига (optimistic lock)')}
            />
            <Row
              label="keycloakOrgId"
              value={tenant.keycloakOrgId}
              hint={t('tenants.hints.keycloakOrgId', 'ID организации в Keycloak 26.2 — привязка к источнику истины пользователей')}
            />
            <Row
              label="yggLineageDbName"
              value={tenant.yggLineageDbName}
              hint={t('tenants.hints.yggLineageDbName', 'ArcadeDB база для lineage-графа (hound_{alias})')}
            />
            <Row
              label="yggSourceArchiveDbName"
              value={tenant.yggSourceArchiveDbName}
              hint={t('tenants.hints.yggSourceArchiveDbName', 'БД архива исходников (hound_src_{alias})')}
            />
            <Row
              label="friggDaliDbName"
              value={tenant.friggDaliDbName}
              hint={t('tenants.hints.friggDaliDbName', 'БД Dali сессий и JobRunr задач (dali_{alias})')}
            />
            <Row
              label="yggInstanceUrl"
              value={tenant.yggInstanceUrl}
              hint={t('tenants.hints.yggInstanceUrl', 'URL YGG ArcadeDB instance (shard) — для будущего горизонтального шардинга')}
            />
            <Row
              label="harvestCron"
              value={tenant.harvestCron}
              hint={t('tenants.hints.harvestCron', '6-полевой cron (sec min hour dom mon dow) расписания автоматического харвестинга')}
            />
            <Row
              label="llmMode"
              value={tenant.llmMode}
              hint={t('tenants.hints.llmMode', 'Режим MIMIR: off | local (Ollama) | openai | azure')}
            />
            <Row
              label="dataRetentionDays"
              value={tenant.dataRetentionDays}
              hint={t('tenants.hints.dataRetentionDays', 'Сколько дней хранятся события и сессии до auto-purge')}
            />
            <Row
              label="maxParseSessions"
              value={tenant.maxParseSessions}
              hint={t('tenants.hints.maxParseSessions', 'Квота на одновременно активные parse-сессии')}
            />
            <Row
              label="maxAtoms"
              value={tenant.maxAtoms}
              hint={t('tenants.hints.maxAtoms', 'Общий лимит извлечённых атомов (stmt/column/table)')}
            />
            <Row
              label="maxSources"
              value={tenant.maxSources}
              hint={t('tenants.hints.maxSources', 'Максимум подключённых источников (FS/S3/GIT)')}
            />
            <Row
              label="maxConcurrentJobs"
              value={tenant.maxConcurrentJobs}
              hint={t('tenants.hints.maxConcurrentJobs', 'JobRunr concurrency cap — во сколько потоков идут задачи тенанта')}
            />
            <Row
              label="archiveS3Key"
              value={tenant.archiveS3Key}
              hint={t('tenants.hints.archiveS3Key', 'Ключ S3-объекта с экспортом БД (заполняется при archive)')}
            />
            {tenant.archiveRetentionUntil && (
              <Row
                label="archiveRetentionUntil"
                value={new Date(tenant.archiveRetentionUntil).toISOString()}
                hint={t('tenants.hints.archiveRetentionUntil', 'Дата после которой запустится purge-job и архив будет удалён')}
              />
            )}
            {tenant.createdAt && (
              <Row
                label="createdAt"
                value={new Date(tenant.createdAt).toISOString()}
                hint={t('tenants.hints.createdAt', 'Момент успешного завершения провижена (7 шагов)')}
              />
            )}
            {tenant.updatedAt && (
              <Row
                label="updatedAt"
                value={new Date(tenant.updatedAt).toISOString()}
                hint={t('tenants.hints.updatedAt', 'Время последнего изменения конфига или lifecycle-статуса')}
              />
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}
