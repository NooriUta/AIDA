/**
 * ProvisionModal tests — covers form validation, successful provisioning,
 * and failed provisioning with step-level error display.
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { I18nextProvider } from 'react-i18next';
import i18n from 'i18next';
import { ProvisionModal, PROVISION_STEPS } from './ProvisionModal';

// ── Minimal i18n setup ────────────────────────────────────────────────────────
void i18n.init({
  lng: 'en',
  resources: {
    en: {
      translation: {
        'tenants.provision.title':      'Создать тенант',
        'tenants.provision.aliasLabel': 'Alias (a-z, 0-9, дефис)',
        'tenants.provision.confirm':    'Создать',
        'action.cancel':                'Отмена',
        'action.close':                 'Закрыть',
      },
    },
  },
});

// Suppress setInterval so step animation doesn't fire in tests
beforeEach(() => {
  vi.spyOn(globalThis, 'setInterval').mockReturnValue(0 as unknown as ReturnType<typeof setInterval>);
  vi.spyOn(globalThis, 'clearInterval').mockImplementation(() => {});
});

afterEach(() => vi.restoreAllMocks());

function renderModal(onDone = vi.fn(), onClose = vi.fn()) {
  return render(
    <I18nextProvider i18n={i18n}>
      <ProvisionModal onDone={onDone} onClose={onClose} />
    </I18nextProvider>,
  );
}

// ── Form validation ────────────────────────────────────────────────────────────
describe('form validation', () => {
  it('renders alias input and submit button', () => {
    renderModal();
    expect(screen.getByTestId('alias-input')).toBeTruthy();
    expect(screen.getByTestId('submit-btn')).toBeTruthy();
  });

  it('submit is disabled when alias is empty', () => {
    renderModal();
    const btn = screen.getByTestId('submit-btn') as HTMLButtonElement;
    expect(btn.disabled).toBe(true);
  });

  it('shows validation error for alias shorter than 4 chars after stripping', async () => {
    renderModal();
    fireEvent.change(screen.getByTestId('alias-input'), { target: { value: 'ab' } });
    await act(async () => { fireEvent.click(screen.getByTestId('submit-btn')); });
    expect(screen.getByTestId('validation-error').textContent).toContain('4 символ');
  });

  it('accepts 4-char alias and proceeds to provisioning phase', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ keycloakOrgId: 'org-abc', lastStep: 7 }),
    } as Response);

    renderModal();
    fireEvent.change(screen.getByTestId('alias-input'), { target: { value: 'acme' } });
    await act(async () => { fireEvent.click(screen.getByTestId('submit-btn')); });

    // phase switches away from 'form' — alias-input no longer visible
    await waitFor(() => {
      expect(screen.queryByTestId('alias-input')).toBeNull();
    });
  });
});

// ── Successful provisioning ────────────────────────────────────────────────────
describe('successful provisioning', () => {
  it('transitions to success phase and shows keycloakOrgId', async () => {
    const onDone = vi.fn();
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ keycloakOrgId: 'org-abc-123', lastStep: 7 }),
    } as Response);

    renderModal(onDone);
    fireEvent.change(screen.getByTestId('alias-input'), { target: { value: 'acme-corp' } });
    await act(async () => { fireEvent.click(screen.getByTestId('submit-btn')); });

    await waitFor(() => {
      expect(screen.getByTestId('success-banner')).toBeTruthy();
    });
    expect(screen.getByTestId('kc-org-id').textContent).toContain('org-abc-123');
    expect(onDone).toHaveBeenCalledOnce();
  });

  it('calls POST /chur/api/admin/tenants with sanitised alias', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ keycloakOrgId: 'org-x', lastStep: 7 }),
    } as Response);

    renderModal();
    fireEvent.change(screen.getByTestId('alias-input'), { target: { value: 'My Tenant!' } });
    await act(async () => { fireEvent.click(screen.getByTestId('submit-btn')); });

    await waitFor(() => screen.getByTestId('success-banner'));
    const [, init] = fetchSpy.mock.calls[0];
    expect(JSON.parse((init as RequestInit).body as string).alias).toBe('mytenant');
  });
});

// ── Failed provisioning ────────────────────────────────────────────────────────
describe('failed provisioning — step 0 (pre-flight)', () => {
  it('shows ✗ icon at step 0 and displays error cause', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({
        error:     'provisioning_failed',
        failedStep: 0,
        cause:     'Pre-flight: FRIGG unreachable',
        message:   'Provisioning failed at step 0: Pre-flight: FRIGG unreachable.',
      }),
    } as Response);

    renderModal();
    fireEvent.change(screen.getByTestId('alias-input'), { target: { value: 'acme' } });
    await act(async () => { fireEvent.click(screen.getByTestId('submit-btn')); });

    await waitFor(() => {
      expect(screen.getByTestId('step-icon-0').textContent).toBe('✗');
    });
    expect(screen.getByTestId('error-cause').textContent).toContain('FRIGG unreachable');
  });
});

describe('failed provisioning — step 2 (frigg insert)', () => {
  it('marks step 2 failed; step 0 is ✓, step 7 is ○', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({
        error: 'provisioning_failed',
        failedStep: 2,
        cause: 'FRIGG INSERT error',
        message: 'Provisioning failed at step 2.',
      }),
    } as Response);

    renderModal();
    fireEvent.change(screen.getByTestId('alias-input'), { target: { value: 'beta' } });
    await act(async () => { fireEvent.click(screen.getByTestId('submit-btn')); });

    await waitFor(() => {
      expect(screen.getByTestId('step-icon-2').textContent).toBe('✗');
    });
    expect(screen.getByTestId('step-icon-0').textContent).toBe('✓');
    expect(screen.getByTestId('step-icon-7').textContent).toBe('○');
  });
});

describe('failed provisioning — step 7 (activate)', () => {
  it('all earlier steps ✓, step 7 ✗', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({ error: 'provisioning_failed', failedStep: 7, cause: 'Update failed' }),
    } as Response);

    renderModal();
    fireEvent.change(screen.getByTestId('alias-input'), { target: { value: 'delta' } });
    await act(async () => { fireEvent.click(screen.getByTestId('submit-btn')); });

    await waitFor(() => {
      expect(screen.getByTestId('step-icon-7').textContent).toBe('✗');
    });
    // steps 0..5 shown as ✓ (visibleStep = failedStep - 2 = 5)
    expect(screen.getByTestId('step-icon-5').textContent).toBe('✓');
  });
});

describe('failed provisioning — network error', () => {
  it('shows back button when fetch throws', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('fetch failed'));

    renderModal();
    fireEvent.change(screen.getByTestId('alias-input'), { target: { value: 'gamma' } });
    await act(async () => { fireEvent.click(screen.getByTestId('submit-btn')); });

    await waitFor(() => {
      expect(screen.getByTestId('back-btn')).toBeTruthy();
    });
  });
});

// ── Navigation ────────────────────────────────────────────────────────────────
describe('navigation', () => {
  it('"Назад" returns to form phase', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({ error: 'provisioning_failed', failedStep: 1, cause: 'KC error' }),
    } as Response);

    renderModal();
    fireEvent.change(screen.getByTestId('alias-input'), { target: { value: 'gamma' } });
    await act(async () => { fireEvent.click(screen.getByTestId('submit-btn')); });
    await waitFor(() => screen.getByTestId('back-btn'));

    await act(async () => { fireEvent.click(screen.getByTestId('back-btn')); });

    expect(screen.getByTestId('alias-input')).toBeTruthy();
  });

  it('"Отмена" calls onClose', async () => {
    const onClose = vi.fn();
    renderModal(vi.fn(), onClose);
    const btns = screen.getAllByRole('button');
    const cancel = btns.find(b => b.textContent?.includes('Отмена'));
    expect(cancel).toBeTruthy();
    fireEvent.click(cancel!);
    expect(onClose).toHaveBeenCalledOnce();
  });
});

// ── PROVISION_STEPS export ─────────────────────────────────────────────────────
describe('PROVISION_STEPS', () => {
  it('has exactly 8 steps matching backend saga', () => {
    expect(PROVISION_STEPS).toHaveLength(8);
  });

  it('step 0 is pre-flight', () => {
    expect(PROVISION_STEPS[0]).toContain('Pre-flight');
  });

  it('step 7 is activation', () => {
    expect(PROVISION_STEPS[7]).toContain('ACTIVE');
  });
});
