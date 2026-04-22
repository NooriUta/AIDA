import { useState, useCallback, useMemo, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { usePageTitle }   from '../hooks/usePageTitle';
import { useAuthStore }   from '../stores/authStore';
import { useIsMobile }    from '../hooks/useIsMobile';
import { UserEditModal }  from '../components/users/UserEditModal';
import { UserInviteModal } from '../components/users/UserInviteModal';
import { UserConfirmModal } from '../components/users/UserConfirmModal';
import { UserDetailDrawer } from '../components/users/UserDetailDrawer';
import { TenantSelector, useTenantSelectorValue } from '../components/TenantSelector';
import type { AidaUser, UserRole } from '../components/users/types';
import { ROLES, ADMIN_ROLES } from '../components/users/types';

const USERS_API = '/chur/api/admin/users';
const ALL_TENANTS = '__all__';

// Shape returned by GET /heimdall/users (mirrors KcUserView in keycloakAdmin.ts)
interface KcUserView {
  id: string; name: string; firstName: string; lastName: string;
  email: string; role: UserRole; active: boolean;
  title: string; dept: string; phone: string;
  avatarColor: string; lang: string;
  tz: string; dateFmt: string; startPage: string;
  notifyEmail: boolean; notifyBrowser: boolean;
  notifyHarvest: boolean; notifyErrors: boolean; notifyDigest: boolean;
  quotas: { mimir: number; sessions: number; atoms: number; workers: number; anvil: number };
  sources: string[];
}

// ── Toast ─────────────────────────────────────────────────────────────────────
let _toastTimer: ReturnType<typeof setTimeout> | null = null;

function useToast() {
  const [msg,  setMsg]  = useState<string | null>(null);
  const [kind, setKind] = useState<'suc' | 'wrn' | 'err'>('suc');

  const show = useCallback((text: string, k: 'suc' | 'wrn' | 'err' = 'suc') => {
    setMsg(text); setKind(k);
    if (_toastTimer) clearTimeout(_toastTimer);
    _toastTimer = setTimeout(() => setMsg(null), 3000);
  }, []);

  return { msg, kind, show };
}

// ── Role badge ─────────────────────────────────────────────────────────────────
function RoleBadge({ roleId }: { roleId: UserRole }) {
  const r = ROLES.find(x => x.id === roleId);
  if (!r) return <span className="badge badge-neutral">{roleId}</span>;
  return (
    <span
      className="badge"
      style={{
        background:  `color-mix(in srgb, ${r.clr} 12%, transparent)`,
        color:        r.clr,
        borderColor: `color-mix(in srgb, ${r.clr} 28%, transparent)`,
      }}
    >
      {r.label}
    </span>
  );
}

// ── User row ───────────────────────────────────────────────────────────────────
function UserRow({
  user, isAdmin, isExpanded,
  onEdit, onBlock, onExpand,
}: {
  user:       AidaUser;
  isAdmin:    boolean;
  isExpanded: boolean;
  onEdit:     (id: number) => void;
  onBlock:    (id: number) => void;
  onExpand:   (id: number) => void;
}) {
  const { t } = useTranslation();
  const r       = ROLES.find(x => x.id === user.role) ?? ROLES[0];
  const ini     = user.name.slice(0, 2).toUpperCase();
  const scopes  = r.scopes;

  return (
    <tr
      style={{ opacity: user.active ? 1 : 0.45, cursor: 'pointer', background: isExpanded ? 'color-mix(in srgb, var(--inf) 6%, transparent)' : undefined }}
      onClick={() => onExpand(user.id)}
    >
      {/* User */}
      <td>
        <div style={{ display: 'flex', alignItems: 'center', gap: 9 }}>
          <div
            className="user-ava"
            style={{
              background:  `color-mix(in srgb, ${r.clr} 16%, var(--bg3))`,
              border:      `1px solid color-mix(in srgb, ${r.clr} 28%, transparent)`,
              color:        r.clr,
            }}
          >
            {ini}
          </div>
          <div>
            <div style={{ fontWeight: 600, fontSize: 12 }}>{user.name}</div>
            <div style={{ fontSize: 11, color: 'var(--t3)' }}>{user.email}</div>
          </div>
        </div>
      </td>

      {/* Role */}
      <td><RoleBadge roleId={user.role} /></td>

      {/* Status */}
      <td>
        <span className={`badge ${user.active ? 'badge-suc' : 'badge-err'}`}>
          {user.active ? 'active' : 'disabled'}
        </span>
      </td>

      {/* Scopes preview */}
      <td>
        <div style={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
          {scopes.slice(0, 2).map(s => (
            <span key={s} className="scope-tag">{s}</span>
          ))}
          {scopes.length > 2 && (
            <span className="scope-tag">+{scopes.length - 2}</span>
          )}
        </div>
      </td>

      {/* Sources */}
      <td style={{ fontSize: 11, color: 'var(--t2)' }}>
        {user.sources.length === 0
          ? <span style={{ color: 'var(--t3)' }}>все</span>
          : <>
            {user.sources[0]}
            {user.sources.length > 1 && ` +${user.sources.length - 1}`}
          </>
        }
      </td>

      {/* Quotas */}
      <td>
        <span style={{ fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t3)' }}>
          {user.quotas.mimir}/ч · {user.quotas.sessions}sess
        </span>
      </td>

      {/* Last active */}
      <td style={{ fontSize: 11, color: 'var(--t3)' }}>{user.lastActive}</td>

      {/* Actions */}
      <td className="user-row-actions" onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', gap: 5, justifyContent: 'flex-end' }}>
          {isAdmin && (
            <>
              <button className="btn btn-secondary btn-sm" onClick={() => onEdit(user.id)}>
                {t('users.edit')}
              </button>
              {user.active
                ? (
                  <button className="btn btn-danger btn-sm" onClick={() => onBlock(user.id)}>
                    {t('users.block')}
                  </button>
                )
                : (
                  <button
                    className="btn btn-secondary btn-sm"
                    style={{ color: 'var(--suc)', borderColor: `color-mix(in srgb, var(--suc) 40%, transparent)` }}
                    onClick={() => onBlock(user.id)}
                  >
                    {t('users.unblock')}
                  </button>
                )
              }
            </>
          )}
        </div>
      </td>
    </tr>
  );
}

// ── User card (mobile) ────────────────────────────────────────────────────────
function UserCard({
  user, isAdmin, onEdit, onBlock,
}: {
  user: AidaUser; isAdmin: boolean;
  onEdit: (id: number) => void; onBlock: (id: number) => void;
}) {
  const { t } = useTranslation();
  const r   = ROLES.find(x => x.id === user.role) ?? ROLES[0];
  const ini = user.name.slice(0, 2).toUpperCase();

  return (
    <div className="user-card" style={{ opacity: user.active ? 1 : 0.55 }}>
      <div className="user-card-header">
        <div className="user-ava" style={{
          background:  `color-mix(in srgb, ${r.clr} 16%, var(--bg3))`,
          border:      `1px solid color-mix(in srgb, ${r.clr} 28%, transparent)`,
          color:        r.clr, flexShrink: 0,
        }}>
          {ini}
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontWeight: 600, fontSize: 13, color: 'var(--t1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {user.name}
          </div>
          <div style={{ fontSize: 11, color: 'var(--t3)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {user.email}
          </div>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4, flexShrink: 0 }}>
          <RoleBadge roleId={user.role} />
          <span className={`badge ${user.active ? 'badge-suc' : 'badge-err'}`} style={{ fontSize: 10 }}>
            {user.active ? 'active' : 'disabled'}
          </span>
        </div>
      </div>
      <div className="user-card-meta">
        <span style={{ fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t3)' }}>
          {user.quotas.mimir}/ч · {user.quotas.sessions}sess
        </span>
        {user.sources.length > 0 && (
          <span style={{ fontSize: 11, color: 'var(--t2)' }}>
            {user.sources[0]}{user.sources.length > 1 ? ` +${user.sources.length - 1}` : ''}
          </span>
        )}
      </div>
      {isAdmin && (
        <div className="user-card-actions">
          <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={() => onEdit(user.id)}>
            {t('users.edit')}
          </button>
          {user.active
            ? <button className="btn btn-danger btn-sm" style={{ flex: 1 }} onClick={() => onBlock(user.id)}>
                {t('users.block')}
              </button>
            : <button
                className="btn btn-secondary btn-sm"
                style={{ flex: 1, color: 'var(--suc)', borderColor: `color-mix(in srgb, var(--suc) 40%, transparent)` }}
                onClick={() => onBlock(user.id)}
              >
                {t('users.unblock')}
              </button>
          }
        </div>
      )}
    </div>
  );
}

// ── UsersPage ─────────────────────────────────────────────────────────────────
export default function UsersPage() {
  const { t } = useTranslation();
  usePageTitle(t('nav.users'));
  const isMobile = useIsMobile();
  const authUser = useAuthStore(s => s.user);
  // authUser.role from aida-shared only has 3 values ('viewer'|'editor'|'admin').
  // HEIMDALL manages 8 roles internally. Treat role as plain string for the
  // isAdmin gate — 'admin' exists in both sets, so the check always works.
  const isAdmin  = (['admin', 'super-admin', 'local-admin', 'tenant-owner'] as string[])
                    .includes(authUser?.role ?? '');

  // ── State ──────────────────────────────────────────────────────────────────
  const [users, setUsers]         = useState<AidaUser[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [crossTenant, setCrossTenant] = useState(false);
  const [activeTenant, setActiveTenant] = useState(() => useTenantSelectorValue());
  const [editId, setEditId]       = useState<number | null>(null);
  const [drawerUser, setDrawerUser] = useState<AidaUser | null>(null);
  const [inviteOpen, setInvite]   = useState(false);
  const [confirmId, setConfirm]   = useState<number | null>(null);

  // ── Fetch users from /chur/api/admin/users ─────────────────────────────────
  const fetchUsers = useCallback((tenant: string) => {
    const url = tenant === ALL_TENANTS
      ? `${USERS_API}?allTenants=true`
      : `${USERS_API}?tenantAlias=${encodeURIComponent(tenant)}`;

    fetch(url, { credentials: 'include' })
      .then(r => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        return r.json() as Promise<{ mode: string; users: KcUserView[] }>;
      })
      .then(({ mode, users: kcUsers }) => {
        setCrossTenant(mode === 'cross-tenant');
        const mapped: AidaUser[] = kcUsers.map((u, i) => ({
          id:    i + 1,
          kcId:  u.id,
          name:  u.name,
          email: u.email,
          role:  u.role,
          active: u.active,
          title: u.title,
          dept:  u.dept,
          phone: u.phone,
          sources: u.sources,
          quotas:  u.quotas,
          lastActive: '—',
          sessions:   { used: 0, total: 0 },
          mimir:      { used: 0, total: 0 },
          prefs: {
            lang:        u.lang,
            tz:          u.tz,
            dateFmt:     u.dateFmt,
            startPage:   u.startPage,
            avatarColor: u.avatarColor,
            notifyEmail:    u.notifyEmail,
            notifyBrowser:  u.notifyBrowser,
            notifyHarvest:  u.notifyHarvest,
            notifyErrors:   u.notifyErrors,
            notifyDigest:   u.notifyDigest,
          },
        }));
        setUsers(mapped);
        setLoadError(null);
      })
      .catch(err => {
        const msg = err instanceof Error ? err.message : String(err);
        const hint = msg.includes('403')
          ? 'Недостаточно прав (требуется aida:admin или выше)'
          : msg.includes('401')
          ? 'Сессия истекла — войдите снова'
          : `Запустите: docker-compose up keycloak`;
        setLoadError(`${msg}. ${hint}`);
      });
  }, []);

  useEffect(() => { fetchUsers(activeTenant); }, [activeTenant, fetchUsers]);
  const [roleFilter, setRole]   = useState('');
  const [statusFilter, setStatus] = useState('');
  const [search, setSearch]     = useState('');
  const toast                   = useToast();

  // ── Derived data ───────────────────────────────────────────────────────────
  const filtered = useMemo(() => users.filter(u => {
    if (roleFilter   && u.role !== roleFilter) return false;
    if (statusFilter === 'active'   && !u.active)  return false;
    if (statusFilter === 'disabled' &&  u.active)  return false;
    if (search) {
      const q = search.toLowerCase();
      if (!u.name.toLowerCase().includes(q) && !u.email.toLowerCase().includes(q)) return false;
    }
    return true;
  }), [users, roleFilter, statusFilter, search]);

  const stats = useMemo(() => ({
    total:    users.length,
    active:   users.filter(u => u.active).length,
    admins:   users.filter(u => ADMIN_ROLES.includes(u.role)).length,
    bindings: users.filter(u => u.sources.length > 0).length,
  }), [users]);

  // ── Handlers ───────────────────────────────────────────────────────────────
  const editUser   = useCallback((id: number) => setEditId(id), []);
  const openDrawer = useCallback((id: number) => {
    setDrawerUser(prev => {
      const u = users.find(x => x.id === id) ?? null;
      return prev?.id === id ? null : u;   // toggle
    });
  }, [users]);

  const saveUser = useCallback((updated: AidaUser) => {
    setUsers(prev => prev.map(u => u.id === updated.id ? updated : u));
    toast.show('Изменения сохранены', 'suc');
  }, [toast]);

  const toggleBlock = useCallback((id: number) => {
    setUsers(prev => prev.map(u =>
      u.id === id ? { ...u, active: !u.active } : u,
    ));
    const u = users.find(x => x.id === id);
    toast.show(u?.active ? 'Пользователь заблокирован' : 'Пользователь разблокирован', 'wrn');
    setConfirm(null);
  }, [users, toast]);

  const requestBlock = useCallback((id: number) => {
    setConfirm(id);
  }, []);

  const handleInvite = useCallback(
    (email: string, name: string, role: UserRole) => {
      const newUser: AidaUser = {
        id:         Date.now(),
        name:       name || email.split('@')[0],
        email,
        role,
        active:     true,
        title:      '',
        dept:       '',
        phone:      '',
        sources:    [],
        quotas:     { mimir: 20, sessions: 2, atoms: 50000, workers: 4, anvil: 50 },
        lastActive: 'только что',
        sessions:   { used: 0, total: 0 },
        mimir:      { used: 0, total: 0 },
        prefs: {
          lang: 'ru', tz: 'Europe/Moscow', dateFmt: 'DD.MM.YYYY',
          startPage: 'dashboard', avatarColor: '#A8B860',
          notifyEmail: true, notifyBrowser: false, notifyHarvest: false,
          notifyErrors: true, notifyDigest: false,
        },
      };
      setUsers(prev => [...prev, newUser]);
      toast.show(`Приглашение отправлено: ${email}`, 'suc');
    },
    [toast],
  );

  const clearFilters = useCallback(() => {
    setRole(''); setStatus(''); setSearch('');
  }, []);

  const editingUser = editId !== null ? users.find(u => u.id === editId) : null;
  const confirmUser = confirmId !== null ? users.find(u => u.id === confirmId) : null;

  return (
    <div style={{ padding: 'var(--seer-space-6)', height: '100%', overflowY: 'auto', background: 'var(--bg0)' }}>

      {/* ── Page header ── */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 20 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--t1)', letterSpacing: '0.01em' }}>
              {t('users.title')}
            </div>
            {crossTenant && (
              <span className="badge badge-warn" style={{ fontSize: 10 }}>
                {t('users.crossTenantMode', 'cross-tenant')}
              </span>
            )}
          </div>
          <div style={{ fontSize: 12, color: 'var(--t3)', marginTop: 2 }}>
            {t('users.subtitle')}
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <TenantSelector onChange={v => { setActiveTenant(v); setCrossTenant(false); }} />
          {isAdmin && (
            <button className="btn btn-secondary" onClick={() => setInvite(true)}>
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              {t('users.invite')}
            </button>
          )}
        </div>
      </div>

      {/* ── KC load error ── */}
      {loadError && (
        <div className="warn-banner" style={{ marginBottom: 16, fontSize: 11 }}>
          ⚠ {loadError}
        </div>
      )}

      {/* ── Stats grid ── */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="section-label">{t('users.total')}</div>
          <div className="stat-val" style={{ color: 'var(--t1)' }}>{stats.total}</div>
          <div className="stat-sub">{stats.active} активных</div>
        </div>
        <div className="stat-card">
          <div className="section-label">{t('users.active')}</div>
          <div className="stat-val" style={{ color: 'var(--suc)' }}>{stats.active}</div>
          <div className="stat-sub">{stats.total - stats.active} заблокировано</div>
        </div>
        <div className="stat-card">
          <div className="section-label">{t('users.admins')}</div>
          <div className="stat-val" style={{ color: 'var(--wrn)' }}>{stats.admins}</div>
          <div className="stat-sub">local-admin и выше</div>
        </div>
        <div className="stat-card">
          <div className="section-label">{t('users.sourceBindings')}</div>
          <div className="stat-val" style={{ color: 'var(--inf)' }}>{stats.bindings}</div>
          <div className="stat-sub">ограничены по источникам</div>
        </div>
      </div>

      {/* ── Filter bar ── */}
      <div className="filter-bar">
        <span className="section-label">Фильтр</span>
        <select
          className="field-input"
          value={roleFilter}
          onChange={e => setRole(e.target.value)}
          style={{ width: 'auto' }}
        >
          <option value="">{t('users.filterRole')}</option>
          {ROLES.map(r => (
            <option key={r.id} value={r.id}>{r.label}</option>
          ))}
        </select>
        <select
          className="field-input"
          value={statusFilter}
          onChange={e => setStatus(e.target.value)}
          style={{ width: 'auto' }}
        >
          <option value="">{t('users.filterStatus')}</option>
          <option value="active">active</option>
          <option value="disabled">disabled</option>
        </select>
        <input
          className="field-input"
          placeholder={t('users.filterSearch')}
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ flex: '1 1 auto', minWidth: 100 }}
        />
        <button className="btn btn-secondary btn-sm" onClick={clearFilters}>
          ✕ {t('users.clearFilters')}
        </button>
        <span style={{ marginLeft: 'auto', fontSize: 11, color: 'var(--t3)', fontFamily: 'var(--mono)' }}>
          {filtered.length} из {users.length}
        </span>
      </div>

      {/* ── User list: cards on mobile, table on desktop ── */}
      {isMobile ? (
        <div className="user-card-list">
          {filtered.map(u => (
            <UserCard key={u.id} user={u} isAdmin={isAdmin} onEdit={editUser} onBlock={requestBlock} />
          ))}
          {filtered.length === 0 && (
            <div style={{ textAlign: 'center', color: 'var(--t3)', padding: 32, fontSize: 12 }}>
              Нет пользователей по заданным фильтрам
            </div>
          )}
        </div>
      ) : (
        <div className="data-panel">
          <table className="data-table" style={{ width: '100%' }}>
            <thead>
              <tr>
                <th>Пользователь</th>
                <th>Роль</th>
                <th>Статус</th>
                <th>Scopes</th>
                <th>Источники</th>
                <th>Квоты</th>
                <th>Активность</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {filtered.map(u => (
                <UserRow key={u.id} user={u} isAdmin={isAdmin}
                  isExpanded={drawerUser?.id === u.id}
                  onEdit={editUser} onBlock={requestBlock} onExpand={openDrawer} />
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={8} style={{ textAlign: 'center', color: 'var(--t3)', padding: 32, fontSize: 12 }}>
                    Нет пользователей по заданным фильтрам
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Modals ── */}
      {editingUser && (
        <UserEditModal
          user={editingUser}
          isAdmin={isAdmin}
          onSave={saveUser}
          onClose={() => setEditId(null)}
          onBlock={requestBlock}
        />
      )}

      {inviteOpen && (
        <UserInviteModal
          onInvite={handleInvite}
          onClose={() => setInvite(false)}
        />
      )}

      {confirmUser && (
        <UserConfirmModal
          title={confirmUser.active ? 'Заблокировать пользователя?' : 'Разблокировать пользователя?'}
          message={
            confirmUser.active
              ? `${confirmUser.name} потеряет доступ к AIDA платформе.`
              : `${confirmUser.name} снова получит доступ к AIDA платформе.`
          }
          danger={confirmUser.active}
          onConfirm={() => toggleBlock(confirmUser.id)}
          onClose={() => setConfirm(null)}
        />
      )}

      {/* ── User detail drawer ── */}
      {drawerUser && (
        <UserDetailDrawer
          user={drawerUser}
          isAdmin={isAdmin}
          onClose={() => setDrawerUser(null)}
          onRefresh={() => fetchUsers(activeTenant)}
        />
      )}

      {/* ── Toast ── */}
      {toast.msg && (
        <div className={`toast visible ${toast.kind}`}>
          {toast.msg}
        </div>
      )}
    </div>
  );
}
