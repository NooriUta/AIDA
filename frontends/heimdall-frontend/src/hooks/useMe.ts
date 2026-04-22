/**
 * Round 5 — Self-service hook for /me/profile · /me/preferences · /me/notifications.
 *
 * Returns `{ data, loading, error, save, reload }`. `save(patch)` issues a PUT
 * with `expectedConfigVersion` from the currently-loaded row for CAS
 * semantics (MTN-27 extension pattern). On 409 `config_version_conflict`
 * the hook auto-reloads and surfaces a "config changed elsewhere" error
 * via the `conflict` flag so the caller can prompt the user.
 *
 * Cross-MF sync: when `endpoint === '/me/preferences'` the hook also
 * broadcasts changes via `window.aida:prefs` event so verdandi / shell
 * pick up theme/lang changes immediately (compatible with existing
 * sharedPrefsStore).
 */
import { useCallback, useEffect, useRef, useState } from 'react';

const CHUR_BASE = '/chur';  // shell-style routing; Vite dev proxy strips prefix

export interface MeRow<T extends Record<string, unknown> = Record<string, unknown>> {
  userId:         string;
  configVersion:  number;
  updatedAt:      number;
  reserved_acl_v2: string | null;
  data:           T;
}

export type MeEndpoint =
  | '/me/profile'
  | '/me/preferences'
  | '/me/notifications';

export interface UseMeResult<T extends Record<string, unknown>> {
  data:     T | null;
  row:      MeRow<T> | null;
  loading:  boolean;
  error:    string | null;
  conflict: { expected: number; current: number } | null;
  save:     (patch: T) => Promise<{ ok: boolean }>;
  reload:   () => void;
}

export function useMe<T extends Record<string, unknown> = Record<string, unknown>>(
  endpoint: MeEndpoint,
): UseMeResult<T> {
  const [row, setRow]         = useState<MeRow<T> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState<string | null>(null);
  const [conflict, setConflict] = useState<{ expected: number; current: number } | null>(null);
  const abortRef              = useRef<AbortController | null>(null);

  const reload = useCallback(() => {
    abortRef.current?.abort();
    const ctrl = new AbortController();
    abortRef.current = ctrl;
    setLoading(true);
    setError(null);
    setConflict(null);

    fetch(`${CHUR_BASE}${endpoint}`, {
      credentials: 'include',
      signal:       ctrl.signal,
    })
      .then(async res => {
        if (!res.ok) {
          if (res.status === 404) {
            setRow(null);
            return;
          }
          throw new Error(`HTTP ${res.status}`);
        }
        const body = await res.json() as MeRow<T>;
        setRow(body);
      })
      .catch(err => {
        if (err instanceof DOMException && err.name === 'AbortError') return;
        setError(err instanceof Error ? err.message : String(err));
      })
      .finally(() => setLoading(false));
  }, [endpoint]);

  useEffect(() => {
    reload();
    return () => abortRef.current?.abort();
  }, [reload]);

  const save = useCallback(async (patch: T): Promise<{ ok: boolean }> => {
    const expected = row?.configVersion ?? 0;
    setError(null);
    setConflict(null);
    try {
      const res = await fetch(`${CHUR_BASE}${endpoint}`, {
        method:      'PUT',
        credentials: 'include',
        headers:     { 'Content-Type': 'application/json' },
        body:        JSON.stringify({ data: patch, expectedConfigVersion: expected }),
      });
      if (res.status === 409) {
        const body = await res.json() as { currentConfigVersion?: number };
        setConflict({ expected, current: body.currentConfigVersion ?? 0 });
        reload();
        return { ok: false };
      }
      if (!res.ok) {
        const body = await res.json().catch(() => ({} as { error?: string }));
        throw new Error(body.error ?? `HTTP ${res.status}`);
      }
      // MTN-05 / sharedPrefsStore cross-MF broadcast for preferences
      if (endpoint === '/me/preferences') {
        window.dispatchEvent(new CustomEvent('aida:prefs', { detail: patch }));
      }
      reload();
      return { ok: true };
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
      return { ok: false };
    }
  }, [endpoint, row, reload]);

  return {
    data:    (row?.data ?? null) as T | null,
    row:     row,
    loading,
    error,
    conflict,
    save,
    reload,
  };
}
