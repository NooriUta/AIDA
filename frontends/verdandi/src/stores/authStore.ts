import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { usePrefsStore } from './prefsStore';

export interface AuthUser {
  id: string;
  username: string;
  role: 'viewer' | 'editor' | 'admin';
  activeTenantAlias?: string;
}

interface AuthStore {
  user:              AuthUser | null;
  isAuthenticated:   boolean;
  isCheckingSession: boolean;
  isLoading:         boolean;
  error:             string | null;

  login:        (username: string, password: string) => Promise<void>;
  logout:       () => Promise<void>;
  checkSession: () => Promise<void>;
  clearError:   () => void;
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
      user:              null,
      isAuthenticated:   false,
      isCheckingSession: true,   // true on init so ProtectedRoute waits for the first /auth/me
      isLoading:         false,
      error:             null,

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

      // ── logout — await server call so httpOnly cookie is cleared BEFORE local
      // state is wiped. Fire-and-forget caused a cyclic redirect: ProtectedRoute
      // sent the user to /login while the cookie was still valid; App.checkSession()
      // then found an active session and navigated back to /, creating a loop.
      logout: async () => {
        stopRefreshTimer();
        try {
          await fetch(`${AUTH_BASE}/logout`, {
            method: 'POST',
            credentials: 'include',
          });
        } catch {
          // Network error — still clear local state so user is logged out locally.
        }
        set({ user: null, isAuthenticated: false, error: null });
      },

      // ── checkSession — verifies the httpOnly cookie is still valid ───────────
      // Always calls /auth/me so that:
      //   • standalone: restores session after page reload
      //   • Shell mode: discovers the Shell-level session even when local
      //     sessionStorage has isAuthenticated=false (fresh tab or first load)
      checkSession: async () => {
        set({ isCheckingSession: true });
        try {
          const res = await fetch(`${AUTH_BASE}/me`, {
            credentials: 'include',
          });
          if (res.status === 401) {
            stopRefreshTimer();
            set({ user: null, isAuthenticated: false, isCheckingSession: false });
          } else if (res.ok) {
            try {
              const data = await res.json();
              set((s) => ({ user: data ?? s.user, isAuthenticated: true, isCheckingSession: false }));
            } catch {
              set({ isAuthenticated: true, isCheckingSession: false });
            }
            startRefreshTimer(() => get().refreshToken());
            if (!usePrefsStore.getState().synced) {
              usePrefsStore.getState().fetchPrefs().catch(() => {});
            }
          } else {
            // Non-401, non-ok (e.g. 500): keep existing auth state
            set({ isCheckingSession: false });
          }
        } catch {
          // Network down — keep existing state
          set({ isCheckingSession: false });
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
