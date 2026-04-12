import { useEffect, useCallback } from 'react';

/**
 * Hotkey definition: a keyboard shortcut mapped to an action.
 *
 * Modifiers:
 *   ctrl  — Ctrl on Windows/Linux, Cmd on macOS
 *   shift — Shift key
 *   alt   — Alt/Option key
 */
export interface HotkeyDef {
  key: string;               // KeyboardEvent.key (case-insensitive match)
  ctrl?: boolean;
  shift?: boolean;
  alt?: boolean;
  action: () => void;
  /** When true, fires even when focus is in an input/textarea. Default: false. */
  global?: boolean;
}

/**
 * Returns true if the active element is an input-like field (text input, textarea,
 * contenteditable). Used to suppress hotkeys when the user is typing.
 */
function isInputFocused(): boolean {
  const el = document.activeElement;
  if (!el) return false;
  const tag = el.tagName;
  if (tag === 'INPUT' || tag === 'TEXTAREA') return true;
  if ((el as HTMLElement).isContentEditable) return true;
  return false;
}

/**
 * Returns true when the modifier pattern from `def` matches the event.
 * Normalises Ctrl/Cmd across platforms (ctrlKey || metaKey).
 */
function modifiersMatch(e: KeyboardEvent, def: HotkeyDef): boolean {
  const wantCtrl  = !!def.ctrl;
  const wantShift = !!def.shift;
  const wantAlt   = !!def.alt;
  const hasCtrl   = e.ctrlKey || e.metaKey;
  return hasCtrl === wantCtrl && e.shiftKey === wantShift && e.altKey === wantAlt;
}

/**
 * Registers a list of keyboard shortcuts on `document`.
 *
 * Usage (inside a React component):
 * ```ts
 * useHotkeys([
 *   { key: 'Escape', action: () => deselectNode() },
 *   { key: 'k', ctrl: true, action: () => openPalette() },
 * ]);
 * ```
 */
export function useHotkeys(defs: HotkeyDef[]) {
  const handler = useCallback(
    (e: KeyboardEvent) => {
      for (const def of defs) {
        if (e.key.toLowerCase() !== def.key.toLowerCase()) continue;
        if (!modifiersMatch(e, def)) continue;
        // Skip if user is typing in an input (unless marked global)
        if (!def.global && isInputFocused()) continue;
        e.preventDefault();
        def.action();
        return;
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [defs],
  );

  useEffect(() => {
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [handler]);
}
