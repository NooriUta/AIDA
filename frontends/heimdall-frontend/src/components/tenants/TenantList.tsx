import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { TenantSummary } from '../../api/admin';
import { TenantStatusBadge } from './TenantStatusBadge';

interface Props {
  tenants: TenantSummary[];
}

export function TenantList({ tenants }: Props) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  if (tenants.length === 0) {
    return <p style={{ color: 'var(--color-muted)' }}>{t('tenants.empty', 'No tenants found.')}</p>;
  }

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
      <thead>
        <tr style={{ textAlign: 'left', borderBottom: '1px solid var(--border)' }}>
          <th style={{ padding: '8px 12px' }}>{t('tenants.alias', 'Alias')}</th>
          <th style={{ padding: '8px 12px' }}>{t('tenants.status', 'Status')}</th>
          <th style={{ padding: '8px 12px' }}>{t('tenants.configVersion', 'Config v')}</th>
          <th style={{ padding: '8px 12px' }}></th>
        </tr>
      </thead>
      <tbody>
        {tenants.map(tenant => (
          <tr
            key={tenant.tenantAlias}
            style={{ borderBottom: '1px solid var(--border)', cursor: 'pointer' }}
            onClick={() => navigate(`/admin/tenants/${tenant.tenantAlias}`)}
          >
            <td style={{ padding: '8px 12px', fontFamily: 'monospace' }}>{tenant.tenantAlias}</td>
            <td style={{ padding: '8px 12px' }}><TenantStatusBadge status={tenant.status} /></td>
            <td style={{ padding: '8px 12px', color: 'var(--color-muted)' }}>v{tenant.configVersion}</td>
            <td style={{ padding: '8px 12px', textAlign: 'right' }}>
              <button
                className="btn-secondary"
                onClick={e => { e.stopPropagation(); navigate(`/admin/tenants/${tenant.tenantAlias}`); }}
              >
                {t('tenants.details', 'Details →')}
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
