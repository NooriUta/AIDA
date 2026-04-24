/**
 * MTN-59 — Tests for the session token AES-256-GCM envelope.
 */
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { randomBytes } from 'node:crypto';
import {
  encryptToken,
  decryptToken,
  isCiphertext,
  __resetKeyCacheForTests,
} from './encryption';

const DEMO_TOKEN = 'eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.sig';

describe('encryption — with DEK configured', () => {
  let originalEnv: string | undefined;

  beforeEach(() => {
    originalEnv = process.env.AIDA_SESSION_DEK_KEY;
    process.env.AIDA_SESSION_DEK_KEY = randomBytes(32).toString('base64');
    __resetKeyCacheForTests();
  });
  afterEach(() => {
    if (originalEnv === undefined) delete process.env.AIDA_SESSION_DEK_KEY;
    else process.env.AIDA_SESSION_DEK_KEY = originalEnv;
    __resetKeyCacheForTests();
  });

  it('roundtrip returns the original plaintext', () => {
    const ct = encryptToken(DEMO_TOKEN);
    expect(ct).not.toBe(DEMO_TOKEN);
    expect(isCiphertext(ct)).toBe(true);
    expect(decryptToken(ct)).toBe(DEMO_TOKEN);
  });

  it('produces different ciphertext for identical plaintext (random IV)', () => {
    const a = encryptToken(DEMO_TOKEN);
    const b = encryptToken(DEMO_TOKEN);
    expect(a).not.toBe(b);
    expect(decryptToken(a)).toBe(DEMO_TOKEN);
    expect(decryptToken(b)).toBe(DEMO_TOKEN);
  });

  it('ciphertext wire format has 4 dot-separated segments starting with version', () => {
    const ct = encryptToken(DEMO_TOKEN);
    const parts = ct.split('.');
    expect(parts).toHaveLength(4);
    expect(parts[0]).toBe('1');
  });

  it('rejects tampered ciphertext via GCM auth tag', () => {
    const ct = encryptToken(DEMO_TOKEN);
    const parts = ct.split('.');
    // Flip a char in the ct segment
    const ctSegment = parts[2];
    const mutated = ctSegment.charAt(0) === 'A' ? 'B' + ctSegment.slice(1) : 'A' + ctSegment.slice(1);
    const tampered = [parts[0], parts[1], mutated, parts[3]].join('.');
    expect(() => decryptToken(tampered)).toThrow();
  });

  it('rejects unsupported version', () => {
    const ct = encryptToken(DEMO_TOKEN);
    const parts = ct.split('.');
    const badVersion = ['99', parts[1], parts[2], parts[3]].join('.');
    expect(() => decryptToken(badVersion)).toThrow(/Unsupported ciphertext version/);
  });

  it('rejects malformed segments (looks like a blob but has bad IV/tag lengths)', () => {
    // Must pass isCiphertext probe (>= 16 chars + 4 segments) so decryptToken
    // actually attempts to parse. Short iv/tag will fail length check.
    expect(() => decryptToken('1.aaaaaaaa.bbbbbbbbbb.cccccccc')).toThrow();
  });

  it('passes through plaintext (legacy) if not a ciphertext blob', () => {
    expect(decryptToken(DEMO_TOKEN)).toBe(DEMO_TOKEN);
    expect(isCiphertext(DEMO_TOKEN)).toBe(false);
  });

  it('empty and nullish inputs pass through', () => {
    expect(encryptToken('')).toBe('');
    expect(decryptToken('')).toBe('');
  });

  it('rejects key of wrong length on load', () => {
    process.env.AIDA_SESSION_DEK_KEY = Buffer.alloc(16).toString('base64');
    __resetKeyCacheForTests();
    expect(() => encryptToken(DEMO_TOKEN)).toThrow(/must be 32 bytes/);
  });
});

describe('encryption — without DEK (dev plaintext fallback)', () => {
  let originalEnv: string | undefined;

  beforeEach(() => {
    originalEnv = process.env.AIDA_SESSION_DEK_KEY;
    delete process.env.AIDA_SESSION_DEK_KEY;
    __resetKeyCacheForTests();
  });
  afterEach(() => {
    if (originalEnv !== undefined) process.env.AIDA_SESSION_DEK_KEY = originalEnv;
    __resetKeyCacheForTests();
  });

  it('encryptToken returns plaintext unchanged', () => {
    expect(encryptToken(DEMO_TOKEN)).toBe(DEMO_TOKEN);
  });

  it('decryptToken of plaintext returns plaintext', () => {
    expect(decryptToken(DEMO_TOKEN)).toBe(DEMO_TOKEN);
  });

  it('decryptToken of valid-looking blob throws (no key to decrypt with)', () => {
    // Simulate a blob produced in a previous run where DEK was set
    const fakeBlob = '1.abcdefghijkl.mnopqrstuvwx.yz0123456789';
    expect(() => decryptToken(fakeBlob)).toThrow(/AIDA_SESSION_DEK_KEY missing/);
  });
});

describe('isCiphertext', () => {
  it('returns true for well-formed blobs (>= 16 chars)', () => {
    expect(isCiphertext('1.aaaaaaaa.bbbbbbbbb.cccccccc')).toBe(true);
  });
  it('returns false for JWT-like plaintext', () => {
    expect(isCiphertext(DEMO_TOKEN)).toBe(false);
  });
  it('returns false for empty / short', () => {
    expect(isCiphertext('')).toBe(false);
    expect(isCiphertext('short')).toBe(false);
    expect(isCiphertext('1.ab.cd.ef')).toBe(false); // too short to be real ciphertext
  });
});
