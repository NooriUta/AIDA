import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useControl } from './useControl';

// HEIMDALL_API defaults to '' in Docker (VITE_HEIMDALL_API=/heimdall baked at build time,
// but in tests import.meta.env is empty so api.ts returns '').
const HEIMDALL_API = '';

function mockFetch(status: number, body: unknown) {
  return vi.fn().mockResolvedValue({
    ok:   status < 400,
    status,
    json: () => Promise.resolve(body),
  });
}

beforeEach(() => {
  vi.stubGlobal('fetch', mockFetch(200, {}));
});
afterEach(() => {
  vi.unstubAllGlobals();
});

describe('resetBuffer', () => {
  it('POSTs to /control/reset with X-Seer-Role: admin', async () => {
    const { result } = renderHook(() => useControl());
    let ok: boolean;
    await act(async () => { ok = await result.current.resetBuffer(); });
    expect(ok!).toBe(true);
    expect(fetch).toHaveBeenCalledWith(
      `${HEIMDALL_API}/control/reset`,
      expect.objectContaining({ method: 'POST', headers: expect.objectContaining({ 'X-Seer-Role': 'admin' }) }),
    );
  });

  it('returns false and sets error on network failure', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('Network error')));
    const { result } = renderHook(() => useControl());
    let ok: boolean;
    await act(async () => { ok = await result.current.resetBuffer(); });
    expect(ok!).toBe(false);
    expect(result.current.error).toBe('Network error');
  });
});

describe('saveSnapshot', () => {
  it('POSTs to /control/snapshot?name=<name>', async () => {
    const { result } = renderHook(() => useControl());
    let ok: boolean;
    await act(async () => { ok = await result.current.saveSnapshot('my-snap'); });
    expect(ok!).toBe(true);
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/control/snapshot?name=my-snap'),
      expect.objectContaining({ method: 'POST' }),
    );
  });

  it('returns false without fetching when name is empty', async () => {
    const { result } = renderHook(() => useControl());
    let ok: boolean;
    await act(async () => { ok = await result.current.saveSnapshot(''); });
    expect(ok!).toBe(false);
    expect(fetch).not.toHaveBeenCalled();
  });
});

describe('listSnapshots', () => {
  it('GETs /control/snapshots and returns parsed array', async () => {
    const snapshots = [{ id: 'abc', name: 'test', eventCount: 10, timestamp: Date.now() }];
    vi.stubGlobal('fetch', mockFetch(200, snapshots));
    const { result } = renderHook(() => useControl());
    let list: typeof snapshots;
    await act(async () => { list = await result.current.listSnapshots(); });
    expect(list!).toEqual(snapshots);
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/control/snapshots'),
      expect.any(Object),
    );
  });
});

describe('deleteSnapshot', () => {
  it('DELETEs /control/snapshot/<id>', async () => {
    const { result } = renderHook(() => useControl());
    let ok: boolean;
    await act(async () => { ok = await result.current.deleteSnapshot('snap-123'); });
    expect(ok!).toBe(true);
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining('/control/snapshot/snap-123'),
      expect.objectContaining({ method: 'DELETE' }),
    );
  });
});
