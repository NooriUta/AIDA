// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { InspectorTable } from './InspectorTable';
import type { DaliNodeData } from '../../types/domain';
import type { KnotTableUsage } from '../../services/lineage';

// ── Global mocks ──────────────────────────────────────────────────────────────

const mockJumpTo = vi.fn();
vi.mock('../../stores/loomStore', () => ({
  useLoomStore: () => ({ jumpTo: mockJumpTo }),
}));

const mockUseKnotTableRoutines = vi.fn();
const mockUseKnotColumnStatements = vi.fn();
vi.mock('../../services/hooks', () => ({
  useKnotTableRoutines:    (...args: unknown[]) => mockUseKnotTableRoutines(...args),
  useKnotColumnStatements: (...args: unknown[]) => mockUseKnotColumnStatements(...args),
}));

// ── Helpers ───────────────────────────────────────────────────────────────────

function makeData(overrides: Partial<DaliNodeData> = {}): DaliNodeData {
  return {
    label: 'TEST_TABLE',
    nodeType: 'DaliTable' as any,
    childrenAvailable: false,
    metadata: { tableGeoid: 'SCH.TEST_TABLE' },
    ...overrides,
  };
}

function makeUsage(overrides: Partial<KnotTableUsage>): KnotTableUsage {
  return {
    routineGeoid: 'r1',
    routineName:  'PKG.PROC',
    edgeType:     'READS_FROM',
    stmtGeoid:    null,
    stmtType:     null,
    ...overrides,
  } as KnotTableUsage;
}

function renderTable(data = makeData()) {
  return render(
    <MemoryRouter>
      <InspectorTable data={data} nodeId="tbl-1" />
    </MemoryRouter>,
  );
}

// ── Setup ─────────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks();
  mockUseKnotTableRoutines.mockReturnValue({ data: undefined, isFetching: false });
  mockUseKnotColumnStatements.mockReturnValue({ data: undefined, isFetching: false });
});

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('InspectorTable: Statements section (EK-02)', () => {
  it('renders Statements section header in the overview tab', () => {
    renderTable();
    expect(screen.getByRole('button', { name: /inspector\.statements/i })).toBeInTheDocument();
  });

  it('deduplicates stmts by stmtGeoid — shows count of unique statements', () => {
    const duplicateData: KnotTableUsage[] = [
      makeUsage({ routineGeoid: 'r1', routineName: 'PKG.A', stmtGeoid: 'stmt-1', stmtType: 'SELECT' }),
      makeUsage({ routineGeoid: 'r2', routineName: 'PKG.B', stmtGeoid: 'stmt-2', stmtType: 'INSERT' }),
      makeUsage({ routineGeoid: 'r3', routineName: 'PKG.A', stmtGeoid: 'stmt-1', stmtType: 'SELECT' }), // duplicate
    ];
    mockUseKnotTableRoutines.mockReturnValue({ data: duplicateData, isFetching: false });

    renderTable();

    // Find the Statements section button (shows count after data loads)
    const stmtsBtn = screen.getByRole('button', { name: /inspector\.statements.*\(2\)/i });
    expect(stmtsBtn).toBeInTheDocument();
  });

  it('shows empty state message when no stmts have stmtGeoid', () => {
    const noStmtData: KnotTableUsage[] = [
      makeUsage({ routineGeoid: 'r1', stmtGeoid: null }),
    ];
    mockUseKnotTableRoutines.mockReturnValue({ data: noStmtData, isFetching: false });

    renderTable();

    // Expand the section
    const btn = screen.getByRole('button', { name: /inspector\.statements.*\(0\)/i });
    fireEvent.click(btn);

    expect(screen.getByText('inspector.noStatements')).toBeInTheDocument();
  });

  it('passes nodeId to useKnotTableRoutines when section is opened', () => {
    renderTable();

    // Hook called once on render (with enabled=false)
    expect(mockUseKnotTableRoutines).toHaveBeenCalledWith('tbl-1', false);
  });
});

describe('InspectorTable: basic rendering', () => {
  it('renders the table label in the header', () => {
    renderTable(makeData({ label: 'MY_TABLE' }));
    expect(screen.getByText('MY_TABLE')).toBeInTheDocument();
  });

  it('shows Overview and DDL tabs', () => {
    renderTable();
    expect(screen.getByRole('tab', { name: /inspector\.tabMain/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'DDL' })).toBeInTheDocument();
  });
});
