/**
 * MTN-20 / ADR-MT-008 — Secret-read abstraction for Chur.
 *
 * Code never reads secrets directly; instead, all reads go through
 * {@link readSecret} which dispatches to one of:
 *   - env (default) — looks up `SEER_SECRET_<PATH_UPPER_SNAKE>` in process.env
 *   - vault        — Phase 2 (post-Round-4); HashiCorp Vault HTTP API
 *   - file         — Phase 3 K8s mounted volume (sealed-secrets / CSI)
 *
 * Pathing is unix-style: `session/dek`, `frigg/password`, `kc/admin/password`.
 * The env mapping camelCases / slashes to `SEER_SECRET_SESSION_DEK` etc.
 *
 * This module does NOT change behavior for existing env-based code — it
 * wraps the same `process.env` read. Future `SECRET_PROVIDER=vault` switches
 * the dispatch without touching call-sites.
 */

const PROVIDER = (process.env.SECRET_PROVIDER ?? 'env').toLowerCase();

function envKey(path: string): string {
  return 'SEER_SECRET_' + path.replace(/[/.\-]/g, '_').toUpperCase();
}

/** @throws if the secret cannot be read (provider error or missing with no default). */
export async function readSecret(path: string, options?: { default?: string }): Promise<string> {
  const v = await readSecretOptional(path);
  if (v !== undefined) return v;
  if (options && 'default' in options && options.default !== undefined) return options.default;
  throw new Error(`secret missing: ${path} (provider=${PROVIDER})`);
}

export async function readSecretOptional(path: string): Promise<string | undefined> {
  switch (PROVIDER) {
    case 'env':
      return process.env[envKey(path)];
    case 'vault':
      // Phase 2 — to be implemented. For now throw so deployment misconfig is loud.
      throw new Error('secret provider "vault" not yet implemented (ADR-MT-008 Phase 2)');
    case 'file':
      throw new Error('secret provider "file" not yet implemented (ADR-MT-008 Phase 3)');
    default:
      throw new Error(`unknown SECRET_PROVIDER: ${PROVIDER}`);
  }
}

/** Trigger eager re-read (rotation hook). No-op for env provider. */
export async function refreshSecret(_path: string): Promise<void> {
  // Env values are always fresh (process.env lookups are live); Vault impl
  // will invalidate its cache entry here.
  return;
}

/** @internal test helper for env-to-path mapping. */
export function __envKeyForTests(path: string): string {
  return envKey(path);
}

export const SECRET_PROVIDER = PROVIDER;
