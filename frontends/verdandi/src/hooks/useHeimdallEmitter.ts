/**
 * EV-09: Fire-and-forget HEIMDALL event emitter hook for Verdandi.
 *
 * Posts events to Chur /heimdall/events which proxies them to the HEIMDALL
 * backend. Never throws — HEIMDALL being down must not affect LOOM.
 *
 * Usage:
 *   const { emit } = useHeimdallEmitter();
 *   emit('LOOM_NODE_SELECTED', 'INFO', { node_id: id, node_type: type });
 */

import { useCallback } from 'react';

export type HeimdallLevel = 'INFO' | 'WARN' | 'ERROR';

export interface UseHeimdallEmitterReturn {
  /** Emit an event to HEIMDALL. Non-blocking, errors swallowed. */
  emit: (
    eventType: string,
    level: HeimdallLevel,
    payload: Record<string, unknown>,
    sessionId?: string,
  ) => void;
}

export function useHeimdallEmitter(): UseHeimdallEmitterReturn {
  const emit = useCallback(
    (
      eventType: string,
      level: HeimdallLevel,
      payload: Record<string, unknown>,
      sessionId?: string,
    ): void => {
      const body = JSON.stringify({
        timestamp:       Date.now(),
        sourceComponent: 'verdandi',
        eventType,
        level,
        sessionId:       sessionId ?? null,
        correlationId:   null,
        durationMs:      0,
        payload,
      });

      fetch('/heimdall/events', {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body,
        credentials: 'include',
        signal: AbortSignal.timeout(2000),
      }).catch(() => { /* fire-and-forget — HEIMDALL down must not break LOOM */ });
    },
    [],
  );

  return { emit };
}
