import { useTranslation } from 'react-i18next';
import type { TenantStatus } from '../../api/admin';

const ALL_STATUSES: TenantStatus[] = ['ACTIVE', 'PROVISIONING', 'PROVISIONING_FAILED', 'SUSPENDED', 'ARCHIVED', 'PURGED'];

interface Props {
  search: string;
  status: TenantStatus | '';
  onSearch: (v: string) => void;
  onStatus: (v: TenantStatus | '') => void;
}

export function TenantSearchFilter({ search, status, onSearch, onStatus }: Props) {
  const { t } = useTranslation();
  return (
    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 12 }}>
      <input
        type="search"
        value={search}
        onChange={e => onSearch(e.target.value)}
        placeholder={t('tenants.searchPlaceholder', 'Search by alias…')}
        style={{ flex: '1 1 180px', padding: '6px 10px', borderRadius: 6, border: '1px solid var(--bd)' }}
      />
      <select
        value={status}
        onChange={e => onStatus(e.target.value as TenantStatus | '')}
        style={{ padding: '6px 10px', borderRadius: 6, border: '1px solid var(--bd)', background: 'var(--bg2)' }}
      >
        <option value="">{t('tenants.allStatuses', 'All statuses')}</option>
        {ALL_STATUSES.map(s => (
          <option key={s} value={s}>{t(`tenants.statusValues.${s}`, s)}</option>
        ))}
      </select>
    </div>
  );
}
