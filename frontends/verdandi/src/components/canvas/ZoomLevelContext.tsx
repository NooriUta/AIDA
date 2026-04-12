import { createContext, useContext } from 'react';

/**
 * Provides the current canvas zoom level to node components for LOD rendering.
 * Nodes can call `useZoomLevel()` to decide whether to render compact or full detail.
 */
const ZoomLevelContext = createContext<number>(1);

export const ZoomLevelProvider = ZoomLevelContext.Provider;

/** Current canvas zoom (0.1–3). Use for LOD: e.g. `zoom < 0.35` → compact. */
export function useZoomLevel(): number {
  return useContext(ZoomLevelContext);
}

/** LOD threshold: below this zoom, nodes render in compact mode. */
export const LOD_COMPACT_ZOOM = 0.35;
