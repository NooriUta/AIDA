/**
 * UA-06: Active users widget — distinct session count in the 24-hour window.
 */
import React from 'react';

interface Props {
  activeSessionCount:  number;
  totalEventsInWindow: number;
  windowMs:            number;
}

function formatWindow(ms: number): string {
  const h = Math.round(ms / 3_600_000);
  return `${h}h`;
}

export function ActiveUsersWidget({ activeSessionCount, totalEventsInWindow, windowMs }: Props) {
  return (
    <div className="active-users-widget" data-testid="active-users-widget">
      <div className="stat-block">
        <span className="stat-value">{activeSessionCount}</span>
        <span className="stat-label">active sessions</span>
      </div>
      <div className="stat-block">
        <span className="stat-value">{totalEventsInWindow.toLocaleString()}</span>
        <span className="stat-label">events in {formatWindow(windowMs)} window</span>
      </div>
    </div>
  );
}
