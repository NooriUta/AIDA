/**
 * MTN-59 — AES-256-GCM envelope encryption for session tokens.
 *
 * Threat model: an operator with read access to the FRIGG ArcadeDB cluster
 * (backup, debug dump, misconfigured replica) obtains plaintext
 * accessToken / refreshToken for every active session → full session
 * takeover of every logged-in user. Encrypting at rest with a DEK held
 * outside the database removes this class of incident.
 *
 * Key material:
 *   - AIDA_SESSION_DEK_KEY — 32 raw bytes, base64-encoded (256-bit AES key).
 *     Dev default: empty string (plaintext fallback, logged once with a
 *     loud warning).
 *   - KMS / Vault migration → SPRINT_MT_NEXT_BACKLOG_v3 (MTN-20). Until
 *     then, rotation = env var rotation + chur rolling restart.
 *
 * Wire format of ciphertext:
 *   <version:1byte>.<ivBase64>.<ctBase64>.<tagBase64>
 *   - version (currently 1) so future KMS-keyRef format can coexist
 *   - IV is 12 bytes (GCM standard), random per-encrypt
 *   - tag is 16 bytes (GCM auth tag)
 * All three base64 segments use url-safe alphabet (no padding) so the
 * blob is friendly to URL contexts if we ever need to carry it there.
 */
import { createCipheriv, createDecipheriv, randomBytes } from 'node:crypto';

const VERSION = 1;
const IV_BYTES = 12;
const TAG_BYTES = 16;

let keyCached: Buffer | null = null;
let plaintextWarningEmitted = false;

function loadKey(): Buffer | null {
  if (keyCached !== null) return keyCached;
  const raw = process.env.AIDA_SESSION_DEK_KEY;
  if (!raw) {
    if (!plaintextWarningEmitted) {
      plaintextWarningEmitted = true;

      console.warn('[chur MTN-59] AIDA_SESSION_DEK_KEY not set — session tokens stored in PLAINTEXT. DEV ONLY.');
    }
    return null;
  }
  const decoded = Buffer.from(raw, 'base64');
  if (decoded.length !== 32) {
    throw new Error(`AIDA_SESSION_DEK_KEY must be 32 bytes (256-bit) base64-encoded, got ${decoded.length}`);
  }
  keyCached = decoded;
  return keyCached;
}

function b64url(buf: Buffer): string {
  return buf.toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}
function fromB64url(s: string): Buffer {
  const padded = s.replace(/-/g, '+').replace(/_/g, '/') + '='.repeat((4 - (s.length % 4)) % 4);
  return Buffer.from(padded, 'base64');
}

/**
 * Encrypt `plaintext` with AES-256-GCM. Returns the ciphertext blob as a
 * versioned dot-separated base64url string. When no DEK is configured
 * (dev mode), returns plaintext unchanged — caller is responsible for
 * routing plaintext through the same field (migration path).
 */
export function encryptToken(plaintext: string): string {
  if (!plaintext) return plaintext;
  const key = loadKey();
  if (!key) return plaintext;

  const iv = randomBytes(IV_BYTES);
  const cipher = createCipheriv('aes-256-gcm', key, iv);
  const ct = Buffer.concat([cipher.update(plaintext, 'utf8'), cipher.final()]);
  const tag = cipher.getAuthTag();
  return `${VERSION}.${b64url(iv)}.${b64url(ct)}.${b64url(tag)}`;
}

/**
 * Decrypt a ciphertext blob produced by {@link encryptToken}. If the input
 * is not in the versioned format, assume legacy plaintext and return as-is
 * (safe migration: sessions written before MTN-59 stayed plaintext and
 * invalidate naturally after the 5m JWT expiry).
 */
export function decryptToken(ciphertext: string): string {
  if (!ciphertext) return ciphertext;

  if (!isCiphertext(ciphertext)) {
    // Legacy plaintext session — return unchanged
    return ciphertext;
  }
  const key = loadKey();
  if (!key) {
    throw new Error('AIDA_SESSION_DEK_KEY missing, cannot decrypt ciphertext blob');
  }
  const parts = ciphertext.split('.');
  if (parts.length !== 4) {
    throw new Error('Malformed ciphertext blob (expected 4 dot-separated segments)');
  }
  const [vRaw, ivB64, ctB64, tagB64] = parts;
  const version = Number(vRaw);
  if (version !== VERSION) {
    throw new Error(`Unsupported ciphertext version: ${version}`);
  }
  const iv = fromB64url(ivB64);
  const ct = fromB64url(ctB64);
  const tag = fromB64url(tagB64);
  if (iv.length !== IV_BYTES || tag.length !== TAG_BYTES) {
    throw new Error(`Malformed ciphertext: iv=${iv.length}, tag=${tag.length}`);
  }
  const decipher = createDecipheriv('aes-256-gcm', key, iv);
  decipher.setAuthTag(tag);
  const pt = Buffer.concat([decipher.update(ct), decipher.final()]);
  return pt.toString('utf8');
}

/**
 * Cheap probe: does `value` look like a MTN-59 ciphertext blob? Used by the
 * session store to decide whether to decrypt or pass through legacy plaintext
 * during the cut-over window.
 */
export function isCiphertext(value: string): boolean {
  if (!value || value.length < 16) return false;
  // Allow only our tiny alphabet of versioned blob characters
  return /^\d+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/.test(value);
}

/** @internal test helper — clears the memoised key so loadKey() re-reads env. */
export function __resetKeyCacheForTests(): void {
  keyCached = null;
  plaintextWarningEmitted = false;
}
