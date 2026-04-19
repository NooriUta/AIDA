import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ThroughputChart } from './ThroughputChart';
import type { HeimdallEvent } from 'aida-shared';

vi.mock('@nivo/line', () => ({
  ResponsiveLine: () => <div data-testid="responsive-line" />,
}));

vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (k: string) => k }),
}));

function makeEvent(sourceComponent: string): HeimdallEvent {
  return {
    timestamp:       Date.now() - 1000,
    sourceComponent,
    eventType:       'PARSE_COMPLETE',
    level:           'INFO',
    sessionId:       's1',
    userId:          'u1',
    correlationId:   'c1',
    durationMs:      10,
    payload:         {},
  };
}

describe('ThroughputChart', () => {
  it('shows waiting text when events is empty', () => {
    render(<ThroughputChart events={[]} />);
    expect(screen.getByText('metrics.waiting')).toBeTruthy();
  });

  it('renders chart when events are present', () => {
    const events = [makeEvent('hound'), makeEvent('dali'), makeEvent('mimir')];
    render(<ThroughputChart events={events} />);
    expect(screen.getByTestId('responsive-line')).toBeTruthy();
  });
});
