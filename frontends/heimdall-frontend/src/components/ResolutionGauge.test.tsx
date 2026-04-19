import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ResolutionGauge } from './ResolutionGauge';

vi.mock('@nivo/pie', () => ({
  ResponsivePie: () => <div data-testid="responsive-pie" />,
}));

vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (k: string) => k }),
}));

describe('ResolutionGauge', () => {
  it('renders without crash and shows na key when rate is NaN', () => {
    render(<ResolutionGauge rate={NaN} />);
    expect(screen.getByText('metrics.na')).toBeTruthy();
    expect(screen.queryByText(/NaN/)).toBeNull();
  });

  it('shows 95.0% when rate is 95 (green range ≥85)', () => {
    render(<ResolutionGauge rate={95} />);
    expect(screen.getByText('95.0%')).toBeTruthy();
  });

  it('shows 50.0% when rate is 50 (red range <70)', () => {
    render(<ResolutionGauge rate={50} />);
    expect(screen.getByText('50.0%')).toBeTruthy();
  });
});
