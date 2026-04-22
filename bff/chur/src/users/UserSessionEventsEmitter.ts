/**
 * MTN-64 — Emit UserSessionEvents to FRIGG for forensic session audit.
 *
 * Retention: 180 days (Q-UA-3). Purge job lives in heimdall-backend
 * (UserSessionEventsPurgeJob — cron daily 03:00).
 *
 * Event types:
 *   - login / logout / refresh  — OIDC flow transitions
 *   - activity                  — debounced heartbeat (5m)
 *   - mfa_challenge             — KC MFA prompt
 *   - password_reset            — KC password reset landing
 *   - session_invalidated       — admin force-logout
 *
 * Fields per event:
 *   userId      — KC user sub
 *   sessionId   — chur sid
 *   ts          — unix ms
 *   eventType   — see above
 *   ipAddress   — remoteAddress
 *   userAgent   — from request header
 *   geoCountry  — nullable (maxmind stub — populated when provider wired)
 *   tenantAlias — active tenant at event time
 *   result      — success | failure
 *
 * The emitter is fire-and-forget — network errors are logged but never
 * block the auth flow (would lock users out during FRIGG outage).
 */
import { randomUUID } from 'node:crypto';
import { friggUsersSql } from './FriggUsersClient';

export type SessionEventType =
  | 'login' | 'logout' | 'refresh' | 'activity'
  | 'mfa_challenge' | 'password_reset' | 'session_invalidated'
  | 'tenant_switch';

export interface SessionEventInput {
  userId:      string;
  sessionId?:  string;
  eventType:   SessionEventType;
  ipAddress?:  string;
  userAgent?:  string;
  tenantAlias?: string;
  result?:     'success' | 'failure';
}

const EMITTER_FAIL_LOGGED = { flag: false };

export async function emitSessionEvent(input: SessionEventInput): Promise<void> {
  const id = randomUUID();
  const now = Date.now();
  try {
    await friggUsersSql(
      `INSERT INTO UserSessionEvents SET id = :id, userId = :userId, sessionId = :sessionId, ` +
      `ts = :ts, eventType = :eventType, ipAddress = :ipAddress, userAgent = :userAgent, ` +
      `tenantAlias = :tenantAlias, result = :result, configVersion = 1, updatedAt = :ts`,
      {
        id,
        userId:      input.userId,
        sessionId:   input.sessionId ?? null,
        ts:          now,
        eventType:   input.eventType,
        ipAddress:   input.ipAddress   ?? null,
        userAgent:   input.userAgent   ?? null,
        tenantAlias: input.tenantAlias ?? 'default',
        result:      input.result      ?? 'success',
      },
    );
  } catch (err) {
    // Log-once; don't block auth flow
    if (!EMITTER_FAIL_LOGGED.flag) {
      EMITTER_FAIL_LOGGED.flag = true;
      console.warn('[MTN-64] UserSessionEvents emit failed (further failures suppressed):',
                   (err as Error).message);
    }
  }
}
