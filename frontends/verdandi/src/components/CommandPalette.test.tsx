// @vitest-environment jsdom
import { describe, it, expect, vi } from 'vitest';
import { screen, fireEvent } from '@testing-library/react';
import { CommandPalette } from './CommandPalette';
import { renderWithRouter } from '../test/router-utils';

// ── Mocks ────────────────────────────────────────────────────────────────────
vi.mock('../stores/loomStore', () => ({
  useLoomStore: () => ({
    toggleTheme: vi.fn(),
    requestFitView: vi.fn(),
    selectNode: vi.fn(),
    navigateToLevel: vi.fn(),
  }),
}));

vi.mock('../services/hooks', () => ({
  useSearch: () => ({ data: [], isLoading: false }),
}));

describe('CommandPalette', () => {
  it('renders overlay and input when open=true', () => {
    renderWithRouter(<CommandPalette open={true} onClose={vi.fn()} />);
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('commandPalette.placeholder')).toBeInTheDocument();
  });

  it('does not render when open=false', () => {
    renderWithRouter(<CommandPalette open={false} onClose={vi.fn()} />);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('shows all 5 static commands when query is empty', () => {
    renderWithRouter(<CommandPalette open={true} onClose={vi.fn()} />);
    expect(screen.getByText('commandPalette.fitView')).toBeInTheDocument();
    expect(screen.getByText('commandPalette.toggleTheme')).toBeInTheDocument();
    expect(screen.getByText('commandPalette.deselectNode')).toBeInTheDocument();
    expect(screen.getByText('commandPalette.goToL1')).toBeInTheDocument();
    expect(screen.getByText('commandPalette.goToKnot')).toBeInTheDocument();
  });

  it('typing filters commands', () => {
    renderWithRouter(<CommandPalette open={true} onClose={vi.fn()} />);
    fireEvent.change(screen.getByPlaceholderText('commandPalette.placeholder'), {
      target: { value: 'fitView' },
    });
    expect(screen.getByText('commandPalette.fitView')).toBeInTheDocument();
    // Non-matching commands should be gone
    expect(screen.queryByText('commandPalette.goToKnot')).not.toBeInTheDocument();
  });

  it('ArrowDown moves activeIndex', () => {
    renderWithRouter(<CommandPalette open={true} onClose={vi.fn()} />);
    const options = screen.getAllByRole('option');
    expect(options[0]).toHaveAttribute('aria-selected', 'true');
    fireEvent.keyDown(screen.getByRole('dialog'), { key: 'ArrowDown' });
    const optionsAfter = screen.getAllByRole('option');
    expect(optionsAfter[1]).toHaveAttribute('aria-selected', 'true');
  });

  it('ArrowUp wraps from first to last', () => {
    renderWithRouter(<CommandPalette open={true} onClose={vi.fn()} />);
    fireEvent.keyDown(screen.getByRole('dialog'), { key: 'ArrowUp' });
    const options = screen.getAllByRole('option');
    expect(options[options.length - 1]).toHaveAttribute('aria-selected', 'true');
  });

  it('Escape calls onClose', () => {
    const onClose = vi.fn();
    renderWithRouter(<CommandPalette open={true} onClose={onClose} />);
    fireEvent.keyDown(screen.getByRole('dialog'), { key: 'Escape' });
    expect(onClose).toHaveBeenCalledOnce();
  });

  it('has role=listbox for results list', () => {
    renderWithRouter(<CommandPalette open={true} onClose={vi.fn()} />);
    expect(screen.getByRole('listbox')).toBeInTheDocument();
  });
});
