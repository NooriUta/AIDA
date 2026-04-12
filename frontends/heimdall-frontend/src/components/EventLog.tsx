import React from 'react';
import { Virtuoso } from 'react-virtuoso';
import type { HeimdallEvent, EventFilter } from 'aida-shared';

interface EventLogProps {
  events: HeimdallEvent[];
  filter?: EventFilter;
  maxHeight?: string;
}

const COMPONENT_COLORS: Record<string, string> = {
  hound:   '#7F77DD',
  dali:    '#1D9E75',
  mimir:   '#BA7517',
  shuttle: '#3B82F6',
};

const LEVEL_COLORS: Record<string, string> = {
  INFO:  'var(--t3)',
  WARN:  'var(--wrn)',
  ERROR: 'var(--danger)',
};

function componentColor(comp: string): string {
  const key = comp.toLowerCase();
  for (const [k, v] of Object.entries(COMPONENT_COLORS)) {
    if (key.includes(k)) return v;
  }
  return '#888888';
}

function formatTime(ts: number): string {
  return new Date(ts).toISOString().substring(11, 23); // HH:mm:ss.mmm
}

function payloadPreview(payload: Record<string, unknown>): string {
  const str = JSON.stringify(payload);
  return str.length > 80 ? str.slice(0, 77) + '…' : str;
}

const rowStyle: React.CSSProperties = {
  display:     'grid',
  gridTemplateColumns: '90px 100px 160px 50px 60px 1fr',
  gap:         'var(--seer-space-2)',
  padding:     '4px var(--seer-space-4)',
  borderBottom: '1px solid var(--bd)',
  fontSize:    '12px',
  fontFamily:  'var(--mono)',
  alignItems:  'center',
  minHeight:   '28px',
};

const headerStyle: React.CSSProperties = {
  ...rowStyle,
  background: 'var(--bg2)',
  color:      'var(--t3)',
  fontWeight: 500,
  fontSize:   '11px',
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
  position:   'sticky',
  top:        0,
  zIndex:     1,
};

function EventRow({ event }: { event: HeimdallEvent }) {
  const compColor  = componentColor(event.sourceComponent ?? '');
  const levelColor = LEVEL_COLORS[event.level] ?? 'var(--t3)';

  return (
    <div style={rowStyle}>
      <span style={{ color: 'var(--t3)' }}>{formatTime(event.timestamp)}</span>
      <span style={{ color: compColor, fontWeight: 600, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
        {event.sourceComponent}
      </span>
      <span style={{ color: 'var(--t2)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
        {event.eventType}
      </span>
      <span style={{ color: levelColor, fontWeight: 600 }}>{event.level}</span>
      <span style={{ color: 'var(--t3)', textAlign: 'right' }}>
        {event.durationMs > 0 ? `${event.durationMs}ms` : ''}
      </span>
      <span style={{ color: 'var(--t3)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
        {payloadPreview(event.payload)}
      </span>
    </div>
  );
}

function applyFilter(events: HeimdallEvent[], filter?: EventFilter): HeimdallEvent[] {
  if (!filter) return events;
  return events.filter(e => {
    if (filter.component && !e.sourceComponent?.toLowerCase().includes(filter.component.toLowerCase())) return false;
    if (filter.sessionId && e.sessionId !== filter.sessionId) return false;
    if (filter.level     && e.level    !== filter.level)     return false;
    if (filter.type      && e.eventType !== filter.type)      return false;
    return true;
  });
}

export function EventLog({ events, filter, maxHeight = '100%' }: EventLogProps) {
  const filtered = applyFilter(events, filter);

  return (
    <div style={{ height: maxHeight, display: 'flex', flexDirection: 'column', background: 'var(--bg0)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)', overflow: 'hidden' }}>
      <div style={headerStyle}>
        <span>Time</span>
        <span>Component</span>
        <span>Event Type</span>
        <span>Level</span>
        <span>Duration</span>
        <span>Payload</span>
      </div>
      {filtered.length === 0 ? (
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--t3)', fontSize: '13px' }}>
          No events
        </div>
      ) : (
        <Virtuoso
          style={{ flex: 1 }}
          data={filtered}
          followOutput="smooth"
          itemContent={(_, event) => <EventRow event={event} />}
        />
      )}
    </div>
  );
}
