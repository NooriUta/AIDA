/**
 * MTN-40/41/42/46 — Tier 4 soft-security batch tests.
 */
import { describe, it, expect, beforeEach } from 'vitest';
import { sanitizeForAudit, sanitizeString } from './sanitizeForAudit';
import {
  checkRetentionChange,
  retentionChangeAudit,
  type RetentionChange,
} from '../admin/retentionGuard';
import {
  checkAuditRate,
  __resetAuditRateLimit,
} from './auditRateLimit';
import { __timingConfig } from './timingSafe403';

// ── MTN-41 sanitizeForAudit ──────────────────────────────────────────────────

describe('sanitizeString', () => {
  it('strips newlines (prevents fake event injection)', () => {
    const injected = 'Acme\n[ADMIN] tenant_deleted actor=victim';
    expect(sanitizeString(injected)).toBe('Acme[ADMIN] tenant_deleted actor=victim');
    expect(sanitizeString(injected)).not.toContain('\n');
  });

  it('strips ANSI escape sequences', () => {
    const injected = 'red\u001b[31m text\u001b[0m';
    expect(sanitizeString(injected)).toBe('red text');
  });

  it('strips zero-width chars', () => {
    expect(sanitizeString('a\u200bb\u200cc\u200dd\ufeff')).toBe('abcd');
  });

  it('truncates overlong strings', () => {
    const huge = 'x'.repeat(5_000);
    const out = sanitizeString(huge);
    expect(out.length).toBeLessThan(5_000);
    expect(out).toMatch(/\[truncated\]$/);
  });

  it('leaves normal strings untouched', () => {
    expect(sanitizeString('hello world')).toBe('hello world');
  });
});

describe('sanitizeForAudit (recursive)', () => {
  it('recurses into nested objects', () => {
    const dirty = { tenant: { name: 'a\nb', notes: 'clean' }, tags: ['x\u001b[0m'] };
    const clean = sanitizeForAudit(dirty);
    expect(clean.tenant.name).toBe('ab');
    expect(clean.tenant.notes).toBe('clean');
    expect(clean.tags[0]).toBe('x');
  });

  it('preserves primitive non-string values', () => {
    const dirty = { count: 42, enabled: true, ratio: 1.5 };
    expect(sanitizeForAudit(dirty)).toEqual(dirty);
  });

  it('sanitizes keys too', () => {
    const dirty = { 'bad\nkey': 'value' };
    const clean = sanitizeForAudit(dirty);
    expect(Object.keys(clean)).toContain('badkey');
    expect(Object.keys(clean)).not.toContain('bad\nkey');
  });

  it('handles null / undefined', () => {
    expect(sanitizeForAudit(null)).toBeNull();
    expect(sanitizeForAudit(undefined)).toBeUndefined();
  });
});

// ── MTN-42 retention guard ───────────────────────────────────────────────────

describe('checkRetentionChange', () => {
  const baseline: RetentionChange = {
    currentDataRetentionUntil:    2_000_000_000_000,
    currentArchiveRetentionUntil: 3_000_000_000_000,
    currentLegalHoldUntil:        null,
  };

  it('allows data retention change when no legal hold', () => {
    const r = checkRetentionChange({
      ...baseline,
      newDataRetentionUntil: 2_500_000_000_000,
    });
    expect(r.ok).toBe(true);
  });

  it('blocks data retention going below legal hold', () => {
    const r = checkRetentionChange({
      ...baseline,
      currentLegalHoldUntil: 2_500_000_000_000,
      newDataRetentionUntil: 2_100_000_000_000,  // below hold
    });
    expect(r.ok).toBe(false);
    if (!r.ok) {
      expect(r.status).toBe(403);
      expect(r.error).toBe('retention_below_legal_hold');
    }
  });

  it('allows data retention equal to legal hold', () => {
    const r = checkRetentionChange({
      ...baseline,
      currentLegalHoldUntil: 2_500_000_000_000,
      newDataRetentionUntil: 2_500_000_000_000,
    });
    expect(r.ok).toBe(true);
  });

  it('blocks archive retention downgrade without force flag', () => {
    const r = checkRetentionChange({
      ...baseline,
      newArchiveRetentionUntil: 2_500_000_000_000,  // less than current 3T
    });
    expect(r.ok).toBe(false);
    if (!r.ok) {
      expect(r.error).toBe('archive_retention_downgrade_blocked');
    }
  });

  it('allows archive retention downgrade with forceDowngrade=true', () => {
    const r = checkRetentionChange({
      ...baseline,
      newArchiveRetentionUntil: 2_500_000_000_000,
      forceDowngrade: true,
    });
    expect(r.ok).toBe(true);
  });

  it('allows archive retention INCREASE always', () => {
    const r = checkRetentionChange({
      ...baseline,
      newArchiveRetentionUntil: 4_000_000_000_000,
    });
    expect(r.ok).toBe(true);
  });
});

describe('retentionChangeAudit', () => {
  it('captures full before/after snapshot', () => {
    const snapshot = retentionChangeAudit({
      currentDataRetentionUntil:    2_000,
      currentArchiveRetentionUntil: 3_000,
      currentLegalHoldUntil:        1_500,
      newDataRetentionUntil:        2_500,
    });
    expect((snapshot.before as any).dataRetentionUntil).toBe(2_000);
    expect((snapshot.after as any).dataRetentionUntil).toBe(2_500);
    // archive unchanged should carry current
    expect((snapshot.after as any).archiveRetentionUntil).toBe(3_000);
  });
});

// ── MTN-46 audit rate limit ──────────────────────────────────────────────────

describe('checkAuditRate', () => {
  beforeEach(() => { __resetAuditRateLimit(); });

  it('first 1000 events emit normally', () => {
    for (let i = 0; i < 1_000; i++) {
      expect(checkAuditRate('acme').action).toBe('EMIT');
    }
  });

  it('1001..10000 are EMIT_SAMPLED', () => {
    for (let i = 0; i < 1_000; i++) checkAuditRate('acme');
    expect(checkAuditRate('acme').action).toBe('EMIT_SAMPLED');
    for (let i = 0; i < 100; i++) checkAuditRate('acme');
    expect(checkAuditRate('acme').action).toBe('EMIT_SAMPLED');
  });

  it('first flood event emits DROP_WITH_FLOOD_SIGNAL, subsequent DROP', () => {
    for (let i = 0; i < 10_000; i++) checkAuditRate('acme');
    expect(checkAuditRate('acme').action).toBe('DROP_WITH_FLOOD_SIGNAL');
    expect(checkAuditRate('acme').action).toBe('DROP');
    expect(checkAuditRate('acme').action).toBe('DROP');
  });

  it('separate tenants have separate buckets', () => {
    for (let i = 0; i < 1_000; i++) checkAuditRate('acme');
    // acme is at cap, beta starts fresh
    expect(checkAuditRate('acme').action).toBe('EMIT_SAMPLED');
    expect(checkAuditRate('beta').action).toBe('EMIT');
  });

  it('blank tenant falls back to default bucket', () => {
    // Should not throw
    expect(checkAuditRate('').action).toBe('EMIT');
  });
});

// ── MTN-40 timing-safe ───────────────────────────────────────────────────────

describe('timingSafe403 config', () => {
  it('has sane delay bounds', () => {
    expect(__timingConfig.min).toBeGreaterThan(0);
    expect(__timingConfig.max).toBeGreaterThan(__timingConfig.min);
    expect(__timingConfig.max).toBeLessThan(1_000); // under 1s for UX
  });
});
