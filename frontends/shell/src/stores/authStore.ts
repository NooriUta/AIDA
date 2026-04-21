import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { AuthUser } from 'aida-shared';

const AUTH_BASE = (import.meta.env.VITE_AUTH_URL as string | undefined) ?? '/auth';

interface ShellAuthState {
  user:              AuthUser | null;
  isAuthenticated:   boolean;
  isCheckingSession: boolean;
  isLoading:         boolean;
  error:             string | null;
  checkSession:      () => Promise<void>;
  login:             (username: string, password: string) => Promise<void>;
  logout:            () => void;
  clearError:        () => void;
}

export const useShellAuthStore = create<ShellAuthState>()(
  persist(
    (set) => ({
      user:              null,
      isAuthenticated:   false,
      isCheckingSession: true,   // true on init — show spinner before first /auth/me resolves
      isLoading:         false,
      error:             null,

      checkSession: async () => {
        set({ isCheckingSession: true });
        try {
          const res = await fetch(`${AUTH_BASE}/me`, { credentials: 'include' });
          if (!res.ok) {
            set({ user: null, isAuthenticated: false, isCheckingSession: false });
            return;
          }
          const user = await res.json() as AuthUser;
          set({ user, isAuthenticated: true, isCheckingSession: false });
        } catch {
          set({ user: null, isAuthenticated: false, isCheckingSession: false });
        }
      },

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
            set({ error: data.error ?? 'auth.error.invalid', isLoading: false });
            return;
          }
          const user = await res.json() as AuthUser;
          set({ user, isAuthenticated: true, isLoading: false, error: null });
        } catch {
          set({ error: 'auth.error.network', isLoading: false });
        }
      },

      logout: () => {
        fetch(`${AUTH_BASE}/logout`, { method: 'POST', credentials: 'include' }).catch(() => {});
        set({ user: null, isAuthenticated: false });
      },

      clearError: () => set({ error: null }),
    }),
    {
      name:       'aida-auth',
      storage:    createJSONStorage(() => sessionStorage),
      partialize: (s) => ({ user: s.user, isAuthenticated: s.isAuthenticated }),
    },
  ),
);
