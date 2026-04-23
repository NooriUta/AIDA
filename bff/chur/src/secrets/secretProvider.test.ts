/**
 * MTN-20 — Tests for the secret provider abstraction (env impl).
 */
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import {
  readSecret,
  readSecretOptional,
  refreshSecret,
  __envKeyForTests,
} from './secretProvider';

const SAVED = { ...process.env };

beforeEach(() => {
  // Each test starts with a clean slate
  for (const k of Object.keys(process.env)) {
    if (k.startsWith('SEER_SECRET_')) delete process.env[k];
  }
});
afterEach(() => {
  for (const k of Object.keys(process.env)) {
    if (k.startsWith('SEER_SECRET_') && !(k in SAVED)) delete process.env[k];
  }
});

describe('envKey mapping', () => {
  it('maps slash-separated path to SEER_SECRET_*', () => {
    expect(__envKeyForTests('session/dek')).toBe('SEER_SECRET_SESSION_DEK');
    expect(__envKeyForTests('frigg/password')).toBe('SEER_SECRET_FRIGG_PASSWORD');
    expect(__envKeyForTests('kc/admin/password')).toBe('SEER_SECRET_KC_ADMIN_PASSWORD');
  });

  it('handles dots and hyphens', () => {
    expect(__envKeyForTests('foo.bar-baz')).toBe('SEER_SECRET_FOO_BAR_BAZ');
  });
});

describe('readSecret (env provider)', () => {
  it('returns value when env var is set', async () => {
    process.env.SEER_SECRET_FRIGG_PASSWORD = 'hunter2';
    expect(await readSecret('frigg/password')).toBe('hunter2');
  });

  it('throws when env var is missing and no default', async () => {
    await expect(readSecret('nonexistent/secret')).rejects.toThrow(/secret missing/);
  });

  it('returns default when env var is missing and default provided', async () => {
    expect(await readSecret('nonexistent/secret', { default: 'fallback' })).toBe('fallback');
  });

  it('empty string default is respected', async () => {
    expect(await readSecret('nonexistent/secret', { default: '' })).toBe('');
  });
});

describe('readSecretOptional', () => {
  it('returns undefined for missing secret (no throw)', async () => {
    expect(await readSecretOptional('nonexistent/secret')).toBeUndefined();
  });

  it('returns value for set secret', async () => {
    process.env.SEER_SECRET_FOO = 'bar';
    expect(await readSecretOptional('foo')).toBe('bar');
  });
});

describe('refreshSecret', () => {
  it('is a no-op on env provider (resolves without throwing)', async () => {
    await expect(refreshSecret('any/path')).resolves.toBeUndefined();
  });
});
