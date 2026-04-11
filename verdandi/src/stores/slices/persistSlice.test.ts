import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { hydratePersistedState, subscribePersist } from './persistSlice';

// ── In-memory localStorage shim ──────────────────────────────────────────────

function makeStorage() {
  const store = new Map<string, string>();
  return {
    getItem: (k: string) => store.get(k) ?? null,
    setItem: (k: string, v: string) => { store.set(k, v); },
    removeItem: (k: string) => { store.delete(k); },
    clear: () => store.clear(),
    _store: store,
  };
}

let storage: ReturnType<typeof makeStorage>;

beforeEach(() => {
  storage = makeStorage();
  vi.stubGlobal('localStorage', storage);
});
afterEach(() => { vi.unstubAllGlobals(); });

// ── hydratePersistedState ────────────────────────────────────────────────────

describe('hydratePersistedState', () => {
  it('returns {} when localStorage is empty', () => {
    expect(hydratePersistedState()).toEqual({});
  });

  it('returns {} on corrupt JSON', () => {
    storage.setItem('seer-loom-filters', '{broken');
    expect(hydratePersistedState()).toEqual({});
  });

  it('parses valid stored object', () => {
    storage.setItem('seer-loom-filters', JSON.stringify({
      viewLevel: 'L2',
      currentScope: 'schema-public',
      currentScopeLabel: 'public',
      filter: {
        startObjectId: 'n1',
        startObjectType: 'DaliSchema',
        startObjectLabel: 'public',
        depth: 3,
        upstream: false,
        downstream: true,
        tableLevelView: true,
        showCfEdges: false,
      },
    }));
    const result = hydratePersistedState();
    expect(result.viewLevel).toBe('L2');
    expect(result.currentScope).toBe('schema-public');
    expect(result.filter?.depth).toBe(3);
    expect(result.filter?.upstream).toBe(false);
    expect(result.filter?.tableLevelView).toBe(true);
  });

  it('applies defaults for missing nested fields', () => {
    storage.setItem('seer-loom-filters', JSON.stringify({
      viewLevel: 'L2',
      filter: {},
    }));
    const result = hydratePersistedState();
    expect(result.filter?.depth).toBe(5);
    expect(result.filter?.upstream).toBe(true);
    expect(result.filter?.downstream).toBe(true);
    expect(result.filter?.tableLevelView).toBe(false);
    expect(result.filter?.showCfEdges).toBe(true);
  });

  it('nulls out tableFilter/stmtFilter/fieldFilter', () => {
    storage.setItem('seer-loom-filters', JSON.stringify({
      viewLevel: 'L2',
      filter: { depth: 3 },
    }));
    const result = hydratePersistedState();
    expect(result.filter?.tableFilter).toBeNull();
    expect(result.filter?.stmtFilter).toBeNull();
    expect(result.filter?.fieldFilter).toBeNull();
  });
});

// ── subscribePersist ─────────────────────────────────────────────────────────

describe('subscribePersist', () => {
  it('writes to localStorage after debounce', () => {
    vi.useFakeTimers();
    const listeners: (() => void)[] = [];
    const mockStore = {
      getState: () => ({
        viewLevel: 'L2',
        currentScope: 'test',
        currentScopeLabel: 'Test',
        filter: {
          startObjectId: 'n1', startObjectType: 'DaliTable', startObjectLabel: 'tbl',
          depth: 5, upstream: true, downstream: true, tableLevelView: false, showCfEdges: true,
        },
      }),
      subscribe: (fn: () => void) => { listeners.push(fn); return () => {}; },
    };
    subscribePersist(mockStore as any);
    // Trigger subscriber
    listeners[0]();
    // Not yet written (debounce)
    expect(storage.getItem('seer-loom-filters')).toBeNull();
    // Advance past debounce
    vi.advanceTimersByTime(500);
    expect(storage.getItem('seer-loom-filters')).not.toBeNull();
    const saved = JSON.parse(storage.getItem('seer-loom-filters')!);
    expect(saved.viewLevel).toBe('L2');
    expect(saved.filter.depth).toBe(5);
    vi.useRealTimers();
  });

  it('debounces rapid writes', () => {
    vi.useFakeTimers();
    let callCount = 0;
    const listeners: (() => void)[] = [];
    const mockStore = {
      getState: () => { callCount++; return {
        viewLevel: 'L1', currentScope: null, currentScopeLabel: null,
        filter: { startObjectId: null, startObjectType: null, startObjectLabel: null,
          depth: 5, upstream: true, downstream: true, tableLevelView: false, showCfEdges: true },
      }; },
      subscribe: (fn: () => void) => { listeners.push(fn); return () => {}; },
    };
    subscribePersist(mockStore as any);
    // Fire 5 rapid changes
    for (let i = 0; i < 5; i++) { listeners[0](); vi.advanceTimersByTime(100); }
    vi.advanceTimersByTime(500);
    // getState called fewer times than 5 writes would require
    expect(callCount).toBeLessThanOrEqual(2);
    vi.useRealTimers();
  });

  it('survives localStorage.setItem throwing', () => {
    vi.useFakeTimers();
    const listeners: (() => void)[] = [];
    const mockStore = {
      getState: () => ({
        viewLevel: 'L1', currentScope: null, currentScopeLabel: null,
        filter: { startObjectId: null, startObjectType: null, startObjectLabel: null,
          depth: 5, upstream: true, downstream: true, tableLevelView: false, showCfEdges: true },
      }),
      subscribe: (fn: () => void) => { listeners.push(fn); return () => {}; },
    };
    storage.setItem = () => { throw new Error('QuotaExceeded'); };
    subscribePersist(mockStore as any);
    listeners[0]();
    // Should not throw
    expect(() => vi.advanceTimersByTime(500)).not.toThrow();
    vi.useRealTimers();
  });

  it('uses seer-loom-filters as storage key', () => {
    vi.useFakeTimers();
    const listeners: (() => void)[] = [];
    const mockStore = {
      getState: () => ({
        viewLevel: 'L1', currentScope: null, currentScopeLabel: null,
        filter: { startObjectId: null, startObjectType: null, startObjectLabel: null,
          depth: 5, upstream: true, downstream: true, tableLevelView: false, showCfEdges: true },
      }),
      subscribe: (fn: () => void) => { listeners.push(fn); return () => {}; },
    };
    subscribePersist(mockStore as any);
    listeners[0]();
    vi.advanceTimersByTime(500);
    expect(storage._store.has('seer-loom-filters')).toBe(true);
    vi.useRealTimers();
  });
});

// ── Round-trip ───────────────────────────────────────────────────────────────

describe('round-trip', () => {
  it('subscribe writes → hydrate reads → values match', () => {
    vi.useFakeTimers();
    const listeners: (() => void)[] = [];
    const mockStore = {
      getState: () => ({
        viewLevel: 'L2', currentScope: 'schema-pub', currentScopeLabel: 'pub',
        filter: {
          startObjectId: 'n5', startObjectType: 'DaliSchema', startObjectLabel: 'pub',
          depth: 7, upstream: false, downstream: true, tableLevelView: true, showCfEdges: false,
        },
      }),
      subscribe: (fn: () => void) => { listeners.push(fn); return () => {}; },
    };
    subscribePersist(mockStore as any);
    listeners[0]();
    vi.advanceTimersByTime(500);

    const hydrated = hydratePersistedState();
    expect(hydrated.viewLevel).toBe('L2');
    expect(hydrated.currentScope).toBe('schema-pub');
    expect(hydrated.filter?.depth).toBe(7);
    expect(hydrated.filter?.upstream).toBe(false);
    expect(hydrated.filter?.tableLevelView).toBe(true);
    expect(hydrated.filter?.showCfEdges).toBe(false);
    vi.useRealTimers();
  });
});
