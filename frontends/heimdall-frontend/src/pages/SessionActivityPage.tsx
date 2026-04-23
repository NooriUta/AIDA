/**
 * Round 5 — MTN-64 FE — recent UserSessionEvents for current user.
 *
 * Reads /me/session-activity. Retention: 180 days (Q-UA-3).
 */
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { usePageTitle } from '../hooks/usePageTitle';

interface SessionEvent {
  eventType:   string;
  ts:          number;
  ipAddress?:  string;
  userAgent?:  string;
  tenantAlias?: string;
  result?:     string;
}

const CHUR_BASE = '/chur';

export default function SessionActivityPage() {
  const { t } = useTranslation();
  usePageTitle(t('nav.sessionActivity', 'Session activity'));

  const [events, setEvents] = useState<SessionEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await fetch(`${CHUR_BASE}/me/session-activity`, { credentials: 'include' });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const body = await res.json() as { events: SessionEvent[] };
        setEvents(body.events ?? []);
      } catch (e) {
        setError(e instanceof Error ? e.message : String(e));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <div style={{ padding: '24px', maxWidth: 960 }}>
      <h2 style={{ margin: 0, marginBottom: 8 }}>{t('nav.sessionActivity', 'Активность сессий')}</h2>
      <p style={{ color: 'var(--t3)', marginTop: 0, marginBottom: 20 }}>
        {t('sessionActivity.desc', 'История аутентификаций (login/logout/refresh) последние 180 дней.')}
      </p>

      {loading && <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>}
      {error && <p style={{ color: 'var(--danger)' }}>⚠ {error}</p>}

      {!loading && !error && events.length === 0 && (
        <p style={{ color: 'var(--t3)' }}>{t('sessionActivity.empty', 'Нет событий.')}</p>
      )}

      {!loading && events.length > 0 && (
        <table className="data-table" style={{ width: '100%', fontSize: '13px' }}>
          <thead>
            <tr>
              <th>{t('sessionActivity.col.ts', 'Время')}</th>
              <th>{t('sessionActivity.col.eventType', 'Событие')}</th>
              <th>{t('sessionActivity.col.tenant', 'Тенант')}</th>
              <th>{t('sessionActivity.col.ip', 'IP')}</th>
              <th>{t('sessionActivity.col.result', 'Результат')}</th>
            </tr>
          </thead>
          <tbody>
            {events.map((ev, i) => (
              <tr key={i}>
                <td style={{ color: 'var(--t3)', fontFamily: 'var(--mono)' }}>
                  {new Date(ev.ts).toLocaleString()}
                </td>
                <td><code>{ev.eventType}</code></td>
                <td>{ev.tenantAlias ?? '—'}</td>
                <td style={{ fontFamily: 'var(--mono)' }}>{ev.ipAddress ?? '—'}</td>
                <td>
                  <span className={`badge ${ev.result === 'success' ? 'badge-suc' : 'badge-err'}`}>
                    {ev.result ?? '—'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
