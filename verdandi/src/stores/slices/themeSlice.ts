import type { LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;
type Get = () => LoomStore;

export function themeActions(set: Set, get: Get) {
  return {
    toggleTheme: () => {
      const next = get().theme === 'dark' ? 'light' : 'dark';
      localStorage.setItem('seer-theme', next);
      document.documentElement.setAttribute('data-theme', next);
      set({ theme: next });
    },

    setPalette: (name: string) => {
      localStorage.setItem('seer-palette', name);
      if (name === 'amber-forest') {
        document.documentElement.removeAttribute('data-palette');
      } else {
        document.documentElement.setAttribute('data-palette', name);
      }
      set({ palette: name });
    },

    setGraphStats: (nodeCount: number, edgeCount: number) => set({ nodeCount, edgeCount }),
    setZoom:       (zoom: number) => set({ zoom }),
  };
}
