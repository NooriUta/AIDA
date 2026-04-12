/**
 * Fire-and-forget helper to emit events to Heimdall.
 *
 * Never throws — if Heimdall is unreachable, the error is silently swallowed
 * so Chur's own request handling is never affected.
 */

const HEIMDALL_URL = (process.env.HEIMDALL_URL ?? 'http://localhost:9093').replace(/\/$/, '');

export function emitToHeimdall(
  eventType: string,
  level:     'INFO' | 'WARN' | 'ERROR',
  payload:   Record<string, unknown>,
  sessionId?: string,
): void {
  const body = JSON.stringify({
    timestamp:       Date.now(),
    sourceComponent: 'chur',
    eventType,
    level,
    sessionId:       sessionId ?? null,
    correlationId:   null,
    durationMs:      0,
    payload,
  });

  fetch(`${HEIMDALL_URL}/events`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body,
    signal: AbortSignal.timeout(2000),
  }).catch(() => { /* fire-and-forget */ });
}
