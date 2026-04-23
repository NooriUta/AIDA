/**
 * Round 5 — UserDetailDrawer — side panel for admin user detail.
 * Opens at 400px from the right when a row is clicked in UsersPage.
 * Sections: KC identity · Source bindings · Lifecycle.
 */
import { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import type { AidaUser } from './types';
import { ROLES } from './types';

const CHUR = '/chur';
const CSRF_HEADERS = { 'Content-Type': 'application/json', Origin: window.location.origin };

interface BindingRow {
  sourceId:  string;
  updatedAt: number;
}

interface LifecycleRow {
  deletedAt?:          number | null;
  deletedBy?:          string | null;
  deletionReason?:     string | null;
  dataRetentionUntil?: number | null;
  legalHoldUntil?:     number | null;
}

type Tab = 'identity' | 'bindings' | 'lifecycle';

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: 20 }}>
      <div style={{ fontSize: 11, fontWeight: 600, color: 'var(--t3)', textTransform: 'uppercase',
                    letterSpacing: '0.08em', marginBottom: 8 }}>
        {title}
      </div>
      {children}
    </div>
  );
}

function Field({ label, value }: { label: string; value?: string | null }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8, fontSize: 12,
                  padding: '4px 0', borderBottom: '1px solid var(--border)' }}>
      <span style={{ color: 'var(--t3)' }}>{label}</span>
      <span style={{ color: 'var(--t1)', fontFamily: value?.startsWith('aida:') ? 'var(--mono)' : undefined,
                     fontSize: value?.startsWith('aida:') ? 11 : undefined }}>
        {value ?? '—'}
      </span>
    </div>
  );
}

// ── Identity tab ──────────────────────────────────────────────────────────────
function IdentityTab({ user }: { user: AidaUser }) {
  const { t } = useTranslation();
  const r = ROLES.find(x => x.id === user.role);
  return (
    <>
      <Section title={t('users.drawer.kcIdentity', 'KC Identity')}>
        {(user.firstName || user.lastName) && (
          <Field label={t('profile.displayName', 'Имя')}
            value={[user.firstName, user.lastName].filter(Boolean).join(' ')} />
        )}
        <Field label="username"   value={user.name} />
        <Field label="email"      value={user.email} />
        <Field label="KC UUID"    value={user.kcId} />
        <Field label={t('users.drawer.status', 'status')}
          value={user.active ? t('status.active', 'active') : t('status.disabled', 'disabled')} />
      </Section>
      <Section title={t('users.drawer.roleScopes', 'Role & Scopes')}>
        <Field label={t('users.drawer.role', 'role')} value={user.role} />
        <Field label={t('users.drawer.tier', 'tier')} value={r?.tier} />
        {r?.scopes.map(s => (
          <div key={s} style={{ fontSize: 11, fontFamily: 'var(--mono)', color: 'var(--t2)',
                                padding: '2px 0' }}>
            {s}
          </div>
        ))}
      </Section>
      <Section title={t('users.drawer.profile', 'Profile')}>
        <Field label={t('profile.title', 'Должность')}  value={user.title} />
        <Field label={t('profile.dept', 'Отдел')}       value={user.dept} />
        <Field label={t('profile.phone', 'Телефон')}    value={user.phone} />
      </Section>
    </>
  );
}

// ── Bindings tab ─────────────────────────────────────────────────────────────
function BindingsTab({ user, isAdmin }: { user: AidaUser; isAdmin: boolean }) {
  const { t } = useTranslation();
  const [bindings, setBindings] = useState<BindingRow[]>([]);
  const [loading, setLoading]   = useState(true);
  const [editing, setEditing]   = useState(false);
  const [draft, setDraft]       = useState('');
  const [saving, setSaving]     = useState(false);

  const load = useCallback(async () => {
    if (!user.kcId) return;
    setLoading(true);
    try {
      const res = await fetch(`${CHUR}/api/admin/users/${encodeURIComponent(user.kcId)}/source-bindings`,
        { credentials: 'include' });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const body = await res.json() as { bindings: BindingRow[] };
      setBindings(body.bindings ?? []);
    } catch { /* ignore */ }
    finally { setLoading(false); }
  }, [user.kcId]);

  useEffect(() => { void load(); }, [load]);

  const save = async () => {
    if (!user.kcId) return;
    setSaving(true);
    const sources = draft.split(',').map(s => s.trim()).filter(Boolean);
    try {
      const res = await fetch(
        `${CHUR}/api/admin/users/${encodeURIComponent(user.kcId)}/source-bindings`,
        { method: 'POST', credentials: 'include', headers: CSRF_HEADERS,
          body: JSON.stringify({ tenantAlias: 'default', sources }) },
      );
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      await load();
      setEditing(false);
    } catch (e) { alert(String(e)); }
    finally { setSaving(false); }
  };

  if (!user.kcId) return <p style={{ color: 'var(--t3)', fontSize: 12 }}>No KC UUID</p>;

  return (
    <Section title={t('users.drawer.sourceBindings', 'Source Bindings')}>
      {loading && <p style={{ color: 'var(--t3)', fontSize: 12 }}>{t('status.loading')}</p>}
      {!loading && bindings.length === 0 && (
        <p style={{ color: 'var(--t3)', fontSize: 12 }}>
          {t('users.drawer.allSources', 'Доступ ко всем источникам')}
        </p>
      )}
      {!loading && bindings.map(b => (
        <div key={b.sourceId} style={{ fontSize: 12, padding: '3px 0', borderBottom: '1px solid var(--border)',
                                       fontFamily: 'var(--mono)', color: 'var(--t1)' }}>
          {b.sourceId}
        </div>
      ))}
      {isAdmin && !editing && (
        <button className="btn btn-secondary" style={{ marginTop: 8, fontSize: 11 }}
          onClick={() => { setDraft(bindings.map(b => b.sourceId).join(', ')); setEditing(true); }}>
          {t('action.edit', 'Изменить')}
        </button>
      )}
      {editing && (
        <div style={{ marginTop: 8 }}>
          <input className="field-input" style={{ width: '100%', fontSize: 12 }}
            value={draft} onChange={e => setDraft(e.target.value)}
            placeholder={t('users.drawer.sourceCSV', 'source-id-1, source-id-2, …')} />
          <div style={{ display: 'flex', gap: 6, marginTop: 6 }}>
            <button className="btn btn-secondary" style={{ fontSize: 11 }}
              onClick={save} disabled={saving}>
              {saving ? t('status.saving', 'Сохранение…') : t('action.save', 'Сохранить')}
            </button>
            <button className="btn btn-secondary" style={{ fontSize: 11 }}
              onClick={() => setEditing(false)} disabled={saving}>
              {t('action.cancel', 'Отмена')}
            </button>
          </div>
        </div>
      )}
    </Section>
  );
}

// ── Lifecycle tab ─────────────────────────────────────────────────────────────
function LifecycleTab({ user }: { user: AidaUser }) {
  const { t } = useTranslation();
  const [lc, setLc]       = useState<LifecycleRow | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user.kcId) { setLoading(false); return; }
    // Try to load lifecycle data via soft-deleted list (no per-user endpoint yet)
    // If user not soft-deleted, lifecycle vertex will be absent → show N/A
    fetch(`${CHUR}/api/admin/users/soft-deleted`, { credentials: 'include' })
      .then(r => r.ok ? r.json() : null)
      .then((body: { users?: (LifecycleRow & { userId: string })[] } | null) => {
        if (!body) return;
        const match = body.users?.find((u) => u.userId === user.kcId);
        setLc(match ? { ...match } : null);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [user.kcId]);

  const fmt = (ms?: number | null) =>
    ms ? new Date(ms).toLocaleDateString() : '—';

  return (
    <Section title={t('users.drawer.lifecycle', 'Lifecycle')}>
      {loading && <p style={{ color: 'var(--t3)', fontSize: 12 }}>{t('status.loading')}</p>}
      {!loading && !lc && (
        <p style={{ color: 'var(--t3)', fontSize: 12 }}>
          {t('users.drawer.notSoftDeleted', 'Пользователь активен, soft-delete не применён.')}
        </p>
      )}
      {!loading && lc && (
        <>
          <Field label={t('softDelete.col.deletedAt', 'Удалён')}   value={fmt(lc.deletedAt)} />
          <Field label={t('softDelete.col.by', 'Кем')}             value={lc.deletedBy} />
          <Field label={t('softDelete.col.reason', 'Причина')}     value={lc.deletionReason} />
          <Field label={t('softDelete.col.retention', 'До purge')} value={fmt(lc.dataRetentionUntil)} />
          <Field label={t('softDelete.col.hold', 'Legal hold')}    value={fmt(lc.legalHoldUntil)} />
        </>
      )}
    </Section>
  );
}

// ── Drawer shell ─────────────────────────────────────────────────────────────
export function UserDetailDrawer({
  user, isAdmin, onClose, onRefresh,
}: {
  user:      AidaUser;
  isAdmin:   boolean;
  onClose:   () => void;
  onRefresh: () => void;
}) {
  const { t } = useTranslation();
  const [tab, setTab] = useState<Tab>('identity');
  const r = ROLES.find(x => x.id === user.role);

  return (
    <>
      {/* Backdrop */}
      <div
        style={{ position: 'fixed', inset: 0, zIndex: 200 }}
        onClick={onClose}
      />
      {/* Drawer panel */}
      <div style={{
        position: 'fixed', top: 0, right: 0, bottom: 0, width: 400,
        background: 'var(--bg1)', borderLeft: '1px solid var(--border)',
        zIndex: 201, display: 'flex', flexDirection: 'column',
        boxShadow: '-4px 0 24px rgba(0,0,0,0.25)',
      }}>
        {/* Header */}
        <div style={{ padding: '16px 20px', borderBottom: '1px solid var(--border)',
                      display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--t1)' }}>
              {(user.firstName || user.lastName)
                ? [user.firstName, user.lastName].filter(Boolean).join(' ')
                : user.name}
            </div>
            <div style={{ fontSize: 11, color: 'var(--t3)' }}>
              {user.name}{user.email ? ` · ${user.email}` : ''}
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span className="badge" style={{
              background:  `color-mix(in srgb, ${r?.clr ?? 'var(--t2)'} 12%, transparent)`,
              color:        r?.clr ?? 'var(--t2)',
              borderColor: `color-mix(in srgb, ${r?.clr ?? 'var(--t2)'} 28%, transparent)`,
              fontSize: 10,
            }}>
              {user.role}
            </span>
            <button
              onClick={onClose}
              style={{ background: 'none', border: 'none', cursor: 'pointer',
                       color: 'var(--t3)', fontSize: 18, lineHeight: 1, padding: '2px 4px' }}
            >
              ×
            </button>
          </div>
        </div>

        {/* Tab bar */}
        <div style={{ display: 'flex', borderBottom: '1px solid var(--border)', padding: '0 12px' }}>
          {(['identity', 'bindings', 'lifecycle'] as Tab[]).map(tabId => (
            <button
              key={tabId}
              onClick={() => setTab(tabId)}
              style={{
                background: 'none', border: 'none', cursor: 'pointer', padding: '8px 10px',
                fontSize: 12, color: tab === tabId ? 'var(--t1)' : 'var(--t3)',
                borderBottom: tab === tabId ? '2px solid var(--accent)' : '2px solid transparent',
                fontWeight: tab === tabId ? 600 : 400,
              }}
            >
              {tabId === 'identity'  ? t('users.drawer.tabIdentity',  'Профиль') :
               tabId === 'bindings'  ? t('users.drawer.tabBindings',  'Источники') :
               t('users.drawer.tabLifecycle', 'Lifecycle')}
            </button>
          ))}
        </div>

        {/* Content */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '16px 20px' }}>
          {tab === 'identity'  && <IdentityTab  user={user} />}
          {tab === 'bindings'  && <BindingsTab  user={user} isAdmin={isAdmin} />}
          {tab === 'lifecycle' && <LifecycleTab user={user} />}
        </div>

        {/* Footer */}
        <div style={{ padding: '12px 20px', borderTop: '1px solid var(--border)',
                      display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
          <button className="btn btn-secondary" style={{ fontSize: 11 }} onClick={onRefresh}>
            {t('action.refresh', 'Обновить')}
          </button>
          <button className="btn btn-secondary" style={{ fontSize: 11 }} onClick={onClose}>
            {t('action.close', 'Закрыть')}
          </button>
        </div>
      </div>
    </>
  );
}
