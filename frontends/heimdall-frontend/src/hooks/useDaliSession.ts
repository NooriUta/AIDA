import { useEffect, useRef, useState } from 'react';
import { getSession, type DaliSession } from '../api/dali';

const POLL_MS = 1500;
const TERMINAL = new Set<string>(['COMPLETED', 'FAILED', 'CANCELLED']);

/**
 * Polls GET /dali/api/sessions/{id} every 1500ms while session is active.
 * Stops automatically when status reaches COMPLETED | FAILED | CANCELLED.
 *
 * @param id      Session UUID to poll
 * @param enabled Set to false to skip polling (e.g. already terminal)
 */
export function useDaliSession(id: string, enabled: boolean, tenantAlias?: string): DaliSession | null {
  const [session, setSession] = useState<DaliSession | null>(null);
  const abortRef = useRef<AbortController | null>(null);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (!enabled || !id) return;
    let stopped = false;

    async function poll() {
      const ctrl = new AbortController();
      abortRef.current = ctrl;
      try {
        const s = await getSession(id, ctrl.signal, tenantAlias);
        if (!stopped) {
          setSession(s);
          if (TERMINAL.has(s.status)) {
            clearInterval(timerRef.current!);
          }
        }
      } catch (err: unknown) {
        const e = err as Error;
        if (e.name === 'AbortError') return;
        if (e.message?.includes('not found') || e.message?.includes('404')) {
          // Session gone (server restart / live reload) — stop polling silently
          clearInterval(timerRef.current!);
        } else {
          console.warn('[useDaliSession]', e);
        }
      }
    }

    poll();
    timerRef.current = setInterval(poll, POLL_MS);

    return () => {
      stopped = true;
      clearInterval(timerRef.current!);
      abortRef.current?.abort();
    };
  }, [id, enabled, tenantAlias]);

  return session;
}
