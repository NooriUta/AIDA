import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useEventStream } from './useEventStream';

class MockWebSocket {
  static instances: MockWebSocket[] = [];

  readyState = 0;
  onopen:    ((ev: Event) => void) | null = null;
  onclose:   ((ev: CloseEvent) => void) | null = null;
  onmessage: ((ev: MessageEvent) => void) | null = null;
  onerror:   ((ev: Event) => void) | null = null;
  close = vi.fn();

  constructor(public url: string) {
    MockWebSocket.instances.push(this);
  }

  static latest() { return MockWebSocket.instances[MockWebSocket.instances.length - 1]; }
  static reset()  { MockWebSocket.instances = []; }
}

beforeEach(() => {
  MockWebSocket.reset();
  vi.stubGlobal('WebSocket', MockWebSocket);
});

afterEach(() => {
  vi.unstubAllGlobals();
  vi.useRealTimers();
});

describe('useEventStream', () => {
  // INV-1: reconnect fires after ws.onclose
  it('schedules reconnect 3 s after WS closes', async () => {
    vi.useFakeTimers();
    const { unmount } = renderHook(() => useEventStream());

    const ws1 = MockWebSocket.latest();
    expect(MockWebSocket.instances).toHaveLength(1);

    await act(async () => { ws1.onclose?.({} as CloseEvent); });
    expect(MockWebSocket.instances).toHaveLength(1); // not yet

    await act(async () => { vi.advanceTimersByTime(3000); });
    expect(MockWebSocket.instances).toHaveLength(2); // reconnected

    unmount();
  });

  // INV-2: clearTimeout fires on unmount — no reconnect after unmount
  it('cancels pending reconnect on unmount', async () => {
    vi.useFakeTimers();
    const { unmount } = renderHook(() => useEventStream());

    const ws1 = MockWebSocket.latest();
    await act(async () => { ws1.onclose?.({} as CloseEvent); }); // schedule reconnect

    unmount(); // clearTimeout called here

    await act(async () => { vi.advanceTimersByTime(3000); });
    expect(MockWebSocket.instances).toHaveLength(1); // still only original ws
  });

  // INV-3: mountedRef guard — onclose after unmount does not trigger reconnect
  it('mountedRef guard prevents reconnect after unmount', async () => {
    vi.useFakeTimers();
    const { unmount } = renderHook(() => useEventStream());

    const ws1 = MockWebSocket.latest();
    unmount(); // mountedRef.current = false

    // Simulate browser calling onclose on already-closed ws
    await act(async () => { ws1.onclose?.({} as CloseEvent); });
    await act(async () => { vi.advanceTimersByTime(3000); });

    expect(MockWebSocket.instances).toHaveLength(1); // no reconnect
  });

  // INV-4: ws.close() called on unmount (no leak)
  it('calls ws.close() on unmount', () => {
    const { unmount } = renderHook(() => useEventStream());
    const ws = MockWebSocket.latest();
    unmount();
    expect(ws.close).toHaveBeenCalledTimes(1);
  });
});
