import { create } from 'zustand';
import type { HeimdallEvent, MetricsSnapshot } from 'aida-shared';

interface DashboardStore {
  events:  HeimdallEvent[];
  metrics: MetricsSnapshot | null;
  setEvents:  (events: HeimdallEvent[])        => void;
  setMetrics: (metrics: MetricsSnapshot | null) => void;
}

export const useDashboardStore = create<DashboardStore>()(set => ({
  events:     [],
  metrics:    null,
  setEvents:  events  => set({ events }),
  setMetrics: metrics => set({ metrics }),
}));
