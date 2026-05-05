/**
 * EV-09 / UA-02: fire-and-forget HEIMDALL event emitter for Verdandi.
 *
 * Events are sent to the Chur proxy (`POST /heimdall/events`), which relays
 * them to HEIMDALL backend. The hook is completely non-blocking — failures are
 * silently discarded so that HEIMDALL being down never affects the UI.
 *
 * tenantAlias is automatically injected from authStore into every event payload
 * so events are filterable by tenant in the HEIMDALL EventStream page.
 */
import { useCallback } from 'react';
import { useAuthStore } from '../stores/authStore';

type EventLevel = 'INFO' | 'WARN' | 'ERROR';

interface EmitFn {
  (
    eventType: string,
    level: EventLevel,
    payload: Record<string, unknown>,
    sessionId?: string,
    durationMs?: number,
  ): void;
}

interface UseHeimdallEmitterReturn {
  emit: EmitFn;
}

export function useHeimdallEmitter(): UseHeimdallEmitterReturn {
  const tenantAlias = useAuthStore(s => s.user?.activeTenantAlias);
  const username    = useAuthStore(s => s.user?.username);

  const emit = useCallback<EmitFn>(
    (eventType, level, payload, sessionId, durationMs) => {
      // Enrich payload with active tenant so events are filterable in HEIMDALL.
      // Fall back to 'default' so HTA-14 never rejects verdandi events
      // (verdandi is not exempt by sourceComponent — it must carry tenantAlias).
      const effectiveTenant = tenantAlias ?? 'default';
      const enrichedPayload: Record<string, unknown> = { tenantAlias: effectiveTenant, ...payload };
      const body = JSON.stringify({
        timestamp:       Date.now(),
        sourceComponent: 'verdandi',
        eventType,
        level,
        sessionId:       sessionId ?? null,
        userId:          username ?? null,
        correlationId:   null,
        durationMs:      durationMs ?? 0,
        payload:         enrichedPayload,
      });
      // fire-and-forget — do not await, do not surface errors to UI
      fetch('/heimdall/events', {
        method:      'POST',
        headers:     { 'Content-Type': 'application/json' },
        body,
        credentials: 'include',
        signal:      AbortSignal.timeout(2000),
      }).catch(() => { /* intentionally silent */ });
    },
    [tenantAlias, username],
  );

  return { emit };
}
