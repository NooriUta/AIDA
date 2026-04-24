/**
 * prefsStore tests (R4.8).
 *
 * Tests: load() → state update & apply(), apply() → DOM attributes,
 * error-resilience (network failure, non-ok response).
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { act, renderHook } from '@testing-library/react';

// ── Mock i18n to avoid loading locale bundles in tests ────────────────────────
vi.mock('../i18n/config', () => ({
  default: { changeLanguage: vi.fn().mockResolvedValue(undefined) },
}));

import { usePrefsStore } from './prefsStore';

// ── Default state (matches store initializer) ─────────────────────────────────
const DEFAULT_STATE = {
  lang: 'ru', theme: 'dark', density: 'normal',
  startPage: 'dashboard', avatarColor: '#A8B860', loaded: false,
};

beforeEach(() => {
  // Reset store to defaults before each test (zustand singleton)
  usePrefsStore.setState({ ...DEFAULT_STATE, loaded: false });
  localStorage.clear();
  vi.clearAllMocks();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

// ── Helpers ───────────────────────────────────────────────────────────────────
function mockFetch(body: unknown, ok = true) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok,
    status: ok ? 200 : 503,
    json:   () => Promise.resolve(body),
  }));
}

// ── load() ────────────────────────────────────────────────────────────────────

describe('load()', () => {
  it('updates state from a successful server response', async () => {
    mockFetch({ theme: 'light', density: 'compact', lang: 'en', startPage: 'loom', avatarColor: '#abcdef' });

    const { result } = renderHook(() => usePrefsStore());
    await act(async () => { await result.current.load(); });

    expect(result.current.theme).toBe('light');
    expect(result.current.density).toBe('compact');
    expect(result.current.lang).toBe('en');
    expect(result.current.startPage).toBe('loom');
    expect(result.current.avatarColor).toBe('#abcdef');
    expect(result.current.loaded).toBe(true);
  });

  it('keeps defaults when fetch throws (network error)', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('Network down')));

    const { result } = renderHook(() => usePrefsStore());
    await act(async () => { await result.current.load(); });

    expect(result.current.theme).toBe('dark');
    expect(result.current.lang).toBe('ru');
    expect(result.current.loaded).toBe(false);
  });

  it('keeps defaults when server returns non-ok (503)', async () => {
    mockFetch({}, false);

    const { result } = renderHook(() => usePrefsStore());
    await act(async () => { await result.current.load(); });

    expect(result.current.loaded).toBe(false);
    expect(result.current.theme).toBe('dark');
  });

  it('falls back to current value when a pref field is absent in response', async () => {
    // Server only returns theme; lang/density/etc should keep current defaults
    mockFetch({ theme: 'light' });

    const { result } = renderHook(() => usePrefsStore());
    await act(async () => { await result.current.load(); });

    expect(result.current.theme).toBe('light');
    expect(result.current.lang).toBe('ru');       // default preserved
    expect(result.current.density).toBe('normal'); // default preserved
  });

  it('calls fetch with credentials: include on /admin/me/prefs', async () => {
    const spy = vi.fn().mockResolvedValue({ ok: true, json: () => Promise.resolve({}) });
    vi.stubGlobal('fetch', spy);

    const { result } = renderHook(() => usePrefsStore());
    await act(async () => { await result.current.load(); });

    expect(spy).toHaveBeenCalledWith('/admin/me/prefs', expect.objectContaining({ credentials: 'include' }));
  });
});

// ── apply() ───────────────────────────────────────────────────────────────────

describe('apply()', () => {
  it('sets data-theme on documentElement', () => {
    usePrefsStore.setState({ theme: 'light', density: 'normal' });
    const { result } = renderHook(() => usePrefsStore());
    act(() => { result.current.apply(); });
    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
  });

  it('sets data-density on documentElement', () => {
    usePrefsStore.setState({ theme: 'dark', density: 'compact' });
    const { result } = renderHook(() => usePrefsStore());
    act(() => { result.current.apply(); });
    expect(document.documentElement.getAttribute('data-density')).toBe('compact');
  });

  it('resolves theme="auto" to "dark" when prefers-color-scheme is dark', () => {
    usePrefsStore.setState({ theme: 'auto', density: 'normal' });
    vi.stubGlobal('matchMedia', vi.fn().mockReturnValue({ matches: true }));

    const { result } = renderHook(() => usePrefsStore());
    act(() => { result.current.apply(); });

    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
  });

  it('resolves theme="auto" to "light" when prefers-color-scheme is light', () => {
    usePrefsStore.setState({ theme: 'auto', density: 'normal' });
    vi.stubGlobal('matchMedia', vi.fn().mockReturnValue({ matches: false }));

    const { result } = renderHook(() => usePrefsStore());
    act(() => { result.current.apply(); });

    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
  });
});
