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

describe('InspectorTable: Routines section (EK-02 dedup)', () => {
  it('deduplicates routines by routineGeoid — shows unique count in header', () => {
    const data: KnotTableUsage[] = [
      makeUsage({ routineGeoid: 'r1', routineName: 'PKG.PROC', edgeType: 'READS_FROM',  stmtGeoid: 'stmt-1', stmtType: 'SELECT' }),
      makeUsage({ routineGeoid: 'r1', routineName: 'PKG.PROC', edgeType: 'WRITES_TO',   stmtGeoid: 'stmt-2', stmtType: 'UPDATE' }),
      makeUsage({ routineGeoid: 'r2', routineName: 'PKG.FUNC', edgeType: 'READS_FROM',  stmtGeoid: 'stmt-3', stmtType: 'SELECT' }),
    ];
    mockUseKnotTableRoutines.mockReturnValue({ data, isFetching: false });

    renderTable();

    // 3 raw entries but 2 unique routines → Routines (2)
    expect(screen.getByRole('button', { name: /inspector\.routines.*\(2\)/i })).toBeInTheDocument();
  });

  it('shows RW badge when routine both reads and writes', () => {
    const data: KnotTableUsage[] = [
      makeUsage({ routineGeoid: 'r1', routineName: 'PKG.PROC', edgeType: 'READS_FROM',  stmtGeoid: 'stmt-1', stmtType: 'SELECT' }),
      makeUsage({ routineGeoid: 'r1', routineName: 'PKG.PROC', edgeType: 'WRITES_TO',   stmtGeoid: 'stmt-2', stmtType: 'INSERT' }),
    ];
    mockUseKnotTableRoutines.mockReturnValue({ data, isFetching: false });

    renderTable();

    // Open the section
    fireEvent.click(screen.getByRole('button', { name: /inspector\.routines.*\(1\)/i }));
    expect(screen.getByText('RW')).toBeInTheDocument();
  });
});

describe('InspectorTable: StmtRow label (EK-02 refactor)', () => {
  it('shows last stmtGeoid segment as primary label, not routineName', () => {
    const data: KnotTableUsage[] = [
      makeUsage({ routineGeoid: 'r1', routineName: 'PKG.PROC', edgeType: 'READS_FROM', stmtGeoid: 'SCH.PKG:stmt-abc', stmtType: 'SELECT' }),
    ];
    mockUseKnotTableRoutines.mockReturnValue({ data, isFetching: false });

    renderTable();

    // Open statements section
    fireEvent.click(screen.getByRole('button', { name: /inspector\.statements.*\(1\)/i }));

    // Primary label is last segment of stmtGeoid
    expect(screen.getByText('stmt-abc')).toBeInTheDocument();
    // stmtType badge is visible
    expect(screen.getByText('SELECT')).toBeInTheDocument();
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
