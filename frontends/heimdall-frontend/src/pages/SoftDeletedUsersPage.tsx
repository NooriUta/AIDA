/**
 * Round 5 — MTN-61 FE — superadmin view of soft-deleted users + restore.
 */
import { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { usePageTitle } from '../hooks/usePageTitle';

interface SoftDeletedRow {
  userId:             string;
  deletedAt?:         number;
  deletedBy?:         string;
  deletionReason?:    string;
  dataRetentionUntil?: number;
  legalHoldUntil?:    number | null;
}

const CHUR_BASE = '/chur';
const CSRF_HEADERS = { 'Content-Type': 'application/json', Origin: window.location.origin };

export default function SoftDeletedUsersPage() {
  const { t } = useTranslation();
  usePageTitle(t('nav.softDeletedUsers', 'Soft-deleted users'));

  const [rows, setRows] = useState<SoftDeletedRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${CHUR_BASE}/api/admin/users/soft-deleted`, { credentials: 'include' });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const body = await res.json() as { users: SoftDeletedRow[] };
      setRows(body.users ?? []);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const onRestore = async (userId: string) => {
    if (!confirm(t('softDelete.confirmRestore', 'Восстановить пользователя?'))) return;
    try {
      const res = await fetch(`${CHUR_BASE}/api/admin/users/${encodeURIComponent(userId)}/restore`, {
        method: 'POST', credentials: 'include', headers: CSRF_HEADERS,
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      await load();
    } catch (e) {
      alert(String(e));
    }
  };

  const onLegalHold = async (userId: string) => {
    const days = prompt(t('softDelete.holdDaysPrompt', 'Установить legal-hold на N дней:'), '365');
    if (!days) return;
    const n = parseInt(days, 10);
    if (!Number.isFinite(n) || n <= 0) return;
    const legalHoldUntil = Date.now() + n * 24 * 60 * 60 * 1000;
    try {
      const res = await fetch(`${CHUR_BASE}/api/admin/users/${encodeURIComponent(userId)}/legal-hold`, {
        method: 'POST', credentials: 'include', headers: CSRF_HEADERS,
        body: JSON.stringify({ legalHoldUntil }),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      await load();
    } catch (e) {
      alert(String(e));
    }
  };

  const daysUntil = (ms?: number) => {
    if (!ms) return '—';
    const diff = ms - Date.now();
    if (diff <= 0) return t('softDelete.expired', 'истекло');
    return t('softDelete.daysLeft', '{{n}} дн.', { n: Math.ceil(diff / (24 * 60 * 60 * 1000)) });
  };

  return (
    <div style={{ padding: '24px', maxWidth: 1200 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>{t('nav.softDeletedUsers', 'Soft-deleted users')}</h2>
        <button className="btn-secondary" onClick={load} disabled={loading}>
          {t('action.refresh', 'Обновить')}
        </button>
      </div>
      <p style={{ color: 'var(--t3)', marginTop: 0, marginBottom: 20 }}>
        {t('softDelete.desc', 'Список пользователей, отключённых через soft-delete. После dataRetentionUntil user будет окончательно удалён, если нет legalHoldUntil.')}
      </p>

      {loading && <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>}
      {error && <p style={{ color: 'var(--danger)' }}>⚠ {error}</p>}

      {!loading && !error && rows.length === 0 && (
        <p style={{ color: 'var(--t3)' }}>{t('softDelete.empty', 'Нет soft-deleted пользователей.')}</p>
      )}

      {!loading && rows.length > 0 && (
        <table className="data-table" style={{ width: '100%', fontSize: '13px' }}>
          <thead>
            <tr>
              <th>User ID</th>
              <th>{t('softDelete.col.deletedAt', 'Удалён')}</th>
              <th>{t('softDelete.col.by', 'Кем')}</th>
              <th>{t('softDelete.col.reason', 'Причина')}</th>
              <th>{t('softDelete.col.retention', 'До purge')}</th>
              <th>{t('softDelete.col.hold', 'Legal hold')}</th>
              <th>{t('softDelete.col.actions', 'Действия')}</th>
            </tr>
          </thead>
          <tbody>
            {rows.map(r => (
              <tr key={r.userId}>
                <td style={{ fontFamily: 'var(--mono)', fontSize: '11px' }}>
                  {r.userId.slice(0, 8)}…
                </td>
                <td>{r.deletedAt ? new Date(r.deletedAt).toLocaleDateString() : '—'}</td>
                <td>{r.deletedBy ?? '—'}</td>
                <td><code>{r.deletionReason ?? '—'}</code></td>
                <td>{daysUntil(r.dataRetentionUntil)}</td>
                <td>
                  {r.legalHoldUntil
                    ? <span className="badge badge-warn">{new Date(r.legalHoldUntil).toLocaleDateString()}</span>
                    : '—'}
                </td>
                <td>
                  <div style={{ display: 'flex', gap: 4 }}>
                    <button className="btn-secondary" onClick={() => onRestore(r.userId)}>
                      {t('action.restore', 'Восстановить')}
                    </button>
                    <button className="btn-secondary" onClick={() => onLegalHold(r.userId)}>
                      {t('action.legalHold', 'Legal hold')}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
