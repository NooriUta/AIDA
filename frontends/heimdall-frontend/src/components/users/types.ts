// ── User domain types for HEIMDALL Users page ────────────────────────────────

export type UserRole =
  | 'viewer'
  | 'editor'
  | 'operator'
  | 'auditor'
  | 'local-admin'
  | 'tenant-owner'
  | 'admin'
  | 'super-admin';

export interface UserQuotas {
  mimir:    number; // requests/hour
  sessions: number; // parallel Dali sessions
  atoms:    number; // max atoms per session
  workers:  number; // max workers from pool
  anvil:    number; // traversals/hour
}

/**
 * UserPrefs — server-side preferences stored as Keycloak user attributes.
 *
 * NOT included here (they live in localStorage, managed per-device by verdandi):
 *   theme, palette, uiFont, monoFont, fontSize, density, lang
 *   (localStorage keys: seer-theme, seer-palette, seer-ui-font, …)
 *
 * FRIGG (R4.3, not yet wired): quotas, sources, activity
 */
export interface UserPrefs {
  // ── Identity/UX — Keycloak attribute keys: "pref.lang", "tz", "dateFmt", "startPage", "avatarColor"
  lang:        string;  // 'ru' | 'en' — KC attr "pref.lang"; admin-visible
  tz:          string;  // IANA timezone: 'Europe/Moscow' | 'UTC' | …
  dateFmt:     string;  // 'DD.MM.YYYY' | 'YYYY-MM-DD' | 'MM/DD/YYYY'
  startPage:   string;  // 'dashboard' | 'loom' | 'events' | 'services'
  avatarColor: string;  // hex — KC attr "avatarColor"

  // ── Notifications — KC attribute keys: "notify.email", "notify.browser", …
  notifyEmail:    boolean;
  notifyBrowser:  boolean;
  notifyHarvest:  boolean;
  notifyErrors:   boolean;
  notifyDigest:   boolean;
}

export interface AidaUser {
  id:         number;
  kcId?:      string;   // KC UUID — for /api/admin/users/:id/* calls
  name:       string;   // KC username
  firstName?: string;
  lastName?:  string;
  email:      string;
  role:       UserRole;
  active:     boolean;
  title:      string;
  dept:       string;
  phone:      string;
  sources:    string[];    // empty = access to all sources
  quotas:     UserQuotas;
  lastActive: string;
  sessions:   { used: number; total: number };
  mimir:      { used: number; total: number };
  prefs:      UserPrefs;
}

export interface RoleDef {
  id:     UserRole;
  label:  string;
  tier:   'user' | 'tenant' | 'platform';
  clr:    string;
  desc:   string;
  scopes: string[];
}

export interface ScopeDef {
  id:   string;
  desc: string;
}

// ── Static reference data ─────────────────────────────────────────────────────

// ── Storage split: what goes where ───────────────────────────────────────────
//
//  KEYCLOAK (user attributes, key-value store in realm):
//    Identity  : username, email, password, 2FA (built-in KC fields)
//    Role      : Keycloak realm role assigned by HEIMDALL admin panel
//    Profile   : title, dept, phone  (KC attribute: "title", "dept", "phone")
//    Lang      : lang  (KC attribute: "pref.lang") — admin-visible + cross-device
//    Shared UI : tz, dateFmt, startPage, avatarColor
//                (KC attributes: "tz", "dateFmt", "startPage", "avatarColor")
//    Notify    : notifyEmail, notifyBrowser, notifyHarvest, notifyErrors, notifyDigest
//                (KC attributes: "notify.email", …)
//
//  FRIGG (ArcadeDB):
//    UI prefs  : theme, palette, density, uiFont, monoFont, fontSize
//                (UserPrefs document, keyed by KC sub — cross-device sync)
//    Quotas    : mimir, sessions, atoms, workers, anvil  (R4.3)
//    Sources   : sources[]  (source binding list, R4.3)
//    Activity  : session history, event log entries, lastActive timestamp (R4.3)
//    Snapshots : ring-buffer snapshots (existing R4.1)
//
//  localStorage (per-device cache):
//    theme, palette, density, uiFont, monoFont, fontSize
//    (keys: seer-theme, seer-palette, … — synced from FRIGG on login)
//
// ─────────────────────────────────────────────────────────────────────────────

export const ROLES: RoleDef[] = [
  {
    id: 'viewer', label: 'viewer', tier: 'user', clr: 'var(--t2)',
    desc: 'Просмотр lineage графа, KNOT, поиск. Только чтение.',
    scopes: ['seer:read'],
  },
  {
    id: 'editor', label: 'editor', tier: 'user', clr: 'var(--inf)',
    desc: 'Аннотации нод, saved views, MIMIR (с квотой).',
    scopes: ['seer:read', 'seer:write'],
  },
  {
    id: 'operator', label: 'operator', tier: 'user', clr: 'var(--wrn)',
    desc: 'Запуск harvest, мониторинг Dali, event stream.',
    scopes: ['seer:read', 'aida:harvest'],
  },
  {
    id: 'auditor', label: 'auditor', tier: 'user', clr: '#afa9ec',
    desc: 'Audit log, tool call trace. Только чтение.',
    scopes: ['seer:read', 'aida:audit'],
  },
  {
    id: 'local-admin', label: 'local-admin', tier: 'tenant', clr: '#5dcaa5',
    desc: 'Управление пользователями tenant, квоты.',
    scopes: ['seer:read', 'seer:write', 'aida:harvest', 'aida:tenant:admin'],
  },
  {
    id: 'tenant-owner', label: 'tenant-owner', tier: 'tenant', clr: '#f0997b',
    desc: 'Настройки tenant, billing, назначение local-admin.',
    scopes: ['seer:read', 'seer:write', 'aida:harvest', 'aida:tenant:admin', 'aida:tenant:owner'],
  },
  {
    id: 'admin', label: 'admin', tier: 'platform', clr: 'var(--suc)',
    desc: 'Полный HEIMDALL, Sources, restart сервисов, DEMODEBUG.',
    scopes: ['seer:read', 'seer:write', 'aida:harvest', 'aida:admin'],
  },
  {
    id: 'super-admin', label: 'super-admin', tier: 'platform', clr: '#ed93b1',
    desc: 'Платформа, cross-tenant, Keycloak realm.',
    scopes: ['seer:read', 'seer:write', 'aida:harvest', 'aida:admin', 'aida:superadmin'],
  },
];

export const SCOPE_INFO: ScopeDef[] = [
  { id: 'seer:read',         desc: 'Чтение lineage данных, LOOM, KNOT, ANVIL results' },
  { id: 'seer:write',        desc: 'Аннотации, saved views, MIMIR full access' },
  { id: 'aida:harvest',      desc: 'Запуск parse-сессий, просмотр Dali' },
  { id: 'aida:audit',        desc: 'Audit log + экспорт CSV/JSON' },
  { id: 'aida:tenant:admin', desc: 'Управление пользователями tenant' },
  { id: 'aida:tenant:owner', desc: 'Billing, настройки tenant' },
  { id: 'aida:admin',        desc: 'HEIMDALL, Sources, restart сервисов' },
  { id: 'aida:superadmin',   desc: 'Платформа, cross-tenant, Keycloak' },
];

export const SOURCES: string[] = [
  'Oracle CRM Production',
  'PostgreSQL Analytics',
  'Oracle ERP',
  'ClickHouse Metrics',
  'Oracle Legacy',
];

export const AVATAR_COLORS: string[] = [
  '#A8B860', '#7DBF78', '#88B8A8', '#D4922A',
  '#C85848', '#afa9ec', '#5dcaa5', '#f0997b',
];

export const ACT_EVENTS: string[] = [
  'LOGIN', 'LOOM_VIEW', 'MIMIR_QUERY', 'HARVEST_START',
  'LOGOUT', 'ANNOTATION_EDIT', 'VIEW_SAVED', 'ANVIL_TRAVERSAL',
];

export const ELEVATED_ROLES: UserRole[] = [
  'admin', 'super-admin', 'tenant-owner', 'local-admin',
];

export const ADMIN_ROLES: UserRole[] = [
  'admin', 'super-admin', 'local-admin', 'tenant-owner',
];

/** Roles allowed to enter Heimdall at all. viewer/editor/operator → 403. */
export const HEIMDALL_ALLOWED_ROLES: UserRole[] = [
  'admin', 'super-admin', 'local-admin', 'tenant-owner', 'auditor',
];
