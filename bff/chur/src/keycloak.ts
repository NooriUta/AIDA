/**
 * Keycloak HTTP client for BFF (Backend-for-Frontend) pattern.
 * Uses Direct Access Grants (Resource Owner Password Credentials) flow.
 * No SDK — plain fetch + jose for JWKS verification.
 */
import { createRemoteJWKSet, jwtVerify, type JWTPayload } from 'jose';
import { config } from './config';
import type { UserRole } from './types';

// ── Types ────────────────────────────────────────────────────────────────────

export interface KeycloakTokenResponse {
  access_token:  string;
  refresh_token: string;
  expires_in:    number;     // seconds
  token_type:    string;
}

export interface KeycloakUserInfo {
  sub:            string;
  username:       string;
  role:           UserRole;
  scopes:         string[];   // from JWT scope claim
  email?:         string;
  firstName?:     string;
  lastName?:      string;
  emailVerified?: boolean;
  /** Tenant alias from KC — `seer_tenant` custom claim or first KC Organization key. */
  tenantAlias?:   string;
  /** Per-tenant role map from KC 26.6 `organization` claim (alias → role/group).
   *  Empty when JWT carries only legacy `organization.role` (single-tenant). */
  tenantRoleMap?: Record<string, string>;
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function tokenUrl(): string {
  return `${config.keycloakUrl}/realms/${config.keycloakRealm}/protocol/openid-connect/token`;
}

/**
 * Browser-facing KC URL for Auth Code redirects (NOT for server-to-server).
 * Uses KEYCLOAK_PUBLIC_URL env var (e.g. http://localhost:18180/kc) — falls back
 * to KEYCLOAK_URL which may be internal docker hostname (http://keycloak:8180/kc).
 */
export function authUrl(): string {
  const publicBase = process.env.KEYCLOAK_PUBLIC_URL ?? config.keycloakUrl;
  return `${publicBase}/realms/${config.keycloakRealm}/protocol/openid-connect/auth`;
}

function logoutUrl(): string {
  return `${config.keycloakUrl}/realms/${config.keycloakRealm}/protocol/openid-connect/logout`;
}

function jwksUrl(): URL {
  return new URL(
    `${config.keycloakUrl}/realms/${config.keycloakRealm}/protocol/openid-connect/certs`,
  );
}

/** Lazy-initialized JWKS fetcher with built-in caching (jose handles TTL). */
let _jwks: ReturnType<typeof createRemoteJWKSet> | null = null;

function getJwks() {
  if (!_jwks) _jwks = createRemoteJWKSet(jwksUrl());
  return _jwks;
}

// ── Public API ───────────────────────────────────────────────────────────────

/**
 * Exchange username + password for Keycloak tokens via Direct Access Grants.
 * Throws on invalid credentials or Keycloak unreachable.
 */
export async function exchangeCredentials(
  username: string,
  password: string,
): Promise<KeycloakTokenResponse> {
  const res = await fetch(tokenUrl(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type:    'password',
      client_id:     config.keycloakClientId,
      client_secret: config.keycloakSecret,
      username,
      password,
    }),
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    const desc = (body as { error_description?: string }).error_description ?? 'authentication failed';
    throw new Error(`Keycloak login failed: ${desc}`);
  }

  return res.json() as Promise<KeycloakTokenResponse>;
}

/**
 * Exchange Authorization Code for tokens (Auth Code + PKCE flow).
 * Throws on invalid code, redirect_uri mismatch, or PKCE verifier mismatch.
 */
export async function exchangeAuthCode(
  code: string,
  redirectUri: string,
  codeVerifier: string,
  clientId?: string,
  clientSecret?: string,
): Promise<KeycloakTokenResponse> {
  const res = await fetch(tokenUrl(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type:    'authorization_code',
      client_id:     clientId ?? config.keycloakClientId,
      client_secret: clientSecret ?? config.keycloakSecret,
      code,
      redirect_uri:  redirectUri,
      code_verifier: codeVerifier,
    }),
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    const desc = (body as { error_description?: string }).error_description ?? 'code exchange failed';
    throw new Error(`Keycloak Auth Code exchange failed: ${desc}`);
  }

  return res.json() as Promise<KeycloakTokenResponse>;
}

/**
 * Refresh an access token using a refresh token.
 * Throws if the refresh token is expired or revoked.
 */
export async function refreshAccessToken(
  refreshToken: string,
): Promise<KeycloakTokenResponse> {
  const res = await fetch(tokenUrl(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type:    'refresh_token',
      client_id:     config.keycloakClientId,
      client_secret: config.keycloakSecret,
      refresh_token: refreshToken,
    }),
  });

  if (!res.ok) {
    throw new Error('Keycloak refresh failed — session expired');
  }

  return res.json() as Promise<KeycloakTokenResponse>;
}

/**
 * Verify a Keycloak access token using JWKS and extract user info.
 * Returns sub, preferred_username, and the highest seer role.
 */
export async function verifyAccessToken(
  accessToken: string,
): Promise<KeycloakUserInfo> {
  const { payload } = await jwtVerify(accessToken, getJwks(), {
    issuer: `${config.keycloakUrl}/realms/${config.keycloakRealm}`,
  });

  return extractUserInfo(payload);
}

/**
 * Decode user info from JWT payload (claims).
 * Role is extracted from `seer_roles` claim (realm role mapper)
 * or falls back to `realm_access.roles`.
 */
export function extractUserInfo(payload: JWTPayload): KeycloakUserInfo {
  const sub      = payload.sub ?? '';
  const username = (payload as { preferred_username?: string }).preferred_username ?? 'anonymous';

  // Try custom claim first (protocol mapper "seer-role-mapper")
  const seerRoles = (payload as { seer_roles?: string[] }).seer_roles;
  // Fallback: standard realm_access.roles
  const realmRoles = (payload as { realm_access?: { roles?: string[] } }).realm_access?.roles;

  // RBAC_MULTITENANT v2: parse `organization` claim. Two formats supported:
  //   1. Legacy (KC 26.2 attribute mapper, single-tenant):
  //        { "organization": { "role": "editor", "alias": "default" } }
  //   2. KC 26.6 oidc-organization-group-membership-mapper (multi-tenant):
  //        { "organization": { "acme": { "groups": ["/editor"] }, "beta": { "groups": ["/viewer"] } } }
  const orgClaim0 = (payload as { organization?: Record<string, unknown> }).organization;
  const tenantRoleMap: Record<string, string> = {};
  let orgRoleAttr: string | undefined;
  if (orgClaim0 && typeof orgClaim0 === 'object') {
    if (typeof orgClaim0['role'] === 'string') {
      // Legacy single-tenant format
      orgRoleAttr = orgClaim0['role'] as string;
    } else {
      // KC 26.6 per-tenant format
      for (const [alias, val] of Object.entries(orgClaim0)) {
        if (alias === 'role' || alias === 'alias') continue;
        const groups = (val as { groups?: string[] })?.groups;
        if (Array.isArray(groups) && groups.length > 0) {
          // Group path like "/editor" → role name "editor"
          const roleName = groups[0]!.replace(/^\//, '').split('/').pop() ?? '';
          if (roleName) tenantRoleMap[alias] = roleName;
        }
      }
    }
  }

  const rolePool = [...(seerRoles ?? realmRoles ?? [])];
  if (orgRoleAttr && !rolePool.includes(orgRoleAttr)) rolePool.push(orgRoleAttr);
  // For KC 26.6 multi-tenant: pick highest role from primary tenant (org-alias attr)
  // If no primary alias, take first entry in map.
  const primaryAlias = orgClaim0 && typeof (orgClaim0 as Record<string, unknown>)['alias'] === 'string'
    ? (orgClaim0 as Record<string, string>)['alias']
    : Object.keys(tenantRoleMap)[0];
  const primaryTenantRole = primaryAlias ? tenantRoleMap[primaryAlias] : undefined;
  if (primaryTenantRole && !rolePool.includes(primaryTenantRole)) rolePool.push(primaryTenantRole);
  const roles = rolePool;
  const role  = pickHighestRole(roles);

  // Extract scopes from the JWT `scope` claim.
  // Keycloak may return `scope` as a space-separated string OR as a string[].
  // Array.isArray guard handles both — without it, .split() throws at runtime
  // when KC is configured to emit scope as an array (FIX-B / INV-3).
  // If the token has no AIDA/Seer scopes (KC scope mappers not configured),
  // fall back to deriving them from realm roles so that requireScope() works
  // without KC reconfiguration.
  const rawScope = (payload as { scope?: string | string[] }).scope;
  const jwtScopes = Array.isArray(rawScope)
    ? rawScope.filter(Boolean)
    : (rawScope?.split(' ').filter(Boolean) ?? []);
  const hasAidaScopes = jwtScopes.some((s) => s.startsWith('aida:') || s.startsWith('seer:'));
  const scopes = hasAidaScopes ? jwtScopes : [...jwtScopes, ...deriveAidaScopes(roles)];

  const email         = (payload as { email?: string }).email;
  const firstName     = (payload as { given_name?: string }).given_name;
  const lastName      = (payload as { family_name?: string }).family_name;
  const emailVerified = (payload as { email_verified?: boolean }).email_verified;

  // Tenant resolution (priority order):
  //   1. seer_tenant — custom KC protocol mapper attribute
  //   2. organization.alias — custom OIDC mapper (claim.name="organization.alias") → {"organization":{"alias":"<name>"}}
  //   3. organization first key — standard KC 26 orgs format → {"organization":{"<name>":{}}}
  const seerTenant = (payload as { seer_tenant?: string }).seer_tenant;
  const orgClaim   = orgClaim0;
  const orgAlias   = orgClaim && typeof orgClaim === 'object'
    ? (typeof orgClaim['alias'] === 'string' ? orgClaim['alias'] : Object.keys(orgClaim).find(k => k !== 'role'))
    : undefined;
  const tenantAlias = seerTenant ?? orgAlias;

  return {
    sub, username, role, scopes, email, firstName, lastName, emailVerified, tenantAlias,
    ...(Object.keys(tenantRoleMap).length > 0 ? { tenantRoleMap } : {}),
  };
}

/** Server-side logout: invalidate the refresh token in Keycloak. */
export async function keycloakLogout(refreshToken: string): Promise<void> {
  await fetch(logoutUrl(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      client_id:     config.keycloakClientId,
      client_secret: config.keycloakSecret,
      refresh_token: refreshToken,
    }),
  }).catch(() => {/* fire-and-forget */});
}

// ── Internal ─────────────────────────────────────────────────────────────────

const ROLE_PRIORITY: UserRole[] = [
  'super-admin', 'admin', 'local-admin', 'tenant-owner',
  'auditor', 'operator', 'editor', 'viewer',
];

function pickHighestRole(roles: string[]): UserRole {
  for (const r of ROLE_PRIORITY) {
    if (roles.includes(r)) return r;
  }
  return 'viewer';
}

/**
 * Fallback scope derivation: used when the KC token `scope` claim doesn't
 * include AIDA scopes (i.e. the KC realm hasn't been configured with
 * role-to-scope protocol mappers yet).
 *
 * Mirrors the optionalClientScopes in seer-realm.json so that the BFF
 * enforces the same access model with or without KC scope mappers.
 */
// Mirrors RBAC_MULTITENANT.md §2 — must match exactly
// G10: aida:admin:destructive scope removed from super-admin (not in spec v1.4+).
//      Destructive ops are gated on aida:superadmin alone.
const ROLE_AIDA_SCOPES: Record<string, string[]> = {
  'super-admin':  ['seer:read', 'seer:write', 'aida:harvest', 'aida:audit', 'aida:admin', 'aida:superadmin'],
  'admin':        ['seer:read', 'seer:write', 'aida:harvest', 'aida:audit', 'aida:admin', 'aida:tenant:admin'],
  'local-admin':  ['seer:read', 'seer:write', 'aida:harvest', 'aida:tenant:admin'],
  'tenant-owner': ['seer:read', 'seer:write', 'aida:harvest', 'aida:tenant:admin', 'aida:tenant:owner'],
  'operator':     ['seer:read', 'aida:harvest'],
  'auditor':      ['seer:read', 'aida:audit'],
  'editor':       ['seer:read', 'seer:write'],
  'viewer':       ['seer:read'],
};

function deriveAidaScopes(roles: string[]): string[] {
  const out = new Set<string>();
  for (const role of roles) {
    for (const scope of ROLE_AIDA_SCOPES[role] ?? []) out.add(scope);
  }
  return [...out];
}
