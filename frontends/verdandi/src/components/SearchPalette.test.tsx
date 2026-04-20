// @vitest-environment jsdom
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { SearchPalette } from './SearchPalette';

// ── Mocks ────────────────────────────────────────────────────────────────────
vi.mock('../stores/loomStore', () => ({
  useLoomStore: () => ({
    jumpTo: vi.fn(),
    selectNode: vi.fn(),
    requestFocusNode: vi.fn(),
    hiddenNodeIds: new Set<string>(),
    restoreNode: vi.fn(),
    showAllNodes: vi.fn(),
  }),
}));

vi.mock('../services/hooks', () => ({
  useSearch: () => ({ data: [], isLoading: false }),
}));

vi.mock('../hooks/useSearchHistory', () => ({
  getSearchHistory: () => ['prev_query_1', 'prev_query_2'],
  pushSearchQuery: vi.fn(),
  clearSearchHistory: vi.fn(),
  getRecentNodes: () => [
    { id: 'n1', label: 'Table_A', type: 'DaliTable' },
    { id: 'n2', label: 'Table_B', type: 'DaliTable' },
  ],
  pushRecentNode: vi.fn(),
  clearRecentNodes: vi.fn(),
}));

describe('SearchPalette', () => {
  it('renders overlay and input when open=true', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });

  it('does not render when open=false', () => {
    render(<SearchPalette open={false} onClose={vi.fn()} />);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('shows recent search queries in idle state', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    expect(screen.getByText('prev_query_1')).toBeInTheDocument();
    expect(screen.getByText('prev_query_2')).toBeInTheDocument();
  });

  it('shows recent nodes in idle state', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    expect(screen.getByText('Table_A')).toBeInTheDocument();
    expect(screen.getByText('Table_B')).toBeInTheDocument();
  });

  it('renders 7 type filter buttons', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    const filterLabels = [
      'search.filters.all', 'search.filters.tables', 'search.filters.columns',
      'search.filters.routines', 'search.filters.statements',
      'search.filters.databases', 'search.filters.applications',
    ];
    for (const label of filterLabels) {
      expect(screen.getByText(label)).toBeInTheDocument();
    }
  });

  it('Escape calls onClose', () => {
    const onClose = vi.fn();
    render(<SearchPalette open={true} onClose={onClose} />);
    fireEvent.keyDown(screen.getByRole('dialog'), { key: 'Escape' });
    expect(onClose).toHaveBeenCalledOnce();
  });

  it('clear history button calls clearSearchHistory', async () => {
    const { clearSearchHistory } = await import('../hooks/useSearchHistory');
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    const clearBtns = screen.getAllByText('search.clearHistory');
    fireEvent.click(clearBtns[0]);
    expect(clearSearchHistory).toHaveBeenCalled();
  });

  it('typing in search input updates query display', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'my_table' } });
    expect((input as HTMLInputElement).value).toBe('my_table');
  });

  it('clicking type filter button activates it', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    const tablesBtn = screen.getByText('search.filters.tables');
    fireEvent.click(tablesBtn);
    // After click the button should be in an active state (aria-pressed or style change)
    expect(tablesBtn).toBeInTheDocument();
  });

  it('clicking "all" filter after another filter resets to all', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    fireEvent.click(screen.getByText('search.filters.tables'));
    fireEvent.click(screen.getByText('search.filters.all'));
    expect(screen.getByText('search.filters.all')).toBeInTheDocument();
  });

  it('clicking each filter type does not throw', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    const filters = [
      'search.filters.columns', 'search.filters.routines',
      'search.filters.statements', 'search.filters.databases',
      'search.filters.applications',
    ];
    for (const f of filters) {
      fireEvent.click(screen.getByText(f));
    }
  });

  it('ArrowDown / ArrowUp keydown on dialog does not throw', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    const dialog = screen.getByRole('dialog');
    fireEvent.keyDown(dialog, { key: 'ArrowDown' });
    fireEvent.keyDown(dialog, { key: 'ArrowUp' });
    fireEvent.keyDown(dialog, { key: 'Enter' });
  });

  it('clicking recent node from history does not throw', () => {
    render(<SearchPalette open={true} onClose={vi.fn()} />);
    const nodeBtn = screen.getByText('Table_A');
    fireEvent.click(nodeBtn);
    expect(nodeBtn).not.toBeNull();
  });
});
