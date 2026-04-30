import { useEffect, useMemo, useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate }    from 'react-router-dom';
import { usePageTitle }   from '../hooks/usePageTitle';
import { HEIMDALL_API }   from '../api';
import { useAuthStore }   from '../stores/authStore';
import { ServiceTopology } from '../components/ServiceTopology';
import {
  SERVICES, BY_ID,
} from '../config/services';
import { useDatabases } from '../hooks/useDatabases';
import {
  type Health, type RawService, type Card,
  POLL_MS, mapStatus, GRID,
} from './servicesPageTypes';
import { ServiceTile, ClusterTile, SectionHeader } from './ServiceCards';
import { ServiceDetailDrawer } from './ServiceDetailDrawer';

// ── Env flag — Dev tools visible only in vite dev build + admin or higher ──────
function useDevVisible(): boolean {
  const role = useAuthStore(s => s.user?.role);
  return import.meta.env.DEV && (role === 'super-admin' || role === 'admin');
}

// ── ArcadeDB clusters ────────────────────────────────────────────────────────
// Two ArcadeDB instances, probed live by chur `GET /heimdall/databases`:
//   · frigg :2481 — profiles + tenant-config + jobrunr scheduler state
//   · ygg   :2480 — per-tenant `ygg-<alias>` lineage graphs
// Each cluster renders as a single card with version, profiler metrics,
// and the real list of databases inside.

// ── Page ──────────────────────────────────────────────────────────────────────
export default function ServicesPage() {
  const { t }    = useTranslation();
  usePageTitle(t('nav.services'));
  const navigate = useNavigate();
  const user     = useAuthStore(s => s.user);
  const isSuper  = user?.role === 'super-admin';
  const devVisible = useDevVisible();

  // ── Health polling ────────────────────────────────────────────────────────
  const [raw, setRaw]     = useState<RawService[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchHealth = useCallback(() => {
    fetch(`${HEIMDALL_API}/services/health`)
      .then(r => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        return r.json() as Promise<RawService[]>;
      })
      .then(data => { setRaw(data); setError(null); })
      .catch(e => setError(e instanceof Error ? e.message : 'error'));
  }, []);

  useEffect(() => {
    fetchHealth();
    const id = setInterval(fetchHealth, POLL_MS);
    return () => clearInterval(id);
  }, [fetchHealth]);

  // ── Tenant filter ─────────────────────────────────────────────────────────
  const [tenant, setTenant] = useState<string>(user?.activeTenantAlias ?? 'default');
  const [showAll, setShowAll] = useState(isSuper);

  // ── Section collapse (Dev collapsed by default) ───────────────────────────
  const [collapsed, setCollapsed] = useState<Record<string, boolean>>({ dev: true });
  const toggle = (k: string) => setCollapsed(s => ({ ...s, [k]: !s[k] }));

  // ── Detail drawer ─────────────────────────────────────────────────────────
  const [selected, setSelected] = useState<RawService | null>(null);

  // ── Bucket API services into sections ─────────────────────────────────────
  const { front, middleware, workers, integrations } = useMemo(() => {
    const out = {
      front:        [] as Card[],
      middleware:   [] as Card[],
      workers:      [] as Card[],
      integrations: [] as Card[],
    };
    for (const svc of raw ?? []) {
      if (svc.mode === 'dev') continue;
      const spec = BY_ID[svc.name];
      const base: Card = {
        id:        `${svc.name}-${svc.mode}-${svc.port}`,
        name:      spec?.label ?? svc.name,
        meta:      `:${svc.port}`,
        health:    mapStatus(svc.status),
        latency:   svc.status === 'self' ? '—' : `${svc.latencyMs} ms`,
        latencyMs: svc.status === 'self' ? null : svc.latencyMs,
        onOpen:    () => setSelected(svc),
      };
      const cat   = spec?.category;
      const layer = spec?.layer ?? 99;
      if (cat === 'platform') {
        if (layer <= 2) out.front.push(base);
        else            out.middleware.push(base);
      }
      else if (cat === 'worker')      out.workers.push(base);
      else if (cat === 'integration') out.integrations.push(base);
    }
    const byName = (a: Card, b: Card) => a.name.localeCompare(b.name);
    out.front.sort(byName);
    out.middleware.sort(byName);
    out.workers.sort(byName);
    out.integrations.sort(byName);
    return out;
  }, [raw, navigate]);

  // ArcadeDB clusters — live via chur /heimdall/databases
  const { clusters: liveClusters, error: dbError } = useDatabases();
  const clusters = liveClusters ?? [];
  const totalDbs = clusters.reduce((n, c) => n + c.dbs.length, 0);

  const filteredWorkers      = workers;
  const filteredIntegrations = integrations;

  return (
    <div style={{
      padding: 'var(--seer-space-6, 18px)',
      height: '100%', overflowY: 'auto', background: 'var(--bg0)',
    }}>

      {/* ── Top bar: tenant pills + error ── */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 12,
        marginBottom: 16, flexWrap: 'wrap',
      }}>
        <TenantPills
          current={showAll ? '__all__' : tenant}
          onPick={(v) => { if (v === '__all__') setShowAll(true); else { setShowAll(false); setTenant(v); } }}
          allowAll={isSuper}
        />
        <span style={{ color: 'var(--t3)', fontSize: 11 }}>
          {showAll ? 'ALL TENANTS scope · Platform always global' : `scoped to ${tenant}`}
        </span>
        <span style={{ flex: 1 }} />
        {error && (
          <span style={{ color: 'var(--danger)', fontSize: 11, fontFamily: 'var(--mono)' }}>
            {error}
          </span>
        )}
        <button
          onClick={fetchHealth}
          style={{
            background: 'var(--bg2)', border: '1px solid var(--bd)',
            borderRadius: 'var(--seer-radius-sm, 4px)', color: 'var(--t2)',
            fontSize: 11, padding: '4px 10px', cursor: 'pointer',
          }}
        >
          ↺ {t('services.refresh', 'Refresh')}
        </button>
      </div>

      {/* ── Dev mode services ── */}
      {devVisible && (
        <section data-testid="tab-dev" style={{
          marginBottom: 22, paddingBottom: 12,
          borderBottom: '1px dashed var(--bd)',
        }}>
          <SectionHeader
            title="Dev mode services"
            count="local pnpm/quarkus dev"
            collapsed={collapsed.dev}
            onToggle={() => toggle('dev')}
          />
          {!collapsed.dev && <DevGrid raw={raw ?? []} onPick={setSelected} />}
        </section>
      )}

      {/* ── Front (edge + UIs: L0..L2) ── */}
      <section style={{ marginBottom: 22 }}>
        <SectionHeader
          title="Front"
          count={`${front.length} · edge + UIs`}
          collapsed={!!collapsed.front}
          onToggle={() => toggle('front')}
        />
        {!collapsed.front && (raw == null
          ? <div style={{ color: 'var(--t3)', fontSize: 13 }}>{t('services.loading')}</div>
          : <div style={GRID}>{front.map(c => <ServiceTile key={c.id} card={c} docker />)}</div>
        )}
      </section>

      {/* ── Middleware (BFF + backend + auth: L3..L5) ── */}
      <section style={{ marginBottom: 22 }}>
        <SectionHeader
          title="Middleware"
          count={`${middleware.length} · BFF + services`}
          collapsed={!!collapsed.middleware}
          onToggle={() => toggle('middleware')}
        />
        {!collapsed.middleware && (raw == null
          ? <div style={{ color: 'var(--t3)', fontSize: 13 }}>{t('services.loading')}</div>
          : <div style={GRID}>{middleware.map(c => <ServiceTile key={c.id} card={c} docker />)}</div>
        )}
      </section>

      {/* ── Tenant workers ── */}
      {filteredWorkers.length > 0 && (
        <section style={{ marginBottom: 22 }}>
          <SectionHeader
            title="Tenant workers"
            count={`${filteredWorkers.length} workers`}
            collapsed={!!collapsed.workers}
            onToggle={() => toggle('workers')}
          />
          {!collapsed.workers && (
            <div style={GRID}>
              {filteredWorkers.map(c => <ServiceTile key={c.id} card={c} docker />)}
            </div>
          )}
        </section>
      )}

      {/* ── ArcadeDB clusters ── */}
      <section style={{ marginBottom: 22 }}>
        <SectionHeader
          title="ArcadeDB clusters"
          count={liveClusters == null
            ? 'loading…'
            : `${clusters.length} clusters · ${totalDbs} databases`}
          collapsed={!!collapsed.db}
          onToggle={() => toggle('db')}
        />
        {!collapsed.db && (
          liveClusters == null
            ? <div style={{ color: 'var(--t3)', fontSize: 13 }}>{t('services.loading')}</div>
            : <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
                gap: 8,
              }}>
                {clusters.map(c => <ClusterTile key={c.id} cluster={c} />)}
                {dbError && (
                  <div style={{ color: 'var(--danger)', fontSize: 11, fontFamily: 'var(--mono)' }}>
                    {dbError}
                  </div>
                )}
              </div>
        )}
      </section>

      {/* ── Integrations ── */}
      {filteredIntegrations.length > 0 && (
        <section style={{ marginBottom: 22 }}>
          <SectionHeader
            title="Integrations"
            count={`${filteredIntegrations.length} external`}
            collapsed={!!collapsed.integrations}
            onToggle={() => toggle('integrations')}
          />
          {!collapsed.integrations && (
            <div style={GRID}>
              {filteredIntegrations.map(c => <ServiceTile key={c.id} card={c} />)}
            </div>
          )}
        </section>
      )}

      {/* ── Topology ── */}
      <ServiceTopology serviceStatuses={(raw ?? []).filter(r => r.mode !== 'dev')} />

      {selected && <ServiceDetailDrawer svc={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}

// ── Tenant pills ──────────────────────────────────────────────────────────────
const DEMO_TENANTS = ['default', 'acme', 'beta-corp']; // TODO: fetch from /api/admin/tenants

function TenantPills({ current, onPick, allowAll }: {
  current:  string;
  onPick:   (v: string) => void;
  allowAll: boolean;
}) {
  const btn = (val: string, label: string, accent = false): React.ReactElement => {
    const active = current === val;
    const color = accent ? 'var(--wrn)' : 'var(--inf)';
    return (
      <button
        key={val}
        onClick={() => onPick(val)}
        style={{
          background:   active ? `color-mix(in srgb, ${color} 18%, transparent)` : 'transparent',
          color:        active ? color : 'var(--t2)',
          border:       'none',
          borderRight:  '1px solid var(--bd)',
          padding:      '6px 11px',
          fontSize:     11, fontWeight: 500, letterSpacing: '0.03em',
          cursor:       'pointer',
        }}
      >
        {label}
      </button>
    );
  };
  return (
    <span style={{
      display: 'inline-flex', background: 'var(--bg2)',
      border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-sm, 6px)',
      overflow: 'hidden',
    }}>
      {allowAll && btn('__all__', 'All tenants', true)}
      {DEMO_TENANTS.map(ten => btn(ten, ten))}
    </span>
  );
}

// ── Dev grid ──────────────────────────────────────────────────────────────────
function DevGrid({ raw, onPick }: { raw: RawService[]; onPick: (svc: RawService) => void }) {
  const devServices = SERVICES
    .filter(s => s.category !== 'integration' && s.portDev)
    .slice()
    .sort((a, b) => a.label.localeCompare(b.label));
  return (
    <div style={{ ...GRID, opacity: 0.9 }}>
      {devServices.map(spec => {
        const match = raw.find(r => r.name === spec.id && r.mode === 'dev');
        const health: Health = match ? mapStatus(match.status) : 'idle';
        const latency =
          match ? (match.status === 'self' ? '—' : `${match.latencyMs} ms`) : 'not running';
        const card: Card = {
          id:        `dev:${spec.id}`,
          name:      spec.label,
          meta:      `:${spec.portDev}`,
          health,
          latency,
          latencyMs: match?.latencyMs ?? null,
          onOpen:    match ? () => onPick(match) : undefined,
        };
        return <ServiceTile key={card.id} card={card} dashed />;
      })}
    </div>
  );
}
