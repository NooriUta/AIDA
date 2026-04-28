import { useEffect, useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '../stores/authStore';

const LS_KEY = 'seer-active-tenant';
const ALL_TENANTS_VALUE = '__all__';

interface TenantOption {
  tenantAlias: string;
  status: string;
}

export function useTenantSelectorValue(): string {
  return localStorage.getItem(LS_KEY) ?? ALL_TENANTS_VALUE;
}

interface TenantSelectorProps {
  onChange?: (value: string) => void;
}

export function TenantSelector({ onChange }: TenantSelectorProps) {
  const { t } = useTranslation();
  const user = useAuthStore(s => s.user);
  const [tenants, setTenants] = useState<TenantOption[]>([]);
  const [value, setValue] = useState<string>(() => localStorage.getItem(LS_KEY) ?? ALL_TENANTS_VALUE);

  const isSuperAdmin = user?.role === 'super-admin';

  const load = useCallback(async () => {
    try {
      const res = await fetch('/chur/api/admin/tenants', { credentials: 'include' });
      if (!res.ok) return;
      const body = await res.json() as TenantOption[];
      const list = Array.isArray(body) ? body : ((body as unknown as { tenants?: TenantOption[] }).tenants ?? []);
      setTenants(list.filter(t => t.status === 'ACTIVE'));
    } catch { /* silent */ }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const handleChange = (v: string) => {
    setValue(v);
    localStorage.setItem(LS_KEY, v);
    window.dispatchEvent(new CustomEvent('aida:tenant', { detail: { activeTenant: v } }));
    onChange?.(v);
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <label style={{ fontSize: '12px', color: 'var(--t3)', whiteSpace: 'nowrap' }}>
        {t('tenantSelector.label', 'Тенант:')}
      </label>
      <select
        value={value}
        onChange={e => handleChange(e.target.value)}
        className="field-input"
        style={{ fontSize: '12px', padding: '2px 6px', height: 28, minWidth: 120 }}
      >
        {isSuperAdmin && (
          <option value={ALL_TENANTS_VALUE}>{t('tenantSelector.all', 'Все тенанты')}</option>
        )}
        {tenants.map(t => (
          <option key={t.tenantAlias} value={t.tenantAlias}>{t.tenantAlias}</option>
        ))}
      </select>
    </div>
  );
}
