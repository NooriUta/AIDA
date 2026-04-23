import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface PrefsState {
  lang:        string;
  theme:       string;
  density:     string;
  startPage:   string;
  avatarColor: string;
  /** true once prefs have been loaded from the server at least once */
  loaded: boolean;

  /** Fetch prefs from KC via /admin/me/prefs and apply them to the DOM. */
  load:  () => Promise<void>;
  /** Apply current state to DOM (data-theme, data-density, i18n language). */
  apply: () => void;
}

/**
 * Server-side user preferences stored in Keycloak attributes (R4.10 / R4.13).
 *
 * On login: call usePrefsStore.getState().load() to pull KC prefs and apply them.
 * On logout: the zustand-persist cache is cleared by clearing the storage key.
 *
 * Theme / palette / fonts that stay purely local (localStorage, Verdandi-owned):
 *   seer-palette, seer-ui-font, seer-mono-font, seer-font-size
 *
 * Prefs owned by this store (synced server-side):
 *   lang, theme, density, startPage, avatarColor
 */
export const usePrefsStore = create<PrefsState>()(
  persist(
    (set, get) => ({
      lang:        'ru',
      theme:       'dark',
      density:     'normal',
      startPage:   'dashboard',
      avatarColor: '#A8B860',
      loaded:      false,

      load: async () => {
        try {
          const res = await fetch('/admin/me/prefs', { credentials: 'include' });
          if (!res.ok) return;
          const prefs = await res.json() as Record<string, unknown>;
          set({
            lang:        String(prefs.lang        ?? get().lang),
            theme:       String(prefs.theme       ?? get().theme),
            density:     String(prefs.density     ?? get().density),
            startPage:   String(prefs.startPage   ?? get().startPage),
            avatarColor: String(prefs.avatarColor ?? get().avatarColor),
            loaded:      true,
          });
          get().apply();
        } catch {
          // Graceful degradation — keep cached/default values
        }
      },

      apply: () => {
        const { theme, density } = get();
        // Resolve 'auto' at runtime
        const effectiveTheme =
          theme === 'auto'
            ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
            : theme;
        document.documentElement.setAttribute('data-theme',   effectiveTheme);
        document.documentElement.setAttribute('data-density', density);

        // Apply language via i18next (lazy import to avoid circular dep)
        const { lang } = get();
        void import('../i18n/config').then(mod => {
          void mod.default.changeLanguage(lang);
        });
      },
    }),
    {
      name: 'aida-prefs',
      // Don't persist 'loaded' so it re-fetches from server on next session
      partialize: (s) => ({
        lang: s.lang, theme: s.theme, density: s.density,
        startPage: s.startPage, avatarColor: s.avatarColor,
      }),
    },
  ),
);
