import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { AuthUser } from 'aida-shared';

const AUTH_BASE = (import.meta.env.VITE_AUTH_URL as string | undefined) ?? '/auth';

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

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      user:            null,
      isAuthenticated: false,
      isLoading:       false,
      error:           null,

      login: async (username, password) => {
        set({ isLoading: true, error: null });
        try {
          const res = await fetch(`${AUTH_BASE}/login`, {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ username, password }),
          });
          if (!res.ok) {
            const data = await res.json().catch(() => ({})) as { error?: string };
            set({ error: data.error ?? 'Invalid credentials', isLoading: false });
            return;
          }
          const user = await res.json() as AuthUser;
          set({ user, isAuthenticated: true, isLoading: false, error: null });
        } catch {
          set({ error: 'Network error — is Chur running?', isLoading: false });
        }
      },

      logout: () => {
        fetch(`${AUTH_BASE}/logout`, { method: 'POST', credentials: 'include' }).catch(() => {});
        set({ user: null, isAuthenticated: false });
      },

      checkSession: async () => {
        try {
          const res = await fetch(`${AUTH_BASE}/me`, { credentials: 'include' });
          if (!res.ok) { set({ user: null, isAuthenticated: false }); return; }
          const user = await res.json() as AuthUser;
          set({ user, isAuthenticated: true });
        } catch {
          set({ user: null, isAuthenticated: false });
        }
      },

      clearError: () => set({ error: null }),
    }),
    {
      name:       'heimdall-auth',
      storage:    createJSONStorage(() => sessionStorage),
      partialize: (s) => ({ user: s.user, isAuthenticated: s.isAuthenticated }),
    },
  ),
);
