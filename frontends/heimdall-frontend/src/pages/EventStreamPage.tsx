import React, { useRef, useState, useMemo } from 'react';
import { useTranslation }    from 'react-i18next';
import { usePageTitle }      from '../hooks/usePageTitle';
import { EventLog }          from '../components/EventLog';
import { useEventStream }    from '../hooks/useEventStream';
import type { EventFilter, EventLevel } from 'aida-shared';

// Active: chur, shuttle, heimdall, dali. Deferred: hound (H3.8). Planned: verdandi.
const COMPONENTS = ['', 'chur', 'shuttle', 'heimdall', 'dali', 'hound', 'verdandi'];
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

const inputStyle: React.CSSProperties = {
  background:   'var(--bg2)',
  border:       '1px solid var(--bd)',
  borderRadius: 'var(--seer-radius-sm)',
  color:        'var(--t1)',
  padding:      '4px 8px',
  fontSize:     '13px',
  fontFamily:   'var(--mono)',
  width:        '160px',
};

export default function EventStreamPage() {
  const { t } = useTranslation();
  usePageTitle(t('nav.events'));

  const [component,  setComponent]  = useState('');
  const [level,      setLevel]      = useState<'' | EventLevel>('');
  const [sessionId,  setSessionId]  = useState('');
  const [eventType,  setEventType]  = useState('');
  const [tenant,     setTenant]     = useState('');
  const [paused, setPaused]         = useState(false);

  // Buffer of events to display when paused
  const pausedEventsRef = useRef<ReturnType<typeof useEventStream>['events']>([]);

  const filter = useMemo<EventFilter | undefined>(
    () => (component || level || sessionId || eventType
      ? {
          component: component  || undefined,
          level:     (level as EventFilter['level']) || undefined,
          sessionId: sessionId  || undefined,
          type:      eventType  || undefined,
        }
      : undefined),
    [component, level, sessionId, eventType],
  );

  const { events, status, clearEvents } = useEventStream(filter);

  // When paused, freeze the displayed events
  if (!paused) {
    pausedEventsRef.current = events;
  }
  const baseEvents = paused ? pausedEventsRef.current : events;

  // HTA-14: client-side tenant filter (payload.tenantAlias exact match)
  const displayedEvents = useMemo(() => {
    if (!tenant) return baseEvents;
    return baseEvents.filter(e => {
      const v = e.payload?.['tenantAlias'];
      return typeof v === 'string' && v === tenant;
    });
  }, [baseEvents, tenant]);

  // Distinct tenant aliases observed in the current buffer — for the filter dropdown
  const seenTenants = useMemo(() => {
    const s = new Set<string>();
    for (const e of events) {
      const v = e.payload?.['tenantAlias'];
      if (typeof v === 'string' && v) s.add(v);
    }
    return [...s].sort();
  }, [events]);

  const wsColor =
    status === 'open'       ? 'var(--suc)'
    : status === 'connecting' ? 'var(--wrn)'
    : 'var(--danger)';

  // Rough events/sec: count last 5s
  const recentCount = events.filter(e => e.timestamp > Date.now() - 5000).length;
  const eventsPerSec = (recentCount / 5).toFixed(1);

  // Counts across ALL events (not paused/filtered) — for the stats breakdown
  const levelCounts = events.reduce<Record<string, number>>((acc, e) => {
    const l = e.level ?? 'UNKNOWN';
    acc[l] = (acc[l] ?? 0) + 1;
    return acc;
  }, {});
  const compCounts = events.reduce<Record<string, number>>((acc, e) => {
    const c = (e.sourceComponent ?? 'unknown').toLowerCase();
    acc[c] = (acc[c] ?? 0) + 1;
    return acc;
  }, {});
  // Sorted list of event types seen in the current buffer (for the type filter dropdown)
  const seenTypes = useMemo(() =>
    Object.entries(
      events.reduce<Record<string, number>>((acc, e) => {
        const t = e.eventType ?? '';
        if (t) acc[t] = (acc[t] ?? 0) + 1;
        return acc;
      }, {})
    ).sort((a, b) => a[0].localeCompare(b[0])),
    [events],
  );

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', padding: 'var(--seer-space-4) var(--seer-space-6)', gap: 'var(--seer-space-3)' }}>
      {/* Toolbar */}
      <div className="event-filter-bar" style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-3)', flexShrink: 0, flexWrap: 'wrap' }}>
        <span style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
          {t('eventStream.filter')}
        </span>

        <label style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', fontSize: '13px', color: 'var(--t2)' }}>
          {t('eventStream.component')}
          <select style={selectStyle} value={component} onChange={e => setComponent(e.target.value)}>
            {COMPONENTS.map(c => <option key={c} value={c}>{c || t('eventStream.all')}</option>)}
          </select>
        </label>

        <label style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', fontSize: '13px', color: 'var(--t2)' }}>
          {t('eventStream.level')}
          <select style={selectStyle} value={level} onChange={e => setLevel(e.target.value as '' | EventLevel)}>
            {LEVELS.map(l => <option key={l} value={l}>{l || t('eventStream.all')}</option>)}
          </select>
        </label>

        <label style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', fontSize: '13px', color: 'var(--t2)' }}>
          {t('eventStream.eventType')}
          <select style={selectStyle} value={eventType} onChange={e => setEventType(e.target.value)}>
            <option value="">{t('eventStream.all')}</option>
            {seenTypes.map(([type, count]) => (
              <option key={type} value={type}>{type} ({count})</option>
            ))}
          </select>
        </label>

        <label style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', fontSize: '13px', color: 'var(--t2)' }}>
          {t('eventStream.sessionId')}
          <input
            style={inputStyle}
            placeholder="e.g. abc-123"
            value={sessionId}
            onChange={e => setSessionId(e.target.value)}
          />
        </label>

        <label data-testid="filter-tenant" style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', fontSize: '13px', color: 'var(--t2)' }}>
          {t('eventStream.tenant', 'Tenant')}
          <select style={selectStyle} value={tenant} onChange={e => setTenant(e.target.value)}>
            <option value="">{t('eventStream.all')}</option>
            {seenTenants.map(a => <option key={a} value={a}>{a}</option>)}
          </select>
        </label>

        {/* Pause / Resume */}
        <button
          onClick={() => setPaused(p => !p)}
          title={paused ? t('eventStream.resume') : t('eventStream.pause')}
          style={{
            ...selectStyle,
            background:  paused ? 'color-mix(in srgb, var(--wrn) 15%, transparent)' : 'var(--bg2)',
            borderColor: paused ? 'var(--wrn)' : 'var(--bd)',
            color:       paused ? 'var(--wrn)' : 'var(--t2)',
            fontFamily:  'var(--font)',
          }}
        >
          {paused ? `▶ ${t('eventStream.resume')}` : `⏸ ${t('eventStream.pause')}`}
        </button>

        <button
          onClick={clearEvents}
          style={{ ...selectStyle, background: 'var(--bg3)', color: 'var(--t2)' }}
        >
          {t('eventStream.clear')}
        </button>
      </div>

      {/* Stats bar */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-4)', fontSize: '12px', fontFamily: 'var(--mono)', flexShrink: 0, color: 'var(--t3)', flexWrap: 'wrap' }}>
        <span>{displayedEvents.length} {t('eventStream.events')}</span>
        <span>·</span>
        <span>{eventsPerSec}/sec</span>
        <span>·</span>
        <span style={{ color: wsColor }}>● {t(`ws.${status}`)}</span>
        {paused && (
          <>
            <span>·</span>
            <span style={{ color: 'var(--wrn)' }}>{t('eventStream.paused')}</span>
          </>
        )}
      </div>

      {/* Event breakdown by level + component */}
      {events.length > 0 && (
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', flexShrink: 0, flexWrap: 'wrap' }}>
          <span className="badge badge-info">{levelCounts['INFO'] ?? 0} INFO</span>
          <span className="badge badge-warn">{levelCounts['WARN'] ?? 0} WARN</span>
          <span className="badge badge-err">{levelCounts['ERROR'] ?? 0} ERR</span>
          <span style={{ color: 'var(--bd)', margin: '0 4px' }}>│</span>
          {Object.entries(compCounts)
            .sort((a, b) => b[1] - a[1])
            .map(([comp, count]) => (
              <span key={comp} className={`comp comp-${comp}`}>
                {comp} <span style={{ opacity: 0.7 }}>{count}</span>
              </span>
            ))}
        </div>
      )}

      {/* Log */}
      <div style={{ flex: 1, minHeight: 0 }}>
        <EventLog events={displayedEvents} filter={filter} maxHeight="100%" />
      </div>
    </div>
  );
}
