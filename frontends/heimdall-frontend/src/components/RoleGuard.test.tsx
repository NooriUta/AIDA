/**
 * RoleGuard component tests (R4.8).
 *
 * Tests navigation guard behaviour:
 *   - renders children when role requirement is met
 *   - redirects to /overview/services when not met
 *   - respects custom redirectTo prop
 */
import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { RoleGuard } from './RoleGuard';

// ── Mock useTenantContext ─────────────────────────────────────────────────────
vi.mock('../hooks/useTenantContext', () => ({
  useTenantContext: vi.fn(),
}));

import { useTenantContext } from '../hooks/useTenantContext';
const mockCtx = vi.mocked(useTenantContext);

type Ctx = ReturnType<typeof useTenantContext>;

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

// ── Render helper ─────────────────────────────────────────────────────────────
function renderGuard(
  require:    'admin' | 'local-admin' | 'viewer',
  ctx:        Partial<Ctx>,
  redirectTo?: string,
) {
  mockCtx.mockReturnValue(makeCtx(ctx));
  return render(
    <MemoryRouter initialEntries={['/protected']}>
      <Routes>
        <Route
          path="/protected"
          element={
            <RoleGuard require={require} {...(redirectTo ? { redirectTo } : {})}>
              <div>Protected Content</div>
            </RoleGuard>
          }
        />
        <Route path="/overview/services"    element={<div>Default Redirect</div>} />
        <Route path="/custom-redirect"      element={<div>Custom Redirect</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

afterEach(() => { vi.clearAllMocks(); });

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('require="admin"', () => {
  it('renders children when isAdmin is true', () => {
    renderGuard('admin', { isAdmin: true });
    expect(screen.getByText('Protected Content')).toBeTruthy();
    expect(screen.queryByText('Default Redirect')).toBeNull();
  });

  it('redirects to /overview/services when isAdmin is false', () => {
    renderGuard('admin', { isAdmin: false });
    expect(screen.queryByText('Protected Content')).toBeNull();
    expect(screen.getByText('Default Redirect')).toBeTruthy();
  });

  it('super-admin (isAdmin: true) also passes', () => {
    renderGuard('admin', { isAdmin: true, isSuperAdmin: true });
    expect(screen.getByText('Protected Content')).toBeTruthy();
  });
});

describe('require="local-admin"', () => {
  it('renders children when isLocalAdmin is true', () => {
    renderGuard('local-admin', { isLocalAdmin: true });
    expect(screen.getByText('Protected Content')).toBeTruthy();
  });

  it('redirects when isLocalAdmin is false', () => {
    renderGuard('local-admin', { isLocalAdmin: false });
    expect(screen.queryByText('Protected Content')).toBeNull();
    expect(screen.getByText('Default Redirect')).toBeTruthy();
  });
});

describe('require="viewer"', () => {
  it('renders children when isViewer is true', () => {
    renderGuard('viewer', { isViewer: true });
    expect(screen.getByText('Protected Content')).toBeTruthy();
  });

  it('redirects when isViewer is false', () => {
    renderGuard('viewer', { isViewer: false });
    expect(screen.queryByText('Protected Content')).toBeNull();
    expect(screen.getByText('Default Redirect')).toBeTruthy();
  });
});

describe('custom redirectTo', () => {
  it('redirects to the custom path when access is denied', () => {
    renderGuard('admin', { isAdmin: false }, '/custom-redirect');
    expect(screen.queryByText('Protected Content')).toBeNull();
    expect(screen.getByText('Custom Redirect')).toBeTruthy();
  });
});
