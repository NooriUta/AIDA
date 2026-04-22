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

/** Build the public KcUserView shape from a raw KC user + role list. */
function buildView(u: KcUser, kcRoles: KcRole[]): KcUserView {
  const roleNames = kcRoles.map(r => r.name);
  return {
    id:        u.id,
    name:      u.username,
    firstName: u.firstName ?? '',
    lastName:  u.lastName  ?? '',
    email:     u.email     ?? '',
    role:      pickRole(roleNames),
    active:    u.enabled,
    // profile attributes (new prefixed keys R4.9)
    title:       attr(u, 'profile.title') || attr(u, 'title'),
    dept:        attr(u, 'profile.dept')  || attr(u, 'dept'),
    phone:       attr(u, 'profile.phone') || attr(u, 'phone'),
    // prefs attributes (new prefixed keys R4.10, fallback to legacy flat keys)
    avatarColor: attr(u, 'prefs.avatarColor') || attr(u, 'avatarColor', '#A8B860'),
    lang:        attr(u, 'prefs.lang')        || attr(u, 'pref.lang',   'ru'),
    tz:          attr(u, 'prefs.tz')          || attr(u, 'tz',          'Europe/Moscow'),
    dateFmt:     attr(u, 'prefs.dateFmt')     || attr(u, 'dateFmt',     'DD.MM.YYYY'),
    startPage:   attr(u, 'prefs.startPage')   || attr(u, 'startPage',   'dashboard'),
    notifyEmail:    attrBool(u, 'prefs.notify.email',   true),
    notifyBrowser:  attrBool(u, 'prefs.notify.browser', false),
    notifyHarvest:  attrBool(u, 'prefs.notify.harvest', false),
    notifyErrors:   attrBool(u, 'prefs.notify.errors',  true),
    notifyDigest:   attrBool(u, 'prefs.notify.digest',  false),
    quotas: {
      mimir:    parseInt(attr(u, 'quota_mimir',    '20'),     10),
      sessions: parseInt(attr(u, 'quota_sessions', '2'),      10),
      atoms:    parseInt(attr(u, 'quota_atoms',    '50000'),  10),
      workers:  parseInt(attr(u, 'quota_workers',  '4'),      10),
      anvil:    parseInt(attr(u, 'quota_anvil',    '50'),     10),
    },
    sources: attr(u, 'source_bindings', '').split(',').filter(Boolean),
  };
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

        return buildView(u, kcRoles);
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
 * Fetch a single user by KC UUID, including their highest realm role and attributes.
 */
export async function getUser(userId: string): Promise<KcUserView> {
  const token   = await getAdminToken();
  const headers: HeadersInit = { Authorization: `Bearer ${token}` };

  const [userRes, rolesRes] = await Promise.all([
    fetch(`${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}`,
      { headers, signal: AbortSignal.timeout(5_000) }),
    fetch(`${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}/role-mappings/realm`,
      { headers, signal: AbortSignal.timeout(5_000) }),
  ]);

  if (!userRes.ok) throw new Error(`KC getUser ${userRes.status}`);
  const u: KcUser     = await userRes.json();
  const kcRoles: KcRole[] = rolesRes.ok ? await rolesRes.json() : [];

  return buildView(u, kcRoles);
}

/**
 * Read specific Keycloak user attributes.
 * Returns flat Record<key, value> (first element of KC string-array value).
 * Keys not present in KC attributes are returned as empty strings.
 */
export async function getUserAttributes(
  userId: string,
  keys:   string[],
): Promise<Record<string, string>> {
  const token = await getAdminToken();
  const res = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}`,
    { headers: { Authorization: `Bearer ${token}` }, signal: AbortSignal.timeout(5_000) },
  );
  if (!res.ok) throw new Error(`KC getUserAttributes ${res.status}`);
  const u: KcUser = await res.json();
  const out: Record<string, string> = {};
  for (const key of keys) {
    out[key] = u.attributes?.[key]?.[0] ?? '';
  }
  return out;
}

/**
 * Patch Keycloak user attributes.
 * Fetches the user first to preserve unrelated attributes, then PUTs the merged map.
 */
export async function setUserAttributes(
  userId: string,
  attrs:  Record<string, string>,
): Promise<void> {
  const token   = await getAdminToken();
  const headers: HeadersInit = {
    Authorization:  `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  // Get current representation so we don't wipe unrelated attributes
  const getRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}`,
    { headers, signal: AbortSignal.timeout(5_000) },
  );
  if (!getRes.ok) throw new Error(`KC setUserAttributes/get ${getRes.status}`);
  const current: KcUser = await getRes.json();

  // Merge: new values override, rest is preserved
  const merged: Record<string, string[]> = { ...(current.attributes as Record<string, string[]> | undefined ?? {}) };
  for (const [key, val] of Object.entries(attrs)) {
    merged[key] = [val];
  }

  const putRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}`,
    {
      method:  'PUT',
      headers,
      body:    JSON.stringify({ attributes: merged }),
      signal:  AbortSignal.timeout(5_000),
    },
  );
  if (!putRes.ok) throw new Error(`KC setUserAttributes/put ${putRes.status}`);
}

// ── KC-ORG-04: Organization member APIs ─────────────────────────────────────

/**
 * KC-ORG-04: List members of a Keycloak Organization (26.2+ Organizations feature).
 * Returns users belonging to the org, with the same shape as listUsers() so the UI
 * can render the same table. Requires `keycloakOrgId` from DaliTenantConfig.
 */
export async function listOrgMembers(orgId: string): Promise<KcUserView[]> {
  try {
    const token = await getAdminToken();
    const headers: HeadersInit = { Authorization: `Bearer ${token}` };

    const res = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/organizations/${orgId}/members?briefRepresentation=false`,
      { headers, signal: AbortSignal.timeout(5_000) },
    );
    if (!res.ok) {
      console.warn(`[KC] listOrgMembers ${res.status}`);
      return [];
    }
    const kcUsers = await res.json() as KcUser[];

    // Resolve realm roles for each member in parallel
    const views = await Promise.all(
      kcUsers.map(async (u): Promise<KcUserView> => {
        const rolesRes = await fetch(
          `${KC_BASE}/admin/realms/${KC_REALM}/users/${u.id}/role-mappings/realm`,
          { headers, signal: AbortSignal.timeout(5_000) },
        );
        const kcRoles: KcRole[] = rolesRes.ok ? await rolesRes.json() as KcRole[] : [];
        return buildView(u, kcRoles);
      }),
    );
    return views;
  } catch (err) {
    console.warn('[KC] listOrgMembers unavailable:', (err as Error).message);
    return [];
  }
}

/**
 * KC-ORG-04: Create a KC user, assign realm role, add them as member of the given org,
 * and set the organization.alias attribute (KC-ORG-03 mapper reads it for JWT claim).
 * Sends password-reset email via executeActionsEmail.
 */
export async function inviteUserToOrg(
  orgId:      string,
  orgAlias:   string,
  email:      string,
  name:       string,
  role:       UserRole,
): Promise<void> {
  const token   = await getAdminToken();
  const headers: HeadersInit = {
    Authorization:  `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  // 1 — Create user with organization.alias attribute (for KC-ORG-03 mapper)
  const createRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users`,
    {
      method: 'POST', headers,
      body: JSON.stringify({
        username:        email,
        email,
        firstName:       name,
        enabled:         true,
        emailVerified:   false,
        requiredActions: ['UPDATE_PASSWORD'],
        attributes:      { 'organization.alias': [orgAlias] },
      }),
      signal: AbortSignal.timeout(10_000),
    },
  );
  if (!createRes.ok && createRes.status !== 201) {
    throw new Error(`KC inviteUserToOrg/create ${createRes.status}`);
  }
  const userId = (createRes.headers.get('Location') ?? '').split('/').pop();
  if (!userId) throw new Error('KC inviteUserToOrg: missing Location header');

  // 2 — Realm role
  const roleRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/roles/${encodeURIComponent(role)}`,
    { headers: { Authorization: `Bearer ${token}` }, signal: AbortSignal.timeout(5_000) },
  );
  if (!roleRes.ok) throw new Error(`KC inviteUserToOrg/getRole ${roleRes.status}`);
  const roleObj: KcRole = await roleRes.json();

  await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}/role-mappings/realm`,
    { method: 'POST', headers, body: JSON.stringify([roleObj]),
      signal: AbortSignal.timeout(5_000) },
  );

  // 3 — Add user to organization
  const addRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/organizations/${orgId}/members`,
    {
      method: 'POST', headers,
      // KC 26.2 accepts userId in body (plain string) OR representation. We use string form.
      body: JSON.stringify(userId),
      signal: AbortSignal.timeout(5_000),
    },
  );
  if (!addRes.ok && addRes.status !== 201 && addRes.status !== 204) {
    throw new Error(`KC inviteUserToOrg/addMember ${addRes.status}: ${await addRes.text().catch(() => '')}`);
  }

  // 4 — Password-reset email
  await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}/execute-actions-email`,
    { method: 'PUT', headers, body: JSON.stringify(['UPDATE_PASSWORD']),
      signal: AbortSignal.timeout(5_000) },
  );
}

/**
 * KC-ORG-04: Remove a user from the given organization (does NOT delete the user from realm).
 */
export async function removeOrgMember(orgId: string, userId: string): Promise<void> {
  try {
    const token = await getAdminToken();
    const res = await fetch(
      `${KC_BASE}/admin/realms/${KC_REALM}/organizations/${orgId}/members/${userId}`,
      {
        method:  'DELETE',
        headers: { Authorization: `Bearer ${token}` },
        signal:  AbortSignal.timeout(5_000),
      },
    );
    if (!res.ok && res.status !== 204) {
      console.warn(`[KC] removeOrgMember ${res.status}`);
    }
  } catch (err) {
    console.warn('[KC] removeOrgMember unavailable:', (err as Error).message);
  }
}

// ── Legacy user-oriented APIs (realm-wide, pre-KC-ORG) ──────────────────────

/**
 * Create a new KC user and immediately send a password-reset email (invite flow).
 * Uses the email as username.  Assigns the given realm role.
 */
export async function inviteUser(
  email: string,
  name:  string,
  role:  UserRole,
): Promise<void> {
  const token   = await getAdminToken();
  const headers: HeadersInit = {
    Authorization:  `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  // 1 — Create user
  const createRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users`,
    {
      method:  'POST',
      headers,
      body:    JSON.stringify({
        username:        email,
        email,
        firstName:       name,
        enabled:         true,
        emailVerified:   false,
        requiredActions: ['UPDATE_PASSWORD'],
      }),
      signal: AbortSignal.timeout(10_000),
    },
  );
  if (!createRes.ok && createRes.status !== 201) {
    throw new Error(`KC inviteUser/create ${createRes.status}`);
  }

  // Location header contains the new user URL
  const location = createRes.headers.get('Location') ?? '';
  const userId   = location.split('/').pop();
  if (!userId) throw new Error('KC inviteUser: missing Location header');

  // 2 — Look up role representation
  const roleRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/roles/${encodeURIComponent(role)}`,
    { headers: { Authorization: `Bearer ${token}` }, signal: AbortSignal.timeout(5_000) },
  );
  if (!roleRes.ok) throw new Error(`KC inviteUser/getRole ${roleRes.status}`);
  const roleObj: KcRole = await roleRes.json();

  // 3 — Assign realm role
  await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}/role-mappings/realm`,
    {
      method:  'POST',
      headers,
      body:    JSON.stringify([roleObj]),
      signal:  AbortSignal.timeout(5_000),
    },
  );

  // 4 — Trigger password-reset email
  await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}/execute-actions-email`,
    {
      method:  'PUT',
      headers,
      body:    JSON.stringify(['UPDATE_PASSWORD']),
      signal:  AbortSignal.timeout(5_000),
    },
  );
}

/** Known application realm roles (in priority order). */
const APP_ROLES = ROLE_PRIORITY; // re-use the existing ordered array

/**
 * Replace all app realm roles on a user with the single given role.
 * Built-in KC roles (offline_access, uma_authorization, default-roles-*) are untouched.
 */
export async function setUserRole(userId: string, role: UserRole): Promise<void> {
  const token   = await getAdminToken();
  const headers: HeadersInit = {
    Authorization:  `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  // 1 — Get current realm roles to remove our app roles
  const currentRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}/role-mappings/realm`,
    { headers: { Authorization: `Bearer ${token}` }, signal: AbortSignal.timeout(5_000) },
  );
  if (currentRes.ok) {
    const current: KcRole[] = await currentRes.json();
    const toRemove = current.filter(r => (APP_ROLES as string[]).includes(r.name));
    if (toRemove.length > 0) {
      await fetch(
        `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}/role-mappings/realm`,
        {
          method:  'DELETE',
          headers,
          body:    JSON.stringify(toRemove),
          signal:  AbortSignal.timeout(5_000),
        },
      );
    }
  }

  // 2 — Fetch the target role representation
  const roleRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/roles/${encodeURIComponent(role)}`,
    { headers: { Authorization: `Bearer ${token}` }, signal: AbortSignal.timeout(5_000) },
  );
  if (!roleRes.ok) throw new Error(`KC setUserRole/getRole ${roleRes.status}`);
  const roleObj: KcRole = await roleRes.json();

  // 3 — Assign new role
  const assignRes = await fetch(
    `${KC_BASE}/admin/realms/${KC_REALM}/users/${userId}/role-mappings/realm`,
    {
      method:  'POST',
      headers,
      body:    JSON.stringify([roleObj]),
      signal:  AbortSignal.timeout(5_000),
    },
  );
  if (!assignRes.ok) throw new Error(`KC setUserRole/assign ${assignRes.status}`);
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
