import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '../../../stores/authStore';

const ROLE_COLORS: Record<string, string> = {
  admin:  'var(--wrn)',
  editor: 'var(--acc)',
  viewer: 'var(--inf)',
};

const PERMISSIONS = [
  { module: 'LOOM — view graph',         viewer: true,  editor: true,  admin: true  },
  { module: 'LOOM — export',             viewer: false, editor: true,  admin: true  },
  { module: 'KNOT — inspect sessions',   viewer: true,  editor: true,  admin: true  },
  { module: 'ANVIL — analysis (H2)',      viewer: false, editor: true,  admin: true  },
  { module: 'SHUTTLE — manage sources',  viewer: false, editor: false, admin: true  },
  { module: 'HEIMDALL — control plane',  viewer: false, editor: false, admin: true  },
];

export const ProfileTabAccess = memo(() => {
  const { t } = useTranslation();
  const { user } = useAuthStore();
  const role  = user?.role ?? 'viewer';
  const color = ROLE_COLORS[role] ?? 'var(--t2)';

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.access')}
      </div>

      {/* Role card */}
      <div style={{ marginBottom: '20px' }}>
        <div style={{
          display: 'flex', alignItems: 'center', gap: '14px',
          padding: '12px 16px', background: 'var(--bg2)',
          border: `1px solid color-mix(in srgb, ${color} 40%, var(--bd))`,
          borderRadius: '6px',
        }}>
          <div style={{
            width: '32px', height: '32px', borderRadius: '6px', flexShrink: 0,
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px',
            background: `color-mix(in srgb, ${color} 14%, transparent)`,
            color,
          }}>
            {role === 'admin' ? '⚡' : role === 'editor' ? '✏' : '👁'}
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--t1)' }}>{t('profile.access.currentRole')}</div>
            <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '1px' }}>{t(`profile.access.roleDesc.${role}`)}</div>
          </div>
          <span style={{
            fontSize: '10px', fontWeight: 600, letterSpacing: '0.06em', textTransform: 'uppercase',
            padding: '2px 7px', borderRadius: '3px',
            background: `color-mix(in srgb, ${color} 15%, transparent)`,
            color, border: `1px solid color-mix(in srgb, ${color} 30%, transparent)`,
          }}>
            {role}
          </span>
        </div>
      </div>

      {/* Permissions matrix */}
      <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--t1)', marginBottom: '12px' }}>
        {t('profile.access.matrix')}
      </div>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '11px' }}>
        <thead>
          <tr>
            {['profile.access.module', 'roles.viewer', 'roles.editor', 'roles.admin'].map((k) => (
              <th key={k} style={{ textAlign: 'left', fontSize: '10px', fontWeight: 600, color: 'var(--t3)', letterSpacing: '0.07em', textTransform: 'uppercase', padding: '6px 8px', borderBottom: '1px solid var(--bd)' }}>
                {t(k)}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {PERMISSIONS.map((p) => (
            <tr key={p.module}>
              <td style={{ padding: '8px 8px', borderBottom: '1px solid var(--bd)', color: 'var(--t1)', fontWeight: 500 }}>{p.module}</td>
              {(['viewer', 'editor', 'admin'] as const).map((r) => (
                <td key={r} style={{ padding: '8px 8px', borderBottom: '1px solid var(--bd)', fontSize: '13px', color: p[r] ? 'var(--suc)' : 'var(--t3)' }}>
                  {p[r] ? '✓' : '—'}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

      <div style={{ marginTop: '18px', padding: '10px 14px', background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: '6px' }}>
        <div style={{ fontSize: '11px', color: 'var(--t3)' }}>{t('profile.access.changeHint')}</div>
      </div>
    </div>
  );
});

ProfileTabAccess.displayName = 'ProfileTabAccess';
