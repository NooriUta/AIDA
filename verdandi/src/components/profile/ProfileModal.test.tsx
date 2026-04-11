// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { ProfileModal } from './ProfileModal';

// ── Mock all 10 tab components as stubs ──────────────────────────────────────
vi.mock('./tabs/ProfileTabProfile',       () => ({ ProfileTabProfile:       () => <div data-testid="tab-profile" /> }));
vi.mock('./tabs/ProfileTabSecurity',      () => ({ ProfileTabSecurity:      () => <div data-testid="tab-security" /> }));
vi.mock('./tabs/ProfileTabAccess',        () => ({ ProfileTabAccess:        () => <div data-testid="tab-access" /> }));
vi.mock('./tabs/ProfileTabAppearance',    () => ({ ProfileTabAppearance:    () => <div data-testid="tab-appearance" /> }));
vi.mock('./tabs/ProfileTabGraph',         () => ({ ProfileTabGraph:         () => <div data-testid="tab-graph" /> }));
vi.mock('./tabs/ProfileTabActivity',      () => ({ ProfileTabActivity:      () => <div data-testid="tab-activity" /> }));
vi.mock('./tabs/ProfileTabNotifications', () => ({ ProfileTabNotifications: () => <div data-testid="tab-notifications" /> }));
vi.mock('./tabs/ProfileTabFavorites',     () => ({ ProfileTabFavorites:     () => <div data-testid="tab-favorites" /> }));
vi.mock('./tabs/ProfileTabShortcuts',     () => ({ ProfileTabShortcuts:     () => <div data-testid="tab-shortcuts" /> }));
vi.mock('./tabs/ProfileTabTokens',        () => ({ ProfileTabTokens:        () => <div data-testid="tab-tokens" /> }));

// ── Mock authStore ───────────────────────────────────────────────────────────
vi.mock('../../stores/authStore', () => ({
  useAuthStore: () => ({
    user: { username: 'admin', email: 'admin@test.com', roles: ['admin'] },
    logout: vi.fn(),
  }),
}));

beforeEach(() => { vi.useFakeTimers(); });
afterEach(() => { vi.useRealTimers(); });

describe('ProfileModal', () => {
  it('renders modal window', () => {
    render(<ProfileModal onClose={vi.fn()} />);
    expect(screen.getByText('profile.title')).toBeInTheDocument();
  });

  it('default active tab is profile', () => {
    render(<ProfileModal onClose={vi.fn()} />);
    expect(screen.getByTestId('tab-profile')).toBeInTheDocument();
  });

  it('clicking appearance tab activates appearance content', () => {
    render(<ProfileModal onClose={vi.fn()} />);
    fireEvent.click(screen.getByText('profile.tabs.appearance'));
    expect(screen.getByTestId('tab-appearance')).toBeInTheDocument();
  });

  it('close button calls onClose after 200ms', () => {
    const onClose = vi.fn();
    render(<ProfileModal onClose={onClose} />);
    fireEvent.click(screen.getByTitle('profile.close'));
    expect(onClose).not.toHaveBeenCalled();
    act(() => { vi.advanceTimersByTime(200); });
    expect(onClose).toHaveBeenCalledOnce();
  });

  it('ESC keydown calls onClose', () => {
    const onClose = vi.fn();
    render(<ProfileModal onClose={onClose} />);
    fireEvent.keyDown(document, { key: 'Escape' });
    act(() => { vi.advanceTimersByTime(200); });
    expect(onClose).toHaveBeenCalledOnce();
  });

  it('all 10 tab buttons present in sidebar', () => {
    render(<ProfileModal onClose={vi.fn()} />);
    const tabLabels = [
      'profile.tabs.profile', 'profile.tabs.security', 'profile.tabs.access',
      'profile.tabs.appearance', 'profile.tabs.graph',
      'profile.tabs.activity', 'profile.tabs.notifications', 'profile.tabs.favorites',
      'profile.tabs.shortcuts', 'profile.tabs.tokens',
    ];
    for (const label of tabLabels) {
      expect(screen.getByText(label)).toBeInTheDocument();
    }
  });

  it('shows user initials in header', () => {
    render(<ProfileModal onClose={vi.fn()} />);
    expect(screen.getByText('AD')).toBeInTheDocument();
  });

  it('shows 3 section headings', () => {
    render(<ProfileModal onClose={vi.fn()} />);
    expect(screen.getByText('profile.account')).toBeInTheDocument();
    expect(screen.getByText('profile.interface')).toBeInTheDocument();
    expect(screen.getByText('profile.system')).toBeInTheDocument();
  });
});
