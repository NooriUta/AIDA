import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { EventLog, formatTime, formatFullTimestamp } from './EventLog';

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

describe('formatTime — local TZ HH:mm:ss.mmm', () => {
  it('formats epoch timestamp as zero-padded local time', () => {
    const d = new Date(2026, 4, 1, 15, 9, 7, 42);
    expect(formatTime(d.getTime())).toBe('15:09:07.042');
  });

  it('zero-pads single-digit hours/minutes/seconds and 3-digit ms', () => {
    const d = new Date(2026, 0, 1, 1, 2, 3, 4);
    expect(formatTime(d.getTime())).toBe('01:02:03.004');
  });

  it('handles end-of-day boundary', () => {
    const d = new Date(2026, 0, 1, 23, 59, 59, 999);
    expect(formatTime(d.getTime())).toBe('23:59:59.999');
  });
});

describe('formatFullTimestamp — local ISO with TZ offset', () => {
  it('emits date + local time + signed UTC offset', () => {
    const d = new Date(2026, 4, 1, 15, 9, 7, 42);
    expect(formatFullTimestamp(d.getTime()))
      .toMatch(/^2026-05-01T15:09:07\.042[+-]\d{2}:\d{2}$/);
  });

  it('zero-pads month and day', () => {
    const d = new Date(2026, 0, 5, 0, 0, 0, 0);
    expect(formatFullTimestamp(d.getTime()))
      .toMatch(/^2026-01-05T00:00:00\.000[+-]\d{2}:\d{2}$/);
  });

  it('uses local TZ — never returns UTC Z suffix', () => {
    const out = formatFullTimestamp(Date.now());
    expect(out).not.toMatch(/Z$/);
    expect(out).toMatch(/[+-]\d{2}:\d{2}$/);
  });
});
