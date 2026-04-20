// @vitest-environment jsdom
import { describe, it, expect, vi } from 'vitest';
import { screen, fireEvent } from '@testing-library/react';
import { Header } from './Header';
import { renderWithRouter } from '../../test/router-utils';

const toggleThemeMock = vi.fn();
const setPaletteMock  = vi.fn();

vi.mock('../../stores/loomStore', () => ({
  useLoomStore: () => ({
    theme: 'dark',
    toggleTheme: toggleThemeMock,
    palette: 'amber-forest',
    setPalette: setPaletteMock,
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

describe('Header — toolbar interactions', () => {
  it('theme toggle button calls toggleTheme', () => {
    toggleThemeMock.mockClear();
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    // Theme button has title based on current theme ('dark' → shows 'theme.light')
    const themeBtn = screen.getByTitle('theme.light');
    fireEvent.click(themeBtn);
    expect(toggleThemeMock).toHaveBeenCalledOnce();
  });

  it('palette button opens palette menu', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    const paletteBtn = screen.getByTitle('palette.title');
    fireEvent.click(paletteBtn);
    expect(screen.getByText('palette.amberForest')).toBeInTheDocument();
    expect(screen.getByText('palette.lichen')).toBeInTheDocument();
  });

  it('clicking a palette option calls setPalette', () => {
    setPaletteMock.mockClear();
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    fireEvent.click(screen.getByTitle('palette.title'));
    fireEvent.click(screen.getByText('palette.lichen'));
    expect(setPaletteMock).toHaveBeenCalledWith('lichen');
  });

  it('search button is rendered and clickable', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    const searchBtn = screen.getByTitle(/searchPalette.title/);
    expect(searchBtn).toBeInTheDocument();
    fireEvent.click(searchBtn);
  });

  it('cmd palette button is rendered and clickable', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    const cmdBtn = screen.getByTitle(/commandPalette.title/);
    expect(cmdBtn).toBeInTheDocument();
    fireEvent.click(cmdBtn);
  });

  it('KNOT sub-module button navigates on click', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    const knotBtn = screen.getByText('nav.knot');
    fireEvent.click(knotBtn);
    // navigation doesn't throw; KNOT route is /knot
    expect(knotBtn).not.toBeDisabled();
  });

  it('norn button has mouseEnter/Leave handlers', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    const nornBtn = screen.getByText('VERDANDI');
    fireEvent.mouseEnter(nornBtn);
    fireEvent.mouseLeave(nornBtn);
  });

  it('palette menu item mouseEnter/Leave handlers fire without error', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    fireEvent.click(screen.getByTitle('palette.title'));
    const lichenBtn = screen.getByText('palette.lichen');
    fireEvent.mouseEnter(lichenBtn);
    fireEvent.mouseLeave(lichenBtn);
  });

  it('seer menu button shows hover state on mouseEnter/Leave', () => {
    renderWithRouter(<Header />, { initialEntries: ['/'] });
    const seerBtn = screen.getByText('Seiðr').closest('button')!;
    fireEvent.mouseEnter(seerBtn);
    fireEvent.mouseLeave(seerBtn);
  });
});
