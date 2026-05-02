// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { TenantPickerButton } from './TenantPickerButton';

// i18n: return the key — test asserts behaviour, not translation strings.
vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (k: string) => k }),
}));

// authStore: not directly used for these branches but imported in subject.
vi.mock('../../stores/authStore', () => ({
  useAuthStore: () => ({}),
}));

const baseUser = {
  id: 'u-1',
  email: 'u@example.com',
  activeTenantAlias: 'acme',
} as any;

describe('TenantPickerButton — smoke', () => {
  let originalFetch: typeof globalThis.fetch;

  beforeEach(() => {
    originalFetch = globalThis.fetch;
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  it('renders fallback to user.activeTenantAlias when /admin/tenants fails', async () => {
    globalThis.fetch = vi.fn(() =>
      Promise.resolve({ ok: false, status: 500, json: () => Promise.reject(500) }),
    ) as any;

    render(<TenantPickerButton user={baseUser} />);

    // After fetch fails, the catch-branch sets tenants to [{ id: alias, name: alias }],
    // so 'acme' becomes the visible label (badge — single-tenant case).
    await waitFor(() => {
      expect(screen.queryByText('acme')).toBeTruthy();
    });
  });

  it('renders dropdown trigger with tenant count > 1', async () => {
    globalThis.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve([
          { id: 'acme', name: 'acme' },
          { id: 'qa-ui-kxfkkx', name: 'qa-ui-kxfkkx' },
        ]),
      }),
    ) as any;

    render(<TenantPickerButton user={baseUser} />);

    await waitFor(() => {
      // Active tenant label still shows "acme"; multi-tenant case adds a chevron.
      expect(screen.queryByText('acme')).toBeTruthy();
    });
  });

  it('calls /admin/tenants on mount', async () => {
    const fetchMock = vi.fn(() =>
      Promise.resolve({ ok: false, status: 404, json: () => Promise.reject(404) }),
    );
    globalThis.fetch = fetchMock as any;

    const user = { id: 'u-2', email: 'x@example.com' } as any;
    render(<TenantPickerButton user={user} />);

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith('/admin/tenants', expect.objectContaining({
        credentials: 'include',
      }));
    });
  });
});
