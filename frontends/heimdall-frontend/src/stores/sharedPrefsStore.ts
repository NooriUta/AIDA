/**
 * sharedPrefsStore — cross-MF preferences: localStorage + DOM + cookie + CustomEvent + FRIGG.
 * Local copy in heimdall-frontend. Canonical source: packages/aida-shared/src/stores/sharedPrefsStore.ts
 */

export interface SharedPrefs {
  theme:    string;
  palette:  string;
  density:  string;
  uiFont:   string;
  monoFont: string;
  fontSize: string;
}

const LS: Record<keyof SharedPrefs, string> = {
  theme:    'seer-theme',
  palette:  'seer-palette',
  density:  'seer-density',
  uiFont:   'seer-ui-font',
  monoFont: 'seer-mono-font',
  fontSize: 'seer-font-size',
};

const DEFAULTS: SharedPrefs = {
  theme:    'dark',
  palette:  'amber-forest',
  density:  'normal',
  uiFont:   'inter',
  monoFont: 'jetbrains',
  fontSize: '14',
};

const PREFS_URL = '/prefs';
const DEBOUNCE  = 1500;
const COOKIE    = 'seer-prefs';

let _timer: ReturnType<typeof setTimeout> | null = null;

export function applyPrefs(p: Partial<SharedPrefs>): void {
  const root = document.documentElement;
  if (p.theme)   root.setAttribute('data-theme', p.theme);
  if (p.density) root.setAttribute('data-density', p.density);
  if (p.palette !== undefined) {
    if (p.palette === 'amber-forest') {
      root.removeAttribute('data-palette');
    } else {
      root.setAttribute('data-palette', p.palette);
    }
  }
  if (p.uiFont)   root.style.setProperty('--ui-font',   p.uiFont);
  if (p.monoFont) root.style.setProperty('--mono-font', p.monoFont);
  if (p.fontSize) root.style.setProperty('--font-size', `${p.fontSize}px`);
}

function writeCookie(prefs: SharedPrefs): void {
  try {
    const expires = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toUTCString();
    document.cookie = `${COOKIE}=${encodeURIComponent(JSON.stringify(prefs))}; expires=${expires}; path=/; SameSite=Lax`;
  } catch { /* ignore */ }
}

function readCookie(): Partial<SharedPrefs> | null {
  try {
    const m = document.cookie.match(new RegExp(`(?:^|; )${COOKIE}=([^;]*)`));
    return m ? JSON.parse(decodeURIComponent(m[1])) as Partial<SharedPrefs> : null;
  } catch { return null; }
}

function readLS(): SharedPrefs {
  return {
    theme:    localStorage.getItem(LS.theme)    ?? DEFAULTS.theme,
    palette:  localStorage.getItem(LS.palette)  ?? DEFAULTS.palette,
    density:  localStorage.getItem(LS.density)  ?? DEFAULTS.density,
    uiFont:   localStorage.getItem(LS.uiFont)   ?? DEFAULTS.uiFont,
    monoFont: localStorage.getItem(LS.monoFont) ?? DEFAULTS.monoFont,
    fontSize: localStorage.getItem(LS.fontSize) ?? DEFAULTS.fontSize,
  };
}

function writeLS(partial: Partial<SharedPrefs>): void {
  (Object.keys(partial) as (keyof SharedPrefs)[]).forEach(k => {
    const v = partial[k];
    if (v !== undefined) localStorage.setItem(LS[k], v);
  });
}

export function initSharedPrefs(): void {
  const cookie = readCookie();
  if (cookie) writeLS(cookie);
  applyPrefs(readLS());
}

export const sharedPrefsStore = {
  getPrefs(): SharedPrefs { return readLS(); },

  savePrefs(partial: Partial<SharedPrefs>): void {
    writeLS(partial);
    applyPrefs(partial);
    writeCookie(readLS());
    try {
      window.dispatchEvent(new CustomEvent('aida:prefs', { detail: partial }));
    } catch { /* non-browser env */ }
    if (_timer) clearTimeout(_timer);
    _timer = setTimeout(async () => {
      try {
        const resp = await fetch(PREFS_URL, {
          method: 'PUT', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(readLS()),
          signal: AbortSignal.timeout(5_000),
        });
        if (!resp.ok) console.warn(`[sharedPrefsStore] PUT /prefs failed: ${resp.status}`);
      } catch (err) {
        console.warn('[sharedPrefsStore] sync failed:', (err as Error).message);
      }
      _timer = null;
    }, DEBOUNCE);
  },

  async fetchPrefs(): Promise<void> {
    try {
      const res = await fetch(PREFS_URL, { credentials: 'include' });
      if (!res.ok) return;
      const server = await res.json() as SharedPrefs;
      writeLS(server);
      applyPrefs(server);
      writeCookie(readLS());
    } catch { /* FRIGG down */ }
  },
};
