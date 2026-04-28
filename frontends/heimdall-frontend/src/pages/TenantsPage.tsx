import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { usePageTitle } from '../hooks/usePageTitle';
import { useTenants } from '../hooks/useTenants';
import { useTenantContext } from '../hooks/useTenantContext';
import { TenantList } from '../components/tenants/TenantList';
import { TenantSearchFilter } from '../components/tenants/TenantSearchFilter';
import { ProvisionModal } from '../components/tenants/ProvisionModal';
import type { TenantStatus } from '../api/admin';

const PAGE_SIZE = 20;

export default function TenantsPage() {
  const { t } = useTranslation();
  usePageTitle(t('tenants.pageTitle', 'Tenants'));

  const { tenants, loading, error, refresh } = useTenants();
  const { isAdmin }       = useTenantContext();
  const navigate          = useNavigate();
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

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const currentPage = Math.min(page, pageCount - 1);
  const pageSlice = filtered.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);

  const handleSearch = (v: string) => { setSearch(v); setPage(0); };
  const handleStatus = (v: TenantStatus | '') => { setStatus(v); setPage(0); };

  return (
    <div style={{ padding: '24px', maxWidth: 960 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16, gap: 8 }}>
        <h2 style={{ margin: 0 }}>{t('tenants.heading', 'Tenants')}</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          {isAdmin && (
            <button className="btn btn-primary" data-testid="btn-create-tenant" onClick={() => setCreateOpen(true)}>
              {t('tenants.create', '+ Create')}
            </button>
          )}
          <button className="btn btn-secondary" data-testid="btn-refresh" onClick={refresh} disabled={loading}>
            {loading ? t('status.loading', 'Loading…') : t('tenants.refresh', 'Refresh')}
          </button>
        </div>
      </div>

      {createOpen && (
        <ProvisionModal
          onDone={alias => { void alias; refresh(); }}
          onClose={alias => {
            setCreateOpen(false);
            if (alias) navigate(`/admin/tenants/${alias}`);
          }}
        />
      )}

      <TenantSearchFilter
        search={search}
        status={status}
        onSearch={handleSearch}
        onStatus={handleStatus}
      />

      {error && (
        <p style={{ color: 'var(--danger)', margin: '8px 0' }}>{error}</p>
      )}

      {loading && !tenants.length ? (
        <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>
      ) : (
        <>
          <TenantList tenants={pageSlice} onRefresh={refresh} />

          {pageCount > 1 && (
            <div style={{ display: 'flex', gap: 8, marginTop: 12, alignItems: 'center' }}>
              <button
                className="btn btn-secondary"
                disabled={currentPage === 0}
                onClick={() => setPage(p => p - 1)}
              >
                ←
              </button>
              <span style={{ color: 'var(--t3)', fontSize: '0.85rem' }}>
                {currentPage + 1} / {pageCount}
              </span>
              <button
                className="btn btn-secondary"
                disabled={currentPage >= pageCount - 1}
                onClick={() => setPage(p => p + 1)}
              >
                →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
