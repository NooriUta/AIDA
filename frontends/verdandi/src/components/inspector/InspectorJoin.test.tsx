// @vitest-environment jsdom
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { InspectorJoin } from './InspectorJoin';
import type { DaliNodeData } from '../../types/domain';

function makeData(overrides: Partial<DaliNodeData> = {}): DaliNodeData {
  return {
    label: 'join_1',
    nodeType: 'DaliJoin' as any,
    childrenAvailable: false,
    metadata: {},
    ...overrides,
  };
}

describe('InspectorJoin', () => {
  it('renders label', () => {
    render(<InspectorJoin data={makeData()} nodeId="n1" />);
    expect(screen.getByText('join_1')).toBeInTheDocument();
  });

  it('shows JoinBadge with join type', () => {
    render(<InspectorJoin data={makeData({ metadata: { joinType: 'INNER' } })} nodeId="n1" />);
    expect(screen.getByText('INNER JOIN')).toBeInTheDocument();
  });

  it('shows left/right table names', () => {
    render(<InspectorJoin data={makeData({ metadata: { joinType: 'LEFT', leftTable: 'users', rightTable: 'orders' } })} nodeId="n1" />);
    expect(screen.getByText('users')).toBeInTheDocument();
    expect(screen.getByText('orders')).toBeInTheDocument();
  });

  it('shows join condition', () => {
    render(<InspectorJoin data={makeData({ metadata: { joinType: 'INNER', condition: 'a.id = b.id' } })} nodeId="n1" />);
    expect(screen.getByText('a.id = b.id')).toBeInTheDocument();
  });
});
