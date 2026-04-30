import { useRef, useState, useEffect } from 'react';
import { ChevronDown } from 'lucide-react';
import { useAuthStore } from '../../stores/authStore';

export function TenantPickerButton() {
  const user = useAuthStore(s => s.user);
  const [open, setOpen]       = useState(false);
  const [tenants, setTenants] = useState<{ tenantAlias: string }[]>([]);
  const [active, setActive]   = useState<string>(() =>
    localStorage.getItem('seer-active-tenant') ?? user?.activeTenantAlias ?? 'default',
  );
  const ref = useRef<HTMLDivElement>(null);

  const isSuperAdmin = user?.role === 'super-admin';
  const isAdmin      = user?.role === 'admin' || isSuperAdmin;
  const canFetch     = isAdmin;

  useEffect(() => {
    if (!canFetch) return;
    fetch('/chur/api/admin/tenants', { credentials: 'include' })
      .then(r => r.ok ? r.json() : [])
      .then((body: unknown) => {
        const arr = Array.isArray(body) ? (body as { tenantAlias: string; status: string }[]) : [];
        setTenants(arr.filter(t => t.status === 'ACTIVE').map(t => ({ tenantAlias: t.tenantAlias })));
      })
      .catch(() => {});
  }, [canFetch]);

  useEffect(() => {
    const h = (e: Event) => {
      const alias = (e as CustomEvent<{ activeTenant: string }>).detail?.activeTenant;
      if (alias && alias !== '__all__') setActive(alias);
    };
    window.addEventListener('aida:tenant', h);
    return () => window.removeEventListener('aida:tenant', h);
  }, []);

  // `active` is initialized from localStorage/user and updated immediately on pick().
  // Use it directly — it's always current, unlike user.activeTenantAlias which
  // lags behind until the async PATCH /auth/me/tenant response comes back.
  const displayActive = active;

  if (!isAdmin) return null;

  const displayTenants = tenants;
  const canSwitch = isAdmin && displayTenants.length > 1;

  const pick = async (alias: string) => {
    setActive(alias);
    localStorage.setItem('seer-active-tenant', alias);
    window.dispatchEvent(new CustomEvent('aida:tenant', { detail: { activeTenant: alias } }));
    setOpen(false);
    try {
      await fetch('/auth/me/tenant', {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tenantAlias: alias }),
      });
      useAuthStore.setState(s => s.user ? { user: { ...s.user, activeTenantAlias: alias } } : {});
    } catch { /* best-effort */ }
  };

  return (
    <div
      ref={ref}
      style={{ position: 'relative' }}
      onBlur={e => { if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false); }}
    >
      <button
        onClick={() => canSwitch && setOpen(v => !v)}
        title={`Tenant: ${displayActive}`}
        style={{
          display: 'flex', alignItems: 'center', gap: '5px',
          padding: '4px 8px',
          background: open
            ? 'color-mix(in srgb, var(--acc) 14%, transparent)'
            : 'color-mix(in srgb, var(--acc) 8%, transparent)',
          border: `1px solid color-mix(in srgb, var(--acc) ${open ? '50%' : '30%'}, transparent)`,
          borderRadius: 'var(--seer-radius-md)',
          color: 'var(--acc)', cursor: canSwitch ? 'pointer' : 'default',
          fontSize: '11px', fontWeight: 700, letterSpacing: '0.06em',
        }}
      >
        <span>T</span>
        <span style={{ fontWeight: 400, maxWidth: 88, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {displayActive}
        </span>
        {canSwitch && (
          <ChevronDown size={9} style={{
            color: 'var(--acc)', opacity: 0.7,
            transform: open ? 'rotate(180deg)' : undefined, transition: 'transform 0.15s',
          }} />
        )}
      </button>

      {open && displayTenants.length > 0 && (
        <div style={{
          position: 'absolute', top: 'calc(100% + 4px)', right: 0, zIndex: 300,
          minWidth: '160px', background: 'var(--bg1)', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-lg)', boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
          overflow: 'hidden',
        }}>
          <div style={{ padding: '6px 12px 5px', fontSize: '10px', color: 'var(--t3)', letterSpacing: '0.07em', borderBottom: '1px solid var(--bd)' }}>
            TENANT
          </div>
          {displayTenants.map(t => (
            <button
              key={t.tenantAlias}
              onClick={() => void pick(t.tenantAlias)}
              style={{
                display: 'flex', alignItems: 'center', gap: '8px', width: '100%', padding: '8px 12px',
                background: active === t.tenantAlias ? 'color-mix(in srgb, var(--acc) 10%, transparent)' : 'transparent',
                border: 'none', color: active === t.tenantAlias ? 'var(--acc)' : 'var(--t2)',
                fontSize: '12px', cursor: 'pointer', textAlign: 'left',
              }}
              onMouseEnter={e => { if (active !== t.tenantAlias) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
              onMouseLeave={e => { if (active !== t.tenantAlias) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
            >
              <div style={{ width: 5, height: 5, borderRadius: '50%', flexShrink: 0, background: active === t.tenantAlias ? 'var(--acc)' : 'transparent', border: `1px solid ${active === t.tenantAlias ? 'var(--acc)' : 'var(--t3)'}` }} />
              {t.tenantAlias}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
