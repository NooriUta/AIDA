import { useState } from 'react';
import { useTranslation } from 'react-i18next';

export type MemberRole = 'viewer' | 'editor' | 'local-admin' | 'tenant-owner';

interface Props {
  onClose:  () => void;
  onInvite: (email: string, name: string, role: MemberRole) => Promise<void>;
}

const ROLES: MemberRole[] = ['viewer', 'editor', 'local-admin', 'tenant-owner'];

export function MemberInviteModal({ onClose, onInvite }: Props) {
  const { t } = useTranslation();
  const [email, setEmail] = useState('');
  const [name,  setName]  = useState('');
  const [role,  setRole]  = useState<MemberRole>('viewer');
  const [busy,  setBusy]  = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canSubmit = email.trim() !== '' && name.trim() !== '' && !busy;

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!canSubmit) return;
    setBusy(true);
    setError(null);
    try {
      await onInvite(email.trim(), name.trim(), role);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div
      style={{
        position:       'fixed',
        inset:          0,
        background:     'rgba(0,0,0,.65)',
        display:        'flex',
        alignItems:     'center',
        justifyContent: 'center',
        zIndex:         200,
      }}
      onClick={e => { if (e.target === e.currentTarget) onClose(); }}
    >
      <form
        onSubmit={submit}
        className="confirm-box"
        style={{ minWidth: 360, display: 'flex', flexDirection: 'column', gap: 12 }}
      >
        <div style={{ fontSize: 14, fontWeight: 600 }}>{t('members.inviteTitle', 'Invite member')}</div>

        <label style={{ fontSize: 12, display: 'flex', flexDirection: 'column', gap: 4 }}>
          <span style={{ color: 'var(--t3)' }}>{t('members.email', 'Email')}</span>
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required autoFocus />
        </label>

        <label style={{ fontSize: 12, display: 'flex', flexDirection: 'column', gap: 4 }}>
          <span style={{ color: 'var(--t3)' }}>{t('members.name', 'Name')}</span>
          <input type="text" value={name} onChange={e => setName(e.target.value)} required />
        </label>

        <label style={{ fontSize: 12, display: 'flex', flexDirection: 'column', gap: 4 }}>
          <span style={{ color: 'var(--t3)' }}>{t('members.role', 'Role')}</span>
          <select value={role} onChange={e => setRole(e.target.value as MemberRole)}>
            {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
          </select>
        </label>

        {error && <div style={{ color: 'var(--danger)', fontSize: 12 }}>{error}</div>}

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 8 }}>
          <button type="button" className="btn btn-secondary" onClick={onClose} disabled={busy}>
            {t('users.cancel', 'Cancel')}
          </button>
          <button type="submit" className="btn btn-primary" disabled={!canSubmit}>
            {t('members.invite', 'Invite')}
          </button>
        </div>
      </form>
    </div>
  );
}
