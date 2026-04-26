import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { TenantSummary } from '../../api/admin';
import {
  suspendTenant, unsuspendTenant, archiveTenant,
  restoreTenant, forceCleanupTenant, resumeProvisioningTenant, triggerHarvest,
} from '../../api/admin';
import { TenantStatusBadge } from './TenantStatusBadge';

// ── Compact count formatter ───────────────────────────────────────────────────
function fmtCount(n?: number | null): string {
  if (n == null) return '—';
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1).replace(/\.0$/, '') + 'M';
  if (n >= 1_000)     return (n / 1_000).toFixed(1).replace(/\.0$/, '') + 'k';
  return String(n);
}

function humanCron(cron?: string): string {
  if (!cron) return '—';
  const parts = cron.trim().split(/\s+/);
  if (parts.length < 5) return cron;
  const [min, hour, dom, , dow] = parts;
  if (dom === '*' && dow === '*') {
    if (min === '0' && hour === '*/6')  return 'every 6h';
    if (min === '0' && hour === '*/12') return 'every 12h';
    if (min === '0' && hour === '*/4')  return 'every 4h';
    if (min === '0' && /^\d+$/.test(hour)) return `daily ${hour}:00`;
  }
  return cron;
}

// ── Per-row action buttons ────────────────────────────────────────────────────
function TenantRowActions({ tenant, onRefresh }: { tenant: TenantSummary; onRefresh: () => void }) {
  const { t } = useTranslation();
  const [busy, setBusy] = useState(false);

  const run = async (fn: () => Promise<unknown>) => {
    setBusy(true);
    try { await fn(); onRefresh(); }
    catch (e) { alert(e instanceof Error ? e.message : String(e)); }
    finally { setBusy(false); }
  };

  const { status, tenantAlias, configVersion } = tenant;
  return (
    <div style={{ display: 'flex', gap: 4, justifyContent: 'flex-end', flexWrap: 'wrap' }}>
      {status === 'ACTIVE' && (
        <button className="btn btn-primary btn-sm" disabled={busy}
          onClick={() => run(() => triggerHarvest(tenantAlias).then(r => { alert(`Harvest queued: ${r.harvestId}`); }))}>
          {t('tenants.action.harvest', 'Harvest')}
        </button>
      )}
      {status === 'ACTIVE' && (
        <button className="btn btn-secondary btn-sm" disabled={busy}
          onClick={() => run(() => suspendTenant(tenantAlias, configVersion))}>
          {t('tenants.action.suspend', 'Suspend')}
        </button>
      )}
      {status === 'SUSPENDED' && (
        <button className="btn btn-secondary btn-sm" disabled={busy}
          onClick={() => run(() => unsuspendTenant(tenantAlias))}>
          {t('tenants.action.unsuspend', 'Restore')}
        </button>
      )}
      {(status === 'ACTIVE' || status === 'SUSPENDED') && (
        <button className="btn btn-secondary btn-sm" disabled={busy}
          onClick={() => { if (confirm(t('tenants.action.archiveConfirm', 'Archive tenant?')))
            void run(() => archiveTenant(tenantAlias, configVersion)); }}>
          {t('tenants.action.archive', 'Archive')}
        </button>
      )}
      {status === 'ARCHIVED' && (
        <button className="btn btn-secondary btn-sm" disabled={busy}
          onClick={() => run(() => restoreTenant(tenantAlias))}>
          {t('tenants.action.restore', 'Restore')}
        </button>
      )}
      {status === 'PROVISIONING_FAILED' && (
        <>
          <button className="btn btn-primary btn-sm" disabled={busy}
            onClick={() => run(() => resumeProvisioningTenant(tenantAlias))}>
            {t('tenants.action.resume', 'Retry')}
          </button>
          <button className="btn btn-danger btn-sm" disabled={busy}
            onClick={() => { if (confirm(t('tenants.action.forceCleanupConfirm', 'Delete all tenant data?')))
              void run(() => forceCleanupTenant(tenantAlias)); }}>
            {t('tenants.action.forceCleanup', 'Cleanup')}
          </button>
        </>
      )}
    </div>
  );
}

// ── Table ─────────────────────────────────────────────────────────────────────
interface Props {
  tenants:   TenantSummary[];
  onRefresh: () => void;
}

export function TenantList({ tenants, onRefresh }: Props) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  if (tenants.length === 0) {
    return (
      <p style={{ textAlign: 'center', color: 'var(--t3)', padding: 32 }}>
        {t('tenants.empty', 'No tenants found.')}
      </p>
    );
  }

  return (
    <div className="data-panel">
      <table className="data-table">
        <thead>
          <tr>
            <th style={{ textAlign: 'left' }}>{t('tenants.alias', 'Alias')}</th>
            <th style={{ textAlign: 'left' }}>{t('tenants.status', 'Status')}</th>
            <th style={{ textAlign: 'right' }} title={t('tenants.membersCount.hint', 'KC users in org')}>
              {t('tenants.membersCount', 'Users')}
            </th>
            <th style={{ textAlign: 'right' }} title={t('tenants.sourcesCount.hint', 'DaliSource in dali_{alias}')}>
              {t('tenants.sourcesCount', 'Sources')}
            </th>
            <th style={{ textAlign: 'right' }} title={t('tenants.atomsCount.hint', 'Graph vertices in hound_{alias}')}>
              {t('tenants.atomsCount', 'Atoms')}
            </th>
            <th style={{ textAlign: 'left' }}>{t('tenants.harvestCron', 'Harvest cron')}</th>
            <th style={{ textAlign: 'right' }}>{t('tenants.configVersion', 'Config v')}</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {tenants.map(tenant => (
            <tr key={tenant.tenantAlias} style={{ cursor: 'pointer' }}
              onClick={() => navigate(`/admin/tenants/${tenant.tenantAlias}`)}>
              <td>
                <span style={{ fontWeight: 600, color: 'var(--t1)', fontSize: 13 }}>
                  {tenant.tenantAlias}
                </span>
              </td>
              <td>
                <TenantStatusBadge status={tenant.status} />
                {tenant.status === 'PROVISIONING_FAILED' && tenant.lastFailedCause && (
                  <div style={{ fontSize: 10, color: 'var(--danger)', marginTop: 2,
                                maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis',
                                whiteSpace: 'nowrap' }}
                    title={tenant.lastFailedCause}>
                    step {tenant.lastFailedStep}: {tenant.lastFailedCause}
                  </div>
                )}
              </td>
              <td style={{ textAlign: 'right', fontFamily: 'var(--mono)', color: 'var(--t2)' }}>
                {fmtCount(tenant.membersCount)}
              </td>
              <td style={{ textAlign: 'right', fontFamily: 'var(--mono)', color: 'var(--t2)' }}>
                {fmtCount(tenant.sourcesCount)}
              </td>
              <td style={{ textAlign: 'right', fontFamily: 'var(--mono)', color: 'var(--t2)' }}>
                {fmtCount(tenant.atomsCount)}
              </td>
              <td style={{ color: 'var(--t3)' }}>{humanCron(tenant.harvestCron)}</td>
              <td style={{ textAlign: 'right', color: 'var(--t3)', fontFamily: 'var(--mono)' }}>
                v{tenant.configVersion}
              </td>
              <td style={{ width: 200 }} onClick={e => e.stopPropagation()}>
                <TenantRowActions tenant={tenant} onRefresh={onRefresh} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
