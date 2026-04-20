import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useMetrics } from './useMetrics';
import type { MetricsSnapshot } from 'aida-shared';

const SNAPSHOT: MetricsSnapshot = {
  atomsExtracted: 42,
  filesParsed:    7,
  toolCallsTotal: 3,
  activeWorkers:  2,
  queueDepth:     0,
  resolutionRate: 95.5,
};

function mockFetch(data: unknown) {
  return vi.fn().mockResolvedValue({
    ok:   true,
    status: 200,
    json: () => Promise.resolve(data),
  });
}

beforeEach(() => {
  vi.useFakeTimers();
  vi.stubGlobal('fetch', mockFetch(SNAPSHOT));
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('useMetrics', () => {
  it('clears interval on unmount', () => {
    const spy = vi.spyOn(globalThis, 'clearInterval');
    const { unmount } = renderHook(() => useMetrics());
    unmount();
    expect(spy).toHaveBeenCalledTimes(1);
    spy.mockRestore();
  });

  it('populates metrics after successful fetch', async () => {
    const { result } = renderHook(() => useMetrics());
    await act(async () => { await vi.advanceTimersByTimeAsync(0); });
    expect(result.current.metrics?.atomsExtracted).toBe(42);
    expect(result.current.error).toBeNull();
  });

  it('sets error state on fetch failure', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('Network down')));
    const { result } = renderHook(() => useMetrics());
    await act(async () => { await vi.advanceTimersByTimeAsync(0); });
    expect(result.current.error).toBe('Network down');
    expect(result.current.metrics).toBeNull();
  });
});
