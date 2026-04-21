import { useEffect, useCallback } from 'react';

export interface HotkeyDef {
  key: string;
  ctrl?: boolean;
  shift?: boolean;
  alt?: boolean;
  action: () => void;
  /** When true, fires even when focus is in an input/textarea. Default: false. */
  global?: boolean;
}

function isInputFocused(): boolean {
  const el = document.activeElement;
  if (!el) return false;
  const tag = el.tagName;
  if (tag === 'INPUT' || tag === 'TEXTAREA') return true;
  if ((el as HTMLElement).isContentEditable) return true;
  return false;
}

function modifiersMatch(e: KeyboardEvent, def: HotkeyDef): boolean {
  const hasCtrl = e.ctrlKey || e.metaKey;
  return hasCtrl === !!def.ctrl && e.shiftKey === !!def.shift && e.altKey === !!def.alt;
}

export function useHotkeys(defs: HotkeyDef[]) {
  const handler = useCallback(
    (e: KeyboardEvent) => {
      for (const def of defs) {
        if (e.key.toLowerCase() !== def.key.toLowerCase()) continue;
        if (!modifiersMatch(e, def)) continue;
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
