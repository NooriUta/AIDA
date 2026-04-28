const IS_PROD = process.env.NODE_ENV === 'production';

// Fail-fast in production if critical secrets are missing.
function requireInProd(name: string, value: string | undefined, fallback: string): string {
  if (IS_PROD && !value) {
    throw new Error(`${name} must be set in production (NODE_ENV=production)`);
  }
  return value ?? fallback;
}

export const config = {
  port:          Number(process.env.PORT ?? 3000),
  corsOrigin:    process.env.CORS_ORIGIN ?? 'http://localhost:5173,http://localhost:5174,http://localhost:5175',

  // ── Keycloak OIDC ──────────────────────────────────────────────────────────
  keycloakUrl:      process.env.KEYCLOAK_URL          ?? 'http://127.0.0.1:8180',
  keycloakRealm:    process.env.KEYCLOAK_REALM        ?? 'seer',
  keycloakClientId: process.env.KEYCLOAK_CLIENT_ID    ?? 'aida-bff',
  keycloakSecret:   requireInProd('KEYCLOAK_CLIENT_SECRET',
                      process.env.KEYCLOAK_CLIENT_SECRET, 'aida-bff-secret-dev'),
  cookieSecret:     requireInProd('COOKIE_SECRET',
                      process.env.COOKIE_SECRET, 'dev-cookie-secret'),

  // ── Keycloak Admin API (master realm bootstrap creds) ────────────────────
  // Used by /heimdall/users to read user list + attributes via Admin REST API.
  // In production: replace with a dedicated service account.
  kcAdminUser:  process.env.KC_ADMIN_USER ?? 'admin',
  kcAdminPass:  process.env.KC_ADMIN_PASS ?? 'admin',

  // ── ArcadeDB (for /api/query proxy — not auth) ────────────────────────────
  arcadeUrl:     process.env.ARCADEDB_URL  ?? 'http://127.0.0.1:2480',
  arcadeDb:      process.env.ARCADEDB_DB   ?? 'hound',
  arcadeUser:    process.env.ARCADEDB_USER ?? 'root',
  arcadePass:    process.env.ARCADEDB_PASS ?? '',

  // ── FRIGG (port 2481) — session store + tenants registry + per-user data ──
  friggUrl:         (process.env.FRIGG_URL  ?? 'http://127.0.0.1:2481').replace(/\/$/, ''),
  friggUser:        process.env.FRIGG_USER  ?? 'root',
  friggPass:        process.env.FRIGG_PASSWORD ?? '',
  friggSessionDb:   process.env.FRIGG_SESSION_DB ?? 'frigg-sessions',
  friggUsersDb:     process.env.FRIGG_USERS_DB   ?? 'frigg-users',
  friggTenantsDb:   process.env.FRIGG_TENANTS_DB ?? 'frigg-tenants',
} as const;
