/**
 * MTN-25: Provisioning saga tests — happy path + compensating rollback scenarios.
 *
 * These tests exercise the fetch-based steps directly rather than through the
 * Fastify route surface. Each scenario overrides vi.stubGlobal('fetch') with a
 * call-counting stub that routes URLs to controllable responses (ok/fail).
 *
 * ArcadeDB 26.x: create/drop databases go through POST /api/v1/server
 * with body { command: "create database <name>" } — not /api/v1/create/<name>.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { provisionTenant, resumeProvisioning } from './provisioning';

// ── Mutable fetch stub controlled per-test ─────────────────────────────────
type Handler = (url: string, init?: any) => Promise<Response> | Promise<any>;
let handler: Handler = async () => {
  throw new Error('no fetch handler set');
};

beforeEach(() => {
  handler = async () => ok();
  vi.stubGlobal('fetch', vi.fn((url: string, init?: any) => handler(String(url), init)));
});

function ok(body: any = {}, status = 200, headers?: Record<string, string>): any {
  return {
    ok: true, status,
    text: async () => '',
    json: async () => body,
    headers: {
      get: (h: string) => headers?.[h.toLowerCase()] ?? null,
    },
  };
}
function fail(status = 500, body = 'backend error'): any {
  return {
    ok: false, status,
    text: async () => body,
    json: async () => ({}),
    headers: { get: () => null },
  };
}

/** Parse the server-command from a /api/v1/server request body. */
function serverCmd(init?: any): string {
  try { return JSON.parse(init?.body ?? '{}').command ?? ''; } catch { return ''; }
}

// ── Happy path ─────────────────────────────────────────────────────────────
describe('provisionTenant — happy path', () => {
  it('runs all 7 steps in order and returns orgId + lastStep=7', async () => {
    const calls: string[] = [];
    handler = async (url, init) => {
      const cmd = serverCmd(init);
      // Track URL or, for server-command calls, the command itself
      calls.push(cmd ? `server:${cmd}` : url.split('?')[0]);

      if (url.includes('/realms/master/protocol/openid-connect/token')) {
        return ok({ access_token: 'admin-tok' });
      }
      if (url.includes('/admin/realms/seer/organizations?search=')) {
        return ok([]); // pre-flight conflict check: no conflict
      }
      if (url.includes('/api/v1/server')) return ok(); // YGG + FRIGG pings + create/drop
      if (url.includes('/admin/realms/seer/organizations') && !url.includes('?')) {
        return ok({}, 201, { location: 'http://kc/admin/realms/seer/organizations/new-org-uuid' });
      }
      if (url.includes('/api/v1/command/frigg-tenants')) return ok({ result: [{ tenantAlias: 'acme' }] });
      if (url.includes('/api/v1/command/dali_acme'))  return ok();
      if (url.includes('/api/cron/harvest'))         return ok({ ok: true });
      return ok();
    };

    const result = await provisionTenant('acme', 'corr-1', 'admin');

    expect(result.lastStep).toBe(7);
    expect(result.keycloakOrgId).toBe('new-org-uuid');
    expect(result.tenantAlias).toBe('acme');

    // Verify key steps were executed via server-command API
    expect(calls.some(c => c.includes('/realms/seer/organizations'))).toBe(true);
    expect(calls.some(c => c === 'server:create database hound_acme')).toBe(true);
    expect(calls.some(c => c === 'server:create database hound_src_acme')).toBe(true);
    expect(calls.some(c => c === 'server:create database dali_acme')).toBe(true);
    expect(calls.some(c => c.includes('/api/cron/harvest'))).toBe(true);
  });
});

// ── Pre-flight rejections ──────────────────────────────────────────────────
describe('provisionTenant — pre-flight', () => {
  it('aborts if YGG is unreachable (no side effects)', async () => {
    const calls: string[] = [];
    handler = async (url, init) => {
      calls.push(url);
      if (url.includes('/realms/master/protocol/openid-connect/token')) {
        return ok({ access_token: 'tok' });
      }
      // YGG ping goes to /api/v1/server — fail it
      if (url.includes(':2480/api/v1/server')) return fail(500);
      return ok();
    };

    await expect(provisionTenant('acme', 'corr', 'admin')).rejects.toMatchObject({
      failedStep: 0,
      cause: expect.stringContaining('YGG unreachable'),
    });
    // No KC org create attempted
    expect(calls.some(c => c.endsWith('/organizations'))).toBe(false);
  });

  it('aborts with 409 when KC org with same alias already exists', async () => {
    handler = async (url) => {
      if (url.includes('/realms/master/')) return ok({ access_token: 't' });
      if (url.includes('/api/v1/server'))   return ok();
      if (url.includes('/admin/realms/seer/organizations?search=acme')) {
        return ok([{ alias: 'acme', id: 'existing-org' }]);
      }
      return ok();
    };

    await expect(provisionTenant('acme', 'corr', 'admin')).rejects.toMatchObject({
      failedStep: 0,
      cause: expect.stringContaining('already exists'),
    });
  });
});

// ── Compensation on step failure ───────────────────────────────────────────
describe('provisionTenant — compensation on failure', () => {
  it('step 5 fails → compensates steps 4, 3, 2, 1 in reverse', async () => {
    const compensations: string[] = [];
    handler = async (url, init) => {
      const method = init?.method ?? 'GET';
      const cmd = serverCmd(init);

      if (url.includes('/realms/master/'))  return ok({ access_token: 't' });

      // All /api/v1/server calls — route by command
      if (url.includes('/api/v1/server')) {
        if (cmd === 'create database hound_acme')     return ok({}, 201);
        if (cmd === 'create database hound_src_acme') return ok({}, 201);
        if (cmd === 'create database dali_acme')      return fail(500, 'FRIGG dali create failed');
        if (cmd === 'drop database hound_acme')       { compensations.push('drop-hound');     return ok(); }
        if (cmd === 'drop database hound_src_acme')   { compensations.push('drop-hound-src'); return ok(); }
        return ok(); // pings
      }

      if (url.includes('/admin/realms/seer/organizations?search=')) return ok([]);

      // Compensation step 1: DELETE org
      if (url.includes('/organizations/org-uuid') && method === 'DELETE') {
        compensations.push('delete-org');
        return ok({}, 204);
      }

      // Step 1: create org
      if (url.endsWith('/admin/realms/seer/organizations') && method === 'POST')
        return ok({}, 201, { location: 'http://kc/admin/realms/seer/organizations/org-uuid' });

      // Step 2 INSERT / comp DELETE on frigg-tenants
      if (url.includes('/api/v1/command/frigg-tenants')) {
        const body = init?.body ?? '';
        if (body.includes('DELETE')) {
          compensations.push('delete-row');
        }
        return ok();
      }

      return ok();
    };

    await expect(provisionTenant('acme', 'corr-5', 'admin')).rejects.toMatchObject({
      failedStep: 5,
      lastSuccessfulStep: 4,
    });

    // Compensation ran in reverse order 4 → 3 → 2 → 1
    expect(compensations).toEqual([
      'drop-hound-src',
      'drop-hound',
      'delete-row',
      'delete-org',
    ]);
  });

  it('step 1 fails → only pre-flight done, no compensation', async () => {
    const compensations: string[] = [];
    handler = async (url, init) => {
      if (url.includes('/realms/master/')) return ok({ access_token: 't' });
      if (url.includes('/api/v1/server'))   return ok();
      if (url.includes('/admin/realms/seer/organizations?search=')) return ok([]);
      // KC org POST fails
      if (url.endsWith('/organizations') && init?.method === 'POST') return fail(500, 'KC internal error');
      if (init?.method === 'DELETE') { compensations.push(url); return ok({}, 204); }
      return ok();
    };

    await expect(provisionTenant('acme', 'corr-1', 'admin')).rejects.toMatchObject({
      failedStep: 1,
      lastSuccessfulStep: 0,
    });
    // No compensations — nothing was committed
    expect(compensations).toEqual([]);
  });
});

// ── Resume ─────────────────────────────────────────────────────────────────
describe('resumeProvisioning', () => {
  it('calls forceCleanup then provisionTenant (happy-path retry)', async () => {
    let phase: 'cleanup' | 'provision' = 'cleanup';
    let provisionCalled = false;

    handler = async (url, init) => {
      const method = init?.method ?? 'GET';
      const cmd = serverCmd(init);

      if (url.includes('/realms/master/')) return ok({ access_token: 't' });

      // All /api/v1/server calls (pings, create, drop)
      if (url.includes('/api/v1/server')) {
        if (cmd.startsWith('drop database'))    return ok();
        if (cmd.startsWith('create database'))  return ok({}, 201);
        return ok(); // ping
      }

      if (url.includes('/admin/realms/seer/organizations?search=')) {
        if (phase === 'cleanup') return ok([{ alias: 'acme', id: 'old-org' }]);
        return ok([]); // no conflict on re-run
      }
      if (url.includes('/old-org') && method === 'DELETE') {
        phase = 'provision';
        return ok({}, 204);
      }

      if (url.includes('/api/v1/command/frigg-tenants')) return ok();
      if (url.endsWith('/organizations') && method === 'POST') {
        provisionCalled = true;
        return ok({}, 201, { location: 'http://kc/admin/realms/seer/organizations/new-org-uuid' });
      }
      if (url.includes('/api/v1/command/dali_acme')) return ok();
      if (url.includes('/api/cron/harvest')) return ok();
      return ok();
    };

    const result = await resumeProvisioning('acme', 'corr-resume', 'admin');

    expect(result.lastStep).toBe(7);
    expect(result.keycloakOrgId).toBe('new-org-uuid');
    expect(provisionCalled).toBe(true);
  });
});
