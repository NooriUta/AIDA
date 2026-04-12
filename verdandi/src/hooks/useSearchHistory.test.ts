import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  getSearchHistory, pushSearchQuery, clearSearchHistory,
  getRecentNodes, pushRecentNode, clearRecentNodes,
  type RecentNode,
} from './useSearchHistory';

// ── In-memory localStorage shim ──────────────────────────────────────────────

function makeStorage() {
  const store = new Map<string, string>();
  return {
    getItem: (k: string) => store.get(k) ?? null,
    setItem: (k: string, v: string) => { store.set(k, v); },
    removeItem: (k: string) => { store.delete(k); },
    clear: () => store.clear(),
  };
}

beforeEach(() => { vi.stubGlobal('localStorage', makeStorage()); });
afterEach(() => { vi.unstubAllGlobals(); });

// ── Search queries ──────────────────────────────────────────────────────────

describe('search history', () => {
  it('getSearchHistory returns [] when empty', () => {
    expect(getSearchHistory()).toEqual([]);
  });

  it('pushSearchQuery adds query to front', () => {
    pushSearchQuery('foo');
    pushSearchQuery('bar');
    expect(getSearchHistory()[0]).toBe('bar');
  });

  it('ignores empty string', () => {
    pushSearchQuery('');
    expect(getSearchHistory()).toEqual([]);
  });

  it('ignores queries shorter than 2 chars', () => {
    pushSearchQuery('a');
    expect(getSearchHistory()).toEqual([]);
  });

  it('deduplicates (moves existing to front)', () => {
    pushSearchQuery('foo');
    pushSearchQuery('bar');
    pushSearchQuery('foo');
    const h = getSearchHistory();
    expect(h[0]).toBe('foo');
    expect(h).toHaveLength(2);
  });

  it('caps at 10 items', () => {
    for (let i = 0; i < 12; i++) pushSearchQuery(`query_${i}`);
    expect(getSearchHistory()).toHaveLength(10);
    expect(getSearchHistory()[0]).toBe('query_11');
  });

  it('clearSearchHistory removes the key', () => {
    pushSearchQuery('foo');
    clearSearchHistory();
    expect(getSearchHistory()).toEqual([]);
  });

  it('returns [] on corrupt JSON', () => {
    localStorage.setItem('seer-search-history', '{broken');
    expect(getSearchHistory()).toEqual([]);
  });
});

// ── Recent nodes ────────────────────────────────────────────────────────────

describe('recent nodes', () => {
  const node = (id: string): RecentNode => ({ id, label: `Node ${id}`, type: 'DaliTable' });

  it('getRecentNodes returns [] when empty', () => {
    expect(getRecentNodes()).toEqual([]);
  });

  it('pushRecentNode adds to front', () => {
    pushRecentNode(node('n1'));
    pushRecentNode(node('n2'));
    expect(getRecentNodes()[0].id).toBe('n2');
  });

  it('deduplicates by id', () => {
    pushRecentNode(node('n1'));
    pushRecentNode(node('n2'));
    pushRecentNode(node('n1'));
    expect(getRecentNodes()).toHaveLength(2);
    expect(getRecentNodes()[0].id).toBe('n1');
  });

  it('caps at 10 items', () => {
    for (let i = 0; i < 12; i++) pushRecentNode(node(`n${i}`));
    expect(getRecentNodes()).toHaveLength(10);
  });

  it('clearRecentNodes removes the key', () => {
    pushRecentNode(node('n1'));
    clearRecentNodes();
    expect(getRecentNodes()).toEqual([]);
  });

  it('returns [] on corrupt JSON', () => {
    localStorage.setItem('seer-recent-nodes', 'not-json');
    expect(getRecentNodes()).toEqual([]);
  });
});
