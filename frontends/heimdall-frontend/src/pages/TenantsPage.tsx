import { useState, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { usePageTitle }  from '../hooks/usePageTitle';
import { useTenants }    from '../hooks/useTenants';
import { TenantStatusBadge } from '../components/tenants/TenantStatusBadge';
import type { TenantStatus, TenantSummary } from '../api/admin';
import {
  suspendTenant, unsuspendTenant, archiveTenant,
  restoreTenant, provisionTenant,
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

// ── Provision modal ────────────────────────────────────────────────────────────
function ProvisionModal({ onDone, onClose }: { onDone: () => void; onClose: () => void }) {
  const { t } = useTranslation();
  const [alias, setAlias] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError]  = useState<string | null>(null);

  const submit = async () => {
    const a = alias.trim().toLowerCase().replace(/[^a-z0-9-]/g, '');
    if (!a) { setError(t('tenants.provision.aliasRequired', 'Alias обязателен')); return; }
    setSaving(true); setError(null);
    try {
      await provisionTenant(a);
      onDone();
      onClose();
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 300 }}>
      <div style={{ background: 'var(--bg1)', border: '1px solid var(--border)', borderRadius: 8,
                    padding: 24, width: 360, boxShadow: '0 8px 32px rgba(0,0,0,0.4)' }}>
        <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 16 }}>
          {t('tenants.provision.title', 'Создать тенант')}
        </div>
        <label style={{ fontSize: 12, color: 'var(--t3)', display: 'block', marginBottom: 4 }}>
          {t('tenants.provision.aliasLabel', 'Alias (a-z, 0-9, дефис)')}
        </label>
        <input
          className="field-input"
          style={{ width: '100%' }}
          value={alias}
          onChange={e => setAlias(e.target.value)}
          placeholder="my-tenant"
          autoFocus
          onKeyDown={e => e.key === 'Enter' && void submit()}
        />
        {error && <p style={{ color: 'var(--danger)', fontSize: 11, marginTop: 6 }}>{error}</p>}
        <div style={{ display: 'flex', gap: 8, marginTop: 16, justifyContent: 'flex-end' }}>
          <button className="btn-secondary" onClick={onClose} disabled={saving}>
            {t('action.cancel', 'Отмена')}
          </button>
          <button className="btn-secondary" onClick={submit} disabled={saving || !alias.trim()}>
            {saving ? t('status.loading', 'Loading…') : t('tenants.provision.confirm', 'Создать')}
          </button>
        </div>
      </div>
    </div>
  );
}

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
        <button className="btn-secondary" style={{ fontSize: 11 }}
          disabled={busy} onClick={() => run(() => suspendTenant(tenantAlias))}>
          {t('tenants.action.suspend', 'Suspend')}
        </button>
      )}
      {status === 'SUSPENDED' && (
        <button className="btn-secondary" style={{ fontSize: 11 }}
          disabled={busy} onClick={() => run(() => unsuspendTenant(tenantAlias))}>
          {t('tenants.action.unsuspend', 'Restore')}
        </button>
      )}
      {(status === 'ACTIVE' || status === 'SUSPENDED') && (
        <button className="btn-secondary" style={{ fontSize: 11, color: 'var(--wrn)' }}
          disabled={busy}
          onClick={() => { if (confirm(t('tenants.action.archiveConfirm', 'Архивировать тенант?')))
            void run(() => archiveTenant(tenantAlias)); }}>
          {t('tenants.action.archive', 'Archive')}
        </button>
      )}
      {status === 'ARCHIVED' && (
        <button className="btn-secondary" style={{ fontSize: 11 }}
          disabled={busy} onClick={() => run(() => restoreTenant(tenantAlias))}>
          {t('tenants.action.restore', 'Restore')}
        </button>
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
  const [search, setSearch]   = useState('');
  const [status, setStatus]   = useState<TenantStatus | ''>('');
  const [page, setPage]       = useState(0);
  const [provisionOpen, setProvisionOpen] = useState(false);

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
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>{t('tenants.heading', 'Tenants')}</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn-secondary" onClick={() => setProvisionOpen(true)}>
            + {t('tenants.provision.button', 'Создать тенант')}
          </button>
          <button className="btn-secondary" onClick={refresh} disabled={loading}>
            {loading ? t('status.loading', 'Loading…') : t('tenants.refresh', 'Refresh')}
          </button>
        </div>
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap', alignItems: 'center' }}>
        <input
          className="field-input" style={{ minWidth: 180 }}
          placeholder={t('tenants.search', 'Поиск по alias…')}
          value={search} onChange={e => handleSearch(e.target.value)}
        />
        <select className="field-input" style={{ width: 'auto' }}
          value={status} onChange={e => handleStatus(e.target.value as TenantStatus | '')}>
          <option value="">{t('tenants.allStatuses', 'Все статусы')}</option>
          {(['ACTIVE','SUSPENDED','ARCHIVED','PROVISIONING','PURGED'] as TenantStatus[]).map(s => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
        <span style={{ fontSize: 11, color: 'var(--t3)', marginLeft: 'auto' }}>
          {filtered.length} {t('tenants.total', 'тенантов')}
        </span>
      </div>

      {error && <p style={{ color: 'var(--danger)', marginBottom: 12 }}>{error}</p>}

      {/* Table */}
      {loading && !tenants.length ? (
        <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>
      ) : (
        <>
          <table className="data-table" style={{ width: '100%', fontSize: 12 }}>
            <thead>
              <tr>
                <th style={{ textAlign: 'left' }}>{t('tenants.alias', 'Alias')}</th>
                <th style={{ textAlign: 'left' }}>{t('tenants.status', 'Status')}</th>
                <th style={{ textAlign: 'left' }}>{t('tenants.harvestCron', 'Harvest cron')}</th>
                <th style={{ textAlign: 'right' }}>{t('tenants.configVersion', 'Config v')}</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {pageSlice.map(tenant => (
                <tr key={tenant.tenantAlias} style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/admin/tenants/${tenant.tenantAlias}`)}>
                  <td style={{ fontFamily: 'var(--mono)', fontWeight: 600 }}>
                    {tenant.tenantAlias}
                  </td>
                  <td><TenantStatusBadge status={tenant.status} /></td>
                  <td style={{ color: 'var(--t3)' }}>
                    {humanCron((tenant as { harvestCron?: string }).harvestCron)}
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
                  <td colSpan={5} style={{ textAlign: 'center', color: 'var(--t3)', padding: 32 }}>
                    {t('tenants.empty', 'No tenants found.')}
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          {pageCount > 1 && (
            <div style={{ display: 'flex', gap: 8, marginTop: 12, alignItems: 'center' }}>
              <button className="btn-secondary" disabled={currentPage === 0}
                onClick={() => setPage(p => p - 1)}>←</button>
              <span style={{ color: 'var(--t3)', fontSize: 11 }}>
                {currentPage + 1} / {pageCount}
              </span>
              <button className="btn-secondary" disabled={currentPage >= pageCount - 1}
                onClick={() => setPage(p => p + 1)}>→</button>
            </div>
          )}
        </>
      )}

      {provisionOpen && (
        <ProvisionModal onDone={refresh} onClose={() => setProvisionOpen(false)} />
      )}
    </div>
  );
}
