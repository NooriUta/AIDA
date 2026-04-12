/**
 * Search history & recent nodes — localStorage persistence.
 *
 * - Last 10 search queries   → `seer-search-history`
 * - Last 10 selected nodes   → `seer-recent-nodes`
 *
 * Pure functions (no hooks) — import in SearchPalette / SearchPanel.
 */

const SEARCH_HISTORY_KEY = 'seer-search-history';
const RECENT_NODES_KEY   = 'seer-recent-nodes';
const MAX_ITEMS = 10;

// ── Types ──────────────────────────────────────────────────────────────────────

export interface RecentNode {
  id:     string;
  label:  string;
  type:   string;
  scope?: string;
}

// ── Helpers ────────────────────────────────────────────────────────────────────

function readJson<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
}

// ── Search queries ─────────────────────────────────────────────────────────────

export function getSearchHistory(): string[] {
  return readJson<string[]>(SEARCH_HISTORY_KEY, []);
}

export function pushSearchQuery(query: string): void {
  if (!query || query.length < 2) return;
  const list = getSearchHistory().filter((q) => q !== query);
  list.unshift(query);
  if (list.length > MAX_ITEMS) list.length = MAX_ITEMS;
  localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(list));
}

export function clearSearchHistory(): void {
  localStorage.removeItem(SEARCH_HISTORY_KEY);
}

// ── Recent nodes ───────────────────────────────────────────────────────────────

export function getRecentNodes(): RecentNode[] {
  return readJson<RecentNode[]>(RECENT_NODES_KEY, []);
}

export function pushRecentNode(node: RecentNode): void {
  const list = getRecentNodes().filter((n) => n.id !== node.id);
  list.unshift(node);
  if (list.length > MAX_ITEMS) list.length = MAX_ITEMS;
  localStorage.setItem(RECENT_NODES_KEY, JSON.stringify(list));
}

export function clearRecentNodes(): void {
  localStorage.removeItem(RECENT_NODES_KEY);
}
