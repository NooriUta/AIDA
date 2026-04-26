import { useTranslation } from 'react-i18next';
import type { TenantMember } from '../../api/admin';

interface Props {
  members:  TenantMember[];
  onRemove: (member: TenantMember) => void;
  busy?:    boolean;
}

export function MemberList({ members, onRemove, busy }: Props) {
  const { t } = useTranslation();

  if (members.length === 0) {
    return <p style={{ color: 'var(--t3)' }}>{t('members.empty', 'No members yet.')}</p>;
  }

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
      <thead>
        <tr style={{ textAlign: 'left', borderBottom: '1px solid var(--bd)' }}>
          <th style={{ padding: '8px 12px' }}>{t('members.username', 'Username')}</th>
          <th style={{ padding: '8px 12px' }}>{t('members.email',    'Email')}</th>
          <th style={{ padding: '8px 12px' }}>{t('members.role',     'Role')}</th>
          <th style={{ padding: '8px 12px' }}>{t('members.enabled',  'Enabled')}</th>
          <th style={{ padding: '8px 12px' }}></th>
        </tr>
      </thead>
      <tbody>
        {members.map(m => (
          <tr key={m.id} style={{ borderBottom: '1px solid var(--bd)' }}>
            <td style={{ padding: '8px 12px', fontFamily: 'monospace' }}>{m.username}</td>
            <td style={{ padding: '8px 12px' }}>{m.email}</td>
            <td style={{ padding: '8px 12px' }}>{m.role}</td>
            <td style={{ padding: '8px 12px', color: m.enabled ? 'var(--suc)' : 'var(--t3)' }}>
              {m.enabled ? '✓' : '—'}
            </td>
            <td style={{ padding: '8px 12px', textAlign: 'right' }}>
              <button
                className="btn btn-danger"
                disabled={busy}
                onClick={() => onRemove(m)}
              >
                {t('members.remove', 'Remove')}
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
