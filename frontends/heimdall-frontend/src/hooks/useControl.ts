import { useCallback, useState } from 'react';
import type { SnapshotInfo } from 'aida-shared';
import { HEIMDALL_API } from '../api';

// In dev: pass X-Seer-Role: admin directly.
// In prod: Chur forwards role from session cookie.
const ADMIN_HEADERS: Record<string, string> = {
  'X-Seer-Role': 'admin',
};

export function useControl() {
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState<string | null>(null);

  const resetBuffer = useCallback(async (): Promise<boolean> => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${HEIMDALL_API}/control/reset`, {
        method:  'POST',
        headers: ADMIN_HEADERS,
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  const saveSnapshot = useCallback(async (name: string): Promise<boolean> => {
    if (!name.trim()) return false;
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(
        `${HEIMDALL_API}/control/snapshot?name=${encodeURIComponent(name)}`,
        { method: 'POST', headers: ADMIN_HEADERS },
      );
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  const listSnapshots = useCallback(async (): Promise<SnapshotInfo[]> => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${HEIMDALL_API}/control/snapshots`, {
        headers: ADMIN_HEADERS,
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return (await res.json()) as SnapshotInfo[];
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      return [];
    } finally {
      setLoading(false);
    }
  }, []);

  return { loading, error, resetBuffer, saveSnapshot, listSnapshots };
}
