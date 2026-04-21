/**
 * prefsStore — server-side UI preferences sync.
 *
 * Storage model:
 *   FRIGG (server)  — source of truth for cross-device sync
 *   localStorage    — client-side cache, applied immediately on change
 *
 * Fields synced to FRIGG:
 *   theme, palette, density, uiFont, monoFont, fontSize
 *
 * Field kept in Keycloak (not here):
 *   lang  — admin can set it; verdandi reads it from /auth/me on login
 *
 * Flow:
 *   Login  → fetchPrefs() → merge server → localStorage → apply DOM
 *   Change → localStorage (instant) + debounced PUT /prefs (1.5 s)
 */
import { create } from 'zustand';

// ── localStorage key map ─────────────────────────────────────────────────────

const LS: Record<keyof ServerPrefs, string> = {
  theme:    'seer-theme',
  palette:  'seer-palette',
  density:  'seer-density',
  uiFont:   'seer-ui-font',
  monoFont: 'seer-mono-font',
  fontSize: 'seer-font-size',
};

// ── Types ────────────────────────────────────────────────────────────────────

export interface ServerPrefs {
  theme:    string;   // "dark" | "light"
  palette:  string;   // "amber-forest" | ...
  density:  string;   // "normal" | "compact"
  uiFont:   string;   // "inter" | "roboto" | "system"
  monoFont: string;   // "jetbrains" | "fira" | "cascadia"
  fontSize: string;   // "12" .. "16"
}

interface PrefsStore {
  /** True once server prefs have been fetched at least once this session. */
  synced: boolean;
  /** Fetch server prefs after login; merge into localStorage + DOM. */
  fetchPrefs: () => Promise<void>;
  /** Call on any setting change — writes localStorage immediately, debounces PUT. */
  savePrefs: (partial: Partial<ServerPrefs>) => void;
  /** Internal debounce handle. */
  _timer: ReturnType<typeof setTimeout> | null;
}

// ── Defaults ─────────────────────────────────────────────────────────────────

const DEFAULTS: ServerPrefs = {
  theme:    'dark',
  palette:  'amber-forest',
  density:  'normal',
  uiFont:   'inter',
  monoFont: 'jetbrains',
  fontSize: '14',
};

// ── localStorage helpers ─────────────────────────────────────────────────────

function readLS(): ServerPrefs {
  return {
    theme:    localStorage.getItem(LS.theme)    ?? DEFAULTS.theme,
    palette:  localStorage.getItem(LS.palette)  ?? DEFAULTS.palette,
    density:  localStorage.getItem(LS.density)  ?? DEFAULTS.density,
    uiFont:   localStorage.getItem(LS.uiFont)   ?? DEFAULTS.uiFont,
    monoFont: localStorage.getItem(LS.monoFont) ?? DEFAULTS.monoFont,
    fontSize: localStorage.getItem(LS.fontSize) ?? DEFAULTS.fontSize,
  };
}

function writeLS(partial: Partial<ServerPrefs>): void {
  (Object.keys(partial) as (keyof ServerPrefs)[]).forEach((k) => {
    const v = partial[k];
    if (v !== undefined) localStorage.setItem(LS[k], v);
  });
}

// ── DOM application ──────────────────────────────────────────────────────────

export function applyDom(prefs: Partial<ServerPrefs>): void {
  const root = document.documentElement;
  if (prefs.theme)   root.setAttribute('data-theme', prefs.theme);
  if (prefs.density) root.setAttribute('data-density', prefs.density);
  if (prefs.palette !== undefined) {
    if (prefs.palette === 'amber-forest') {
      root.removeAttribute('data-palette');
    } else {
      root.setAttribute('data-palette', prefs.palette);
    }
  }
  if (prefs.uiFont)   root.style.setProperty('--ui-font',   prefs.uiFont);
  if (prefs.monoFont) root.style.setProperty('--mono-font', prefs.monoFont);
  if (prefs.fontSize) root.style.setProperty('--font-size', `${prefs.fontSize}px`);
}

// ── Store ────────────────────────────────────────────────────────────────────

const PREFS_URL = '/prefs';
const DEBOUNCE  = 1500;
const COOKIE    = 'seer-prefs';

function writeCookie(prefs: ServerPrefs): void {
  try {
    const expires = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toUTCString();
    document.cookie = `${COOKIE}=${encodeURIComponent(JSON.stringify(prefs))}; expires=${expires}; path=/; SameSite=Lax`;
  } catch { /* ignore */ }
}

export const usePrefsStore = create<PrefsStore>()((set, get) => ({
  synced: false,
  _timer: null,

  fetchPrefs: async () => {
    try {
      const res = await fetch(PREFS_URL, { credentials: 'include' });
      if (!res.ok) return;
      const server = await res.json() as ServerPrefs;
      // Server wins — overwrite localStorage and apply to DOM
      writeLS(server);
      applyDom(server);
      writeCookie(server);
      set({ synced: true });
    } catch {
      // FRIGG down / offline — localStorage is already applied (verdandi reads it on mount)
    }
  },

  savePrefs: (partial) => {
    // 1. Instant localStorage write + DOM + cookie
    writeLS(partial);
    applyDom(partial);
    writeCookie(readLS());

    // 2. Cross-MF broadcast (same tab — HEIMDALL and others react without reload)
    try {
      window.dispatchEvent(new CustomEvent('aida:prefs', { detail: partial }));
    } catch { /* non-browser env */ }

    // 3. Debounced server sync
    const { _timer } = get();
    if (_timer) clearTimeout(_timer);

    const timer = setTimeout(async () => {
      try {
        const resp = await fetch(PREFS_URL, {
          method:      'PUT',
          credentials: 'include',
          headers:     { 'Content-Type': 'application/json' },
          body:        JSON.stringify(readLS()),
          signal:      AbortSignal.timeout(5_000),
        });
        if (!resp.ok) {
          console.warn(`[prefsStore] PUT /prefs failed: ${resp.status}`);
        }
      } catch (err) {
        console.warn('[prefsStore] sync failed:', (err as Error).message);
      }
      set({ _timer: null });
    }, DEBOUNCE);

    set({ _timer: timer });
  },
}));
