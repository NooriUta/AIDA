import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { usePageTitle } from '../hooks/usePageTitle';
import { useTenantMembers } from '../hooks/useTenantMembers';
import { useTenantDetails } from '../hooks/useTenantDetails';
import { TenantStatusBadge } from '../components/tenants/TenantStatusBadge';
import { MemberList } from '../components/tenants/MemberList';
import { MemberInviteModal, type MemberRole } from '../components/tenants/MemberInviteModal';
import { UserConfirmModal } from '../components/users/UserConfirmModal';
import { addMember, removeMember, type TenantMember } from '../api/admin';
import { useTenantContext } from '../hooks/useTenantContext';

export default function TenantMembersPage() {
  const { alias }  = useParams<{ alias: string }>();
  const { t }      = useTranslation();
  const navigate   = useNavigate();
  const { canManageUsers } = useTenantContext();
  usePageTitle(
    alias
      ? `${t('members.pageTitle', 'Members')}: ${alias}`
      : t('members.pageTitle', 'Members')
  );

  const { members, loading, error, refresh } = useTenantMembers(alias);
  const { tenant } = useTenantDetails(alias);
  const [inviteOpen, setInviteOpen]   = useState(false);
  const [pendingRemove, setPendingRemove] = useState<TenantMember | null>(null);
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  async function handleInvite(email: string, name: string, role: MemberRole) {
    if (!alias) return;
    setBusy(true);
    setActionError(null);
    try {
      await addMember(alias, email, name, role);
      refresh();
    } catch (e) {
      setActionError(e instanceof Error ? e.message : String(e));
      throw e;
    } finally {
      setBusy(false);
    }
  }

  async function handleRemove() {
    if (!alias || !pendingRemove) return;
    setBusy(true);
    setActionError(null);
    try {
      await removeMember(alias, pendingRemove.id);
      refresh();
    } catch (e) {
      setActionError(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
      setPendingRemove(null);
    }
  }

  return (
    <div className="page-content" style={{ padding: '24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
        <button className="btn btn-secondary" onClick={() => navigate(`/admin/tenants/${alias}`)}>
          ← {t('members.backToTenant', 'Tenant')}
        </button>
        <h2 style={{ margin: 0, fontFamily: 'monospace', display: 'flex', alignItems: 'center', gap: 8 }}>
          {alias}
          {tenant && <TenantStatusBadge status={tenant.status} />}
          <span style={{ fontFamily: 'sans-serif', fontWeight: 400, color: 'var(--t3)', fontSize: '0.9em' }}>· {t('members.heading', 'Members')}</span>
        </h2>
        <div style={{ flex: 1 }} />
        <button className="btn btn-secondary" onClick={refresh} disabled={loading}>
          {t('tenants.refresh', 'Refresh')}
        </button>
        {canManageUsers && (
          <button
            className="btn btn-primary"
            onClick={() => setInviteOpen(true)}
            disabled={busy}
          >
            + {t('members.invite', 'Invite')}
          </button>
        )}
      </div>

      {(error || actionError) && (
        <p style={{ color: 'var(--danger)' }}>{actionError ?? error}</p>
      )}

      {loading && members.length === 0 ? (
        <p style={{ color: 'var(--t3)' }}>{t('status.loading', 'Loading…')}</p>
      ) : (
        <MemberList
          members={members}
          busy={busy}
          onRemove={m => canManageUsers && setPendingRemove(m)}
        />
      )}

      {inviteOpen && alias && (
        <MemberInviteModal
          tenantAlias={alias}
          onClose={() => setInviteOpen(false)}
          onInvite={handleInvite}
        />
      )}

      {pendingRemove && (
        <UserConfirmModal
          title={t('members.confirmRemoveTitle', 'Remove member')}
          message={t(
            'members.confirmRemoveMsg',
            'Remove {{name}} ({{email}}) from this tenant?',
            { name: pendingRemove.name, email: pendingRemove.email }
          )}
          onConfirm={handleRemove}
          onClose={() => setPendingRemove(null)}
        />
      )}
    </div>
  );
}
