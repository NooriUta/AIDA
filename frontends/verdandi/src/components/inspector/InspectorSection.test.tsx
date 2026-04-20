// @vitest-environment jsdom
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { InspectorSection, InspectorRow } from './InspectorSection';

describe('InspectorSection', () => {
  it('renders title and children when open', () => {
    render(
      <InspectorSection title="Details">
        <span>child content</span>
      </InspectorSection>,
    );
    expect(screen.getByText('Details')).toBeInTheDocument();
    expect(screen.getByText('child content')).toBeInTheDocument();
  });

  it('hides children when defaultOpen=false', () => {
    render(
      <InspectorSection title="Hidden" defaultOpen={false}>
        <span>hidden content</span>
      </InspectorSection>,
    );
    expect(screen.queryByText('hidden content')).not.toBeInTheDocument();
  });

  it('toggle button expands and collapses section', () => {
    render(
      <InspectorSection title="Toggle">
        <span>toggled content</span>
      </InspectorSection>,
    );
    const btn = screen.getByRole('button', { name: 'Toggle' });
    // Initially open — content visible
    expect(screen.getByText('toggled content')).toBeInTheDocument();
    // Click to collapse
    fireEvent.click(btn);
    expect(screen.queryByText('toggled content')).not.toBeInTheDocument();
    // Click to expand again
    fireEvent.click(btn);
    expect(screen.getByText('toggled content')).toBeInTheDocument();
  });

  it('calls onToggle callback on toggle', () => {
    const onToggle = vi.fn();
    render(
      <InspectorSection title="CB" onToggle={onToggle}>
        <span>x</span>
      </InspectorSection>,
    );
    fireEvent.click(screen.getByRole('button', { name: 'CB' }));
    expect(onToggle).toHaveBeenCalledWith(false);
    fireEvent.click(screen.getByRole('button', { name: 'CB' }));
    expect(onToggle).toHaveBeenCalledWith(true);
  });

  it('mouseEnter/Leave handlers on toggle button fire without error', () => {
    render(
      <InspectorSection title="Hover"><span /></InspectorSection>,
    );
    const btn = screen.getByRole('button', { name: 'Hover' });
    fireEvent.mouseEnter(btn);
    fireEvent.mouseLeave(btn);
  });
});

describe('InspectorRow', () => {
  it('renders label and value', () => {
    render(<InspectorRow label="Type" value="DaliTable" />);
    expect(screen.getByText('Type')).toBeInTheDocument();
    expect(screen.getByText('DaliTable')).toBeInTheDocument();
  });
});
