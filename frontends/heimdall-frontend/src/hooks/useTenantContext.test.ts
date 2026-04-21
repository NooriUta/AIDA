/**
 * useTenantContext — role-level hierarchy tests (R4.8).
 */
import { describe, it, expect, vi, afterEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useTenantContext } from './useTenantContext';

// ── Mock authStore ────────────────────────────────────────────────────────────
vi.mock('../stores/authStore', () => ({
  useAuthStore: vi.fn(),
}));

import { useAuthStore } from '../stores/authStore';
const mockStore = vi.mocked(useAuthStore);

/** Wire the mock so that useAuthStore(selector) returns selector({ user }) */
function withRole(role: string | null) {
  mockStore.mockImplementation((selector: any) =>
    selector({ user: role !== null ? { id: '1', username: 'testuser', role } : null }),
  );
}

afterEach(() => { vi.clearAllMocks(); });

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('super-admin', () => {
  it('all permission flags are true', () => {
    withRole('super-admin');
    const { result } = renderHook(() => useTenantContext());
    expect(result.current.isSuperAdmin).toBe(true);
    expect(result.current.isAdmin).toBe(true);
    expect(result.current.isTenantOwner).toBe(true);
    expect(result.current.isLocalAdmin).toBe(true);
    expect(result.current.canManageUsers).toBe(true);
    expect(result.current.isViewer).toBe(true);
  });

  it('role is preserved', () => {
    withRole('super-admin');
    const { result } = renderHook(() => useTenantContext());
    expect(result.current.role).toBe('super-admin');
  });
});

describe('admin', () => {
  it('isAdmin true, isSuperAdmin false', () => {
    withRole('admin');
    const { result } = renderHook(() => useTenantContext());
    expect(result.current.isAdmin).toBe(true);
    expect(result.current.isSuperAdmin).toBe(false);
    expect(result.current.isLocalAdmin).toBe(true);
    expect(result.current.canManageUsers).toBe(true);
  });
});

describe('tenant-owner', () => {
  it('isTenantOwner true, isAdmin false', () => {
    withRole('tenant-owner');
    const { result } = renderHook(() => useTenantContext());
    expect(result.current.isTenantOwner).toBe(true);
    expect(result.current.isAdmin).toBe(false);
    expect(result.current.isLocalAdmin).toBe(true);
    expect(result.current.canManageUsers).toBe(true);
  });
});

describe('local-admin', () => {
  it('canManageUsers true, isAdmin and isTenantOwner false', () => {
    withRole('local-admin');
    const { result } = renderHook(() => useTenantContext());
    expect(result.current.canManageUsers).toBe(true);
    expect(result.current.isLocalAdmin).toBe(true);
    expect(result.current.isAdmin).toBe(false);
    expect(result.current.isTenantOwner).toBe(false);
    expect(result.current.isViewer).toBe(true);
  });
});

describe('viewer', () => {
  it('only isViewer true, all management flags false', () => {
    withRole('viewer');
    const { result } = renderHook(() => useTenantContext());
    expect(result.current.isViewer).toBe(true);
    expect(result.current.isLocalAdmin).toBe(false);
    expect(result.current.isAdmin).toBe(false);
    expect(result.current.canManageUsers).toBe(false);
  });
});

describe('null user (unauthenticated)', () => {
  it('defaults role to viewer → isViewer true, all else false', () => {
    withRole(null);
    const { result } = renderHook(() => useTenantContext());
    expect(result.current.isViewer).toBe(true);
    expect(result.current.isLocalAdmin).toBe(false);
    expect(result.current.isAdmin).toBe(false);
  });
});

describe('unknown/custom role', () => {
  it('unknown role maps to level 0 → isViewer false', () => {
    // 'guest' is not in ROLE_LEVELS → maps to 0, below viewer threshold of 20
    withRole('guest');
    const { result } = renderHook(() => useTenantContext());
    expect(result.current.isViewer).toBe(false);
    expect(result.current.isLocalAdmin).toBe(false);
  });
});

describe('role tier ordering', () => {
  it('each tier is strictly more privileged than the previous', () => {
    // local-admin can manage, editor cannot
    withRole('editor');
    const editorResult = renderHook(() => useTenantContext()).result;
    expect(editorResult.current.canManageUsers).toBe(false);

    withRole('local-admin');
    const ladminResult = renderHook(() => useTenantContext()).result;
    expect(ladminResult.current.canManageUsers).toBe(true);
  });

  it('admin can do everything local-admin can', () => {
    withRole('admin');
    const { result } = renderHook(() => useTenantContext());
    expect(result.current.canManageUsers).toBe(true);
    expect(result.current.isLocalAdmin).toBe(true);
    expect(result.current.isTenantOwner).toBe(true);
  });
});
