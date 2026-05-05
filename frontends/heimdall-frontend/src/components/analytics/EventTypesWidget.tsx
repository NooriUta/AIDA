/**
 * Verdandi event type distribution widget — shows which LOOM features
 * users actually use, sorted by frequency.
 */
import type { EventTypeCount } from '../../api/analytics';

interface Props {
  eventTypes: EventTypeCount[];
}

/** Human-readable labels for known Verdandi event types. */
const LABELS: Record<string, string> = {
  LOOM_NODE_SELECTED:   'Node click',
  LOOM_VIEW_SLOW:       'Slow render',
  LOOM_FILTER_APPLIED:  'Filter applied',
  LOOM_SEARCH_USED:     'Search',
  KNOT_SESSION_OPENED:  'Session opened',
  KNOT_TAB_VIEWED:      'Tab viewed',
  LOOM_DRILL_DOWN:      'Drill-down',
  LOOM_DEPTH_CHANGED:   'Depth changed',
  LOOM_DIRECTION_CHANGED: 'Direction toggle',
};

export function EventTypesWidget({ eventTypes }: Props) {
  if (eventTypes.length === 0) {
    return (
      <div className="analytics-empty" data-testid="event-types-empty">
        No Verdandi events recorded yet.
      </div>
    );
  }

  const max = Math.max(...eventTypes.map(e => e.count), 1);
  const total = eventTypes.reduce((s, e) => s + e.count, 0);

  return (
    <div className="event-types-widget" data-testid="event-types-widget">
      {eventTypes.map(({ eventType, count }) => (
        <div key={eventType} className="event-type-row">
          <div className="event-type-label" title={eventType}>
            {LABELS[eventType] ?? eventType}
          </div>
          <div className="event-type-bar-wrap">
            <div
              className="event-type-bar"
              style={{ width: `${(count / max) * 100}%` }}
            />
            <span className="event-type-count">
              {count} <span className="event-type-pct">({total > 0 ? Math.round((count / total) * 100) : 0}%)</span>
            </span>
          </div>
        </div>
      ))}
    </div>
  );
}
