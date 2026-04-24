import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliTenantConfig } from '../../api/admin';
import {
  suspendTenant,
  unsuspendTenant,
  archiveTenant,
  restoreTenant,
  extendRetention,
} from '../../api/admin';
import { useTenantContext } from '../../hooks/useTenantContext';
import { UserConfirmModal } from '../users/UserConfirmModal';

interface Props {
  tenant:    DaliTenantConfig;
  onRefresh: () => void;
}

type PendingAction =
  | { kind: 'suspend' }
  | { kind: 'archive' }
  | { kind: 'extend';  retainUntil: number }
  | null;

const DEFAULT_EXTEND_DAYS = 30;

export function TenantLifecycleActions({ tenant, onRefresh }: Props) {
  const { t } = useTranslation();
  const { isSuperAdmin } = useTenantContext();
  const [pending, setPending] = useState<PendingAction>(null);
  const [error,   setError]   = useState<string | null>(null);
  const [busy,    setBusy]    = useState(false);

  if (!isSuperAdmin) return null;

  async function run<T>(fn: () => Promise<T>) {
    setBusy(true);
    setError(null);
    try {
      await fn();
      onRefresh();
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  }

  async function doSuspend()   { await run(() => suspendTenant(tenant.tenantAlias)); }
  async function doUnsuspend() { await run(() => unsuspendTenant(tenant.tenantAlias)); }
  async function doArchive()   { await run(() => archiveTenant(tenant.tenantAlias)); }
  async function doRestore()   { await run(() => restoreTenant(tenant.tenantAlias)); }
  async function doExtend(retainUntil: number) {
    await run(() => extendRetention(tenant.tenantAlias, retainUntil));
  }

  const S = tenant.status;

  return (
    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center', marginBottom: 16 }}>
      {S === 'ACTIVE' && (
        <>
          <button className="btn btn-danger" disabled={busy}
                  onClick={() => setPending({ kind: 'suspend' })}>
            {t('tenants.suspend', 'Suspend')}
          </button>
          <button className="btn btn-danger" disabled={busy}
                  onClick={() => setPending({ kind: 'archive' })}>
            {t('tenants.archiveNow', 'Archive now')}
          </button>
        </>
      )}

      {S === 'SUSPENDED' && (
        <>
          <button className="btn btn-primary" disabled={busy} onClick={doUnsuspend}>
            {t('tenants.unsuspend', 'Unsuspend')}
          </button>
          <button className="btn btn-danger" disabled={busy}
                  onClick={() => setPending({ kind: 'archive' })}>
            {t('tenants.archiveNow', 'Archive now')}
          </button>
        </>
      )}

      {S === 'ARCHIVED' && (
        <>
          <button className="btn btn-primary" disabled={busy} onClick={doRestore}>
            {t('tenants.restore', 'Restore')}
          </button>
          <button className="btn btn-secondary" disabled={busy}
                  onClick={() => setPending({
                    kind: 'extend',
                    retainUntil: Date.now() + DEFAULT_EXTEND_DAYS * 24 * 3600 * 1000,
                  })}>
            {t('tenants.extendRetention', 'Extend Retention')}
          </button>
        </>
      )}

      {error && <span style={{ color: 'var(--danger)', fontSize: '0.85rem' }}>{error}</span>}

      {pending?.kind === 'suspend' && (
        <UserConfirmModal
          title={t('tenants.confirmSuspendTitle', 'Confirm suspend')}
          message={t('tenants.confirmSuspendMsg',
            'Suspending "{{alias}}" blocks all tenant traffic. Continue?',
            { alias: tenant.tenantAlias })}
          onConfirm={doSuspend}
          onClose={() => setPending(null)}
        />
      )}

      {pending?.kind === 'archive' && (
        <UserConfirmModal
          title={t('tenants.confirmArchiveTitle', 'Confirm archive')}
          message={t('tenants.confirmArchiveMsg',
            'Archiving "{{alias}}" exports all databases to S3 and drops them. This is irreversible without Restore.',
            { alias: tenant.tenantAlias })}
          onConfirm={doArchive}
          onClose={() => setPending(null)}
        />
      )}

      {pending?.kind === 'extend' && (
        <UserConfirmModal
          danger={false}
          title={t('tenants.confirmExtendTitle', 'Confirm extend retention')}
          message={t('tenants.confirmExtendMsg',
            'Extend retention for "{{alias}}" to {{date}} (+{{days}} days)?',
            {
              alias: tenant.tenantAlias,
              date:  new Date(pending.retainUntil).toISOString().slice(0, 10),
              days:  DEFAULT_EXTEND_DAYS,
            })}
          onConfirm={() => doExtend(pending.retainUntil)}
          onClose={() => setPending(null)}
        />
      )}
    </div>
  );
}
