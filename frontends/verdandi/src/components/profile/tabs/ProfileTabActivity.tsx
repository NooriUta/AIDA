import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';

const MOCK_SESSIONS = [
  { id: 'current', browser: 'Chrome', os: 'Windows 11', ip: '127.0.0.1', location: 'localhost', time: 'now',       current: true  },
  { id: 's2',      browser: 'Firefox', os: 'Ubuntu 22',  ip: '10.0.1.45', location: 'office',    time: '2h ago',    current: false },
];

const MOCK_ACTIVITY = [
  { icon: '🔍', title: 'Search: order_items',                    detail: 'LOOM',     time: '5 min ago'  },
  { icon: '📊', title: 'Impact analysis — users.email',          detail: 'LOOM · L3',time: '1 hour ago' },
  { icon: '✏️', title: 'Updated annotation — orders.status',     detail: 'ANVIL',    time: '3 hours ago'},
  { icon: '🔌', title: 'Source sync — PostgreSQL prod',          detail: 'SHUTTLE',  time: 'yesterday'  },
  { icon: '🔐', title: 'Signed in',                              detail: 'auth',     time: 'yesterday'  },
];

export const ProfileTabActivity = memo(() => {
  const { t } = useTranslation();
  const [sessions, setSessions] = useState(MOCK_SESSIONS);

  function terminateSession(id: string) {
    setSessions((s) => s.filter((x) => x.id !== id));
  }

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.activity')}
      </div>

      {/* Active sessions */}
      <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '12px', textTransform: 'uppercase' }}>
        {t('profile.activity.sessions')}
      </div>
      <div style={{ marginBottom: '24px' }}>
        {sessions.map((s) => (
          <div key={s.id} style={{
            display: 'flex', alignItems: 'center', gap: '12px',
            background: 'var(--bg2)', borderRadius: 'var(--seer-radius-md)', padding: '10px 14px', marginBottom: '8px',
            border: s.current ? '1px solid color-mix(in srgb, var(--suc) 40%, transparent)' : '1px solid var(--bd)',
          }}>
            <span style={{ fontSize: '16px', flexShrink: 0 }}>{s.browser === 'Chrome' ? '💻' : '🖥'}</span>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: '12px', fontWeight: 500, color: 'var(--t1)' }}>{s.browser} · {s.os}</div>
              <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '2px' }}>{s.ip} · {s.location} · {s.time}</div>
            </div>
            {s.current ? (
              <span style={{ fontSize: '10px', fontWeight: 600, padding: '2px 7px', borderRadius: '3px', background: 'color-mix(in srgb, var(--suc) 14%, transparent)', color: 'var(--suc)', border: '1px solid color-mix(in srgb, var(--suc) 30%, transparent)', letterSpacing: '0.05em' }}>
                {t('profile.activity.current')}
              </span>
            ) : (
              <button
                onClick={() => terminateSession(s.id)}
                style={{ fontSize: '11px', padding: '4px 10px', borderRadius: 'var(--seer-radius-md)', cursor: 'pointer', border: '1px solid color-mix(in srgb, var(--danger) 40%, transparent)', background: 'transparent', color: 'var(--danger)', fontFamily: 'inherit' }}
              >
                {t('profile.activity.terminate')}
              </button>
            )}
          </div>
        ))}
      </div>

      {/* Recent activity */}
      <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.04em', marginBottom: '12px', textTransform: 'uppercase' }}>
        {t('profile.activity.recent')}
      </div>
      <div>
        {MOCK_ACTIVITY.map((a, i) => (
          <div key={i} style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', padding: '10px 0', borderBottom: '1px solid var(--bd)' }}>
            <div style={{ width: '28px', height: '28px', borderRadius: 'var(--seer-radius-sm)', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px', background: 'var(--bg3)', marginTop: '1px' }}>
              {a.icon}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: '12px', color: 'var(--t1)', fontWeight: 500 }}>{a.title}</div>
              <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '2px', display: 'flex', gap: '12px' }}>
                <span>{a.detail}</span>
                <span>{a.time}</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
});

ProfileTabActivity.displayName = 'ProfileTabActivity';
