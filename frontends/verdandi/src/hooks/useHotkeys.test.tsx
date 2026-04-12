// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useHotkeys, type HotkeyDef } from './useHotkeys';

// ── Helpers ──────────────────────────────────────────────────────────────────

function fire(key: string, opts: Partial<KeyboardEventInit> = {}) {
  const e = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true, ...opts });
  document.dispatchEvent(e);
  return e;
}

afterEach(() => { vi.restoreAllMocks(); });

// ── Tests ────────────────────────────────────────────────────────────────────

describe('useHotkeys', () => {
  it('fires action on matching keydown', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'k', action }]));
    fire('k');
    expect(action).toHaveBeenCalledOnce();
  });

  it('matches key case-insensitively', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'k', action }]));
    fire('K');
    expect(action).toHaveBeenCalledOnce();
  });

  it('does not fire when key does not match', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'k', action }]));
    fire('j');
    expect(action).not.toHaveBeenCalled();
  });

  it('fires with ctrl modifier', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'k', ctrl: true, action }]));
    fire('k', { ctrlKey: true });
    expect(action).toHaveBeenCalledOnce();
  });

  it('fires with metaKey when ctrl is required (Mac Cmd)', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'k', ctrl: true, action }]));
    fire('k', { metaKey: true });
    expect(action).toHaveBeenCalledOnce();
  });

  it('does not fire when required modifier is missing', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'k', ctrl: true, action }]));
    fire('k'); // no ctrl
    expect(action).not.toHaveBeenCalled();
  });

  it('skips action when input is focused', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'k', action }]));
    const input = document.createElement('input');
    document.body.appendChild(input);
    input.focus();
    fire('k');
    expect(action).not.toHaveBeenCalled();
    document.body.removeChild(input);
  });

  it('fires despite input focus when global=true', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'k', action, global: true }]));
    const input = document.createElement('input');
    document.body.appendChild(input);
    input.focus();
    fire('k');
    expect(action).toHaveBeenCalledOnce();
    document.body.removeChild(input);
  });

  it('calls preventDefault on match', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'k', action }]));
    const e = fire('k');
    expect(e.defaultPrevented).toBe(true);
  });

  it('only first matching def fires', () => {
    const a1 = vi.fn();
    const a2 = vi.fn();
    const defs: HotkeyDef[] = [
      { key: 'k', action: a1 },
      { key: 'k', action: a2 },
    ];
    renderHook(() => useHotkeys(defs));
    fire('k');
    expect(a1).toHaveBeenCalledOnce();
    expect(a2).not.toHaveBeenCalled();
  });

  it('cleans up listener on unmount', () => {
    const action = vi.fn();
    const { unmount } = renderHook(() => useHotkeys([{ key: 'k', action }]));
    unmount();
    fire('k');
    expect(action).not.toHaveBeenCalled();
  });

  it('handles shift+alt modifiers', () => {
    const action = vi.fn();
    renderHook(() => useHotkeys([{ key: 'z', shift: true, alt: true, action }]));
    fire('z', { shiftKey: true, altKey: true });
    expect(action).toHaveBeenCalledOnce();
  });
});
