import { create } from 'zustand';
import type { AppContext } from 'aida-shared';
import { buildAppUrl }    from 'aida-shared';

type AppId = 'verdandi' | 'heimdall';

interface ShellState {
  currentApp: AppId;
  /** Internal — set by App.tsx once BrowserRouter navigate() is available */
  _navigate:    ((path: string) => void) | null;
  _setNavigate: (fn: (path: string) => void) => void;
  /** Navigate to an app, optionally with URL context params (ADR-DA-013) */
  navigateTo: (app: AppId, context?: AppContext) => void;
}

export const useShellStore = create<ShellState>((set, get) => ({
  currentApp:   'verdandi',
  _navigate:    null,
  _setNavigate: (fn) => set({ _navigate: fn }),

  navigateTo: (app, context) => {
    set({ currentApp: app });
    const base = app === 'heimdall' ? '/heimdall' : '/verdandi';
    const path = context ? buildAppUrl(base, context) : base;
    get()._navigate?.(path);
  },
}));
