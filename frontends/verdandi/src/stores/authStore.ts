import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { usePrefsStore } from './prefsStore';

export interface AuthUser {
  id: string;
  username: string;
  role: 'viewer' | 'editor' | 'admin';
}

interface AuthStore {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  checkSession: () => Promise<void>;
  clearError: () => void;
  /** Silently renew the JWT if the user is authenticated. */
  refreshToken: () => Promise<void>;
}

// ── Silent token refresh interval (30 minutes) ───────────────────────────────
const REFRESH_INTERVAL = 30 * 60 * 1000;
let refreshTimer: ReturnType<typeof setInterval> | null = null;

function startRefreshTimer(refreshFn: () => Promise<void>) {
  stopRefreshTimer();
  refreshTimer = setInterval(refreshFn, REFRESH_INTERVAL);
}

function stopRefreshTimer() {
  if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null; }
}

const AUTH_BASE = import.meta.env.VITE_AUTH_URL ?? '/auth';

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // ── login ──────────────────────────���───────────────────────���────────────
      login: async (username, password) => {
        set({ isLoading: true, error: null });
        try {
          const res = await fetch(`${AUTH_BASE}/login`, {
            method: 'POST',
            credentials: 'include',           // receive httpOnly cookie
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
          });

          if (res.status === 401) {
            set({ isLoading: false, error: 'auth.error.invalid' });
            return;
          }
          if (res.status === 429) {
            set({ isLoading: false, error: 'auth.error.rateLimit' });
            return;
          }
          if (!res.ok) {
            set({ isLoading: false, error: 'auth.error.server' });
            return;
          }

          const user: AuthUser = await res.json();
          set({ user, isAuthenticated: true, isLoading: false, error: null });
          startRefreshTimer(() => get().refreshToken());
          // Fetch server prefs from FRIGG and merge into localStorage/DOM
          usePrefsStore.getState().fetchPrefs().catch(() => {});
        } catch {
          set({ isLoading: false, error: 'auth.error.network' });
        }
      },

      // ── logout — fire-and-forget, clears cookie server-side ─────────────────
      logout: () => {
        stopRefreshTimer();
        set({ user: null, isAuthenticated: false, error: null });
        fetch(`${AUTH_BASE}/logout`, {
          method: 'POST',
          credentials: 'include',
        }).catch(() => {});
      },

      // ── checkSession — verifies the httpOnly cookie is still valid ───────────
      // Call on app mount when sessionStorage says isAuthenticated=true.
      // If the 8h token has expired, clears state so ProtectedRoute redirects.
      checkSession: async () => {
        if (!get().isAuthenticated) return;
        try {
          const res = await fetch(`${AUTH_BASE}/me`, {
            credentials: 'include',
          });
          if (res.status === 401) {
            stopRefreshTimer();
            set({ user: null, isAuthenticated: false });
          } else {
            // Session still valid — start silent refresh cycle.
            startRefreshTimer(() => get().refreshToken());
            // Hydrate prefs if not yet synced (e.g. page reload)
            if (!usePrefsStore.getState().synced) {
              usePrefsStore.getState().fetchPrefs().catch(() => {});
            }
          }
        } catch {
          // Network down — keep existing state, will fail on next API call
        }
      },

      // ── refreshToken — silently renew JWT before expiry ────────────────────
      refreshToken: async () => {
        if (!get().isAuthenticated) return;
        try {
          const res = await fetch(`${AUTH_BASE}/refresh`, {
            method: 'POST',
            credentials: 'include',
          });
          if (res.status === 401) {
            // Token already expired — force re-login.
            stopRefreshTimer();
            set({ user: null, isAuthenticated: false });
          }
          // 200 OK: server set a fresh cookie, nothing to update in state.
        } catch {
          // Network error — will retry on next interval.
        }
      },

      clearError: () => set({ error: null }),
    }),
    {
      name: 'seer-auth',
      storage: createJSONStorage(() => sessionStorage),
      partialize: (s) => ({ user: s.user, isAuthenticated: s.isAuthenticated }),
    },
  ),
);
