import { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { ChevronDown } from 'lucide-react';
import { useAuthStore } from '../../stores/authStore';

// ── TenantPickerButton ────────────────────────────────────────────────────────
// All users: fetches accessible tenants from /admin/tenants on mount.
// Shows a dropdown if 2+ tenants (multi-org membership), badge otherwise.

interface TenantItem { id: string; name: string }

export function TenantPickerButton({ user }: { user: import('../../stores/authStore').AuthUser }) {
  const { t } = useTranslation();

  const [open,    setOpen]    = useState(false);
  const [tenants, setTenants] = useState<TenantItem[]>([]);
  const [loading, setLoading] = useState(true);
  const ref = useRef<HTMLDivElement>(null);

  // Fetch accessible tenants on mount — count determines badge vs dropdown
  useEffect(() => {
    fetch('/admin/tenants', { credentials: 'include' })
      .then(r => r.ok ? r.json() : Promise.reject(r.status))
      .then((data: TenantItem[]) => setTenants(data))
      .catch(() => {
        const alias = user.activeTenantAlias ?? 'default';
        setTenants([{ id: alias, name: alias }]);
      })
      .finally(() => setLoading(false));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  async function pick(alias: string) {
    localStorage.setItem('seer-active-tenant', alias);
    window.dispatchEvent(new CustomEvent('seer-tenant-changed', { detail: { alias } }));
    try {
      await fetch('/auth/me/tenant', {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tenantAlias: alias }),
      });
      // Update authStore in-place so header badge refreshes immediately
      useAuthStore.setState((s) =>
        s.user ? { user: { ...s.user, activeTenantAlias: alias } } : {},
      );
    } catch { /* session patch is best-effort */ }
    setOpen(false);
  }

  const badgeStyle: React.CSSProperties = {
    display: 'inline-flex', alignItems: 'center', gap: '5px',
    padding: '3px 8px',
    fontSize: '10px', fontWeight: 600, letterSpacing: '0.06em',
    color: 'var(--acc)',
    background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
    border: '1px solid color-mix(in srgb, var(--acc) 35%, transparent)',
    borderRadius: 'var(--seer-radius-sm)',
    textTransform: 'uppercase', flexShrink: 0,
  };

  // Badge-only: still loading, or only one accessible tenant
  if (loading || tenants.length <= 1) {
    return (
      <span title={t('tenant.activeHint', { defaultValue: 'Active tenant' })} style={badgeStyle}>
        <div style={{ width: 5, height: 5, borderRadius: '50%', background: 'var(--acc)' }} />
        {user.activeTenantAlias}
      </span>
    );
  }

  return (
    <div ref={ref} style={{ position: 'relative', flexShrink: 0 }}
      onBlur={(e) => { if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false); }}
    >
      <button
        onClick={() => setOpen((v) => !v)}
        title={t('tenant.switchHint', { defaultValue: 'Switch tenant' })}
        style={{ ...badgeStyle, cursor: 'pointer', border: 'none', gap: '5px' }}
      >
        <div style={{ width: 5, height: 5, borderRadius: '50%', background: 'var(--acc)' }} />
        {user.activeTenantAlias}
        <ChevronDown size={10} style={{
          color: 'var(--acc)', marginLeft: '1px',
          transform: open ? 'rotate(180deg)' : 'rotate(0)',
          transition: 'transform 0.15s',
        }} />
      </button>

      {open && (
        <div style={{
          position: 'absolute', top: 'calc(100% + 4px)', left: 0,
          zIndex: 300, minWidth: '180px',
          background: 'var(--bg1)', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-lg)',
          boxShadow: '0 8px 24px rgba(0,0,0,0.35)',
          overflow: 'hidden',
        }}>
          <div style={{
            padding: '6px 12px 5px', fontSize: '10px', fontWeight: 600,
            color: 'var(--t3)', letterSpacing: '0.08em',
            borderBottom: '1px solid var(--bd)',
          }}>
            {t('tenant.switchTitle', { defaultValue: 'SWITCH TENANT' })}
          </div>

          {loading && (
            <div style={{ padding: '10px 12px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('profile.loading')}
            </div>
          )}

          {!loading && tenants.map((ten) => {
            const isActive = ten.id === user.activeTenantAlias;
            return (
              <button key={ten.id} onClick={() => pick(ten.id)}
                style={{
                  display: 'flex', alignItems: 'center', gap: '8px',
                  width: '100%', padding: '9px 12px',
                  background: isActive ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
                  border: 'none',
                  color: isActive ? 'var(--acc)' : 'var(--t1)',
                  fontSize: '12px', fontWeight: isActive ? 600 : 400,
                  cursor: 'pointer', textAlign: 'left',
                  transition: 'background 0.1s',
                }}
                onMouseEnter={(e) => {
                  if (!isActive) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                }}
                onMouseLeave={(e) => {
                  if (!isActive) (e.currentTarget as HTMLElement).style.background = 'transparent';
                }}
              >
                <div style={{
                  width: 5, height: 5, borderRadius: '50%', flexShrink: 0,
                  background: isActive ? 'var(--acc)' : 'var(--t3)',
                }} />
                <span style={{ flex: 1, textTransform: 'uppercase', letterSpacing: '0.05em', fontSize: '11px' }}>
                  {ten.id}
                </span>
                {ten.name !== ten.id && (
                  <span style={{ fontSize: '10px', color: 'var(--t3)' }}>{ten.name}</span>
                )}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
