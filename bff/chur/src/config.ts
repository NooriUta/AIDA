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
  corsOrigin:    process.env.CORS_ORIGIN ?? 'http://localhost:5173',

  // ── Keycloak OIDC ──────────────────────────────────────────────────────────
  keycloakUrl:      process.env.KEYCLOAK_URL          ?? 'http://localhost:8180',
  keycloakRealm:    process.env.KEYCLOAK_REALM        ?? 'seer',
  keycloakClientId: process.env.KEYCLOAK_CLIENT_ID    ?? 'aida-bff',
  keycloakSecret:   requireInProd('KEYCLOAK_CLIENT_SECRET',
                      process.env.KEYCLOAK_CLIENT_SECRET, 'aida-bff-secret-dev'),
  cookieSecret:     requireInProd('COOKIE_SECRET',
                      process.env.COOKIE_SECRET, 'dev-cookie-secret'),

  // ── ArcadeDB (for /api/query proxy — not auth) ────────────────────────────
  arcadeUrl:     process.env.ARCADEDB_URL  ?? 'http://localhost:2480',
  arcadeDb:      process.env.ARCADEDB_DB   ?? 'hound',
  arcadeUser:    process.env.ARCADEDB_USER ?? 'root',
  arcadePass:    process.env.ARCADEDB_PASS ?? 'playwithdata',
} as const;
