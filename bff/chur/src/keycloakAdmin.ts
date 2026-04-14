/**
 * Keycloak Admin REST API client.
 * Uses the Keycloak master-realm bootstrap admin to read users + attributes
 * from the `seer` realm.
 *
 * Storage split (what lives where):
 *   KEYCLOAK attributes — profile (title, dept, phone), avatarColor,
 *                         tz, dateFmt, startPage, notify.*,
 *                         quota_*, source_bindings  (R4.3)
 *   FRIGG (M3 backlog)  — session history, usage counters
 *   localStorage (client)— theme, palette, uiFont, monoFont, fontSize,
 *                          density, lang  (synced by verdandi / ProfileModal)
 */

import { config } from './config';
import type { UserRole } from './types';

const KC_BASE  = config.keycloakUrl;
const KC_REALM = config.keycloakRealm;

// ── Keycloak raw shapes ───────────────────────────────────────────────────────

interface KcUser {
  id:         string;
  username:   string;
  firstName?: string;
  lastName?:  string;
  email?:     string;
  enabled:    boolean;
  /** KC attributes: each value is a string array  */
  attributes?: Record<string, string[]>;
}

interface KcRole {
  id:   string;
  name: string;
}

// ── Public shape returned to HEIMDALL frontend ────────────────────────────────

export interface KcUserView {
  id:        string;    // KC UUID
  name:      string;    // KC username
  firstName: string;
  lastName:  string;
  email:     string;
  role:      UserRole;
  active:    boolean;
  // ── Keycloak attributes ──
  title:       string;
  dept:        string;
  phone:       string;
  avatarColor: string;
  lang:        string;  // KC attr "pref.lang" — "ru" | "en" | ...
  tz:          string;
  dateFmt:     string;
  startPage:   string;
  notifyEmail:    boolean;
  notifyBrowser:  boolean;
  notifyHarvest:  boolean;
  notifyErrors:   boolean;
  notifyDigest:   boolean;
  // ── R4.3: quotas + source bindings (from KC attrs quota_* / source_bindings) ──
  quotas: {
    mimir:    number;
    sessions: number;
    atoms:    number;
    workers:  number;
    anvil:    number;
  };
  sources: string[];   // source system IDs e.g. ["hound", "dali"]
}

// ── Helpers ───────────────────────────────────────────────────────────────────

const ROLE_PRIORITY: UserRole[] = [
  'super-admin', 'admin', 'local-admin', 'tenant-owner',
  'auditor', 'operator', 'editor', 'viewer',
];

function pickRole(roles: string[]): UserRole {
  for (const r of ROLE_PRIORITY) {
    if (roles.includes(r)) return r;
  }
  return 'viewer';
}

function attr(u: KcUser, key: string, fallback = ''): string {
  return u.attributes?.[key]?.[0] ?? fallback;
}

function attrBool(u: KcUser, key: string, fallback = false): boolean {
  const v = u.attributes?.[key]?.[0];
  if (v === undefined) return fallback;
  return v === 'true';
}

// ── Admin token (master realm, admin-cli client) ──────────────────────────────

async function getAdminToken(): Promise<string> {
  const res = await fetch(
    `${KC_BASE}/realms/master/protocol/openid-connect/token`,
    {
      method:  'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body:    new URLSearchParams({
        grant_type: 'password',
        client_id:  'admin-cli',
        username:   config.kcAdminUser,
        password:   config.kcAdminPass,
      }),
      signal: AbortSignal.timeout(5_000),
    },
  );
  if (!res.ok) {
    throw new Error(`KC admin auth failed: ${res.status} ${res.statusText}`);
  }
  const data = await res.json() as { access_token: string };
  return data.access_token;
}

// ── Public API ────────────────────────────────────────────────────────────────

/**
 * List all users in the seer realm with their attributes and highest role.
 * Returns a flat array ready for HEIMDALL UsersPage.
 * Returns [] if Keycloak Admin API is unavailable.
 */
export async function listUsers(): Promise<KcUserView[]> {
  try {
    const token = await getAdminToken();
    const headers: HeadersInit = { Authorization: `Bearer ${token}` };

    // 1. Fetch all users (includes attributes)
    const usersRes = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/users?max=200&briefRepresentation=false`,
      { headers, signal: AbortSignal.timeout(5_000) },
    );
    if (!usersRes.ok) {
      console.warn(`[KC] listUsers ${usersRes.status}`);
      return [];
    }
    const kcUsers = await usersRes.json() as KcUser[];

    // 2. Fetch realm roles for each user (parallel)
    const views = await Promise.all(
      kcUsers.map(async (u): Promise<KcUserView> => {
        const rolesRes = await fetch(
          `${KC_BASE}/admin/realms/${KC_REALM}/users/${u.id}/role-mappings/realm`,
          { headers, signal: AbortSignal.timeout(5_000) },
        );
        const kcRoles: KcRole[] = rolesRes.ok
          ? await rolesRes.json() as KcRole[]
          : [];

        const roleNames = kcRoles.map(r => r.name);
        return {
          id:        u.id,
          name:      u.username,
          firstName: u.firstName ?? '',
          lastName:  u.lastName  ?? '',
          email:     u.email     ?? '',
          role:      pickRole(roleNames),
          active:    u.enabled,
          // KC attributes
          title:       attr(u, 'title'),
          dept:        attr(u, 'dept'),
          phone:       attr(u, 'phone'),
          avatarColor: attr(u, 'avatarColor', '#A8B860'),
          lang:        attr(u, 'pref.lang',   'ru'),
          tz:          attr(u, 'tz',          'Europe/Moscow'),
          dateFmt:     attr(u, 'dateFmt',     'DD.MM.YYYY'),
          startPage:   attr(u, 'startPage',   'dashboard'),
          notifyEmail:    attrBool(u, 'notify.email',    true),
          notifyBrowser:  attrBool(u, 'notify.browser',  false),
          notifyHarvest:  attrBool(u, 'notify.harvest',  false),
          notifyErrors:   attrBool(u, 'notify.errors',   true),
          notifyDigest:   attrBool(u, 'notify.digest',   false),
          quotas: {
            mimir:    parseInt(attr(u, 'quota_mimir',    '20'),  10),
            sessions: parseInt(attr(u, 'quota_sessions', '2'),   10),
            atoms:    parseInt(attr(u, 'quota_atoms',    '50000'), 10),
            workers:  parseInt(attr(u, 'quota_workers',  '4'),   10),
            anvil:    parseInt(attr(u, 'quota_anvil',    '50'),  10),
          },
          sources: attr(u, 'source_bindings', '').split(',').filter(Boolean),
        };
      }),
    );

    return views;
  } catch (err) {
    // AbortError = timeout, TypeError = network unreachable
    console.warn('[KC] listUsers unavailable:', (err as Error).message);
    return [];
  }
}

/**
 * Update user attributes in Keycloak (profile + prefs).
 * Does NOT change role or enabled state — those are separate KC operations.
 * Silently swallows errors if Keycloak Admin API is unavailable.
 */
export async function updateUserAttrs(
  userId: string,
  patch: Partial<Omit<KcUserView, 'id' | 'name' | 'email' | 'role' | 'active' | 'quotas' | 'sources'>>
       & { quotas?: KcUserView['quotas']; sources?: string[] },
): Promise<void> {
  try {
    const token = await getAdminToken();

    // Build KC attributes object (all values are string arrays)
    const attributes: Record<string, string[]> = {};
    if (patch.title       !== undefined) attributes['title']           = [patch.title];
    if (patch.dept        !== undefined) attributes['dept']            = [patch.dept];
    if (patch.phone       !== undefined) attributes['phone']           = [patch.phone];
    if (patch.avatarColor !== undefined) attributes['avatarColor']     = [patch.avatarColor];
    if (patch.lang        !== undefined) attributes['pref.lang']       = [patch.lang];
    if (patch.tz          !== undefined) attributes['tz']              = [patch.tz];
    if (patch.dateFmt     !== undefined) attributes['dateFmt']         = [patch.dateFmt];
    if (patch.startPage   !== undefined) attributes['startPage']       = [patch.startPage];
    if (patch.notifyEmail    !== undefined) attributes['notify.email']   = [String(patch.notifyEmail)];
    if (patch.notifyBrowser  !== undefined) attributes['notify.browser'] = [String(patch.notifyBrowser)];
    if (patch.notifyHarvest  !== undefined) attributes['notify.harvest'] = [String(patch.notifyHarvest)];
    if (patch.notifyErrors   !== undefined) attributes['notify.errors']  = [String(patch.notifyErrors)];
    if (patch.notifyDigest   !== undefined) attributes['notify.digest']  = [String(patch.notifyDigest)];
    if (patch.quotas !== undefined) {
      attributes['quota_mimir']    = [String(patch.quotas.mimir)];
      attributes['quota_sessions'] = [String(patch.quotas.sessions)];
      attributes['quota_atoms']    = [String(patch.quotas.atoms)];
      attributes['quota_workers']  = [String(patch.quotas.workers)];
      attributes['quota_anvil']    = [String(patch.quotas.anvil)];
    }
    if (patch.sources !== undefined) {
      attributes['source_bindings'] = [patch.sources.join(',')];
    }

    const res = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}`,
      {
        method:  'PUT',
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        body:    JSON.stringify({
          firstName:  patch.firstName,
          attributes,
        }),
        signal: AbortSignal.timeout(5_000),
      },
    );
    if (!res.ok) {
      console.warn(`[KC] updateUserAttrs ${res.status}`);
    }
  } catch (err) {
    console.warn('[KC] updateUserAttrs unavailable:', (err as Error).message);
  }
}

/**
 * Enable or disable a user in Keycloak.
 * Silently swallows errors if Keycloak Admin API is unavailable.
 */
export async function setUserEnabled(userId: string, enabled: boolean): Promise<void> {
  try {
    const token = await getAdminToken();
    const res = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}`,
      {
        method:  'PUT',
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        body:    JSON.stringify({ enabled }),
        signal:  AbortSignal.timeout(5_000),
      },
    );
    if (!res.ok) {
      console.warn(`[KC] setUserEnabled ${res.status}`);
    }
  } catch (err) {
    console.warn('[KC] setUserEnabled unavailable:', (err as Error).message);
  }
}
