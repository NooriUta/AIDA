import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

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
          if (!res.ok) {
            set({ isLoading: false, error: 'auth.error.server' });
            return;
          }

          const user: AuthUser = await res.json();
          set({ user, isAuthenticated: true, isLoading: false, error: null });
        } catch {
          set({ isLoading: false, error: 'auth.error.network' });
        }
      },

      // ── logout — fire-and-forget, clears cookie server-side ─────────────────
      logout: () => {
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
            set({ user: null, isAuthenticated: false });
          }
        } catch {
          // Network down — keep existing state, will fail on next API call
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
