import fp from 'fastify-plugin';
import type { FastifyPluginAsync, FastifyRequest, FastifyReply } from 'fastify';
import type { UserRole } from '../types';
import { ensureValidSession } from '../sessions';

/** SQL/Cypher write-operation detector. */
export function isWriteQuery(command: string): boolean {
  return /^\s*(INSERT|UPDATE|DELETE|CREATE|DROP|ALTER|MERGE|SET)\b/i.test(command.trim());
}

/** Minimum role required for write operations via the proxy. */
const WRITE_MIN_ROLE: UserRole = 'admin';

const ROLE_RANK: Record<UserRole, number> = {
  viewer:          0,
  editor:          1,
  operator:        2,
  auditor:         3,
  'local-admin':   4,
  'tenant-owner':  5,
  admin:           6,
  'super-admin':   7,
};

function hasMinRole(userRole: UserRole, minRole: UserRole): boolean {
  return ROLE_RANK[userRole] >= ROLE_RANK[minRole];
}

/**
 * RBAC plugin — session-based authentication via Keycloak BFF.
 *
 * `app.authenticate`: reads "sid" cookie → looks up in-memory session →
 * lazy-refreshes access token if expired → attaches user to request.
 *
 * `app.authorizeQuery`: authenticate + write-permission check.
 */
const rbacPlugin: FastifyPluginAsync = async (app) => {
  app.decorate(
    'authenticate',
    async (request: FastifyRequest, reply: FastifyReply) => {
      const sid = request.cookies.sid;
      if (!sid) {
        return reply.status(401).send({ error: 'Unauthorized' });
      }

      try {
        const session = await ensureValidSession(sid);
        request.user = {
          sub:               session.sub,
          username:          session.username,
          role:              session.role,
          scopes:            session.scopes,
          activeTenantAlias: session.activeTenantAlias,
          email:             session.email,
          firstName:         session.firstName,
          lastName:          session.lastName,
        };
      } catch {
        return reply.status(401).send({ error: 'Unauthorized' });
      }
    },
  );

  app.decorate(
    'authorizeQuery',
    async (request: FastifyRequest, reply: FastifyReply) => {
      const sid = request.cookies.sid;
      if (!sid) {
        return reply.status(401).send({ error: 'Unauthorized' });
      }

      try {
        const session = await ensureValidSession(sid);
        request.user = {
          sub:               session.sub,
          username:          session.username,
          role:              session.role,
          scopes:            session.scopes,
          activeTenantAlias: session.activeTenantAlias,
          email:             session.email,
          firstName:         session.firstName,
          lastName:          session.lastName,
        };
      } catch {
        return reply.status(401).send({ error: 'Unauthorized' });
      }

      const body = request.body as { command?: string } | undefined;
      if (body?.command && isWriteQuery(body.command)) {
        const role = request.user.role;
        if (!hasMinRole(role, WRITE_MIN_ROLE)) {
          return reply.status(403).send({ error: 'Forbidden: write access requires admin role' });
        }
      }
    },
  );
};

export default fp(rbacPlugin, { name: 'rbac' });

// ── Fastify type augmentation ─────────────────────────────────────────────────
declare module 'fastify' {
  interface FastifyInstance {
    authenticate:   (req: FastifyRequest, rep: FastifyReply) => Promise<void>;
    authorizeQuery: (req: FastifyRequest, rep: FastifyReply) => Promise<void>;
  }
}
