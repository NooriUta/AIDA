import React, { useState } from 'react';
import { EventLog }       from '../components/EventLog';
import { useEventStream } from '../hooks/useEventStream';
import type { EventFilter, EventLevel } from 'aida-shared';

const COMPONENTS = ['', 'hound', 'dali', 'mimir', 'shuttle'];
const LEVELS: Array<'' | EventLevel> = ['', 'INFO', 'WARN', 'ERROR'];

const selectStyle: React.CSSProperties = {
  background:   'var(--bg2)',
  border:       '1px solid var(--bd)',
  borderRadius: 'var(--seer-radius-sm)',
  color:        'var(--t1)',
  padding:      '4px 8px',
  fontSize:     '13px',
  fontFamily:   'var(--font)',
  cursor:       'pointer',
};

export default function EventStreamPage() {
  const [component, setComponent] = useState('');
  const [level, setLevel]         = useState<'' | EventLevel>('');

  const filter: EventFilter | undefined =
    component || level
      ? { component: component || undefined, level: (level as EventLevel) || undefined }
      : undefined;

  const { events, status, clearEvents } = useEventStream(filter);

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', padding: 'var(--seer-space-4) var(--seer-space-6)', gap: 'var(--seer-space-3)' }}>
      {/* Toolbar */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-3)', flexShrink: 0 }}>
        <span style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>Filter</span>

        <label style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', fontSize: '13px', color: 'var(--t2)' }}>
          Component
          <select style={selectStyle} value={component} onChange={e => setComponent(e.target.value)}>
            {COMPONENTS.map(c => <option key={c} value={c}>{c || 'All'}</option>)}
          </select>
        </label>

        <label style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', fontSize: '13px', color: 'var(--t2)' }}>
          Level
          <select style={selectStyle} value={level} onChange={e => setLevel(e.target.value as '' | EventLevel)}>
            {LEVELS.map(l => <option key={l} value={l}>{l || 'All'}</option>)}
          </select>
        </label>

        <button
          onClick={clearEvents}
          style={{ ...selectStyle, marginLeft: 'auto', background: 'var(--bg3)', color: 'var(--t2)' }}
        >
          Clear
        </button>

        <span style={{ fontSize: '12px', fontFamily: 'var(--mono)', color: status === 'open' ? 'var(--suc)' : status === 'connecting' ? 'var(--wrn)' : 'var(--danger)' }}>
          ● {status} · {events.length} events
        </span>
      </div>

      {/* Log */}
      <div style={{ flex: 1, minHeight: 0 }}>
        <EventLog events={events} maxHeight="100%" />
      </div>
    </div>
  );
}
