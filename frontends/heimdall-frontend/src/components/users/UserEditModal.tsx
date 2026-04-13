import { useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import type { AidaUser, UserRole } from './types';
import { ROLES, SCOPE_INFO, SOURCES, AVATAR_COLORS, ACT_EVENTS } from './types';

// ── Section keys ──────────────────────────────────────────────────────────────
type Section = 'profile' | 'role' | 'permissions' | 'quotas' | 'sources' | 'activity' | 'prefs';

// ── Helpers ───────────────────────────────────────────────────────────────────
function roleColor(roleId: UserRole): string {
  return ROLES.find(r => r.id === roleId)?.clr ?? 'var(--t2)';
}

function initials(name: string): string {
  return name.slice(0, 2).toUpperCase();
}

// ── Toggle component ──────────────────────────────────────────────────────────
function Toggle({ on, onChange }: { on: boolean; onChange: (v: boolean) => void }) {
  return (
    <div
      className={`toggle ${on ? 'on' : ''}`}
      onClick={() => onChange(!on)}
      role="switch"
      aria-checked={on}
    >
      <div className="toggle-knob" />
    </div>
  );
}

// ── ProfileSection ────────────────────────────────────────────────────────────
function ProfileSection({ draft, setDraft }: { draft: AidaUser; setDraft: (u: AidaUser) => void }) {
  const { t } = useTranslation();
  const clr = roleColor(draft.role);

  return (
    <div>
      <div className="section-title">{t('users.sections.profile')}</div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
        <div>
          <label className="field-label">{t('profile.fieldUsername')}</label>
          <input
            className="field-input"
            value={draft.name}
            onChange={e => setDraft({ ...draft, name: e.target.value })}
          />
        </div>
        <div>
          <label className="field-label">{t('profile.fieldEmail')}</label>
          <input
            className="field-input"
            value={draft.email}
            readOnly
            style={{ opacity: 0.6, cursor: 'not-allowed' }}
          />
        </div>
        <div>
          <label className="field-label">Должность</label>
          <input
            className="field-input"
            placeholder="Senior Data Engineer"
            value={draft.title}
            onChange={e => setDraft({ ...draft, title: e.target.value })}
          />
        </div>
        <div>
          <label className="field-label">Отдел / Команда</label>
          <input
            className="field-input"
            placeholder="Analytics Team"
            value={draft.dept}
            onChange={e => setDraft({ ...draft, dept: e.target.value })}
          />
        </div>
      </div>
      <div style={{ marginTop: 14 }}>
        <label className="field-label">
          Телефон{' '}
          <span style={{ fontWeight: 400, color: 'var(--t3)', textTransform: 'none' }}>
            (необязательно)
          </span>
        </label>
        <input
          className="field-input"
          placeholder="+7 900 000-00-00"
          value={draft.phone}
          onChange={e => setDraft({ ...draft, phone: e.target.value })}
          style={{ maxWidth: 260 }}
        />
      </div>
      <div style={{ marginTop: 16 }}>
        <div className="security-block">
          <div className="security-block-title">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: 'var(--t2)' }}>
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
            Статус аккаунта
          </div>
          <div className="setting-row">
            <div>
              <div className="setting-name">Активен</div>
              <div className="setting-desc">Пользователь имеет доступ к AIDA платформе</div>
            </div>
            <Toggle on={draft.active} onChange={v => setDraft({ ...draft, active: v })} />
          </div>
          <div className="setting-row">
            <div>
              <div className="setting-name">2FA</div>
              <div className="setting-desc">Управляется через Keycloak</div>
            </div>
            <span className="badge badge-neutral" style={{ fontSize: 10 }}>keycloak</span>
          </div>
        </div>
      </div>
      {/* Avatar preview */}
      <div style={{ marginTop: 12, display: 'flex', alignItems: 'center', gap: 10 }}>
        <div
          className="user-ava"
          style={{
            width: 36, height: 36, fontSize: 12,
            background: `color-mix(in srgb, ${clr} 18%, var(--bg3))`,
            border: `1px solid color-mix(in srgb, ${clr} 28%, transparent)`,
            color: clr,
          }}
        >
          {initials(draft.name)}
        </div>
        <span style={{ fontSize: 12, color: 'var(--t3)' }}>
          Цвет аватара: настраивается в разделе «Настройки»
        </span>
      </div>
    </div>
  );
}

// ── RoleSection ───────────────────────────────────────────────────────────────
function RoleSection({
  draft, setDraft, isAdmin,
}: { draft: AidaUser; setDraft: (u: AidaUser) => void; isAdmin: boolean }) {
  const tierClass: Record<string, string> = {
    user: 'tier-user', tenant: 'tier-tenant', platform: 'tier-platform',
  };

  return (
    <div>
      <div className="section-title">Роль</div>
      <div className="info-banner">
        Роль определяет базовый набор scopes. Дополнительные scopes настраиваются в разделе «Разрешения».
      </div>
      {ROLES.map(r => {
        const isElevated = ['admin', 'super-admin', 'tenant-owner'].includes(r.id);
        const disabled = isElevated && !isAdmin;
        const selected = draft.role === r.id;
        return (
          <div
            key={r.id}
            className={`role-card-item ${selected ? 'selected' : ''} ${disabled ? 'disabled' : ''}`}
            onClick={() => {
              if (!disabled) setDraft({ ...draft, role: r.id as UserRole });
            }}
          >
            <div
              className="role-icon-wrap"
              style={{
                background: `color-mix(in srgb, ${r.clr} 16%, var(--bg3))`,
                color: r.clr,
              }}
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              </svg>
            </div>
            <div className="role-card-info">
              <div className="role-card-name" style={{ color: r.clr }}>{r.label}</div>
              <div className="role-card-desc">{r.desc}</div>
            </div>
            <span className={`role-tier-tag ${tierClass[r.tier] ?? ''}`}>{r.tier}</span>
          </div>
        );
      })}
    </div>
  );
}

// ── PermissionsSection ────────────────────────────────────────────────────────
function PermissionsSection({ draft }: { draft: AidaUser }) {
  const roleDef = ROLES.find(r => r.id === draft.role);
  const activeScopes = roleDef?.scopes ?? [];

  return (
    <div>
      <div className="section-title">Разрешения (Scopes)</div>
      <div className="info-banner">
        Scopes дополняют роль. Вы можете назначать только те scopes, которые входят в вашу роль или ниже.
      </div>
      <table className="perm-table" style={{ width: '100%' }}>
        <thead>
          <tr>
            <th>Scope</th>
            <th>Описание</th>
            <th style={{ textAlign: 'center' }}>Включён</th>
          </tr>
        </thead>
        <tbody>
          {SCOPE_INFO.map(s => {
            const has = activeScopes.includes(s.id);
            return (
              <tr key={s.id}>
                <td style={{ fontFamily: 'var(--mono)' }}>{s.id}</td>
                <td style={{ color: 'var(--t3)' }}>{s.desc}</td>
                <td style={{ textAlign: 'center' }}>
                  {has
                    ? <span className="perm-yes" style={{ fontSize: 14 }}>✓</span>
                    : <span className="perm-no">—</span>}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

// ── QuotasSection ─────────────────────────────────────────────────────────────
function QuotasSection({ draft, setDraft }: { draft: AidaUser; setDraft: (u: AidaUser) => void }) {
  const q = draft.quotas;

  const set = useCallback((key: keyof typeof q, val: number) => {
    setDraft({ ...draft, quotas: { ...q, [key]: val } });
  }, [draft, setDraft, q]);

  return (
    <div>
      <div className="section-title">Квоты ресурсов</div>
      <div className="warn-banner">
        Enforces Dali (сессии, atoms) и MIMIR (запросы). HEIMDALL настраивает и отображает значения.
      </div>

      <div className="security-block">
        <div className="security-block-title">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: 'var(--t2)' }}>
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
          </svg>
          MIMIR &amp; ANVIL
        </div>
        <div className="quota-row">
          <span className="quota-label">Запросов MIMIR / час</span>
          <input type="range" min={0} max={100} step={5} value={q.mimir}
            onChange={e => set('mimir', +e.target.value)} />
          <span className="quota-val">{q.mimir}</span>
        </div>
        <div className="quota-row">
          <span className="quota-label">Traversals ANVIL / час</span>
          <input type="range" min={0} max={200} step={10} value={q.anvil}
            onChange={e => set('anvil', +e.target.value)} />
          <span className="quota-val">{q.anvil}</span>
        </div>
      </div>

      <div className="security-block">
        <div className="security-block-title">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: 'var(--t2)' }}>
            <rect x="2" y="3" width="20" height="14" rx="2" /><line x1="8" y1="21" x2="16" y2="21" /><line x1="12" y1="17" x2="12" y2="21" />
          </svg>
          Dali — Парсинг
        </div>
        <div className="quota-row">
          <span className="quota-label">Параллельных сессий</span>
          <input type="range" min={0} max={10} value={q.sessions}
            onChange={e => set('sessions', +e.target.value)} />
          <span className="quota-val">{q.sessions}</span>
        </div>
        <div className="quota-row">
          <span className="quota-label">Max atoms / сессия</span>
          <input type="range" min={1000} max={100000} step={1000} value={q.atoms}
            onChange={e => set('atoms', +e.target.value)} />
          <span className="quota-val">{Math.round(q.atoms / 1000)}K</span>
        </div>
        <div className="quota-row">
          <span className="quota-label">Воркеров из пула (макс)</span>
          <input type="range" min={1} max={8} value={q.workers}
            onChange={e => set('workers', +e.target.value)} />
          <span className="quota-val">{q.workers}</span>
        </div>
      </div>
    </div>
  );
}

// ── SourcesSection ────────────────────────────────────────────────────────────
function SourcesSection({ draft, setDraft }: { draft: AidaUser; setDraft: (u: AidaUser) => void }) {
  const toggle = (src: string) => {
    const next = draft.sources.includes(src)
      ? draft.sources.filter(s => s !== src)
      : [...draft.sources, src];
    setDraft({ ...draft, sources: next });
  };

  return (
    <div>
      <div className="section-title">Доступ к источникам</div>
      <div className="info-banner">
        Пустой список — доступ ко всем источникам по роли. Явный список ограничивает до выбранных.
      </div>
      {SOURCES.map(src => (
        <div key={src} className="setting-row">
          <div>
            <div className="setting-name">{src}</div>
            <div className="setting-desc">JDBC source · Dali</div>
          </div>
          <Toggle
            on={draft.sources.includes(src)}
            onChange={() => toggle(src)}
          />
        </div>
      ))}
    </div>
  );
}

// ── ActivitySection ───────────────────────────────────────────────────────────
function ActivitySection({ user }: { user: AidaUser }) {
  // Deterministic pseudo-random activity log based on user id
  const seed = user.id * 7;
  const events = Array.from({ length: 7 }, (_, i) => ({
    type: ACT_EVENTS[(seed + i * 3) % ACT_EVENTS.length],
    ok:   (seed + i) % 5 !== 0,
    ts:   `${String(Math.floor(9 + ((seed + i * 11) % 14))).padStart(2, '0')}:${String((seed * 3 + i * 7) % 60).padStart(2, '0')}`,
  }));

  return (
    <div>
      <div className="section-title">Активность</div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10, marginBottom: 20 }}>
        <div className="stat-card">
          <div className="section-label">Сессий всего</div>
          <div className="stat-val" style={{ color: 'var(--inf)', fontSize: 20 }}>{user.sessions.total}</div>
          <div className="stat-sub">{user.sessions.used} активных</div>
        </div>
        <div className="stat-card">
          <div className="section-label">MIMIR запросов</div>
          <div className="stat-val" style={{ color: 'var(--acc)', fontSize: 20 }}>{user.mimir.total}</div>
          <div className="stat-sub">{user.mimir.used} сегодня</div>
        </div>
        <div className="stat-card">
          <div className="section-label">Последняя активность</div>
          <div className="stat-val" style={{ color: 'var(--t1)', fontSize: 14, marginTop: 6 }}>
            {user.lastActive}
          </div>
        </div>
      </div>
      <div className="security-block" style={{ padding: '0 16px' }}>
        {events.map((ev, i) => (
          <div key={i} className="act-row">
            <span className="act-ts">{ev.ts}</span>
            <span className="act-type">{ev.type}</span>
            <span style={{ flex: 1 }} />
            <span className={`badge ${ev.ok ? 'badge-suc' : 'badge-err'}`} style={{ fontSize: 9 }}>
              {ev.ok ? 'OK' : 'FAIL'}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

// ── PrefsSection ──────────────────────────────────────────────────────────────
function PrefsSection({ draft, setDraft }: { draft: AidaUser; setDraft: (u: AidaUser) => void }) {
  const p = draft.prefs;
  const setPref = (key: keyof typeof p, val: string | boolean | number) => {
    setDraft({ ...draft, prefs: { ...p, [key]: val } });
  };

  return (
    <div>
      <div className="section-title">Настройки личного кабинета</div>

      {/* Interface settings */}
      <div className="security-block">
        <div className="security-block-title">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: 'var(--t2)' }}>
            <rect x="2" y="3" width="20" height="14" rx="2" /><line x1="8" y1="21" x2="16" y2="21" /><line x1="12" y1="17" x2="12" y2="21" />
          </svg>
          Интерфейс
        </div>
        {/* lang/tz/dateFmt/startPage → Keycloak attributes (server-side, cross-device).
            theme/palette/fonts/density → FRIGG (synced by verdandi prefsStore on login). */}
        <div className="info-banner" style={{ marginBottom: 16 }}>
          Язык, часовой пояс и стартовая страница хранятся в Keycloak.
          Тема, палитра и шрифты — в FRIGG (синхронизируются при логине).
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <div>
            <label className="field-label">Язык интерфейса</label>
            <select className="field-input" value={p.lang} onChange={e => setPref('lang', e.target.value)}>
              <option value="ru">Русский</option>
              <option value="en">English</option>
            </select>
          </div>
          <div>
            <label className="field-label">Часовой пояс</label>
            <select className="field-input" value={p.tz} onChange={e => setPref('tz', e.target.value)}>
              <option value="Europe/Moscow">Europe/Moscow (UTC+3)</option>
              <option value="Europe/Kiev">Europe/Kiev (UTC+2)</option>
              <option value="Asia/Novosibirsk">Asia/Novosibirsk (UTC+7)</option>
              <option value="Asia/Yekaterinburg">Asia/Yekaterinburg (UTC+5)</option>
              <option value="UTC">UTC±0</option>
              <option value="America/New_York">America/New_York (UTC−5)</option>
            </select>
          </div>
          <div>
            <label className="field-label">Формат даты</label>
            <select className="field-input" value={p.dateFmt} onChange={e => setPref('dateFmt', e.target.value)}>
              <option value="DD.MM.YYYY">DD.MM.YYYY</option>
              <option value="YYYY-MM-DD">YYYY-MM-DD</option>
              <option value="MM/DD/YYYY">MM/DD/YYYY</option>
            </select>
          </div>
          <div>
            <label className="field-label">Стартовая страница</label>
            <select className="field-input" value={p.startPage} onChange={e => setPref('startPage', e.target.value)}>
              <option value="dashboard">Дашборд</option>
              <option value="loom">LOOM (граф)</option>
              <option value="events">Поток событий</option>
              <option value="services">Сервисы</option>
            </select>
          </div>
        </div>
      </div>

      {/* Avatar color */}
      <div className="security-block">
        <div className="security-block-title">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: 'var(--t2)' }}>
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
          </svg>
          Аватар
        </div>
        <label className="field-label">Цвет</label>
        <div className="color-swatches">
          {AVATAR_COLORS.map(clr => (
            <div
              key={clr}
              className={`color-swatch ${p.avatarColor === clr ? 'selected' : ''}`}
              style={{ background: clr }}
              onClick={() => setPref('avatarColor', clr)}
              title={clr}
            />
          ))}
        </div>
      </div>

      {/* Notifications */}
      <div className="security-block">
        <div className="security-block-title">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: 'var(--t2)' }}>
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          Уведомления
        </div>
        {([
          ['notifyEmail',   'Email-уведомления',       'Системные события и отчёты на почту'],
          ['notifyBrowser', 'Браузерные уведомления',  'Push-уведомления в браузере'],
          ['notifyHarvest', 'Harvest завершён',         'Оповещение по завершении parse-сессии'],
          ['notifyErrors',  'Ошибки сессий',            'Алерты при сбоях Dali / ANVIL'],
          ['notifyDigest',  'Еженедельный дайджест',   'Сводка активности за неделю на email'],
        ] as [keyof typeof p, string, string][]).map(([key, name, desc]) => (
          <div key={key} className="setting-row">
            <div>
              <div className="setting-name">{name}</div>
              <div className="setting-desc">{desc}</div>
            </div>
            <Toggle on={!!p[key]} onChange={v => setPref(key, v)} />
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Sidebar nav ───────────────────────────────────────────────────────────────
interface NavItemProps {
  section: Section;
  active: Section;
  onClick: (s: Section) => void;
  icon: React.ReactNode;
  label: string;
}

function NavItem({ section, active, onClick, icon, label }: NavItemProps) {
  return (
    <button
      className={`nav-item ${active === section ? 'active' : ''}`}
      onClick={() => onClick(section)}
    >
      {icon}
      {label}
    </button>
  );
}

// ── Main modal ────────────────────────────────────────────────────────────────
interface UserEditModalProps {
  user: AidaUser;
  isAdmin: boolean;
  onSave: (updated: AidaUser) => void;
  onClose: () => void;
  onBlock: (uid: number) => void;
}

export function UserEditModal({ user, isAdmin, onSave, onClose, onBlock }: UserEditModalProps) {
  const { t } = useTranslation();
  const [section, setSection] = useState<Section>('profile');
  const [draft, setDraft] = useState<AidaUser>({ ...user });

  const clr = roleColor(draft.role);

  const handleSave = () => {
    onSave(draft);
    onClose();
  };

  return (
    <div className="modal-overlay open" onClick={e => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal" onClick={e => e.stopPropagation()}>

        {/* Header */}
        <div className="modal-header">
          <div
            className="modal-header-ava"
            style={{
              background: `color-mix(in srgb, ${clr} 18%, var(--bg3))`,
              border: `1px solid color-mix(in srgb, ${clr} 28%, transparent)`,
              color: clr,
            }}
          >
            {initials(draft.name)}
          </div>
          <div className="modal-header-info">
            <div className="modal-header-name">{draft.name}</div>
            <div className="modal-header-sub">{draft.email}</div>
          </div>
          <span className={`badge ${draft.active ? 'badge-suc' : 'badge-err'}`} style={{ fontSize: 10 }}>
            {draft.active ? 'active' : 'disabled'}
          </span>
          <button className="modal-close" onClick={onClose}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="modal-body">

          {/* Sidebar */}
          <nav className="modal-nav">
            <span className="nav-section-label">Аккаунт</span>
            <NavItem section="profile" active={section} onClick={setSection} label={t('users.sections.profile')}
              icon={<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>} />
            <NavItem section="role" active={section} onClick={setSection} label={t('users.sections.role')}
              icon={<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" /></svg>} />
            <NavItem section="permissions" active={section} onClick={setSection} label={t('users.sections.permissions')}
              icon={<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" /></svg>} />
            <span className="nav-section-label">Ресурсы</span>
            <NavItem section="quotas" active={section} onClick={setSection} label={t('users.sections.quotas')}
              icon={<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12" /></svg>} />
            <NavItem section="sources" active={section} onClick={setSection} label={t('users.sections.sources')}
              icon={<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><ellipse cx="12" cy="5" rx="9" ry="3" /><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3" /><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5" /></svg>} />
            <span className="nav-section-label">Мониторинг</span>
            <NavItem section="activity" active={section} onClick={setSection} label={t('users.sections.activity')}
              icon={<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" /><path d="M3 9h18M9 21V9" /></svg>} />
            <span className="nav-section-label">Личный кабинет</span>
            <NavItem section="prefs" active={section} onClick={setSection} label={t('users.sections.prefs')}
              icon={<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" /></svg>} />
          </nav>

          {/* Content */}
          <div className="modal-content">
            {section === 'profile'     && <ProfileSection     draft={draft} setDraft={setDraft} />}
            {section === 'role'        && <RoleSection        draft={draft} setDraft={setDraft} isAdmin={isAdmin} />}
            {section === 'permissions' && <PermissionsSection draft={draft} />}
            {section === 'quotas'      && <QuotasSection      draft={draft} setDraft={setDraft} />}
            {section === 'sources'     && <SourcesSection     draft={draft} setDraft={setDraft} />}
            {section === 'activity'    && <ActivitySection    user={user} />}
            {section === 'prefs'       && <PrefsSection       draft={draft} setDraft={setDraft} />}
          </div>
        </div>

        {/* Footer */}
        <div className="modal-footer">
          <div style={{ display: 'flex', gap: 8 }}>
            {isAdmin && (
              draft.active
                ? (
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={() => { onBlock(user.id); onClose(); }}
                  >
                    {t('users.block')}
                  </button>
                )
                : (
                  <button
                    className="btn btn-secondary btn-sm"
                    style={{ color: 'var(--suc)' }}
                    onClick={() => { onBlock(user.id); onClose(); }}
                  >
                    {t('users.unblock')}
                  </button>
                )
            )}
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-secondary" onClick={onClose}>{t('users.cancel')}</button>
            <button className="btn btn-primary" onClick={handleSave}>{t('users.save')}</button>
          </div>
        </div>
      </div>
    </div>
  );
}
