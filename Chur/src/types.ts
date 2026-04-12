export type UserRole = 'viewer' | 'editor' | 'admin';

export interface SeerUser {
  sub:      string;
  username: string;
  role:     UserRole;
}

// Extend Fastify request to carry user info after session-based authentication
declare module 'fastify' {
  interface FastifyRequest {
    user: SeerUser;
  }
}
