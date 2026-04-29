import type { FastifyPluginAsync } from 'fastify';
import {
  requireScope,
  requireAnyScope,
  requireSameTenant,
} from '../middleware/requireAdmin';
import {
  listUsers,
  getUser,
  inviteUser,
  setUserRole,
  setUserEnabled,
  getUserAttributes,
  setUserAttributes,
  listRoles,
  listAllOrganizations,
  getUserOrganizations,
} from '../keycloakAdmin';
import { config } from '../config';

const HEIMDALL_ORIGIN = process.env.HEIMDALL_URL ?? 'http://127.0.0.1:9093';

// ── FRIGG tenant helper ───────────────────────────────────────────────────────
const FRIGG_BASIC = Buffer.from(`${config.friggUser}:${config.friggPass}`).toString('base64');

async function listActiveTenants(): Promise<{ id: string; name: string }[]> {
  try {
    const res = await fetch(
      `${config.friggUrl}/api/v1/query/${encodeURIComponent(config.friggTenantsDb)}`,
      {
        method:  'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Basic ${FRIGG_BASIC}` },
        body:    JSON.stringify({
          language: 'sql',
          command:  `SELECT tenantAlias, status FROM DaliTenantConfig WHERE status = 'ACTIVE' ORDER BY tenantAlias`,
        }),
        signal: AbortSignal.timeout(5_000),
      },
    );
    if (!res.ok) throw new Error(`FRIGG ${res.status}`);
    const data = await res.json() as { result?: Array<{ tenantAlias?: string; status?: string }> };
    return (data.result ?? [])
      .filter(r => r.tenantAlias)
      .map(r => ({ id: r.tenantAlias!, name: r.tenantAlias! }));
  } catch {
    // Fallback: at least return default so UI doesn't break
    return [{ id: 'default', name: 'Default' }];
  }
}

/**
 * Admin routes — R4.2/R4.11 (Sprint 4).
 *
 * Scope enforcement:
 *   aida:admin        — cross-tenant admin operations (list tenants)
 *   aida:tenant:admin — per-tenant user management (local-admin+)
 *
 * Phase 1 — single-tenant: tenantId path param always = "default".
 * Phase 2 — multi-tenant: requireSameTenant() will enforce tenantId matching.
 *
 * Self-service routes (/admin/me/*) are accessible to any authenticated user.
 */
export const adminRoutes: FastifyPluginAsync = async (app) => {

  // ── Roles reference (local-admin+) ──────────────────────────────────────────
  // Returns application roles from KC in priority order.
  // Used by role pickers in Verdandi / HEIMDALL user management UI.

  app.get(
    '/admin/roles',
    { preHandler: [app.authenticate, requireAnyScope('aida:admin', 'aida:superadmin', 'aida:tenant:admin')] },
    async (_request, reply) => {
      try {
        const roles = await listRoles();
        return reply.send(roles);
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  // ── Tenants (all authenticated — role-aware response) ────────────────────
  // Admin/superadmin → all active tenants (FRIGG).
  // Everyone else   → own KC org memberships (or own tenant alias as fallback).
  // No scope guard here: TenantSelector calls this for every role, including
  // viewer/editor/operator who need to know which tenant(s) they belong to.
  // Security: viewers receive only their own org data — not the full list.

  app.get(
    '/admin/tenants',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const isSuperAdmin = request.user.scopes?.includes('aida:superadmin');
      // Cross-tenant admins (super-admin + admin) can see ALL active tenants.
      // Regular admin = platform-wide admin who can switch between any tenant.
      const isAdmin = request.user.scopes?.includes('aida:admin');
      if (isSuperAdmin || isAdmin) {
        // FRIGG is the operational tenant registry — always the primary source.
        // KC orgs are used for user-membership routing but may lag behind FRIGG
        // (e.g. tenants provisioned via API without a corresponding KC org yet).
        const friggTenants = await listActiveTenants();
        if (friggTenants.length > 0) return reply.send(friggTenants);
        // Fallback: KC orgs (when FRIGG is unavailable)
        const orgs = await listAllOrganizations();
        if (orgs.length > 0) {
          return reply.send(orgs.map(o => ({ id: o.alias, name: o.name || o.alias })));
        }
        return reply.send([{ id: 'default', name: 'Default' }]);
      }
      // All other roles: fetch this user's KC org memberships (multi-org support)
      const orgs = await getUserOrganizations(request.user.sub);
      if (orgs.length > 0) {
        return reply.send(orgs.map(o => ({ id: o.alias, name: o.name || o.alias })));
      }
      // Fallback: own active tenant only
      const alias = request.user.activeTenantAlias ?? 'default';
      return reply.send([{ id: alias, name: alias }]);
    },
  );

  // ── Users (local-admin+, same-tenant) ────────────────────────────────────

  app.get(
    '/admin/tenants/:tenantId/users',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant()] },
    async (_request, reply) => {
      try {
        const users = await listUsers();
        return reply.send(users);
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  app.post(
    '/admin/tenants/:tenantId/users/invite',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant()] },
    async (request, reply) => {
      const body = request.body as { email: string; name?: string; role: string };
      // Guard: local-admin cannot assign elevated roles
      const elevatedRoles = ['admin', 'super-admin', 'tenant-owner', 'auditor'];
      const isAdmin = request.user.scopes?.includes('aida:admin');
      if (!isAdmin && elevatedRoles.includes(body.role)) {
        return reply.status(403).send({ error: 'cannot_assign_elevated_role' });
      }
      try {
        await inviteUser(body.email, body.name ?? body.email.split('@')[0], body.role as any);
        return reply.status(202).send({ ok: true });
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  app.put(
    '/admin/tenants/:tenantId/users/:userId/role',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant()] },
    async (request, reply) => {
      const { userId } = request.params as { userId: string };
      const { role } = request.body as { role: string };
      const elevatedRoles = ['admin', 'super-admin', 'tenant-owner'];
      const isAdmin = request.user.scopes?.includes('aida:admin');
      const isTenantOwner = request.user.scopes?.includes('aida:tenant:owner');
      if (!isAdmin && !isTenantOwner && elevatedRoles.includes(role)) {
        return reply.status(403).send({ error: 'insufficient_privileges' });
      }
      try {
        await setUserRole(userId, role as any);
        return reply.send({ ok: true });
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  app.put(
    '/admin/tenants/:tenantId/users/:userId/disable',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant()] },
    async (request, reply) => {
      const { userId } = request.params as { userId: string };
      const { enabled } = request.body as { enabled?: boolean };
      try {
        await setUserEnabled(userId, enabled ?? false);
        return reply.send({ ok: true });
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  // ── Profile (local-admin reads/writes other users' profile) ─────────────

  const PROFILE_ATTRS = ['profile.title', 'profile.dept', 'profile.phone'];

  app.get(
    '/admin/tenants/:tenantId/users/:userId/profile',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant()] },
    async (request, reply) => {
      const { userId } = request.params as { userId: string };
      try {
        const attrs = await getUserAttributes(userId, PROFILE_ATTRS);
        return reply.send({
          title: attrs['profile.title'] ?? '',
          dept:  attrs['profile.dept']  ?? '',
          phone: attrs['profile.phone'] ?? '',
        });
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  app.put(
    '/admin/tenants/:tenantId/users/:userId/profile',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant()] },
    async (request, reply) => {
      const { userId } = request.params as { userId: string };
      const body = request.body as { title?: string; dept?: string; phone?: string };
      try {
        await setUserAttributes(userId, {
          'profile.title': body.title ?? '',
          'profile.dept':  body.dept  ?? '',
          'profile.phone': body.phone ?? '',
        });
        return reply.send({ ok: true });
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  // ── Preferences (admin reads/writes other users' prefs) ─────────────────

  const PREFS_ATTRS = [
    'prefs.lang', 'prefs.theme', 'prefs.tz', 'prefs.dateFmt',
    'prefs.density', 'prefs.startPage', 'prefs.avatarColor',
    'prefs.notify.email', 'prefs.notify.browser', 'prefs.notify.harvest',
    'prefs.notify.errors', 'prefs.notify.digest',
    'verdandi.palette', 'verdandi.uiFont', 'verdandi.monoFont',
    'verdandi.fontSize', 'verdandi.graphPrefs',
  ];

  app.get(
    '/admin/tenants/:tenantId/users/:userId/prefs',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant()] },
    async (request, reply) => {
      const { userId } = request.params as { userId: string };
      try {
        const attrs = await getUserAttributes(userId, PREFS_ATTRS);
        return reply.send(prefsFromAttrs(attrs));
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  app.put(
    '/admin/tenants/:tenantId/users/:userId/prefs',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant()] },
    async (request, reply) => {
      const { userId } = request.params as { userId: string };
      try {
        await setUserAttributes(userId, prefsToAttrs(request.body as Record<string, unknown>));
        return reply.send({ ok: true });
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  // ── Self-service: any authenticated user reads/writes their own data ─────

  app.get('/admin/me/profile', { preHandler: [app.authenticate] }, async (request, reply) => {
    try {
      const attrs = await getUserAttributes(request.user.sub, PROFILE_ATTRS);
      return reply.send({
        title: attrs['profile.title'] ?? '',
        dept:  attrs['profile.dept']  ?? '',
        phone: attrs['profile.phone'] ?? '',
      });
    } catch { return reply.status(503).send({ error: 'KC_UNAVAILABLE' }); }
  });

  app.put('/admin/me/profile', { preHandler: [app.authenticate] }, async (request, reply) => {
    const body = request.body as { title?: string; dept?: string; phone?: string };
    try {
      await setUserAttributes(request.user.sub, {
        'profile.title': body.title ?? '',
        'profile.dept':  body.dept  ?? '',
        'profile.phone': body.phone ?? '',
      });
      return reply.send({ ok: true });
    } catch { return reply.status(503).send({ error: 'KC_UNAVAILABLE' }); }
  });

  app.get('/admin/me/prefs', { preHandler: [app.authenticate] }, async (request, reply) => {
    try {
      const attrs = await getUserAttributes(request.user.sub, PREFS_ATTRS);
      return reply.send(prefsFromAttrs(attrs));
    } catch { return reply.status(503).send({ error: 'KC_UNAVAILABLE' }); }
  });

  app.put('/admin/me/prefs', { preHandler: [app.authenticate] }, async (request, reply) => {
    try {
      await setUserAttributes(request.user.sub, prefsToAttrs(request.body as Record<string, unknown>));
      return reply.send({ ok: true });
    } catch { return reply.status(503).send({ error: 'KC_UNAVAILABLE' }); }
  });
};

// ── Helpers ───────────────────────────────────────────────────────────────────

function prefsFromAttrs(attrs: Record<string, string>): Record<string, unknown> {
  return {
    lang:          attrs['prefs.lang']          ?? 'ru',
    theme:         attrs['prefs.theme']         ?? 'dark',
    tz:            attrs['prefs.tz']            ?? 'Europe/Moscow',
    dateFmt:       attrs['prefs.dateFmt']       ?? 'DD.MM.YYYY',
    density:       attrs['prefs.density']       ?? 'normal',
    startPage:     attrs['prefs.startPage']     ?? 'dashboard',
    avatarColor:   attrs['prefs.avatarColor']   ?? '#A8B860',
    notifyEmail:   attrs['prefs.notify.email']   === 'true',
    notifyBrowser: attrs['prefs.notify.browser'] === 'true',
    notifyHarvest: attrs['prefs.notify.harvest'] !== 'false',
    notifyErrors:  attrs['prefs.notify.errors']  !== 'false',
    notifyDigest:  attrs['prefs.notify.digest']  === 'true',
    verdandiPalette:   attrs['verdandi.palette']    ?? 'amber-forest',
    verdandiUiFont:    attrs['verdandi.uiFont']     ?? 'Manrope',
    verdandiMonoFont:  attrs['verdandi.monoFont']   ?? 'IBM Plex Mono',
    verdandiFontSize:  attrs['verdandi.fontSize']   ?? '13',
    verdandiGraphPrefs: attrs['verdandi.graphPrefs'] ?? null,
  };
}

function prefsToAttrs(prefs: Record<string, unknown>): Record<string, string> {
  const attrs: Record<string, string> = {};
  const str = (v: unknown) => (v != null ? String(v) : '');
  if (prefs.lang          != null) attrs['prefs.lang']           = str(prefs.lang);
  if (prefs.theme         != null) attrs['prefs.theme']          = str(prefs.theme);
  if (prefs.tz            != null) attrs['prefs.tz']             = str(prefs.tz);
  if (prefs.dateFmt       != null) attrs['prefs.dateFmt']        = str(prefs.dateFmt);
  if (prefs.density       != null) attrs['prefs.density']        = str(prefs.density);
  if (prefs.startPage     != null) attrs['prefs.startPage']      = str(prefs.startPage);
  if (prefs.avatarColor   != null) attrs['prefs.avatarColor']    = str(prefs.avatarColor);
  if (prefs.notifyEmail   != null) attrs['prefs.notify.email']   = str(prefs.notifyEmail);
  if (prefs.notifyBrowser != null) attrs['prefs.notify.browser'] = str(prefs.notifyBrowser);
  if (prefs.notifyHarvest != null) attrs['prefs.notify.harvest'] = str(prefs.notifyHarvest);
  if (prefs.notifyErrors  != null) attrs['prefs.notify.errors']  = str(prefs.notifyErrors);
  if (prefs.notifyDigest  != null) attrs['prefs.notify.digest']  = str(prefs.notifyDigest);
  if (prefs.verdandiPalette   != null) attrs['verdandi.palette']    = str(prefs.verdandiPalette);
  if (prefs.verdandiUiFont    != null) attrs['verdandi.uiFont']     = str(prefs.verdandiUiFont);
  if (prefs.verdandiMonoFont  != null) attrs['verdandi.monoFont']   = str(prefs.verdandiMonoFont);
  if (prefs.verdandiFontSize  != null) attrs['verdandi.fontSize']   = str(prefs.verdandiFontSize);
  if (prefs.verdandiGraphPrefs != null) attrs['verdandi.graphPrefs'] = str(prefs.verdandiGraphPrefs);
  return attrs;
}
