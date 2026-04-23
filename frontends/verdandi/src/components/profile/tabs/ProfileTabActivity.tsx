import { memo, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

interface SessionEvent {
  eventType:   string;
  ts:          number;
  ipAddress?:  string;
  userAgent?:  string;
  tenantAlias?: string;
  result?:     string;
}

const EVENT_ICON: Record<string, string> = {
  login:               '🔐',
  logout:              '🚪',
  tenant_switch:       '🔀',
  session_invalidated: '⚠️',
  refresh:             '🔄',
  mfa_challenge:       '🛡',
  password_reset:      '🔑',
  activity:            '📋',
};

function formatTs(ts: number): string {
  if (!ts) return '—';
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  }).format(new Date(ts));
}

export const ProfileTabActivity = memo(() => {
  const { t } = useTranslation();
  const [events,  setEvents]  = useState<SessionEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetch('/me/session-activity', { credentials: 'include' })
      .then(r => {
        if (!r.ok) throw new Error(`${r.status}`);
        return r.json();
      })
      .then((data: { events?: SessionEvent[] }) => {
        if (!cancelled) setEvents(data.events ?? []);
      })
      .catch(() => { if (!cancelled) setError(t('profile.loadError')); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [t]);

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.activity')}
      </div>

      <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '12px', textTransform: 'uppercase' }}>
        {t('profile.activity.recent')}
      </div>

      {loading && (
        <div style={{ color: 'var(--t3)', fontSize: '12px' }}>{t('profile.loading')}</div>
      )}

      {error && (
        <div style={{ color: 'var(--danger)', fontSize: '12px' }}>{error}</div>
      )}

      {!loading && !error && events.length === 0 && (
        <div style={{ color: 'var(--t3)', fontSize: '12px' }}>{t('profile.activity.empty')}</div>
      )}

      {!loading && !error && events.length > 0 && (
        <div>
          {events.map((ev, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', padding: '10px 0', borderBottom: '1px solid var(--bd)' }}>
              <div style={{ width: '28px', height: '28px', borderRadius: 'var(--seer-radius-sm)', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '13px', background: 'var(--bg3)', marginTop: '1px' }}>
                {EVENT_ICON[ev.eventType] ?? '📋'}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: '12px', color: 'var(--t1)', fontWeight: 500, display: 'flex', alignItems: 'center', gap: '8px' }}>
                  {ev.eventType}
                  {ev.result && ev.result !== 'success' && (
                    <span style={{ fontSize: '10px', padding: '1px 6px', borderRadius: '3px', background: 'color-mix(in srgb, var(--danger) 14%, transparent)', color: 'var(--danger)', border: '1px solid color-mix(in srgb, var(--danger) 30%, transparent)' }}>
                      {ev.result}
                    </span>
                  )}
                </div>
                <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '3px', display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                  <span>{formatTs(ev.ts)}</span>
                  {ev.tenantAlias && <span>tenant: {ev.tenantAlias}</span>}
                  {ev.ipAddress && <span>{ev.ipAddress}</span>}
                </div>
                {ev.userAgent && (
                  <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: '2px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={ev.userAgent}>
                    {ev.userAgent}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
});

ProfileTabActivity.displayName = 'ProfileTabActivity';
