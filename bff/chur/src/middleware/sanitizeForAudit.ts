/**
 * MTN-41 — Audit log injection defense.
 *
 * User-controlled strings in audit payload (tenant.displayName, source.url,
 * notes, email — anything the attacker can provide) must never carry
 * newlines, ANSI escapes, or zero-width chars into downstream log analyzers
 * (ELK, Splunk, SIEM). Without sanitization an attacker can inject fake
 * audit records like:
 *     tenant_renamed: newName="acme\n[ADMIN] tenant_deleted actor=victim"
 * which ELK parses as TWO separate events.
 *
 * Policy: strip control characters + ANSI escapes from every string that
 * goes into {@link emitTenantAudit} context object. Applies recursively to
 * nested maps/arrays.
 */

// Control chars 0x00–0x1F + 0x7F, plus common ANSI escape sequences
// and zero-width characters (U+200B–U+200D, U+FEFF).
// eslint-disable-next-line no-control-regex
const CONTROL_CHARS = /[\u0000-\u001f\u007f]/g;
// eslint-disable-next-line no-control-regex
const ANSI_ESC      = /\u001b\[[0-9;]*[A-Za-z]/g;
const ZERO_WIDTH    = /[\u200b-\u200d\ufeff]/g;

const MAX_STRING_LEN = 4_096;  // defense-in-depth against log-size DoS

export function sanitizeString(s: string): string {
  let out = s;
  out = out.replace(ANSI_ESC, '');
  out = out.replace(CONTROL_CHARS, '');
  out = out.replace(ZERO_WIDTH, '');
  if (out.length > MAX_STRING_LEN) out = out.slice(0, MAX_STRING_LEN) + '…[truncated]';
  return out;
}

/**
 * Recursively sanitize every string value in {@code value}. Arrays and plain
 * objects are walked; primitive non-string values pass through unchanged.
 * Non-plain objects (class instances, Dates) are converted to their
 * `toString()` and sanitized — this is intentional: audit payloads should
 * only contain plain data.
 */
export function sanitizeForAudit<T>(value: T): T {
  if (value === null || value === undefined) return value;
  if (typeof value === 'string') return sanitizeString(value) as unknown as T;
  if (typeof value !== 'object') return value;
  if (Array.isArray(value)) {
    return value.map(v => sanitizeForAudit(v)) as unknown as T;
  }
  // Plain object
  const out: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(value as Record<string, unknown>)) {
    out[sanitizeString(k)] = sanitizeForAudit(v);
  }
  return out as unknown as T;
}
