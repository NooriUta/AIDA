import { describe, it, expect } from 'vitest';
import { EVENT_LABELS, formatPayload, levelClass } from './eventFormat';
import type { HeimdallEvent } from 'aida-shared';

function mkEvent(overrides: Partial<HeimdallEvent>): HeimdallEvent {
  return {
    timestamp:       Date.now(),
    sourceComponent: 'hound',
    eventType:       'QUERY_RECEIVED',
    level:           'INFO',
    sessionId:       null,
    userId:          null,
    correlationId:   null,
    durationMs:      0,
    payload:         {},
    ...overrides,
  };
}

describe('EVENT_LABELS', () => {
  it('maps REQUEST_RECEIVED', () => {
    expect(EVENT_LABELS['REQUEST_RECEIVED']).toBe('GraphQL request');
  });
  it('maps AUTH_LOGIN_SUCCESS', () => {
    expect(EVENT_LABELS['AUTH_LOGIN_SUCCESS']).toBe('Login');
  });
  it('maps AUTH_LOGIN_FAILED', () => {
    expect(EVENT_LABELS['AUTH_LOGIN_FAILED']).toBe('Login failed');
  });
  it('maps AUTH_LOGOUT', () => {
    expect(EVENT_LABELS['AUTH_LOGOUT']).toBe('Logout');
  });
});

describe('formatPayload', () => {
  it('AUTH_LOGIN_SUCCESS → username (role)', () => {
    const e = mkEvent({
      eventType: 'AUTH_LOGIN_SUCCESS',
      payload:   { username: 'alice', role: 'admin' },
    });
    expect(formatPayload(e)).toBe('alice (admin)');
  });

  it('AUTH_LOGIN_FAILED → username · invalid credentials', () => {
    const e = mkEvent({
      eventType: 'AUTH_LOGIN_FAILED',
      payload:   { username: 'bob' },
    });
    expect(formatPayload(e)).toBe('bob · invalid credentials');
  });

  it('AUTH_LOGOUT → username', () => {
    const e = mkEvent({
      eventType: 'AUTH_LOGOUT',
      payload:   { username: 'carol' },
    });
    expect(formatPayload(e)).toBe('carol');
  });

  it('LLM_RESPONSE_READY → tokens + duration', () => {
    const e = mkEvent({
      eventType:  'LLM_RESPONSE_READY',
      durationMs: 1234,
      payload:    { tokens_in: 100, tokens_out: 50 },
    });
    expect(formatPayload(e)).toBe('100→50 tokens 1234ms');
  });

  it('REQUEST_RECEIVED → op name', () => {
    const e = mkEvent({
      eventType: 'REQUEST_RECEIVED',
      payload:   { op: 'GetAtoms' },
    });
    expect(formatPayload(e)).toBe('GetAtoms');
  });

  it('unknown type with payload → key:value pairs', () => {
    const e = mkEvent({
      eventType: 'UNKNOWN_EVENT',
      payload:   { foo: 'bar', count: 3 },
    });
    const result = formatPayload(e);
    expect(result).toContain('foo:"bar"');
    expect(result).toContain('count:3');
  });

  it('unknown type with empty payload → em dash', () => {
    const e = mkEvent({ eventType: 'EMPTY_EVENT', payload: {} });
    expect(formatPayload(e)).toBe('—');
  });
});

describe('levelClass', () => {
  it('INFO → badge-info',    () => expect(levelClass('INFO')).toBe('badge-info'));
  it('WARN → badge-warn',    () => expect(levelClass('WARN')).toBe('badge-warn'));
  it('ERROR → badge-err',    () => expect(levelClass('ERROR')).toBe('badge-err'));
  it('unknown → badge-neutral', () => expect(levelClass('DEBUG')).toBe('badge-neutral'));
});
