import {
  ReactFlow, Background, Controls,
  type Node, type Edge, MarkerType,
  Handle, Position, useNodesState,
} from '@xyflow/react';
import { useEffect, useMemo } from 'react';
import '@xyflow/react/dist/style.css';

// ── Docker SVG icon ────────────────────────────────────────────────────────────
function DockerIcon({ size = 10 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="#2496ED" aria-hidden="true">
      <path d="M13.983 11.078h2.119a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.119a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185m-2.954-5.43h2.118a.186.186 0 0 0 .186-.186V3.574a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m0 2.716h2.118a.187.187 0 0 0 .186-.186V6.29a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.887c0 .102.082.186.185.186m-2.93 0h2.12a.186.186 0 0 0 .184-.186V6.29a.185.185 0 0 0-.185-.185H8.1a.185.185 0 0 0-.185.185v1.887c0 .102.083.186.185.186m-2.964 0h2.119a.186.186 0 0 0 .185-.186V6.29a.185.185 0 0 0-.185-.185H5.136a.186.186 0 0 0-.186.185v1.887c0 .102.084.186.186.186m5.893 2.715h2.118a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m-2.93 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.184.185v1.888c0 .102.083.185.185.185m-2.964 0h2.119a.185.185 0 0 0 .185-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.186.186 0 0 0-.186.185v1.888c0 .102.084.185.186.185m-2.92 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185M23.763 9.89c-.065-.051-.672-.51-1.954-.51-.338.001-.676.03-1.01.087-.248-1.7-1.653-2.53-1.716-2.566l-.344-.199-.226.327c-.284.438-.49.922-.612 1.43-.23.97-.09 1.882.403 2.661-.595.332-1.55.413-1.744.42H.751a.751.751 0 0 0-.75.748 11.376 11.376 0 0 0 .692 4.062c.545 1.428 1.355 2.48 2.41 3.124 1.18.723 3.1 1.137 5.275 1.137.983.003 1.963-.086 2.93-.266a12.248 12.248 0 0 0 3.823-1.389c.98-.567 1.86-1.288 2.61-2.136 1.252-1.418 1.998-2.997 2.553-4.4h.221c1.372 0 2.215-.549 2.68-1.009.309-.293.55-.65.707-1.046l.098-.288Z"/>
    </svg>
  );
}

// ── Types ─────────────────────────────────────────────────────────────────────

/** Minimal health snapshot used by the topology to colour nodes. */
export interface ServiceHealth {
  name:   string;
  mode:   'dev' | 'docker';
  status: 'up' | 'degraded' | 'down' | 'self';
}

interface ServiceNodeData {
  label:        string;
  port:         string;   // internal / dev port
  extPort?:     string;   // host-mapped port (Docker only)
  mode?:        'dev' | 'docker';
  color?:       string;   // static type colour — shown when health is unknown
  statusColor?: string;   // live health colour — overrides `color` when present
  [key: string]: unknown;
}

interface LaneNodeData {
  label:    string;
  isDocker: boolean;
  [key: string]: unknown;
}

// ── Service node ───────────────────────────────────────────────────────────────
function ServiceNode({ data }: { data: ServiceNodeData }) {
  const isDocker = data.mode === 'docker';

  // Health colour takes priority; fall back to static type colour
  const borderColor = data.statusColor
    ? data.statusColor
    : isDocker
      ? 'color-mix(in srgb, #2496ED 50%, var(--bd))'
      : (data.color ?? 'var(--bd)');

  const hostPort = isDocker ? (data.extPort ?? data.port) : data.port;

  const handleDoubleClick = () => {
    window.open(`http://localhost:${hostPort}`, '_blank', 'noopener,noreferrer');
  };

  return (
    <div
      onDoubleClick={handleDoubleClick}
      title={`Double-click to open http://localhost:${hostPort}`}
      style={{
        background:   'var(--bg1)',
        border:       `1px solid ${borderColor}`,
        borderRadius: 'var(--seer-radius-md)',
        padding:      '5px 10px',
        minWidth:     118,
        textAlign:    'center',
        cursor:       'pointer',
        // Glow when health status is known
        boxShadow: data.statusColor
          ? `0 0 6px color-mix(in srgb, ${data.statusColor} 40%, transparent)`
          : undefined,
        transition: 'border-color 0.4s, box-shadow 0.4s',
      }}>
      <Handle id="t" type="target" position={Position.Top}    style={{ background: 'var(--bd)' }} />
      <Handle id="l" type="target" position={Position.Left}   style={{ background: 'var(--bd)' }} />

      {/* Label row */}
      <div style={{
        fontSize: '11px', fontWeight: 600, color: 'var(--t1)',
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4,
      }}>
        {isDocker && <DockerIcon size={10} />}
        {data.label}
      </div>

      {/* Health indicator dot (only when health is known) */}
      {data.statusColor && (
        <div style={{
          width: 5, height: 5, borderRadius: '50%',
          background: data.statusColor,
          margin: '2px auto 0',
        }} />
      )}

      {/* Internal port */}
      <div style={{
        fontSize: '10px', fontFamily: 'var(--mono)',
        color: isDocker ? '#2496ED' : 'var(--t3)',
        marginTop: data.statusColor ? 1 : 2,
      }}>
        :{data.port}
      </div>

      {/* Host-mapped port (Docker only) */}
      {data.extPort && (
        <div style={{ fontSize: '9px', fontFamily: 'var(--mono)', color: 'var(--t3)', marginTop: 1 }}>
          ext :{data.extPort}
        </div>
      )}

      <Handle id="b" type="source" position={Position.Bottom} style={{ background: 'var(--bd)' }} />
      <Handle id="r" type="source" position={Position.Right}  style={{ background: 'var(--bd)' }} />
    </div>
  );
}

// ── Lane label node ────────────────────────────────────────────────────────────
function LaneNode({ data }: { data: LaneNodeData }) {
  return (
    <div style={{
      padding:       '3px 14px',
      background:    data.isDocker
        ? 'color-mix(in srgb, #2496ED 10%, transparent)'
        : 'color-mix(in srgb, var(--acc) 10%, transparent)',
      border: `1px solid ${data.isDocker
        ? 'color-mix(in srgb, #2496ED 25%, transparent)'
        : 'color-mix(in srgb, var(--acc) 25%, transparent)'}`,
      borderRadius:  'var(--seer-radius-sm)',
      fontSize:      '9px',
      fontFamily:    'var(--mono)',
      fontWeight:    600,
      letterSpacing: '0.08em',
      textTransform: 'uppercase',
      color:         data.isDocker ? '#2496ED' : 'var(--acc)',
      display:       'flex',
      alignItems:    'center',
      gap:           5,
      userSelect:    'none',
      cursor:        'default',
    }}>
      {data.isDocker && <DockerIcon size={9} />}
      {data.label}
    </div>
  );
}

const NODE_TYPES = { service: ServiceNode, lane: LaneNode };

// ── Base nodes (static — health colours are applied dynamically) ───────────────
//
//  IDE lane (left)          Docker lane (right)        Shared (centre)
//  ───────────────          ───────────────────        ───────────────
//  port = dev port          port = internal container  port = external access
//                           extPort = host-mapped
//
//  Keycloak — ONE shared instance (accessed by both IDE and Docker clients):
//    IDE clients:    localhost:8180
//    Docker clients: keycloak:8180  (internal docker net)  ext :18180
//    → single node centred between the two lanes at (350, 430)
//
//  Ygg — shared ArcadeDB (HoundArcade external container): single node
//  Frigg — separate IDE (localhost:2481) vs Docker (frigg:2480) paths
const BASE_NODES: Node[] = [
  // ── Lane headers ─────────────────────────────────────────────────────────────
  { id: 'lane-dev',    type: 'lane', selectable: false, draggable: false,
    position: { x: 110, y: -55 }, data: { label: 'IDE / Dev', isDocker: false } },
  { id: 'lane-docker', type: 'lane', selectable: false, draggable: false,
    position: { x: 570, y: -55 }, data: { label: 'Docker', isDocker: true } },

  // ── Row 0: Shell ─────────────────────────────────────────────────────────────
  { id: 'shell-dev',    type: 'service', position: { x: 130, y: 0 },
    data: { label: 'Shell',  port: '5175', mode: 'dev',    color: 'var(--acc)' } },
  { id: 'shell-docker', type: 'service', position: { x: 590, y: 0 },
    data: { label: 'Shell',  port: '5175', extPort: '25175', mode: 'docker', color: 'var(--acc)' } },

  // ── Row 1: MF remotes ────────────────────────────────────────────────────────
  { id: 'verdandi-dev',    type: 'service', position: { x: 0,   y: 140 },
    data: { label: 'Seiðr Studio', port: '5173', mode: 'dev',    color: 'var(--inf)' } },
  { id: 'verdandi-docker', type: 'service', position: { x: 470, y: 140 },
    data: { label: 'Seiðr Studio', port: '5173', extPort: '15173', mode: 'docker', color: 'var(--inf)' } },
  { id: 'hf-dev',          type: 'service', position: { x: 270, y: 140 },
    data: { label: 'Heimðallr UI',  port: '5174', mode: 'dev',    color: 'var(--inf)' } },
  { id: 'hf-docker',       type: 'service', position: { x: 740, y: 140 },
    data: { label: 'Heimðallr UI',  port: '5174', extPort: '25174', mode: 'docker', color: 'var(--inf)' } },

  // ── Row 2: BFF + backend ─────────────────────────────────────────────────────
  { id: 'chur-dev',    type: 'service', position: { x: 0,   y: 290 },
    data: { label: 'Chur (BFF)',  port: '3000', mode: 'dev',    color: 'var(--suc)' } },
  { id: 'chur-docker', type: 'service', position: { x: 470, y: 290 },
    data: { label: 'Chur (BFF)',  port: '3000', extPort: '13000', mode: 'docker', color: 'var(--suc)' } },
  { id: 'hb-dev',      type: 'service', position: { x: 270, y: 290 },
    data: { label: 'Heimdall Bk', port: '9093', mode: 'dev',    color: 'var(--suc)' } },
  { id: 'hb-docker',   type: 'service', position: { x: 740, y: 290 },
    data: { label: 'Heimdall Bk', port: '9093', extPort: '19093', mode: 'docker', color: 'var(--suc)' } },

  // ── Row 3: Auth + GraphQL ────────────────────────────────────────────────────
  // Keycloak: ONE shared instance — centred between the two lanes
  { id: 'keycloak',        type: 'service', position: { x: 350, y: 430 },
    data: { label: 'Keycloak', port: '8180', extPort: '18180', color: 'var(--wrn)' } },
  { id: 'shuttle-dev',     type: 'service', position: { x: 80,  y: 430 },
    data: { label: 'Shuttle',  port: '8080', mode: 'dev',    color: 'var(--suc)' } },
  { id: 'shuttle-docker',  type: 'service', position: { x: 640, y: 430 },
    data: { label: 'Shuttle',  port: '8080', extPort: '18080', mode: 'docker', color: 'var(--suc)' } },

  // ── Row 4: Ðali (async PL/SQL parser, port 9090) ─────────────────────────────
  { id: 'dali-dev',    type: 'service', position: { x: 80,  y: 570 },
    data: { label: 'Ðali (Parser)', port: '9090', mode: 'dev',    color: 'var(--acc)' } },
  { id: 'dali-docker', type: 'service', position: { x: 640, y: 570 },
    data: { label: 'Ðali (Parser)', port: '9090', extPort: '19090', mode: 'docker', color: 'var(--acc)' } },

  // ── Row 5: Storage ────────────────────────────────────────────────────────────
  // Ygg = HoundArcade — shared external container
  { id: 'ygg',         type: 'service', position: { x: 350, y: 710 },
    data: { label: 'Ygg (HoundArcade)', port: '2480', color: 'var(--t3)' } },
  // Frigg — ONE shared ArcadeDB instance (Heimðallr + Ðali persistence)
  //   IDE access:    localhost:2481  (host-mapped)
  //   Docker access: frigg:2480      (internal docker net)  ext :2481
  { id: 'frigg', type: 'service', position: { x: 640, y: 710 },
    data: { label: 'Frigg', port: '2481', extPort: '2481', color: 'var(--t3)' } },
];

// ── Health colour helper ───────────────────────────────────────────────────────
function healthColor(status: ServiceHealth['status'] | undefined): string | undefined {
  switch (status) {
    case 'up':
    case 'self':     return 'var(--suc)';
    case 'degraded': return 'var(--wrn)';
    case 'down':     return 'var(--danger)';
    default:         return undefined;
  }
}

// ── Node → service mapping ─────────────────────────────────────────────────────
// Maps each topology node ID to the service name + mode used for health lookup.
// mode='any' = pick whichever instance is available (for shared/single nodes).
const NODE_SERVICE_MAP: Record<string, { name: string; mode: 'dev' | 'docker' | 'any' }> = {
  'shell-dev':       { name: 'shell',             mode: 'dev'    },
  'shell-docker':    { name: 'shell',             mode: 'docker' },
  'verdandi-dev':    { name: 'verdandi',          mode: 'dev'    },
  'verdandi-docker': { name: 'verdandi',          mode: 'docker' },
  'hf-dev':          { name: 'heimdall-frontend', mode: 'dev'    },
  'hf-docker':       { name: 'heimdall-frontend', mode: 'docker' },
  'chur-dev':        { name: 'chur',              mode: 'dev'    },
  'chur-docker':     { name: 'chur',              mode: 'docker' },
  'hb-dev':          { name: 'heimdall-backend',  mode: 'dev'    },
  'hb-docker':       { name: 'heimdall-backend',  mode: 'docker' },
  'keycloak':        { name: 'keycloak',          mode: 'any'    },  // single shared instance
  'shuttle-dev':     { name: 'shuttle',           mode: 'dev'    },
  'shuttle-docker':  { name: 'shuttle',           mode: 'docker' },
  'dali-dev':        { name: 'dali',              mode: 'dev'    },
  'dali-docker':     { name: 'dali',              mode: 'docker' },
  'ygg':             { name: 'ygg',               mode: 'any'    },
  'frigg':           { name: 'frigg',             mode: 'any'    },
};

// ── Edge factory ──────────────────────────────────────────────────────────────
function e(
  id: string, src: string, tgt: string, label: string,
  color = 'var(--bd)', animated = false,
): Edge {
  return {
    id, source: src, target: tgt, label, animated,
    markerEnd:    { type: MarkerType.ArrowClosed, color },
    style:        { stroke: color, strokeWidth: 1.5 },
    labelStyle:   { fill: 'var(--t3)', fontSize: 9, fontFamily: 'var(--mono)' },
    labelBgStyle: { fill: 'var(--bg0)', opacity: 0.85 },
  };
}

const EDGES: Edge[] = [
  // ── IDE lane ─────────────────────────────────────────────────────────────────
  e('sd-vd',    'shell-dev',    'verdandi-dev',    'MF',         'var(--inf)'),
  e('sd-hfd',   'shell-dev',    'hf-dev',          'MF',         'var(--inf)'),
  e('vd-cd',    'verdandi-dev', 'chur-dev',        'auth/proxy', 'var(--suc)'),
  e('hfd-hbd',  'hf-dev',       'hb-dev',          'REST/WS',    'var(--suc)', true),
  e('cd-kc',    'chur-dev',     'keycloak',        'OAuth2',     'var(--wrn)'),   // → shared KC
  e('cd-std',   'chur-dev',     'shuttle-dev',     'GraphQL',    'var(--acc)'),
  e('cd-hbd',   'chur-dev',     'hb-dev',          'events',     'var(--t3)'),
  e('std-ygg',   'shuttle-dev',  'ygg',             'ArcadeDB',   'var(--t3)'),
  e('hbd-fri',   'hb-dev',       'frigg',           'ArcadeDB',   'var(--t3)'),
  e('cd-dali',   'chur-dev',     'dali-dev',        'REST',       'var(--acc)'),
  e('dali-fri',  'dali-dev',     'frigg',           'JobRunr',    'var(--t3)'),

  // ── Docker lane (internal ports, docker network) ──────────────────────────
  e('sk-vk',     'shell-docker',    'verdandi-docker',    'MF',         'var(--inf)'),
  e('sk-hfk',    'shell-docker',    'hf-docker',          'MF',         'var(--inf)'),
  e('vk-ck',     'verdandi-docker', 'chur-docker',        'auth/proxy', 'var(--suc)'),
  e('hfk-hbk',   'hf-docker',      'hb-docker',          'REST/WS',    'var(--suc)', true),
  e('ck-kc',     'chur-docker',     'keycloak',           'OAuth2',     'var(--wrn)'),  // → shared KC
  e('ck-stk',    'chur-docker',     'shuttle-docker',     'GraphQL',    'var(--acc)'),
  e('ck-hbk',    'chur-docker',     'hb-docker',          'events',     'var(--t3)'),
  e('stk-ygg',   'shuttle-docker',  'ygg',                'ArcadeDB',   'var(--t3)'),
  e('hbk-frik',  'hb-docker',       'frigg',              'ArcadeDB',   'var(--t3)'),  // → shared Frigg
  e('ck-dalik',  'chur-docker',     'dali-docker',        'REST',       'var(--acc)'),
  e('dalik-frik','dali-docker',     'frigg',              'JobRunr',    'var(--t3)'),
];

// ── Component ─────────────────────────────────────────────────────────────────
export function ServiceTopology({ serviceStatuses = [] }: { serviceStatuses?: ServiceHealth[] }) {
  // Build fast lookup: "name:mode" → status
  const statusLookup = useMemo(() => {
    const m = new Map<string, ServiceHealth['status']>();
    for (const s of serviceStatuses) m.set(`${s.name}:${s.mode}`, s.status);
    return m;
  }, [serviceStatuses]);

  // Controlled nodes — ReactFlow manages drag positions; we only update `data`
  const [nodes, setNodes, onNodesChange] = useNodesState(BASE_NODES);

  // Re-apply health colours whenever statuses are refreshed (every 10s)
  useEffect(() => {
    setNodes(prev => prev.map(n => {
      const mapping = NODE_SERVICE_MAP[n.id];
      if (!mapping) return n;

      let status: ServiceHealth['status'] | undefined;
      if (mapping.mode === 'any') {
        status = statusLookup.get(`${mapping.name}:dev`)
              ?? statusLookup.get(`${mapping.name}:docker`);
      } else {
        status = statusLookup.get(`${mapping.name}:${mapping.mode}`);
      }

      const hc = healthColor(status);
      const prev_hc = (n.data as ServiceNodeData).statusColor;
      if (hc === prev_hc) return n; // no change — avoid re-render
      return { ...n, data: { ...n.data, statusColor: hc } };
    }));
  }, [statusLookup, setNodes]);

  return (
    <div style={{
      background:   'var(--bg1)',
      border:       '1px solid var(--bd)',
      borderRadius: 'var(--seer-radius-md)',
      marginTop:    'var(--seer-space-6)',
      overflow:     'hidden',
    }}>
      <div style={{
        padding:      'var(--seer-space-3) var(--seer-space-4)',
        borderBottom: '1px solid var(--bd)',
        fontSize:     '10px',
        color:        'var(--t3)',
        textTransform:'uppercase',
        letterSpacing:'0.06em',
        display:      'flex',
        alignItems:   'center',
        gap:          'var(--seer-space-3)',
      }}>
        Service Topology
        {serviceStatuses.length > 0 && (
          <span style={{ display: 'flex', alignItems: 'center', gap: 6, marginLeft: 'auto' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
              <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--suc)', display: 'inline-block' }} />
              <span>up</span>
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
              <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--wrn)', display: 'inline-block' }} />
              <span>degraded</span>
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
              <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--danger)', display: 'inline-block' }} />
              <span>down</span>
            </span>
          </span>
        )}
      </div>
      <div style={{ height: 880 }}>
        <ReactFlow
          nodes={nodes}
          edges={EDGES}
          onNodesChange={onNodesChange}
          nodeTypes={NODE_TYPES}
          fitView
          fitViewOptions={{ padding: 0.12 }}
          proOptions={{ hideAttribution: true }}
          style={{ background: 'var(--bg0)' }}
        >
          <Background color="var(--bd)" gap={20} />
          <Controls style={{ background: 'var(--bg1)', border: '1px solid var(--bd)' }} />
        </ReactFlow>
      </div>
    </div>
  );
}
