import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

// ── Minimal HEIMDALL event types (mirrors aida-shared — verdandi has no dep on it) ──
type EventLevel = 'INFO' | 'WARN' | 'ERROR';

interface StreamEvent {
  timestamp:       number;
  sourceComponent: string;
  eventType:       string;
  level:           EventLevel;
  sessionId:       string | null;
  durationMs:      number;
  payload:         Record<string, unknown>;
}

// ── WebSocket URL ─────────────────────────────────────────────────────────────
// In dev:  proxied by Vite → Chur → HEIMDALL backend  (see vite.config.ts)
// In prod: nginx routes /heimdall/ws/events to Chur WS proxy on the same host
function resolveWsUrl(): string {
  if (import.meta.env.VITE_HEIMDALL_WS) return import.meta.env.VITE_HEIMDALL_WS as string;
  if (typeof window !== 'undefined') {
    const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${proto}//${window.location.host}/heimdall/ws/events`;
  }
  return 'ws://localhost:3000/heimdall/ws/events';
}

const WS_URL      = resolveWsUrl();
const MAX_EVENTS  = 500;
const RECONNECT_MS = 3_000;

// ── Helpers ───────────────────────────────────────────────────────────────────
function formatTime(ts: number): string {
  return new Date(ts).toISOString().substring(11, 23); // HH:mm:ss.mmm
}

function levelColor(level: EventLevel): string {
  if (level === 'ERROR') return 'var(--danger)';
  if (level === 'WARN')  return 'var(--wrn)';
  return 'var(--suc)';
}

/**
 * Returns true if any string-serialisable value in `payload` contains one of
 * the highlighted ArcadeDB geoid strings (e.g. "DaliTable:prod.orders").
 * Case-insensitive for partial matches like plain table names.
 */
function payloadMatchesHighlight(
  payload: Record<string, unknown>,
  ids:     Set<string>,
): boolean {
  if (ids.size === 0) return false;
  const flat = JSON.stringify(payload).toLowerCase();
  for (const id of ids) {
    if (flat.includes(id.toLowerCase())) return true;
  }
  return false;
}

// ── Component ─────────────────────────────────────────────────────────────────
export interface EventStreamPanelProps {
  /** ArcadeDB geoids of currently highlighted LOOM nodes. Events whose
   *  payload references one of these IDs are visually highlighted in the panel. */
  highlightNodeIds: Set<string>;
  /** Called when the user presses the close button. */
  onClose: () => void;
}

type ConnStatus = 'connecting' | 'open' | 'closed' | 'error';

export const EventStreamPanel = memo(({ highlightNodeIds, onClose }: EventStreamPanelProps) => {
  const { t } = useTranslation();

  const [events,  setEvents]  = useState<StreamEvent[]>([]);
  const [status,  setStatus]  = useState<ConnStatus>('connecting');

  const wsRef        = useRef<WebSocket | null>(null);
  const reconnectRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const mountedRef   = useRef(true);
  const bodyRef      = useRef<HTMLDivElement>(null);

  // ── WebSocket lifecycle ───────────────────────────────────────────────────
  const connect = useCallback(() => {
    if (!mountedRef.current) return;
    setStatus('connecting');
    setEvents([]);

    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => {
      if (mountedRef.current && wsRef.current === ws) setStatus('open');
    };

    ws.onmessage = (msg: MessageEvent<string>) => {
      if (!mountedRef.current || wsRef.current !== ws) return;
      try {
        const ev = JSON.parse(msg.data) as StreamEvent;
        setEvents(prev => {
          const next = [...prev, ev];
          return next.length > MAX_EVENTS ? next.slice(next.length - MAX_EVENTS) : next;
        });
        requestAnimationFrame(() => {
          if (bodyRef.current) bodyRef.current.scrollTop = bodyRef.current.scrollHeight;
        });
      } catch { /* ignore malformed JSON */ }
    };

    ws.onerror = () => {
      if (mountedRef.current && wsRef.current === ws) setStatus('error');
    };

    ws.onclose = () => {
      if (!mountedRef.current || wsRef.current !== ws) return;
      setStatus('closed');
      reconnectRef.current = setTimeout(connect, RECONNECT_MS);
    };
  }, []);

  useEffect(() => {
    mountedRef.current = true;
    connect();
    return () => {
      mountedRef.current = false;
      if (reconnectRef.current) clearTimeout(reconnectRef.current);
      const cur = wsRef.current;
      wsRef.current = null;
      cur?.close();
    };
  }, [connect]);

  // ── Derived state ─────────────────────────────────────────────────────────
  const relatedSet = useMemo(
    () => new Set(events.filter(e => payloadMatchesHighlight(e.payload, highlightNodeIds))),
    [events, highlightNodeIds],
  );

  const statusDot =
    status === 'open'        ? 'var(--suc)' :
    status === 'connecting'  ? 'var(--wrn)' :
    /* closed | error */       'var(--danger)';

  // ── Render ────────────────────────────────────────────────────────────────
  return (
    <div style={{
      display:        'flex',
      flexDirection:  'column',
      borderTop:      '1px solid var(--bd)',
      background:     'var(--bg0)',
      overflow:       'hidden',
      minHeight:      0,
    }}>

      {/* ── Header bar ──────────────────────────────────────────────────── */}
      <div style={{
        display:        'flex',
        alignItems:     'center',
        gap:            8,
        padding:        '0 10px',
        height:         28,
        flexShrink:     0,
        background:     'var(--bg1)',
        borderBottom:   '1px solid var(--bd)',
        fontSize:       11,
        color:          'var(--t2)',
        userSelect:     'none',
      }}>
        {/* Status dot */}
        <span style={{
          width:        6,
          height:       6,
          borderRadius: '50%',
          background:   statusDot,
          flexShrink:   0,
        }} />

        <span style={{ fontWeight: 600, letterSpacing: '0.04em' }}>
          {t('eventStream.title', 'Event Stream')}
        </span>

        {highlightNodeIds.size > 0 && (
          <span style={{ color: 'var(--acc)', fontSize: 10 }}>
            · {highlightNodeIds.size} {t('eventStream.highlighted', 'highlighted')}
          </span>
        )}

        <span style={{ color: 'var(--t3)', fontSize: 10 }}>
          {events.length} {t('eventStream.events', 'events')}
        </span>

        <div style={{ flex: 1 }} />

        {/* Clear button */}
        <button
          onClick={() => setEvents([])}
          style={{
            background: 'none', border: 'none',
            color: 'var(--t3)', cursor: 'pointer',
            fontSize: 12, padding: '0 6px', lineHeight: 1,
          }}
          title={t('eventStream.clear', 'Clear events')}
        >
          ⊘
        </button>

        {/* Close button */}
        <button
          onClick={onClose}
          style={{
            background: 'none', border: 'none',
            color: 'var(--t3)', cursor: 'pointer',
            fontSize: 13, padding: '0 4px', lineHeight: 1,
          }}
          title={t('eventStream.close', 'Close')}
        >
          ✕
        </button>
      </div>

      {/* ── Column header ──────────────────────────────────────────────── */}
      <div style={{
        display:             'grid',
        gridTemplateColumns: '84px 96px 1fr 46px 58px',
        padding:             '3px 10px',
        background:          'var(--bg1)',
        borderBottom:        '1px solid var(--bd)',
        fontSize:            10,
        fontWeight:          600,
        color:               'var(--t3)',
        letterSpacing:       '0.06em',
        textTransform:       'uppercase',
        flexShrink:          0,
      }}>
        <span>{t('eventStream.col.time',      'Time')}</span>
        <span>{t('eventStream.col.component', 'Component')}</span>
        <span>{t('eventStream.col.event',     'Event')}</span>
        <span>{t('eventStream.col.level',     'Level')}</span>
        <span>{t('eventStream.col.duration',  'Duration')}</span>
      </div>

      {/* ── Event rows ─────────────────────────────────────────────────── */}
      <div ref={bodyRef} style={{ flex: 1, overflowY: 'auto', overflowX: 'hidden' }}>
        {events.length === 0 ? (
          <div style={{
            display:         'flex',
            alignItems:      'center',
            justifyContent:  'center',
            height:          '100%',
            color:           'var(--t3)',
            fontSize:        12,
          }}>
            {status === 'connecting'
              ? t('eventStream.connecting', 'Connecting to event stream…')
              : t('eventStream.empty',      'No events')}
          </div>
        ) : (
          events.map((ev, i) => {
            const isRelated = relatedSet.has(ev);
            return (
              <div
                key={`${ev.timestamp}-${ev.sourceComponent}-${i}`}
                style={{
                  display:             'grid',
                  gridTemplateColumns: '84px 96px 1fr 46px 58px',
                  padding:             '2px 10px',
                  fontSize:            11,
                  borderBottom:        '1px solid color-mix(in srgb, var(--bd) 35%, transparent)',
                  background:          isRelated
                    ? 'color-mix(in srgb, var(--acc) 10%, transparent)'
                    : 'transparent',
                  color:               'var(--t2)',
                  lineHeight:          1.4,
                }}
              >
                <span style={{
                  color:      'var(--t3)',
                  fontFamily: 'var(--seer-font-mono, monospace)',
                  fontSize:   10,
                }}>
                  {formatTime(ev.timestamp)}
                </span>

                <span style={{
                  color:         'var(--inf)',
                  overflow:      'hidden',
                  textOverflow:  'ellipsis',
                  whiteSpace:    'nowrap',
                }}>
                  {ev.sourceComponent}
                </span>

                <span style={{
                  overflow:     'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace:   'nowrap',
                }}>
                  {ev.eventType}
                </span>

                <span style={{
                  color:      levelColor(ev.level),
                  fontWeight: 500,
                  fontSize:   10,
                }}>
                  {ev.level}
                </span>

                <span style={{ color: 'var(--t3)' }}>
                  {ev.durationMs > 0 ? `${ev.durationMs}ms` : '—'}
                </span>
              </div>
            );
          })
        )}
      </div>

    </div>
  );
});

EventStreamPanel.displayName = 'EventStreamPanel';
