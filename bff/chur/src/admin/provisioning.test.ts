/**
 * MTN-25: Provisioning saga tests — happy path + compensating rollback scenarios.
 *
 * These tests exercise the fetch-based steps directly rather than through the
 * Fastify route surface. Each scenario overrides vi.stubGlobal('fetch') with a
 * call-counting stub that routes URLs to controllable responses (ok/fail).
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

// ── Happy path ─────────────────────────────────────────────────────────────
describe('provisionTenant — happy path', () => {
  it('runs all 7 steps in order and returns orgId + lastStep=7', async () => {
    const calls: string[] = [];
    handler = async (url) => {
      calls.push(url.split('?')[0]);
      if (url.includes('/realms/master/protocol/openid-connect/token')) {
        return ok({ access_token: 'admin-tok' });
      }
      if (url.includes('/admin/realms/seer/organizations?search=')) {
        return ok([]); // pre-flight conflict check: no conflict
      }
      if (url.includes('/api/v1/server')) return ok();            // YGG + FRIGG pings
      if (url.includes('/admin/realms/seer/organizations') && !url.includes('?')) {
        return ok({}, 201, { location: 'http://kc/admin/realms/seer/organizations/new-org-uuid' });
      }
      if (url.includes('/api/v1/command/frigg-tenants')) return ok({ result: [{ tenantAlias: 'acme' }] });
      if (url.includes('/api/v1/create/')) return ok({}, 201);
      if (url.includes('/api/v1/command/dali_acme'))  return ok();
      if (url.includes('/api/cron/harvest'))         return ok({ ok: true });
      return ok();
    };

    const result = await provisionTenant('acme', 'corr-1', 'admin');

    expect(result.lastStep).toBe(7);
    expect(result.keycloakOrgId).toBe('new-org-uuid');
    expect(result.tenantAlias).toBe('acme');

    // Minimal shape check — should have hit Keycloak token, YGG ping, FRIGG ping,
    // KC org create, FRIGG insert, YGG create hound_, YGG create hound_src_,
    // FRIGG create dali_, FRIGG schema setup, Heimdall cron, FRIGG update ACTIVE.
    expect(calls.some(c => c.includes('/realms/seer/organizations'))).toBe(true);
    expect(calls.some(c => c.includes('/api/v1/create/hound_acme'))).toBe(true);
    expect(calls.some(c => c.includes('/api/v1/create/hound_src_acme'))).toBe(true);
    expect(calls.some(c => c.includes('/api/v1/create/dali_acme'))).toBe(true);
    expect(calls.some(c => c.includes('/api/cron/harvest'))).toBe(true);
  });
});

// ── Pre-flight rejections ──────────────────────────────────────────────────
describe('provisionTenant — pre-flight', () => {
  it('aborts if YGG is unreachable (no side effects)', async () => {
    const calls: string[] = [];
    handler = async (url) => {
      calls.push(url);
      if (url.includes('/realms/master/protocol/openid-connect/token')) {
        return ok({ access_token: 'tok' });
      }
      if (url.includes(':2480/api/v1/server')) return fail(500);   // YGG down
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

      if (url.includes('/realms/master/'))  return ok({ access_token: 't' });
      if (url.includes('/api/v1/server'))    return ok();
      if (url.includes('/admin/realms/seer/organizations?search='))
        return ok([]);

      // Compensation step 1: DELETE org (check BEFORE plain /organizations POST)
      if (url.includes('/organizations/org-uuid') && method === 'DELETE') {
        compensations.push('delete-org');
        return ok({}, 204);
      }

      // Step 1: create org (POST /admin/realms/seer/organizations, no /org-uuid suffix)
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

      // Step 3: YGG create hound_acme / comp POST drop
      if (url.includes('/api/v1/create/hound_acme') && !url.includes('hound_src_'))
        return ok({}, 201);
      if (url.includes('/api/v1/drop/hound_acme') && !url.includes('hound_src_')) {
        compensations.push('drop-hound');
        return ok();
      }

      // Step 4: hound_src_acme
      if (url.includes('/api/v1/create/hound_src_acme')) return ok({}, 201);
      if (url.includes('/api/v1/drop/hound_src_acme')) {
        compensations.push('drop-hound-src');
        return ok();
      }

      // Step 5: fail FRIGG create dali_acme
      if (url.includes('/api/v1/create/dali_acme')) return fail(500, 'FRIGG dali create failed');

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
      // Assume cleanup completes after first KC token + org DELETE + YGG/FRIGG drops
      // (we don't assert order, just that the provision phase runs to completion)
      if (url.includes('/realms/master/')) return ok({ access_token: 't' });
      if (url.includes('/api/v1/server'))   return ok();

      if (url.includes('/admin/realms/seer/organizations?search=')) {
        if (phase === 'cleanup') return ok([{ alias: 'acme', id: 'old-org' }]);
        return ok([]);                              // no conflict on re-run
      }
      if (url.includes('/api/v1/drop/')) return ok();
      if (url.endsWith('/old-org') && init?.method === 'DELETE') {
        phase = 'provision';
        return ok({}, 204);
      }

      if (url.includes('/api/v1/command/frigg-tenants')) return ok();
      if (url.endsWith('/organizations') && init?.method === 'POST') {
        provisionCalled = true;
        return ok({}, 201, { location: 'http://kc/admin/realms/seer/organizations/new-org' });
      }
      if (url.includes('/api/v1/create/')) return ok({}, 201);
      if (url.includes('/api/v1/command/dali_acme')) return ok();
      if (url.includes('/api/cron/harvest')) return ok();
      return ok();
    };

    const result = await resumeProvisioning('acme', 'corr-resume', 'admin');

    expect(result.lastStep).toBe(7);
    expect(result.keycloakOrgId).toBe('new-org');
    expect(provisionCalled).toBe(true);
  });
});
