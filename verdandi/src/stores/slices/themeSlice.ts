// eslint-disable-next-line @typescript-eslint/no-explicit-any
type S = (p: any) => void;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type G = () => any;

export function themeActions(set: S, get: G) {
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
