export type UserRole =
  | 'viewer'
  | 'editor'
  | 'operator'
  | 'auditor'
  | 'local-admin'
  | 'tenant-owner'
  | 'admin'
  | 'super-admin';

export interface SeerUser {
  sub:                string;
  username:           string;
  role:               UserRole;
  scopes:             string[];   // JWT scope claim (space-separated → array)
  activeTenantAlias?: string;     // MTN-13: last tenant switch via PATCH /auth/me/tenant
  email?:             string;
  firstName?:         string;
  lastName?:          string;
  emailVerified?:     boolean;
}

// Extend Fastify request to carry user info after session-based authentication
declare module 'fastify' {
  interface FastifyRequest {
    user: SeerUser;
  }
}
