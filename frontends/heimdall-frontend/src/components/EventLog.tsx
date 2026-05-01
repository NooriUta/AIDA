import { useState } from 'react';
import { Virtuoso } from 'react-virtuoso';
import { useTranslation } from 'react-i18next';
import type { HeimdallEvent, EventFilter } from 'aida-shared';
import { EVENT_LABELS, formatPayload, levelClass } from '../utils/eventFormat';

interface EventLogProps {
  events: HeimdallEvent[];
  filter?: EventFilter;
  /** Height of the container (CSS string or px number). Default: '100%' */
  maxHeight?: string;
  height?: string | number;
  connected?: boolean;
}

function formatTime(ts: number): string {
  // Local TZ — toISOString() returns UTC, which confused users when comparing to logs.
  const d = new Date(ts);
  const hh = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  const ss = String(d.getSeconds()).padStart(2, '0');
  const ms = String(d.getMilliseconds()).padStart(3, '0');
  return `${hh}:${mm}:${ss}.${ms}`;
}

function formatFullTimestamp(ts: number): string {
  // Local TZ ISO-like string with offset — for the expanded event detail panel.
  const d = new Date(ts);
  const pad = (n: number, w = 2) => String(n).padStart(w, '0');
  const offMin = -d.getTimezoneOffset();
  const offSign = offMin >= 0 ? '+' : '-';
  const offH = pad(Math.floor(Math.abs(offMin) / 60));
  const offM = pad(Math.abs(offMin) % 60);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
       + `T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}.${pad(d.getMilliseconds(), 3)}`
       + `${offSign}${offH}:${offM}`;
}

function EventRow({
  event,
  selected,
  onClick,
}: {
  event:    HeimdallEvent;
  selected: boolean;
  onClick:  () => void;
}) {
  const comp     = (event.sourceComponent ?? '').toLowerCase();
  const tenant   = event.payload?.['tenantAlias'] as string | undefined;
  const rowCls   = [
    'event-row-grid',
    event.level === 'ERROR' ? 'evt-row-error' : event.level === 'WARN' ? 'evt-row-warn' : '',
    selected ? 'evt-row-selected' : '',
  ].filter(Boolean).join(' ');

  return (
    <div className={rowCls} onClick={onClick} style={{ cursor: 'pointer' }}>
      <span className="evt-ts">{formatTime(event.timestamp)}</span>
      <span><span className={`comp comp-${comp}`}>{event.sourceComponent}</span></span>
      <span className="evt-type">{EVENT_LABELS[event.eventType] ?? event.eventType}</span>
      <span><span className={`badge ${levelClass(event.level)}`}>{event.level}</span></span>
      <span className="evt-tenant">{tenant ?? '—'}</span>
      <span className="evt-dur">{event.durationMs > 0 ? `${event.durationMs}ms` : '—'}</span>
      <span className="evt-payload">{formatPayload(event)}</span>
    </div>
  );
}

function EventDetail({ event, onClose }: { event: HeimdallEvent; onClose: () => void }) {
  const comp = (event.sourceComponent ?? '').toLowerCase();
  return (
    <div style={{
      borderTop:   '1px solid var(--bd)',
      background:  'var(--bg1)',
      padding:     '10px 14px',
      flexShrink:  0,
      fontSize:    12,
      lineHeight:  1.6,
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
          <span className={`comp comp-${comp}`}>{event.sourceComponent}</span>
          <span style={{ color: 'var(--t1)', fontWeight: 600 }}>{event.eventType}</span>
          <span className={`badge ${levelClass(event.level)}`}>{event.level}</span>
          {event.durationMs > 0 && (
            <span style={{ color: 'var(--t3)' }}>{event.durationMs}ms</span>
          )}
          <span style={{ color: 'var(--t3)' }}>{formatFullTimestamp(event.timestamp)}</span>
        </div>
        <button
          onClick={onClose}
          style={{
            background: 'none', border: 'none', color: 'var(--t3)',
            cursor: 'pointer', fontSize: 14, lineHeight: 1, padding: '0 4px',
          }}
        >✕</button>
      </div>
      {(event.sessionId || event.correlationId || event.userId) && (
        <div style={{ display: 'flex', gap: 16, marginBottom: 6, color: 'var(--t3)', fontSize: 11 }}>
          {event.sessionId    && <span>session: <span style={{ color: 'var(--t2)' }}>{event.sessionId}</span></span>}
          {event.correlationId && <span>correlation: <span style={{ color: 'var(--t2)' }}>{event.correlationId}</span></span>}
          {event.userId       && <span>user: <span style={{ color: 'var(--t2)' }}>{event.userId}</span></span>}
        </div>
      )}
      {event.payload && Object.keys(event.payload).length > 0 && (
        <pre style={{
          margin: 0, padding: '6px 10px',
          background: 'var(--bg0)',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-sm)',
          color: 'var(--t1)',
          fontSize: 11,
          overflowX: 'auto',
          maxHeight: 120,
          overflowY: 'auto',
        }}>
          {JSON.stringify(event.payload, null, 2)}
        </pre>
      )}
    </div>
  );
}

function applyFilter(events: HeimdallEvent[], filter?: EventFilter): HeimdallEvent[] {
  if (!filter) return events;
  return events.filter(e => {
    if (filter.component && !e.sourceComponent?.toLowerCase().includes(filter.component.toLowerCase())) return false;
    if (filter.sessionId && e.sessionId !== filter.sessionId) return false;
    if (filter.level     && e.level     !== filter.level)     return false;
    if (filter.type      && e.eventType !== filter.type)       return false;
    return true;
  });
}

export function EventLog({ events, filter, maxHeight, height, connected }: EventLogProps) {
  const { t }          = useTranslation();
  const filtered       = applyFilter(events, filter);
  const h              = height ?? maxHeight ?? '100%';
  const heightVal      = typeof h === 'number' ? `${h}px` : h;
  const [selected, setSelected] = useState<HeimdallEvent | null>(null);

  function handleRowClick(event: HeimdallEvent) {
    setSelected(prev => prev?.timestamp === event.timestamp && prev?.sourceComponent === event.sourceComponent ? null : event);
  }

  return (
    <div style={{
      height:        heightVal,
      display:       'flex',
      flexDirection: 'column',
      background:    'var(--bg0)',
      border:        '1px solid var(--bd)',
      borderRadius:  'var(--seer-radius-md)',
      overflow:      'hidden',
    }}>
      {/* Connection status bar */}
      {connected !== undefined && (
        <div style={{
          display:      'flex',
          alignItems:   'center',
          gap:          6,
          padding:      '5px 14px',
          background:   'var(--bg1)',
          borderBottom: '1px solid var(--bd)',
          fontSize:     11,
          color:        'var(--t3)',
          flexShrink:   0,
        }}>
          <span style={{
            width:        6,
            height:       6,
            borderRadius: '50%',
            background:   connected ? 'var(--suc)' : 'var(--danger)',
            display:      'inline-block',
            flexShrink:   0,
          }} />
          {connected
            ? `${t('ws.open')} · ${filtered.length} ${t('eventStream.events')}`
            : t('ws.closed')}
        </div>
      )}

      {/* Header */}
      <div className="event-grid-head">
        <span>{t('eventLog.time')}</span>
        <span>{t('eventLog.component')}</span>
        <span>{t('eventLog.eventType')}</span>
        <span>{t('eventLog.level')}</span>
        <span>{t('eventLog.tenant')}</span>
        <span>{t('eventLog.duration')}</span>
        <span>{t('eventLog.payload')}</span>
      </div>

      {/* Body */}
      {filtered.length === 0 ? (
        <div style={{
          flex:           1,
          display:        'flex',
          alignItems:     'center',
          justifyContent: 'center',
          color:          'var(--t3)',
          fontSize:       '13px',
        }}>
          {t('eventLog.noEvents')}
        </div>
      ) : (
        <Virtuoso
          style={{ flex: 1 }}
          data={filtered}
          followOutput="smooth"
          itemContent={(_, event) => (
            <EventRow
              event={event}
              selected={
                selected?.timestamp === event.timestamp &&
                selected?.sourceComponent === event.sourceComponent
              }
              onClick={() => handleRowClick(event)}
            />
          )}
        />
      )}

      {/* Detail panel */}
      {selected && (
        <EventDetail event={selected} onClose={() => setSelected(null)} />
      )}
    </div>
  );
}
