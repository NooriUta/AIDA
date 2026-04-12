import { useEffect, useRef, useState, useCallback } from 'react';
import type { HeimdallEvent, EventFilter } from 'aida-shared';
import { HEIMDALL_WS } from '../api';

const MAX_EVENTS = 500;
const RECONNECT_DELAY_MS = 3000;

function buildWsUrl(filter?: EventFilter): string {
  if (!filter) return HEIMDALL_WS;
  const parts: string[] = [];
  if (filter.component) parts.push(`component:${filter.component}`);
  else if (filter.sessionId) parts.push(`session_id:${filter.sessionId}`);
  else if (filter.level)    parts.push(`level:${filter.level}`);
  else if (filter.type)     parts.push(`type:${filter.type}`);
  if (parts.length === 0) return HEIMDALL_WS;
  return `${HEIMDALL_WS}?filter=${encodeURIComponent(parts[0])}`;
}

export type ConnectionStatus = 'connecting' | 'open' | 'closed' | 'error';

export function useEventStream(filter?: EventFilter) {
  const [events, setEvents]   = useState<HeimdallEvent[]>([]);
  const [status, setStatus]   = useState<ConnectionStatus>('connecting');
  const wsRef                 = useRef<WebSocket | null>(null);
  const reconnectRef          = useRef<ReturnType<typeof setTimeout> | null>(null);
  const mountedRef            = useRef(true);

  const connect = useCallback(() => {
    if (!mountedRef.current) return;
    const url = buildWsUrl(filter);
    setStatus('connecting');

    const ws = new WebSocket(url);
    wsRef.current = ws;

    ws.onopen = () => {
      if (mountedRef.current) setStatus('open');
    };

    ws.onmessage = (msg: MessageEvent<string>) => {
      if (!mountedRef.current) return;
      try {
        const event = JSON.parse(msg.data) as HeimdallEvent;
        setEvents(prev => {
          const next = [...prev, event];
          return next.length > MAX_EVENTS ? next.slice(next.length - MAX_EVENTS) : next;
        });
      } catch {
        // ignore malformed JSON
      }
    };

    ws.onerror = () => {
      if (mountedRef.current) setStatus('error');
    };

    ws.onclose = () => {
      if (!mountedRef.current) return;
      setStatus('closed');
      reconnectRef.current = setTimeout(connect, RECONNECT_DELAY_MS);
    };
  }, [filter]);

  useEffect(() => {
    mountedRef.current = true;
    connect();
    return () => {
      mountedRef.current = false;
      if (reconnectRef.current) clearTimeout(reconnectRef.current);
      wsRef.current?.close();
    };
  }, [connect]);

  const clearEvents = useCallback(() => setEvents([]), []);

  return { events, status, clearEvents };
}
