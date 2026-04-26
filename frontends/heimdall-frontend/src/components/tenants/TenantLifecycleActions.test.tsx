/**
 * HTA-06 + L1/L2: TenantLifecycleActions component tests.
 *
 * L1 Regression — single tenant "default": button matrix per status, role gating,
 *   confirm-modal flow for destructive actions, recoverable-action skip-confirm.
 *
 * L2 Multi-tenant — acme/beta/gamma: each action must invoke the API with the
 *   correct alias and never leak across tenants.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import { I18nextProvider, initReactI18next } from 'react-i18next';
import i18n from 'i18next';

import { TenantLifecycleActions } from './TenantLifecycleActions';
import type { DaliTenantConfig, TenantStatus } from '../../api/admin';
import * as admin from '../../api/admin';
import * as ctx   from '../../hooks/useTenantContext';

// ── i18n bootstrap (inline — tests use the defaultValue fallback) ────────────
if (!i18n.isInitialized) {
  i18n.use(initReactI18next).init({
    lng:            'en',
    fallbackLng:    'en',
    resources:      { en: { translation: {} } },
    interpolation:  { escapeValue: false },
    returnEmptyString: false,
  });
}

// ── Mocks ────────────────────────────────────────────────────────────────────
vi.mock('../../api/admin', async () => {
  const actual = await vi.importActual<typeof import('../../api/admin')>('../../api/admin');
  return {
    ...actual,
    suspendTenant:    vi.fn().mockResolvedValue({ ok: true, status: 'SUSPENDED' }),
    unsuspendTenant:  vi.fn().mockResolvedValue({ ok: true }),
    archiveTenant:    vi.fn().mockResolvedValue({ ok: true }),
    restoreTenant:    vi.fn().mockResolvedValue({ ok: true }),
    extendRetention:  vi.fn().mockResolvedValue({ ok: true }),
  };
});

vi.mock('../../hooks/useTenantContext', () => ({
  useTenantContext: vi.fn(),
}));

const mockCtx = vi.mocked(ctx.useTenantContext);

type Ctx = ReturnType<typeof ctx.useTenantContext>;
function makeCtx(overrides: Partial<Ctx>): Ctx {
  return {
    role:           'viewer',
    isAdmin:        false,
    isTenantOwner:  false,
    isLocalAdmin:   false,
    isViewer:       true,
    isSuperAdmin:   false,
    canManageUsers: false,
    ...overrides,
  };
}

const asSuperAdmin = () => mockCtx.mockReturnValue(makeCtx({
  role: 'super-admin', isSuperAdmin: true, isAdmin: true, isTenantOwner: true,
  isLocalAdmin: true, canManageUsers: true,
}));
const asAdmin = () => mockCtx.mockReturnValue(makeCtx({
  role: 'admin', isAdmin: true, isTenantOwner: false, isLocalAdmin: false,
}));

function tenant(overrides: Partial<DaliTenantConfig> & { status: TenantStatus }): DaliTenantConfig {
  return {
    tenantAlias:   'default',
    configVersion: 1,
    ...overrides,
  };
}

function renderActions(props: { tenant: DaliTenantConfig; onRefresh?: () => void }) {
  return render(
    <I18nextProvider i18n={i18n}>
      <TenantLifecycleActions tenant={props.tenant} onRefresh={props.onRefresh ?? vi.fn()} />
    </I18nextProvider>,
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  cleanup();
});

// ── L1 Regression — single tenant "default" ──────────────────────────────────
describe('Regression — single tenant "default"', () => {

  it('ACTIVE → [Suspend, Archive now] visible to superadmin', () => {
    asSuperAdmin();
    renderActions({ tenant: tenant({ status: 'ACTIVE' }) });
    expect(screen.getByRole('button', { name: /suspend/i })).toBeTruthy();
    expect(screen.getByRole('button', { name: /archive/i })).toBeTruthy();
    expect(screen.queryByRole('button', { name: /restore/i })).toBeNull();
  });

  it('SUSPENDED → [Unsuspend, Archive now]', () => {
    asSuperAdmin();
    renderActions({ tenant: tenant({ status: 'SUSPENDED' }) });
    expect(screen.getByRole('button', { name: /unsuspend/i })).toBeTruthy();
    expect(screen.getByRole('button', { name: /archive/i })).toBeTruthy();
  });

  it('ARCHIVED → [Restore, Extend Retention]', () => {
    asSuperAdmin();
    renderActions({ tenant: tenant({ status: 'ARCHIVED' }) });
    expect(screen.getByRole('button', { name: /restore/i })).toBeTruthy();
    expect(screen.getByRole('button', { name: /extend/i })).toBeTruthy();
  });

  it('PROVISIONING → no action buttons', () => {
    asSuperAdmin();
    const { container } = renderActions({ tenant: tenant({ status: 'PROVISIONING' }) });
    expect(container.querySelectorAll('button')).toHaveLength(0);
  });

  it('admin (non-superadmin) → renders nothing', () => {
    asAdmin();
    const { container } = renderActions({ tenant: tenant({ status: 'ACTIVE' }) });
    expect(container.querySelectorAll('button')).toHaveLength(0);
  });

  it('Suspend → confirm modal → API + onRefresh', async () => {
    asSuperAdmin();
    const onRefresh = vi.fn();
    renderActions({ tenant: tenant({ status: 'ACTIVE' }), onRefresh });

    fireEvent.click(screen.getByRole('button', { name: /suspend/i }));
    expect(admin.suspendTenant).not.toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: /users\.confirm/i }));
    await waitFor(() => expect(admin.suspendTenant).toHaveBeenCalledWith('default'));
    await waitFor(() => expect(onRefresh).toHaveBeenCalled());
  });

  it('Archive-now → confirm modal → API', async () => {
    asSuperAdmin();
    renderActions({ tenant: tenant({ status: 'ACTIVE' }) });
    fireEvent.click(screen.getByRole('button', { name: /archive/i }));
    expect(admin.archiveTenant).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: /users\.confirm/i }));
    await waitFor(() => expect(admin.archiveTenant).toHaveBeenCalledWith('default'));
  });

  it('Restore → API without confirm', async () => {
    asSuperAdmin();
    const onRefresh = vi.fn();
    renderActions({ tenant: tenant({ status: 'ARCHIVED' }), onRefresh });
    fireEvent.click(screen.getByRole('button', { name: /restore/i }));
    await waitFor(() => expect(admin.restoreTenant).toHaveBeenCalledWith('default'));
    await waitFor(() => expect(onRefresh).toHaveBeenCalled());
  });
});

// ── L2 Multi-tenant — alias isolation ────────────────────────────────────────
describe('Multi-tenant — alias isolation', () => {

  it('Suspend "acme" → suspendTenant("acme") not "beta"', async () => {
    asSuperAdmin();
    renderActions({ tenant: tenant({ tenantAlias: 'acme', status: 'ACTIVE' }) });
    fireEvent.click(screen.getByRole('button', { name: /suspend/i }));
    fireEvent.click(screen.getByRole('button', { name: /users\.confirm/i }));
    await waitFor(() => expect(admin.suspendTenant).toHaveBeenCalledWith('acme'));
    expect(admin.suspendTenant).not.toHaveBeenCalledWith('beta');
    expect(admin.suspendTenant).not.toHaveBeenCalledWith('default');
  });

  it('Restore "beta" → restoreTenant("beta") not "acme"', async () => {
    asSuperAdmin();
    renderActions({ tenant: tenant({ tenantAlias: 'beta', status: 'ARCHIVED', configVersion: 2 }) });
    fireEvent.click(screen.getByRole('button', { name: /restore/i }));
    await waitFor(() => expect(admin.restoreTenant).toHaveBeenCalledWith('beta'));
    expect(admin.restoreTenant).not.toHaveBeenCalledWith('acme');
  });

  it('Three tenants in sequence — each correct alias in API', async () => {
    asSuperAdmin();
    for (const alias of ['acme', 'beta', 'gamma']) {
      vi.clearAllMocks();
      asSuperAdmin();
      const { unmount } = renderActions({
        tenant: tenant({ tenantAlias: alias, status: 'ACTIVE' }),
      });
      fireEvent.click(screen.getByRole('button', { name: /suspend/i }));
      fireEvent.click(screen.getByRole('button', { name: /users\.confirm/i }));
      await waitFor(() => expect(admin.suspendTenant).toHaveBeenCalledWith(alias));
      unmount();
    }
  });

  it('"gamma" ARCHIVED — extendRetention("gamma", ts) on confirm', async () => {
    asSuperAdmin();
    renderActions({ tenant: tenant({ tenantAlias: 'gamma', status: 'ARCHIVED', configVersion: 3 }) });
    fireEvent.click(screen.getByRole('button', { name: /extend/i }));
    fireEvent.click(screen.getByRole('button', { name: /users\.confirm/i }));
    await waitFor(() => expect(admin.extendRetention)
      .toHaveBeenCalledWith('gamma', expect.any(Number)));
  });
});
