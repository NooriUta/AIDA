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
  sub:      string;
  username: string;
  role:     UserRole;
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function tokenUrl(): string {
  return `${config.keycloakUrl}/realms/${config.keycloakRealm}/protocol/openid-connect/token`;
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

  const roles = seerRoles ?? realmRoles ?? [];
  const role  = pickHighestRole(roles);

  return { sub, username, role };
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

const ROLE_PRIORITY: UserRole[] = ['admin', 'editor', 'viewer'];

function pickHighestRole(roles: string[]): UserRole {
  for (const r of ROLE_PRIORITY) {
    if (roles.includes(r)) return r;
  }
  return 'viewer';
}
