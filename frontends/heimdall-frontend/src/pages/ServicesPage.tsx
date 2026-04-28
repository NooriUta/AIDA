import { useEffect, useMemo, useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate }    from 'react-router-dom';
import { usePageTitle }   from '../hooks/usePageTitle';
import { HEIMDALL_API }   from '../api';
import { useAuthStore }   from '../stores/authStore';
import { ServiceTopology } from '../components/ServiceTopology';
import {
  SERVICES, BY_ID,
  LATENCY_GOOD_MAX, LATENCY_WARN_MAX,
} from '../config/services';
import { useDatabases, type ClusterHealth } from '../hooks/useDatabases';

// ── Types ─────────────────────────────────────────────────────────────────────
type Health = 'up' | 'deg' | 'down' | 'idle';

interface RawService {
  name:      string;
  port:      number;
  mode:      'dev' | 'docker';
  status:    'up' | 'degraded' | 'down' | 'self';
  latencyMs: number;
  /** Only populated when peer Quarkus service exposes /q/info/build. Currently only heimdall-backend returns its own version. */
  version?:  string | null;
}

interface Card {
  id:         string;         // stable key for keyed rendering
  name:       string;
  meta:       string;         // short meta line (":3000", "graph · 312 MB")
  health:     Health;
  latency?:   string;         // "38 ms", "SUSPENDED", "—"
  latencyMs?: number | null;  // raw number for threshold coloring
  tenant?:    string;         // filters by activeTenantAlias when set
  onOpen?:    () => void;     // click handler (drawer / navigate)
}

const POLL_MS = 10_000;

// ── Helpers ───────────────────────────────────────────────────────────────────
function mapStatus(s: RawService['status']): Health {
  if (s === 'up' || s === 'self') return 'up';
  if (s === 'degraded')            return 'deg';
  return 'down';
}

function healthColor(h: Health): string {
  return h === 'up'   ? 'var(--suc)'
       : h === 'deg'  ? 'var(--wrn)'
       : h === 'down' ? 'var(--danger)'
       :                'var(--t3)';
}

/** Latency tier color — kept aligned with alerting thresholds. */
function latencyColor(ms: number | null): string {
  if (ms == null)                return 'var(--t3)';
  if (ms <= LATENCY_GOOD_MAX)    return 'var(--suc)';
  if (ms <= LATENCY_WARN_MAX)    return 'var(--wrn)';
  return 'var(--danger)';
}

function healthWord(h: Health, t: (k: string) => string): string {
  return h === 'up'  ? t('services.up')
       : h === 'deg' ? t('services.degraded')
       : h === 'down'? t('services.down')
       :               'IDLE';
}

// ── Shared inline styles ──────────────────────────────────────────────────────
const CARD_BASE: React.CSSProperties = {
  position:      'relative',
  padding:       '7px 10px 8px',
  background:    'var(--bg1)',
  border:        '1px solid var(--bd)',
  borderLeftWidth: 3,
  borderRadius:  'var(--seer-radius-sm, 4px)',
  cursor:        'pointer',
  display:       'flex',
  flexDirection: 'column',
  gap:           2,
  minHeight:     58,
};

const GRID: React.CSSProperties = {
  display:             'grid',
  gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
  gap:                 6,
};

function statusPillStyles(h: Health, latencyMs: number | null) {
  const c = healthColor(h);
  return {
    word: {
      padding:       '2px 6px',
      borderRadius:  3,
      fontSize:      9,
      letterSpacing: '0.08em',
      background:    `color-mix(in srgb, ${c} 18%, transparent)`,
      color:         c,
      fontFamily:    'var(--mono)',
      fontWeight:    600,
    } as React.CSSProperties,
    lat: {
      color:      latencyColor(h === 'up' || h === 'deg' ? latencyMs : null),
      fontSize:   11,
      fontWeight: 600,
      fontFamily: 'var(--mono)',
      marginLeft: 6,
    } as React.CSSProperties,
  };
}

// ── Pieces ────────────────────────────────────────────────────────────────────
function StatusPill({ health, text, latencyMs }: {
  health:     Health;
  text?:      string;
  latencyMs?: number | null;
}) {
  const { t } = useTranslation();
  const s = statusPillStyles(health, latencyMs ?? null);
  return (
    <span style={{ display: 'inline-flex', alignItems: 'baseline', marginTop: 'auto' }}>
      <span style={s.word}>{healthWord(health, t)}</span>
      <span style={s.lat}>{text ?? '—'}</span>
    </span>
  );
}

function DockerIcon({ size = 11 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="#2496ED" aria-hidden="true" style={{ flexShrink: 0 }}>
      <path d="M13.983 11.078h2.119a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.119a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185m-2.954-5.43h2.118a.186.186 0 0 0 .186-.186V3.574a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m0 2.716h2.118a.187.187 0 0 0 .186-.186V6.29a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.887c0 .102.082.186.185.186m-2.93 0h2.12a.186.186 0 0 0 .184-.186V6.29a.185.185 0 0 0-.185-.185H8.1a.185.185 0 0 0-.185.185v1.887c0 .102.083.186.185.186m-2.964 0h2.119a.186.186 0 0 0 .185-.186V6.29a.185.185 0 0 0-.185-.185H5.136a.186.186 0 0 0-.186.185v1.887c0 .102.084.186.186.186m5.893 2.715h2.118a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m-2.93 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.184.185v1.888c0 .102.083.185.185.185m-2.964 0h2.119a.185.185 0 0 0 .185-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.186.186 0 0 0-.186.185v1.888c0 .102.084.185.186.185m-2.92 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185M23.763 9.89c-.065-.051-.672-.51-1.954-.51-.338.001-.676.03-1.01.087-.248-1.7-1.653-2.53-1.716-2.566l-.344-.199-.226.327c-.284.438-.49.922-.612 1.43-.23.97-.09 1.882.403 2.661-.595.332-1.55.413-1.744.42H.751a.751.751 0 0 0-.75.748 11.376 11.376 0 0 0 .692 4.062c.545 1.428 1.355 2.48 2.41 3.124 1.18.723 3.1 1.137 5.275 1.137.983.003 1.963-.086 2.93-.266a12.248 12.248 0 0 0 3.823-1.389c.98-.567 1.86-1.288 2.61-2.136 1.252-1.418 1.998-2.997 2.553-4.4h.221c1.372 0 2.215-.549 2.68-1.009.309-.293.55-.65.707-1.046l.098-.288Z"/>
    </svg>
  );
}

function ServiceTile({ card, dashed = false, docker = false }: {
  card:    Card;
  dashed?: boolean;
  docker?: boolean;
}) {
  const border = healthColor(card.health);
  return (
    <div
      role="button"
      onClick={card.onOpen}
      style={{
        ...CARD_BASE,
        borderLeftColor: border,
        borderStyle:     dashed ? 'dashed' : 'solid',
        borderLeftStyle: 'solid',
      }}
    >
      <span style={{
        position: 'absolute', top: 10, right: 10,
        width: 7, height: 7, borderRadius: '50%',
        background: border,
        boxShadow: card.health === 'up' || card.health === 'deg'
          ? `0 0 0 3px color-mix(in srgb, ${border} 18%, transparent)`
          : 'none',
      }} />
      <div style={{
        fontWeight: 600, color: 'var(--t1)', fontSize: 13,
        display: 'flex', alignItems: 'center', gap: 5,
      }}>
        {docker && <DockerIcon />}
        <span>{card.name}</span>
      </div>
      <div style={{ color: 'var(--t3)', fontSize: 11, fontFamily: 'var(--mono)' }}>{card.meta}</div>
      <StatusPill health={card.health} text={card.latency} latencyMs={card.latencyMs} />
    </div>
  );
}

function formatBytes(b: number | null): string {
  if (b == null) return '—';
  if (b < 1024) return `${b} B`;
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
  if (b < 1024 * 1024 * 1024) return `${(b / 1024 / 1024).toFixed(1)} MB`;
  return `${(b / 1024 / 1024 / 1024).toFixed(2)} GB`;
}

/** Live cluster tile — driven by chur `/heimdall/databases` (probeCluster). */
function ClusterTile({ cluster }: { cluster: ClusterHealth }) {
  const health: Health = cluster.health === 'up' ? 'up' : 'down';
  const border = healthColor(health);
  const m = cluster.metrics;
  return (
    <div style={{
      ...CARD_BASE,
      padding: '8px 10px 10px',
      minHeight: 'unset',
      borderLeftColor: border,
    }}>
      <span style={{
        position: 'absolute', top: 10, right: 10,
        width: 7, height: 7, borderRadius: '50%',
        background: border,
        boxShadow: health === 'up'
          ? `0 0 0 3px color-mix(in srgb, ${border} 18%, transparent)` : 'none',
      }} />
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
        <span style={{ fontWeight: 600, color: 'var(--t1)', fontSize: 13 }}>{cluster.id}</span>
        <span style={{ color: 'var(--t3)', fontSize: 11, fontFamily: 'var(--mono)' }}>:{cluster.port}</span>
        {cluster.version && (
          <span
            title={cluster.version}
            style={{ color: 'var(--t3)', fontSize: 10, fontFamily: 'var(--mono)' }}>
            v{cluster.version.split(' ')[0]}
          </span>
        )}
        <span style={{
          fontSize: 10, color: latencyColor(cluster.latencyMs),
          fontFamily: 'var(--mono)', marginLeft: 'auto', fontWeight: 600,
        }}>{cluster.latencyMs != null ? `${cluster.latencyMs} ms` : '—'}</span>
      </div>

      {/* Metrics row (profiler) */}
      {m && (
        <div style={{
          marginTop: 6, display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)', gap: 6,
          fontSize: 10, fontFamily: 'var(--mono)',
        }}>
          <MetricCell label="cache" value={m.cacheHitPct != null ? `${m.cacheHitPct}%` : '—'} />
          <MetricCell label="q/min" value={m.queriesPerMin != null ? String(m.queriesPerMin) : '—'} />
          <MetricCell label="wal"   value={formatBytes(m.walBytesWritten)} />
          <MetricCell label="files" value={m.openFiles != null ? String(m.openFiles) : '—'} />
        </div>
      )}

      {/* Databases */}
      <div style={{
        marginTop: 6, borderTop: '1px solid var(--bd)', paddingTop: 6,
        display: 'flex', flexDirection: 'column', gap: 3,
      }}>
        {cluster.dbs.length === 0 && (
          <div style={{ color: 'var(--t3)', fontSize: 11, fontStyle: 'italic' }}>
            {cluster.error ?? 'no databases'}
          </div>
        )}
        {cluster.dbs.map(db => (
          <div key={db} style={{
            display: 'flex', alignItems: 'baseline', gap: 8,
            fontSize: 11, fontFamily: 'var(--mono)',
          }}>
            <span style={{
              width: 6, height: 6, borderRadius: '50%',
              background: healthColor(health),
              display: 'inline-block', flexShrink: 0,
            }} />
            <span style={{ color: 'var(--t1)', fontWeight: 500 }}>{db}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function MetricCell({ label, value }: { label: string; value: string }) {
  return (
    <div style={{
      background: 'var(--bg2)', padding: '3px 5px',
      borderRadius: 3, textAlign: 'center',
    }}>
      <div style={{ color: 'var(--t3)', fontSize: 9, letterSpacing: '0.06em' }}>{label}</div>
      <div style={{ color: 'var(--t1)', fontWeight: 600 }}>{value}</div>
    </div>
  );
}

function SectionHeader({ title, count, collapsed, onToggle }: {
  title:     string;
  count?:    string;
  collapsed: boolean;
  onToggle:  () => void;
}) {
  return (
    <div
      onClick={onToggle}
      style={{
        display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer',
        userSelect: 'none', marginBottom: 10,
      }}
    >
      <span style={{
        color: 'var(--t3)', fontSize: 10,
        transform: collapsed ? 'rotate(-90deg)' : 'none', transition: 'transform .12s',
      }}>▼</span>
      <span style={{
        fontSize: 12, fontWeight: 600, letterSpacing: '0.08em',
        textTransform: 'uppercase', color: 'var(--t2)',
      }}>{title}</span>
      {count && <span style={{ color: 'var(--t3)', fontSize: 11 }}>{count}</span>}
    </div>
  );
}

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

  // ── Health polling (platform + workers + integrations come from one API) ──
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

  // ── Detail drawer — click on any service card opens this ──────────────────
  const [selected, setSelected] = useState<RawService | null>(null);

  // ── Bucket API services into sections ─────────────────────────────────────
  // Platform/Workers/Integrations show ONLY docker-mode entries; dev-mode
  // entries are surfaced exclusively under "Dev mode services" to avoid
  // double-listing (e.g. chur:3000 dev + chur:13000 docker).
  //
  // Sort each bucket by service id so card order stays stable between polls
  // (backend returns entries in probe-completion order — non-deterministic).
  // Split by topology layer: Front = L0..L2 (edge + browser-facing UIs),
  //                         Middleware = L3..L5 (BFF, backend services, auth).
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

  const filteredWorkers = showAll
    ? workers
    : workers; // chur /services/health has no tenant dim yet — TODO: per-tenant workers
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

      {/* ── Dev mode — our own services running locally (pnpm dev / quarkus dev)
           rather than in Docker. Hidden entirely in Docker-only stacks. ── */}
      {devVisible && (
        <section style={{
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

      {/* ── Middleware (BFF + backend services + auth: L3..L5) ── */}
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

      {/* ── Tenant workers (before DBs per user preference) ── */}
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

      {/* ── ArcadeDB clusters — each card shows its databases inside ── */}
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

      {/* ── Topology (existing) — L1…L5 service map; dev entries excluded ── */}
      <ServiceTopology serviceStatuses={(raw ?? []).filter(r => r.mode !== 'dev')} />

      {selected && <ServiceDetailDrawer svc={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}

// ── Service detail drawer — slides in from the left with extra info ──────────
function ServiceDetailDrawer({ svc, onClose }: {
  svc:     RawService;
  onClose: () => void;
}) {
  const navigate = useNavigate();
  const spec     = BY_ID[svc.name];
  const health   = mapStatus(svc.status);
  const url      = svc.mode === 'dev' ? spec?.devUrl : spec?.dockerUrl;
  const border   = healthColor(health);
  const { t }    = useTranslation();

  return (
    <>
      <div
        onClick={onClose}
        style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.35)',
          zIndex: 90,
        }}
      />
      <aside style={{
        position:   'fixed', top: 0, right: 0, bottom: 0, width: 400,
        background: 'var(--bg1)', borderLeft: '1px solid var(--bd)',
        zIndex:     91, display: 'flex', flexDirection: 'column',
        boxShadow:  '-2px 0 16px rgba(0,0,0,0.35)',
      }}>
        <header style={{
          padding: '14px 16px', borderBottom: '1px solid var(--bd)',
          display: 'flex', alignItems: 'center', gap: 10,
        }}>
          <span style={{
            width: 10, height: 10, borderRadius: '50%', background: border,
            boxShadow: health === 'up' || health === 'deg'
              ? `0 0 0 3px color-mix(in srgb, ${border} 18%, transparent)` : 'none',
          }} />
          <span style={{ fontSize: 15, fontWeight: 600, color: 'var(--t1)' }}>
            {spec?.label ?? svc.name}
          </span>
          <span style={{
            fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--t3)',
            background: 'var(--bg2)', padding: '2px 6px', borderRadius: 3,
          }}>{svc.mode}</span>
          <span style={{ flex: 1 }} />
          <button onClick={onClose} style={{
            background: 'transparent', border: 'none', color: 'var(--t2)',
            cursor: 'pointer', fontSize: 18, lineHeight: 1,
          }}>×</button>
        </header>

        <div style={{ padding: 16, display: 'flex', flexDirection: 'column', gap: 14, overflowY: 'auto' }}>
          <DetailRow label="Status" value={healthWord(health, t)} valueStyle={{ color: border, fontWeight: 600 }} />
          <DetailRow label="Latency"
                     value={svc.status === 'self' ? '—' : `${svc.latencyMs} ms`}
                     valueStyle={{ color: latencyColor(svc.status === 'self' ? null : svc.latencyMs), fontWeight: 600 }} />
          <DetailRow label="Port (active)" value={`:${svc.port}`} mono />
          {spec?.portDev && <DetailRow label="Dev port"    value={`:${spec.portDev}`}    mono />}
          {spec?.portDocker && <DetailRow label="Docker port" value={`:${spec.portDocker}`} mono />}
          <DetailRow label="Category" value={spec?.category ?? 'unknown'} />
          {svc.version && <DetailRow label="Build" value={`v${svc.version}`} mono />}

          <div style={{ display: 'flex', gap: 8, marginTop: 8, flexWrap: 'wrap' }}>
            {url && (
              <a href={url} target="_blank" rel="noopener noreferrer" style={ACTION_BTN}>
                Open UI ↗
              </a>
            )}
            <button
              onClick={() => { navigate(`../events?comp=${svc.name}`); onClose(); }}
              style={ACTION_BTN}
            >
              View events
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}

function DetailRow({ label, value, valueStyle, mono }: {
  label:       string;
  value:       string;
  valueStyle?: React.CSSProperties;
  mono?:       boolean;
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'baseline', gap: 10 }}>
      <span style={{ color: 'var(--t3)', fontSize: 11, width: 110, flexShrink: 0 }}>{label}</span>
      <span style={{
        color: 'var(--t1)', fontSize: 13,
        fontFamily: mono ? 'var(--mono)' : 'inherit',
        ...(valueStyle ?? {}),
      }}>{value}</span>
    </div>
  );
}

const ACTION_BTN: React.CSSProperties = {
  padding: '6px 12px',
  background: 'var(--bg2)',
  border: '1px solid var(--bd)',
  borderRadius: 'var(--seer-radius-sm, 4px)',
  color: 'var(--t1)',
  fontSize: 12,
  fontWeight: 500,
  cursor: 'pointer',
  textDecoration: 'none',
};

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

// ── Dev grid — our own services currently running in dev mode (pnpm dev /
// quarkus dev) rather than Docker. One tile per service from SERVICES.
// Hidden entirely when no service is in dev mode (full-Docker stack).
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
