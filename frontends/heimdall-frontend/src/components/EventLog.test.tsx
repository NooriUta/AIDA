import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { EventLog } from './EventLog';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (k: string) => k }),
}));

vi.mock('react-virtuoso', () => ({
  Virtuoso: () => <div data-testid="virtuoso" />,
}));

describe('EventLog', () => {
  it('renders without crash when events is empty', () => {
    render(<EventLog events={[]} />);
    expect(screen.getByText('eventLog.noEvents')).toBeTruthy();
  });

  it('shows disconnected status when connected is false', () => {
    render(<EventLog events={[]} connected={false} />);
    expect(screen.getByText('ws.closed')).toBeTruthy();
  });
});
