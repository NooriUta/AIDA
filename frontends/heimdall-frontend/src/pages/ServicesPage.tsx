import { useEffect, useState, useCallback } from 'react';
import { useTranslation }    from 'react-i18next';
import { useNavigate }       from 'react-router-dom';
import { HEIMDALL_API }      from '../api';
import { useAuthStore }      from '../stores/authStore';
import { ServiceTopology }   from '../components/ServiceTopology';

// ── Types ─────────────────────────────────────────────────────────────────────
interface ServiceStatus {
  name:      string;
  port:      number;
  mode:      'dev' | 'docker';
  status:    'up' | 'degraded' | 'down' | 'self';
  latencyMs: number;
}

type ServiceGroup = {
  name:      string;
  instances: ServiceStatus[];
};

// ── Config ────────────────────────────────────────────────────────────────────
const DISPLAY_NAMES: Record<string, string> = {
  shuttle:           'Shuttle (GraphQL)',
  'heimdall-backend':'Heimdall (Monitor)',
  'heimdall-frontend':'Heimdall UI',
  chur:              'Chur (BFF)',
  verdandi:          'Seiðr Studio',
  shell:             'Shell (Platform)',
  keycloak:          'Keycloak (Auth)',
  frigg:             'Frigg (Store)',
  ygg:               'Yggdrasil (DB)',
  dali:              'Dali (ArcadeDB)',
  mimir:             'Mímir (Memory)',
  anvil:             'Anvil (Indexer)',
};

// Display order for grouped cards
const SERVICE_ORDER = [
  'shuttle', 'chur', 'heimdall-backend', 'verdandi',
  'heimdall-frontend', 'shell', 'keycloak', 'frigg', 'ygg',
  'dali', 'mimir', 'anvil',
];

// Frontend-only WIP stubs — not pinged by backend
const WIP_SERVICES = ['dali', 'mimir', 'anvil'];

// Heimdall-backend must not be restartable from its own UI
const NO_RESTART = new Set(['heimdall-backend']);

const POLL_MS = 10_000;

// ── Helpers ───────────────────────────────────────────────────────────────────
function dotColor(status: ServiceStatus['status']): string {
  switch (status) {
    case 'up':       return 'var(--suc)';
    case 'self':     return 'var(--suc)';
    case 'degraded': return 'var(--wrn)';
    case 'down':     return 'var(--danger)';
  }
}

function statusBadgeClass(status: ServiceStatus['status']): string {
  switch (status) {
    case 'up':       return 'badge badge-suc';
    case 'self':     return 'badge badge-suc';
    case 'degraded': return 'badge badge-warn';
    case 'down':     return 'badge badge-err';
  }
}

function groupAndSort(list: ServiceStatus[]): ServiceGroup[] {
  const map = new Map<string, ServiceStatus[]>();
  for (const svc of list) {
    if (!map.has(svc.name)) map.set(svc.name, []);
    map.get(svc.name)!.push(svc);
  }
  // Sort instances within group: dev first, then docker
  for (const instances of map.values()) {
    instances.sort((a, b) => (a.mode === 'dev' ? -1 : 1) - (b.mode === 'dev' ? -1 : 1));
  }
  return SERVICE_ORDER
    .filter(n => map.has(n) || WIP_SERVICES.includes(n))
    .map(n => ({ name: n, instances: map.get(n) ?? [] }));
}

function upCount(instances: ServiceStatus[]): number {
  return instances.filter(s => s.status !== 'down').length;
}

function isUp(status: ServiceStatus['status']): boolean {
  return status === 'up' || status === 'self' || status === 'degraded';
}

// ── Docker icon ───────────────────────────────────────────────────────────────
function DockerIcon({ size = 12 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
      <path d="M13.983 11.078h2.119a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.119a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185m-2.954-5.43h2.118a.186.186 0 0 0 .186-.186V3.574a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m0 2.716h2.118a.187.187 0 0 0 .186-.186V6.29a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.887c0 .102.082.186.185.186m-2.93 0h2.12a.186.186 0 0 0 .184-.186V6.29a.185.185 0 0 0-.185-.185H8.1a.185.185 0 0 0-.185.185v1.887c0 .102.083.186.185.186m-2.964 0h2.119a.186.186 0 0 0 .185-.186V6.29a.185.185 0 0 0-.185-.185H5.136a.186.186 0 0 0-.186.185v1.887c0 .102.084.186.186.186m5.893 2.715h2.118a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m-2.93 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.184.185v1.888c0 .102.083.185.185.185m-2.964 0h2.119a.185.185 0 0 0 .185-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.186.186 0 0 0-.186.185v1.888c0 .102.084.185.186.185m-2.92 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185M23.763 9.89c-.065-.051-.672-.51-1.954-.51-.338.001-.676.03-1.01.087-.248-1.7-1.653-2.53-1.716-2.566l-.344-.199-.226.327c-.284.438-.49.922-.612 1.43-.23.97-.09 1.882.403 2.661-.595.332-1.55.413-1.744.42H.751a.751.751 0 0 0-.75.748 11.376 11.376 0 0 0 .692 4.062c.545 1.428 1.355 2.48 2.41 3.124 1.18.723 3.1 1.137 5.275 1.137.983.003 1.963-.086 2.93-.266a12.248 12.248 0 0 0 3.823-1.389c.98-.567 1.86-1.288 2.61-2.136 1.252-1.418 1.998-2.997 2.553-4.4h.221c1.372 0 2.215-.549 2.68-1.009.309-.293.55-.65.707-1.046l.098-.288Z"/>
    </svg>
  );
}

// ── InstanceRow ───────────────────────────────────────────────────────────────
function InstanceRow({ svc, isAdmin, onRefresh }: {
  svc:       ServiceStatus;
  isAdmin:   boolean;
  onRefresh: () => void;
}) {
  const { t } = useTranslation();
  const canRestart = isAdmin && svc.mode === 'docker' && !NO_RESTART.has(svc.name);
  const [restarting, setRestarting] = useState(false);
  const [msg,        setMsg]        = useState<string | null>(null);

  const handleRestart = useCallback(async () => {
    setRestarting(true);
    setMsg(null);
    try {
      const res = await fetch(`${HEIMDALL_API}/services/${svc.name}/restart?mode=docker`, {
        method: 'POST', headers: { 'X-Seer-Role': 'admin' },
      });
      const ok = res.ok;
      setMsg(ok ? t('services.restartOk') : t('services.restartFail'));
      if (ok) setTimeout(onRefresh, 3000);
    } catch {
      setMsg(t('services.restartFail'));
    } finally {
      setRestarting(false);
    }
  }, [svc.name, onRefresh, t]);

  const isDocker = svc.mode === 'docker';

  return (
    <div style={{
      background:   'var(--bg0)',
      border:       '1px solid var(--bd)',
      borderRadius: 'var(--seer-radius-sm)',
      padding:      '6px 8px',
      display:      'flex',
      flexDirection:'column',
      gap:          2,
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: '11px' }}>
        {/* Status dot — fixed width so dots align across rows */}
        <div style={{
          width: 6, height: 6, borderRadius: '50%',
          background: dotColor(svc.status), flexShrink: 0,
        }} />

        {/* Mode label — fixed width 52px so ports start at the same column */}
        <span style={{
          width: 52, flexShrink: 0,
          fontFamily: 'var(--mono)', fontSize: '9px', textTransform: 'uppercase',
          color: isDocker ? '#2496ED' : 'var(--t3)',
          display: 'inline-flex', alignItems: 'center', gap: 3,
        }}>
          {isDocker && <DockerIcon size={9} />}
          {isDocker ? 'Docker' : 'IDE'}
        </span>

        {/* Port — fixed width 52px */}
        <span style={{
          width: 52, flexShrink: 0,
          fontFamily: 'var(--mono)', color: 'var(--t3)', fontSize: '11px',
        }}>
          :{svc.port}
        </span>

        {/* Latency — fixed width 44px, color-coded by speed */}
        <span style={{
          width: 44, flexShrink: 0,
          fontFamily: 'var(--mono)', fontSize: '10px',
          color: !isUp(svc.status)      ? 'transparent'
               : svc.status === 'self'  ? 'var(--t3)'
               : svc.latencyMs < 50    ? 'var(--suc)'
               : svc.latencyMs < 200   ? 'var(--t2)'
               : svc.latencyMs < 500   ? 'var(--wrn)'
               :                         'var(--danger)',
        }}>
          {svc.status === 'self' ? 'self' : isUp(svc.status) ? `${svc.latencyMs}ms` : ''}
        </span>

        <span style={{ flex: 1 }} />

        {/* Status badge */}
        <span className={statusBadgeClass(svc.status)} style={{ fontSize: '9px', flexShrink: 0 }}>
          {svc.status === 'self' ? 'SELF' : t(`services.${svc.status}`)}
        </span>

        {/* Restart — Docker + admin only, not protected services */}
        {canRestart && (
          <button
            onClick={handleRestart}
            disabled={restarting}
            title={t('services.restart')}
            style={{
              width: 22, padding: '1px 0', textAlign: 'center',
              background: 'none', border: '1px solid var(--bd)',
              borderRadius: 'var(--seer-radius-sm)',
              color: restarting ? 'var(--t3)' : 'var(--t2)',
              fontSize: '10px', fontFamily: 'var(--mono)',
              cursor: restarting ? 'default' : 'pointer', flexShrink: 0,
            }}
          >
            {restarting ? '…' : '↺'}
          </button>
        )}
      </div>

      {/* Restart feedback */}
      {msg && (
        <span style={{
          fontSize: '10px', fontFamily: 'var(--mono)', paddingLeft: '14px',
          color: msg === t('services.restartOk') ? 'var(--suc)' : 'var(--danger)',
        }}>
          {msg}
        </span>
      )}
    </div>
  );
}

// ── ServiceCard ───────────────────────────────────────────────────────────────
function ServiceCard({ group, isAdmin, onRefresh, onEventsClick }: {
  group:         ServiceGroup;
  isAdmin:       boolean;
  onRefresh:     () => void;
  onEventsClick: (name: string) => void;
}) {
  const { t }  = useTranslation();
  const isWip  = WIP_SERVICES.includes(group.name);
  const total  = group.instances.length;
  const up     = upCount(group.instances);

  return (
    <div style={{
      background:    'var(--bg1)',
      border:        `1px solid ${!isWip && up === 0 && total > 0 ? 'color-mix(in srgb, var(--danger) 35%, var(--bd))' : 'var(--bd)'}`,
      borderRadius:  'var(--seer-radius-md)',
      padding:       'var(--seer-space-4)',
      display:       'flex',
      flexDirection: 'column',
      gap:           'var(--seer-space-3)',
      opacity:       isWip ? 0.65 : 1,
    }}>

      {/* ── Header ── */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)' }}>
          <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--t1)' }}>
            {DISPLAY_NAMES[group.name] ?? group.name}
          </span>
        </div>

        {isWip ? (
          <span className="badge badge-neutral" style={{ fontSize: '9px' }}>
            {t('services.underConstruction')}
          </span>
        ) : total > 0 ? (
          <span style={{
            fontSize: '10px', fontFamily: 'var(--mono)',
            color: up === total ? 'var(--suc)' : up > 0 ? 'var(--wrn)' : 'var(--danger)',
          }}>
            {up}/{total}
          </span>
        ) : null}
      </div>

      {/* ── Instance rows ── */}
      {!isWip && group.instances.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--seer-space-2)' }}>
          {group.instances.map(svc => (
            <InstanceRow
              key={`${svc.mode}-${svc.port}`}
              svc={svc}
              isAdmin={isAdmin}
              onRefresh={onRefresh}
            />
          ))}
        </div>
      )}

      {/* ── Events link ── */}
      {!isWip && (
        <button
          onClick={() => onEventsClick(group.name)}
          style={{
            padding: '2px 0', background: 'none', border: 'none',
            color: 'var(--inf)', fontSize: '11px', fontFamily: 'var(--font)',
            cursor: 'pointer', textAlign: 'left',
          }}
        >
          {t('services.viewEvents')} →
        </button>
      )}
    </div>
  );
}

// ── ServicesPage ──────────────────────────────────────────────────────────────
export default function ServicesPage() {
  const { t }   = useTranslation();
  const navigate = useNavigate();
  const user     = useAuthStore(s => s.user);
  const isAdmin  = user?.role === 'admin';

  const [services, setServices] = useState<ServiceStatus[] | null>(null);
  const [error,    setError]    = useState<string | null>(null);

  const fetchHealth = useCallback(() => {
    fetch(`${HEIMDALL_API}/services/health`)
      .then(r => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        return r.json() as Promise<ServiceStatus[]>;
      })
      .then(data => { setServices(data); setError(null); })
      .catch(e  => { setError(e instanceof Error ? e.message : 'error'); });
  }, []);

  useEffect(() => {
    fetchHealth();
    const id = setInterval(fetchHealth, POLL_MS);
    return () => clearInterval(id);
  }, [fetchHealth]);

  const groups = groupAndSort(services ?? []);

  return (
    <div style={{ padding: 'var(--seer-space-6)', height: '100%', overflowY: 'auto', background: 'var(--bg0)' }}>

      {/* Title row */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        marginBottom: 'var(--seer-space-4)',
      }}>
        <span style={{ fontSize: '11px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
          {t('services.title')}
        </span>
        {error && (
          <span style={{ color: 'var(--danger)', fontSize: '11px', fontFamily: 'var(--mono)' }}>
            {error}
          </span>
        )}
      </div>

      {/* Loading */}
      {services === null && !error && (
        <div style={{ color: 'var(--t3)', fontSize: '13px' }}>{t('status.loading')}</div>
      )}

      {/* Grid */}
      <div style={{
        display:             'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
        gap:                 'var(--seer-space-4)',
      }}>
        {groups.map(group => (
          <ServiceCard
            key={group.name}
            group={group}
            isAdmin={isAdmin}
            onRefresh={fetchHealth}
            onEventsClick={name => navigate(`../events?comp=${name}`)}
          />
        ))}
      </div>

      {/* Topology diagram */}
      <ServiceTopology />
    </div>
  );
}
