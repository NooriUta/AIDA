import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MetricsBar } from './MetricsBar';
import type { MetricsSnapshot } from 'aida-shared';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (k: string) => k }),
}));

const SNAPSHOT: MetricsSnapshot = {
  atomsExtracted: 1234,
  filesParsed:    56,
  toolCallsTotal: 78,
  activeWorkers:  4,
  queueDepth:     2,
  resolutionRate: 91.5,
};

describe('MetricsBar', () => {
  it('renders metric values from snapshot', () => {
    render(<MetricsBar metrics={SNAPSHOT} />);
    expect(screen.getByText('1234')).toBeTruthy();
    expect(screen.getByText('56')).toBeTruthy();
    expect(screen.getByText('91.5%')).toBeTruthy();
  });

  it('shows -- for all values when metrics is null', () => {
    render(<MetricsBar metrics={null} />);
    const dashes = screen.getAllByText('--');
    expect(dashes.length).toBeGreaterThanOrEqual(5);
  });

  it('shows na key instead of NaN% when resolutionRate is NaN', () => {
    render(<MetricsBar metrics={{ ...SNAPSHOT, resolutionRate: NaN }} />);
    expect(screen.queryByText(/NaN/)).toBeNull();
    expect(screen.getByText('metrics.na')).toBeTruthy();
  });
});
