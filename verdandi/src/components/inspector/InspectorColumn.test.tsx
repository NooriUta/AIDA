// @vitest-environment jsdom
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { InspectorColumn } from './InspectorColumn';
import type { DaliNodeData } from '../../types/domain';

function makeData(overrides: Partial<DaliNodeData> = {}): DaliNodeData {
  return {
    label: 'test_col',
    nodeType: 'DaliColumn' as any,
    childrenAvailable: false,
    metadata: {},
    ...overrides,
  };
}

describe('InspectorColumn', () => {
  it('renders label', () => {
    render(<InspectorColumn data={makeData()} nodeId="n1" />);
    expect(screen.getByText('test_col')).toBeInTheDocument();
  });

  it('shows TypeBadge with short type name', () => {
    render(<InspectorColumn data={makeData({ nodeType: 'DaliColumn' as any })} nodeId="n1" />);
    expect(screen.getByText('Column')).toBeInTheDocument();
  });

  it('shows dataType when present', () => {
    render(<InspectorColumn data={makeData({ dataType: 'VARCHAR2(100)' } as any)} nodeId="n1" />);
    expect(screen.getByText('VARCHAR2(100)')).toBeInTheDocument();
  });

  it('hides optional fields when data is empty', () => {
    const { container } = render(<InspectorColumn data={makeData()} nodeId="n1" />);
    expect(container.textContent).not.toContain('inspector.operation');
    expect(container.textContent).not.toContain('inspector.schema');
  });
});
