// @vitest-environment jsdom
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { InspectorParameter } from './InspectorParameter';
import type { DaliNodeData } from '../../types/domain';

function makeData(overrides: Partial<DaliNodeData> = {}): DaliNodeData {
  return {
    label: 'p_user_id',
    nodeType: 'DaliParameter' as any,
    childrenAvailable: false,
    metadata: {},
    ...overrides,
  };
}

describe('InspectorParameter', () => {
  it('renders label', () => {
    render(<InspectorParameter data={makeData()} nodeId="n1" />);
    expect(screen.getByText('p_user_id')).toBeInTheDocument();
  });

  it('shows ParamBadge (Parameter for DaliParameter)', () => {
    render(<InspectorParameter data={makeData()} nodeId="n1" />);
    expect(screen.getByText('Parameter')).toBeInTheDocument();
  });

  it('shows direction from metadata', () => {
    render(<InspectorParameter data={makeData({ metadata: { direction: 'in' } })} nodeId="n1" />);
    expect(screen.getByText('IN')).toBeInTheDocument();
  });

  it('shows routine name from metadata', () => {
    render(<InspectorParameter data={makeData({ metadata: { routineName: 'get_user' } })} nodeId="n1" />);
    expect(screen.getByText('get_user')).toBeInTheDocument();
  });
});
