// @vitest-environment jsdom
import { describe, it, expect, vi } from 'vitest';
import { screen, fireEvent } from '@testing-library/react';
import { Header } from './Header';
import { renderWithRouter } from '../../test/router-utils';

vi.mock('../../stores/loomStore', () => ({
  useLoomStore: () => ({
    theme: 'dark',
    toggleTheme: vi.fn(),
    palette: 'amber-forest',
    setPalette: vi.fn(),
    requestFitView: vi.fn(),
  }),
}));

vi.mock('../../stores/authStore', () => ({
  useAuthStore: () => ({ user: { username: 'admin', role: 'admin' } }),
}));

vi.mock('../../hooks/useHotkeys', () => ({
  useHotkeys: () => {},
}));

vi.mock('./LanguageSwitcher', () => ({ LanguageSwitcher: () => null }));
vi.mock('./LegendButton',      () => ({ LegendButton:      () => null }));
vi.mock('../ui/ToolbarPrimitives', () => ({ ToolbarDivider: () => null }));
vi.mock('../profile/ProfileModal', () => ({ ProfileModal: () => null }));
vi.mock('../CommandPalette',  () => ({ CommandPalette:  () => null }));
vi.mock('../SearchPalette',   () => ({ SearchPalette:   () => null }));

describe('Header — navigation state from useLocation', () => {
  it('shows VERDANDI as active norn on root path', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    expect(screen.getByText('VERDANDI')).toBeInTheDocument();
  });

  it('shows URD as active norn on /urd path', () => {
    renderWithRouter(<Header />, { initialEntries: ['/urd'] });
    expect(screen.getByText('URD')).toBeInTheDocument();
  });

  it('shows SKULD as active norn on /skuld path', () => {
    renderWithRouter(<Header />, { initialEntries: ['/skuld'] });
    expect(screen.getByText('SKULD')).toBeInTheDocument();
  });

  it('LOOM sub-module is active on root path', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    const loomBtn = screen.getByText('nav.loom');
    expect(loomBtn).not.toBeDisabled();
  });

  it('KNOT sub-module is active on /knot path', () => {
    renderWithRouter(<Header />, { initialEntries: ['/knot'] });
    const knotBtn = screen.getByText('nav.knot');
    expect(knotBtn).not.toBeDisabled();
  });

  it('ANVIL sub-module is disabled (H2 horizon)', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    const anvilBtn = screen.getByText('nav.anvil');
    expect(anvilBtn).toBeDisabled();
  });

  it('user initials are shown in the user badge', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    expect(screen.getByText('AD')).toBeInTheDocument();
    expect(screen.getByText('admin')).toBeInTheDocument();
  });

  it('clicking norn dropdown shows all three norns', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    fireEvent.click(screen.getByText('Seiðr'));
    expect(screen.getAllByText('VERDANDI').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('URD')).toBeInTheDocument();
    expect(screen.getByText('SKULD')).toBeInTheDocument();
  });
});
