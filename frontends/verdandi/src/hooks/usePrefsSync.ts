import { useEffect, useRef } from 'react';

/**
 * Verdandi preference sync hook (R4.14).
 *
 * ON LOGIN (mount):
 *   Fetches /admin/me/prefs from Chur → KC attributes.
 *   Restores: theme, palette, uiFont, monoFont, fontSize, density, verdandiGraphPrefs.
 *
 * ON CHANGE (localStorage storage event, debounced 1500ms):
 *   Saves changed localStorage keys back to KC via /admin/me/prefs PUT.
 *
 * Integration:
 *   Call `usePrefsSync()` once inside the authenticated root component (App.tsx).
 *   ProfileTabAppearance / ProfileTabGraph must dispatch a StorageEvent after
 *   writing to localStorage so the debounced handler triggers.
 */
export function usePrefsSync() {
  const saveTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  // ── RESTORE on mount (login) ──────────────────────────────────────────────
  useEffect(() => {
    void (async () => {
      try {
        const res = await fetch('/admin/me/prefs', { credentials: 'include' });
        if (!res.ok) return;
        const prefs = await res.json() as Record<string, unknown>;

        const str = (v: unknown) => (v != null && v !== '' ? String(v) : null);

        // Theme
        const theme = str(prefs.theme);
        if (theme) {
          localStorage.setItem('seer-theme', theme);
          document.documentElement.setAttribute(
            'data-theme',
            theme === 'auto'
              ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
              : theme,
          );
        }

        // Density
        const density = str(prefs.density);
        if (density) {
          localStorage.setItem('seer-density', density);
          document.documentElement.setAttribute('data-density', density);
        }

        // Verdandi-specific prefs
        applyIfPresent(prefs, 'verdandiPalette',  'seer-palette',    v => {
          document.documentElement.setAttribute('data-palette', v);
        });
        applyIfPresent(prefs, 'verdandiUiFont',   'seer-ui-font');
        applyIfPresent(prefs, 'verdandiMonoFont',  'seer-mono-font');
        applyIfPresent(prefs, 'verdandiFontSize',  'seer-font-size');
        applyIfPresent(prefs, 'verdandiGraphPrefs','seer-graph-prefs');
      } catch {
        // Graceful degradation — keep localStorage values
      }
    })();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // ── SAVE on localStorage change (debounce 1500ms) ─────────────────────────
  useEffect(() => {
    const handler = () => {
      if (saveTimer.current) clearTimeout(saveTimer.current);
      saveTimer.current = setTimeout(() => {
        void fetch('/admin/me/prefs', {
          method:      'PUT',
          credentials: 'include',
          headers:     { 'Content-Type': 'application/json' },
          body:        JSON.stringify(buildPrefsPayload()),
        });
      }, 1500);
    };

    window.addEventListener('storage', handler);
    return () => {
      window.removeEventListener('storage', handler);
      if (saveTimer.current) clearTimeout(saveTimer.current);
    };
  }, []);
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function applyIfPresent(
  prefs:    Record<string, unknown>,
  prefsKey: string,
  lsKey:    string,
  apply?:   (val: string) => void,
) {
  const v = prefs[prefsKey];
  if (v != null && v !== '') {
    const s = String(v);
    localStorage.setItem(lsKey, s);
    apply?.(s);
  }
}

function buildPrefsPayload(): Record<string, string | null> {
  return {
    theme:            localStorage.getItem('seer-theme')      ?? 'dark',
    density:          localStorage.getItem('seer-density')    ?? 'compact',
    verdandiPalette:  localStorage.getItem('seer-palette')    ?? 'amber-forest',
    verdandiUiFont:   localStorage.getItem('seer-ui-font')    ?? 'Manrope',
    verdandiMonoFont: localStorage.getItem('seer-mono-font')  ?? 'IBM Plex Mono',
    verdandiFontSize: localStorage.getItem('seer-font-size')  ?? '13',
    verdandiGraphPrefs: localStorage.getItem('seer-graph-prefs'),
  };
}
