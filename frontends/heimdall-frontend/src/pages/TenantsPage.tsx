import { useState, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { usePageTitle }   from '../hooks/usePageTitle';
import { useTenants }     from '../hooks/useTenants';
import { useTenantContext } from '../hooks/useTenantContext';
import { TenantStatusBadge }  from '../components/tenants/TenantStatusBadge';
import { TenantCreateModal }  from '../components/tenants/TenantCreateModal';
import type { TenantStatus, TenantSummary } from '../api/admin';
import {
  suspendTenant, unsuspendTenant, archiveTenant,
  restoreTenant, forceCleanupTenant, resumeProvisioningTenant, triggerHarvest,
} from '../api/admin';

const PAGE_SIZE = 20;

// ── Inline cron humanizer (no cronstrue dep) ───────────────────────────────────
function humanCron(cron?: string): string {
  if (!cron) return '—';
  const parts = cron.trim().split(/\s+/);
  if (parts.length < 5) return cron;
  const [min, hour, dom, , dow] = parts;
  if (dom === '*' && dow === '*') {
    if (min === '0' && hour === '*/6') return 'every 6h';
    if (min === '0' && hour === '*/12') return 'every 12h';
    if (min === '0' && hour === '*/4') return 'every 4h';
    if (min === '0' && /^\d+$/.test(hour)) return `daily ${hour}:00`;
  }
  return cron;
}

// Compact count formatter: null → "—", ≥1000 → "1.2k", ≥1000000 → "1.2M"
function fmtCount(n?: number | null): string {
  if (n == null) return '—';
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1).replace(/\.0$/, '') + 'M';
  if (n >= 1_000)     return (n / 1_000).toFixed(1).replace(/\.0$/, '') + 'k';
  return String(n);
}

// ProvisionModal is now in components/tenants/ProvisionModal.tsx

// ── Per-row action buttons ────────────────────────────────────────────────────
function TenantActions({
  tenant, onRefresh,
}: {
  tenant:    TenantSummary;
  onRefresh: () => void;
}) {
  const { t } = useTranslation();
  const [busy, setBusy] = useState(false);

  const run = async (fn: () => Promise<unknown>) => {
    setBusy(true);
    try { await fn(); onRefresh(); }
    catch (e) { alert(e instanceof Error ? e.message : String(e)); }
    finally { setBusy(false); }
  };

  const { status, tenantAlias } = tenant;
  return (
    <div style={{ display: 'flex', gap: 4, justifyContent: 'flex-end', flexWrap: 'wrap' }}>
      {status === 'ACTIVE' && (
        <button className="btn btn-primary btn-sm"
          disabled={busy} onClick={() => run(() => triggerHarvest(tenantAlias)
            .then(r => { alert(`Harvest queued: ${r.harvestId}`); }))}>
          {t('tenants.action.harvest', 'Harvest')}
        </button>
      )}
      {status === 'ACTIVE' && (
        <button className="btn btn-secondary btn-sm"
          disabled={busy} onClick={() => run(() => suspendTenant(tenantAlias))}>
          {t('tenants.action.suspend', 'Suspend')}
        </button>
      )}
      {status === 'SUSPENDED' && (
        <button className="btn btn-secondary btn-sm"
          disabled={busy} onClick={() => run(() => unsuspendTenant(tenantAlias))}>
          {t('tenants.action.unsuspend', 'Restore')}
        </button>
      )}
      {(status === 'ACTIVE' || status === 'SUSPENDED') && (
        <button className="btn btn-secondary btn-sm"
          disabled={busy}
          onClick={() => { if (confirm(t('tenants.action.archiveConfirm', 'Архивировать тенант?')))
            void run(() => archiveTenant(tenantAlias)); }}>
          {t('tenants.action.archive', 'Archive')}
        </button>
      )}
      {status === 'ARCHIVED' && (
        <button className="btn btn-secondary btn-sm"
          disabled={busy} onClick={() => run(() => restoreTenant(tenantAlias))}>
          {t('tenants.action.restore', 'Restore')}
        </button>
      )}
      {status === 'PROVISIONING_FAILED' && (
        <>
          <button className="btn btn-primary btn-sm"
            disabled={busy}
            onClick={() => run(() => resumeProvisioningTenant(tenantAlias))}>
            {t('tenants.action.resume', 'Retry')}
          </button>
          <button className="btn btn-danger btn-sm"
            disabled={busy}
            onClick={() => { if (confirm(t('tenants.action.forceCleanupConfirm', 'Удалить все данные тенанта?')))
              void run(() => forceCleanupTenant(tenantAlias)); }}>
            {t('tenants.action.forceCleanup', 'Cleanup')}
          </button>
        </>
      )}
    </div>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function TenantsPage() {
  const { t } = useTranslation();
  usePageTitle(t('tenants.pageTitle', 'Tenants'));
  const navigate = useNavigate();

  const { tenants, loading, error, refresh } = useTenants();
  const { isAdmin }       = useTenantContext();
  const [search, setSearch]   = useState('');
  const [status, setStatus]   = useState<TenantStatus | ''>('');
  const [page, setPage]       = useState(0);
  const [createOpen, setCreateOpen] = useState(false);

  const filtered = useMemo(() => {
    let result = tenants;
    if (status) result = result.filter(t => t.status === status);
    if (search.trim()) {
      const q = search.trim().toLowerCase();
      result = result.filter(t => t.tenantAlias.toLowerCase().includes(q));
    }
    return result;
  }, [tenants, search, status]);

  const pageCount   = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const currentPage = Math.min(page, pageCount - 1);
  const pageSlice   = filtered.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);

  const handleSearch = useCallback((v: string) => { setSearch(v); setPage(0); }, []);
  const handleStatus = useCallback((v: TenantStatus | '') => { setStatus(v); setPage(0); }, []);

  return (
    <div style={{ padding: '24px', maxWidth: 1100 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>{t('tenants.heading', 'Tenants')}</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => setCreateOpen(true)}>
              {t('tenants.create', '+ Create')}
            </button>
          )}
          <button className="btn btn-secondary" onClick={refresh} disabled={loading}>
            {loading ? t('status.loading', 'Loading…') : t('tenants.refresh', 'Refresh')}
          </button>
        </div>
      </div>

      {createOpen && (
        <TenantCreateModal
          onClose={() => setCreateOpen(false)}
          onCreated={alias => { refresh(); navigate(`/admin/tenants/${alias}`); }}
        />
      )}

      {/* Filters — single row */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 12, alignItems: 'center' }}>
        <input
          className="field-input"
          style={{ flex: '1 1 240px', minWidth: 180, maxWidth: 420, width: 'auto' }}
          placeholder={t('tenants.search', 'Поиск по псевдониму…')}
          value={search} onChange={e => handleSearch(e.target.value)}
        />
        <select className="field-input" style={{ width: 'auto', flex: '0 0 auto' }}
          value={status} onChange={e => handleStatus(e.target.value as TenantStatus | '')}>
          <option value="">{t('tenants.allStatuses', 'Все статусы')}</option>
          {(['ACTIVE','SUSPENDED','ARCHIVED','PROVISIONING','PROVISIONING_FAILED','PURGED'] as TenantStatus[]).map(s => (
            <option key={s} value={s}>{t(`tenants.statusName.${s}`, s)}</option>
          ))}
        </select>
        <span style={{ fontSize: 11, color: 'var(--t3)', marginLeft: 'auto', whiteSpace: 'nowrap' }}>
          {filtered.length} {t('tenants.total', 'тенантов')}
        </span>
      </div>

      {error && <p style={{ color: 'var(--danger)', marginBottom: 12 }}>{error}</p>}

      {/* Table */}
      {loading && !tenants.length ? (
        <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>
      ) : (
        <>
          <div className="data-panel">
          <table className="data-table">
            <thead>
              <tr>
                <th style={{ textAlign: 'left' }}>{t('tenants.alias', 'Alias')}</th>
                <th style={{ textAlign: 'left' }}>{t('tenants.status', 'Status')}</th>
                <th style={{ textAlign: 'right' }} title={t('tenants.membersCount.hint', 'KC-пользователи в организации')}>
                  {t('tenants.membersCount', 'Users')}
                </th>
                <th style={{ textAlign: 'right' }} title={t('tenants.sourcesCount.hint', 'DaliSource в dali_{alias}')}>
                  {t('tenants.sourcesCount', 'Sources')}
                </th>
                <th style={{ textAlign: 'right' }} title={t('tenants.atomsCount.hint', 'Вершины графа в hound_{alias}')}>
                  {t('tenants.atomsCount', 'Atoms')}
                </th>
                <th style={{ textAlign: 'left' }}>{t('tenants.harvestCron', 'Harvest cron')}</th>
                <th style={{ textAlign: 'right' }}>{t('tenants.configVersion', 'Config v')}</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {pageSlice.map(tenant => (
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
                  <td style={{ color: 'var(--t3)' }}>
                    {humanCron(tenant.harvestCron)}
                  </td>
                  <td style={{ textAlign: 'right', color: 'var(--t3)', fontFamily: 'var(--mono)' }}>
                    v{tenant.configVersion}
                  </td>
                  <td style={{ width: 200 }} onClick={e => e.stopPropagation()}>
                    <TenantActions tenant={tenant} onRefresh={refresh} />
                  </td>
                </tr>
              ))}
              {pageSlice.length === 0 && (
                <tr>
                  <td colSpan={8} style={{ textAlign: 'center', color: 'var(--t3)', padding: 32 }}>
                    {t('tenants.empty', 'No tenants found.')}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          </div>

          {pageCount > 1 && (
            <div style={{ display: 'flex', gap: 8, marginTop: 12, alignItems: 'center' }}>
              <button className="btn btn-secondary" disabled={currentPage === 0}
                onClick={() => setPage(p => p - 1)}>←</button>
              <span style={{ color: 'var(--t3)', fontSize: 11 }}>
                {currentPage + 1} / {pageCount}
              </span>
              <button className="btn btn-secondary" disabled={currentPage >= pageCount - 1}
                onClick={() => setPage(p => p + 1)}>→</button>
            </div>
          )}
        </>
      )}

    </div>
  );
}
