// @vitest-environment jsdom
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProfileTabShortcuts } from './ProfileTabShortcuts';

describe('ProfileTabShortcuts', () => {
  it('renders all 5 shortcut group headings', () => {
    render(<ProfileTabShortcuts />);
    expect(screen.getByText('profile.shortcuts.navigation')).toBeInTheDocument();
    expect(screen.getByText('profile.shortcuts.canvas')).toBeInTheDocument();
    expect(screen.getByText('profile.shortcuts.panels')).toBeInTheDocument();
    expect(screen.getByText('profile.shortcuts.graph')).toBeInTheDocument();
    expect(screen.getByText('profile.shortcuts.export')).toBeInTheDocument();
  });

  it('each group has at least 1 shortcut item', () => {
    render(<ProfileTabShortcuts />);
    // Check at least one item from each group exists
    expect(screen.getByText('Drill down in node')).toBeInTheDocument();
    expect(screen.getByText('Command palette')).toBeInTheDocument();
    expect(screen.getByText('Toggle theme')).toBeInTheDocument();
    expect(screen.getByText('Fit view')).toBeInTheDocument();
    expect(screen.getByText('Export PNG')).toBeInTheDocument();
  });

  it('renders kbd elements for key combinations', () => {
    const { container } = render(<ProfileTabShortcuts />);
    const kbdElements = container.querySelectorAll('kbd');
    expect(kbdElements.length).toBeGreaterThan(0);
  });

  it('shows modifier hint text at bottom', () => {
    const { container } = render(<ProfileTabShortcuts />);
    expect(container.textContent).toContain('Ctrl on Windows/Linux');
    expect(container.textContent).toContain('Shift');
  });
});
