import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { useTenantMetrics } from '../hooks/useMetrics';
import { useTenantContext } from '../hooks/useTenantContext';

interface Props {
  /** If set, hide the tenant filter dropdown and show only this tenant's row. */
  lockTenant?: string;
}

export function TenantMetricsPanel({ lockTenant }: Props) {
  const { t } = useTranslation();
  const { tenantMetrics, error } = useTenantMetrics();
  const { isSuperAdmin }         = useTenantContext();
  const [filter, setFilter]      = useState('');

  const rows = useMemo(() => {
    const all = tenantMetrics?.top20 ?? [];
    if (lockTenant) return all.filter(r => r.tenantAlias === lockTenant);
    if (!filter)    return all;
    return all.filter(r => r.tenantAlias.toLowerCase().includes(filter.toLowerCase()));
  }, [tenantMetrics, filter, lockTenant]);

  if (error && !tenantMetrics) {
    return <p style={{ color: 'var(--color-danger)', fontSize: '0.85rem' }}>{error}</p>;
  }

  return (
    <div style={{ padding: 16, border: '1px solid var(--border)', borderRadius: 6 }}>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 12, marginBottom: 12 }}>
        <h3 style={{ margin: 0, fontSize: '0.95rem' }}>
          {t('metrics.byTenant', 'Per-tenant metrics')}
        </h3>
        <span style={{ color: 'var(--color-muted)', fontSize: '0.8rem' }}>
          {t('metrics.tenantCount', '{{n}} tenants',
            { n: tenantMetrics?.tenantCount ?? 0 })}
        </span>
        <div style={{ flex: 1 }} />
        {!lockTenant && isSuperAdmin && (
          <input
            type="search"
            placeholder={t('metrics.filterTenant', 'Filter alias…')}
            value={filter}
            onChange={e => setFilter(e.target.value)}
            style={{ fontSize: '0.85rem' }}
          />
        )}
      </div>

      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
        <thead>
          <tr style={{ textAlign: 'left', borderBottom: '1px solid var(--border)' }}>
            <th style={{ padding: '6px 10px' }}>{t('metrics.alias',    'Alias')}</th>
            <th style={{ padding: '6px 10px' }}>{t('metrics.events',   'Events')}</th>
            <th style={{ padding: '6px 10px' }}>{t('metrics.sessions', 'Sessions')}</th>
            <th style={{ padding: '6px 10px' }}>{t('metrics.atoms',    'Atoms')}</th>
            <th style={{ padding: '6px 10px' }}>{t('metrics.jobs',     'Jobs')}</th>
            <th style={{ padding: '6px 10px' }}>{t('metrics.errors',   'Errors')}</th>
            <th style={{ padding: '6px 10px' }}>{t('metrics.last',     'Last event')}</th>
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td colSpan={7} style={{ padding: '12px', color: 'var(--color-muted)' }}>
                {t('metrics.noTenantData', 'No tenant activity yet.')}
              </td>
            </tr>
          ) : rows.map(r => (
            <tr key={r.tenantAlias} style={{ borderBottom: '1px solid var(--border)' }}>
              <td style={{ padding: '6px 10px', fontFamily: 'monospace' }}>{r.tenantAlias}</td>
              <td style={{ padding: '6px 10px' }}>{r.totalEvents.toLocaleString()}</td>
              <td style={{ padding: '6px 10px' }}>{r.parseSessions.toLocaleString()}</td>
              <td style={{ padding: '6px 10px' }}>{r.atoms.toLocaleString()}</td>
              <td style={{ padding: '6px 10px' }}>{r.activeJobs.toLocaleString()}</td>
              <td style={{
                padding: '6px 10px',
                color: r.errors > 0 ? 'var(--color-danger)' : 'var(--color-muted)',
              }}>{r.errors.toLocaleString()}</td>
              <td style={{ padding: '6px 10px', color: 'var(--color-muted)' }}>
                {r.lastEventAt ? new Date(r.lastEventAt).toLocaleTimeString() : '—'}
              </td>
            </tr>
          ))}
        </tbody>
        {!lockTenant && tenantMetrics && tenantMetrics.rest.totalEvents > 0 && (
          <tfoot>
            <tr style={{ borderTop: '1px solid var(--border)', color: 'var(--color-muted)' }}>
              <td style={{ padding: '6px 10px', fontStyle: 'italic' }}>
                … + {tenantMetrics.tenantCount - tenantMetrics.top20.length} {t('metrics.moreTenants', 'more')}
              </td>
              <td style={{ padding: '6px 10px' }}>{tenantMetrics.rest.totalEvents.toLocaleString()}</td>
              <td style={{ padding: '6px 10px' }}>{tenantMetrics.rest.parseSessions.toLocaleString()}</td>
              <td style={{ padding: '6px 10px' }}>{tenantMetrics.rest.atoms.toLocaleString()}</td>
              <td style={{ padding: '6px 10px' }}>{tenantMetrics.rest.activeJobs.toLocaleString()}</td>
              <td style={{ padding: '6px 10px' }}>{tenantMetrics.rest.errors.toLocaleString()}</td>
              <td />
            </tr>
          </tfoot>
        )}
      </table>
    </div>
  );
}
