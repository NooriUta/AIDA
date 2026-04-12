import {
  ReactFlow, Background, Controls,
  type Node, type Edge, MarkerType,
  Handle, Position,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

// ── Docker SVG icon ────────────────────────────────────────────────────────────
function DockerIcon({ size = 10 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="#2496ED" aria-hidden="true">
      <path d="M13.983 11.078h2.119a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.119a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185m-2.954-5.43h2.118a.186.186 0 0 0 .186-.186V3.574a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m0 2.716h2.118a.187.187 0 0 0 .186-.186V6.29a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.887c0 .102.082.186.185.186m-2.93 0h2.12a.186.186 0 0 0 .184-.186V6.29a.185.185 0 0 0-.185-.185H8.1a.185.185 0 0 0-.185.185v1.887c0 .102.083.186.185.186m-2.964 0h2.119a.186.186 0 0 0 .185-.186V6.29a.185.185 0 0 0-.185-.185H5.136a.186.186 0 0 0-.186.185v1.887c0 .102.084.186.186.186m5.893 2.715h2.118a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m-2.93 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.184.185v1.888c0 .102.083.185.185.185m-2.964 0h2.119a.185.185 0 0 0 .185-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.186.186 0 0 0-.186.185v1.888c0 .102.084.185.186.185m-2.92 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185M23.763 9.89c-.065-.051-.672-.51-1.954-.51-.338.001-.676.03-1.01.087-.248-1.7-1.653-2.53-1.716-2.566l-.344-.199-.226.327c-.284.438-.49.922-.612 1.43-.23.97-.09 1.882.403 2.661-.595.332-1.55.413-1.744.42H.751a.751.751 0 0 0-.75.748 11.376 11.376 0 0 0 .692 4.062c.545 1.428 1.355 2.48 2.41 3.124 1.18.723 3.1 1.137 5.275 1.137.983.003 1.963-.086 2.93-.266a12.248 12.248 0 0 0 3.823-1.389c.98-.567 1.86-1.288 2.61-2.136 1.252-1.418 1.998-2.997 2.553-4.4h.221c1.372 0 2.215-.549 2.68-1.009.309-.293.55-.65.707-1.046l.098-.288Z"/>
    </svg>
  );
}

// ── Node data ─────────────────────────────────────────────────────────────────
interface ServiceNodeData {
  label:    string;
  port:     string;   // internal container port (or dev port for IDE nodes)
  extPort?: string;   // host-mapped port shown as "→ :XXXXX" (Docker nodes only)
  mode?:    'dev' | 'docker';
  color?:   string;
  [key: string]: unknown;
}

interface LaneNodeData {
  label:    string;
  isDocker: boolean;
  [key: string]: unknown;
}

// ── Service node ───────────────────────────────────────────────────────────────
function ServiceNode({ data }: { data: ServiceNodeData }) {
  const isDocker    = data.mode === 'docker';
  const borderColor = isDocker
    ? 'color-mix(in srgb, #2496ED 50%, var(--bd))'
    : (data.color ?? 'var(--bd)');

  return (
    <div style={{
      background:   'var(--bg1)',
      border:       `1px solid ${borderColor}`,
      borderRadius: 'var(--seer-radius-md)',
      padding:      '5px 10px',
      minWidth:     118,
      textAlign:    'center',
    }}>
      <Handle id="t" type="target" position={Position.Top}    style={{ background: 'var(--bd)' }} />
      <Handle id="l" type="target" position={Position.Left}   style={{ background: 'var(--bd)' }} />

      {/* Label */}
      <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--t1)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4 }}>
        {isDocker && <DockerIcon size={10} />}
        {data.label}
      </div>

      {/* Port: internal */}
      <div style={{ fontSize: '10px', fontFamily: 'var(--mono)', color: isDocker ? '#2496ED' : 'var(--t3)', marginTop: 2 }}>
        :{data.port}
      </div>

      {/* External / host-mapped port (Docker only) */}
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

// ── Nodes ─────────────────────────────────────────────────────────────────────
//
//  IDE lane (left)          Docker lane (right)
//  ───────────────          ───────────────────
//  port = dev port          port = INTERNAL container port (inter-service comms)
//                           extPort = host-mapped port (external access)
//
//  Ygg — shared ArcadeDB (HoundArcade external container):
//    IDE: shuttle dev → localhost:2480
//    Docker: shuttle docker → HoundArcade:2480 (internal docker net, no host offset)
//
//  Frigg — HEIMDALL's ArcadeDB:
//    IDE: heimdall-backend dev → localhost:2481 (host-mapped)
//    Docker: heimdall-backend docker → frigg:2480 (internal container port)

const NODES: Node[] = [
  // ── Lane headers ─────────────────────────────────────────────────────────────
  { id: 'lane-dev',    type: 'lane', selectable: false, draggable: false,
    position: { x: 110, y: -55 }, data: { label: 'IDE / Dev', isDocker: false } },
  { id: 'lane-docker', type: 'lane', selectable: false, draggable: false,
    position: { x: 570, y: -55 }, data: { label: 'Docker', isDocker: true } },

  // ── Row 0: Shell ─────────────────────────────────────────────────────────────
  { id: 'shell-dev',    type: 'service', position: { x: 130, y: 0 },
    data: { label: 'Shell',  port: '5175', mode: 'dev', color: 'var(--acc)' } },
  { id: 'shell-docker', type: 'service', position: { x: 590, y: 0 },
    data: { label: 'Shell',  port: '5175', extPort: '25175', mode: 'docker', color: 'var(--acc)' } },

  // ── Row 1: MF remotes ────────────────────────────────────────────────────────
  { id: 'verdandi-dev',    type: 'service', position: { x: 0,   y: 140 },
    data: { label: 'Seiðr Studio', port: '5173', mode: 'dev',    color: 'var(--inf)' } },
  { id: 'verdandi-docker', type: 'service', position: { x: 470, y: 140 },
    data: { label: 'Seiðr Studio', port: '5173', extPort: '15173', mode: 'docker', color: 'var(--inf)' } },
  { id: 'hf-dev',          type: 'service', position: { x: 270, y: 140 },
    data: { label: 'Heimdall UI',  port: '5174', mode: 'dev',    color: 'var(--inf)' } },
  { id: 'hf-docker',       type: 'service', position: { x: 740, y: 140 },
    data: { label: 'Heimdall UI',  port: '5174', extPort: '25174', mode: 'docker', color: 'var(--inf)' } },

  // ── Row 2: BFF + backend ─────────────────────────────────────────────────────
  { id: 'chur-dev',    type: 'service', position: { x: 0,   y: 290 },
    data: { label: 'Chur (BFF)',     port: '3000', mode: 'dev',    color: 'var(--suc)' } },
  { id: 'chur-docker', type: 'service', position: { x: 470, y: 290 },
    data: { label: 'Chur (BFF)',     port: '3000', extPort: '13000', mode: 'docker', color: 'var(--suc)' } },
  { id: 'hb-dev',      type: 'service', position: { x: 270, y: 290 },
    data: { label: 'Heimdall Bk',    port: '9093', mode: 'dev',    color: 'var(--suc)' } },
  { id: 'hb-docker',   type: 'service', position: { x: 740, y: 290 },
    data: { label: 'Heimdall Bk',    port: '9093', extPort: '19093', mode: 'docker', color: 'var(--suc)' } },

  // ── Row 3: Auth + GraphQL ────────────────────────────────────────────────────
  { id: 'keycloak-dev',    type: 'service', position: { x: -80,  y: 430 },
    data: { label: 'Keycloak', port: '8180', mode: 'dev',    color: 'var(--wrn)' } },
  { id: 'keycloak-docker', type: 'service', position: { x: 385,  y: 430 },
    data: { label: 'Keycloak', port: '8180', extPort: '18180', mode: 'docker', color: 'var(--wrn)' } },
  { id: 'shuttle-dev',     type: 'service', position: { x: 130,  y: 430 },
    data: { label: 'Shuttle',  port: '8080', mode: 'dev',    color: 'var(--suc)' } },
  { id: 'shuttle-docker',  type: 'service', position: { x: 600,  y: 430 },
    data: { label: 'Shuttle',  port: '8080', extPort: '18080', mode: 'docker', color: 'var(--suc)' } },

  // ── Row 4: Dali (async PL/SQL parser, port 9090) ─────────────────────────────
  { id: 'dali-dev',    type: 'service', position: { x: 130, y: 550 },
    data: { label: 'Dali (Parser)', port: '9090', mode: 'dev',    color: 'var(--acc)' } },
  { id: 'dali-docker', type: 'service', position: { x: 600, y: 550 },
    data: { label: 'Dali (Parser)', port: '9090', extPort: '19090', mode: 'docker', color: 'var(--acc)' } },

  // ── Row 5: Storage ────────────────────────────────────────────────────────────
  // Ygg = HoundArcade external container, same instance for both lanes
  // IDE: localhost:2480 | Docker: HoundArcade:2480 (internal docker net)
  { id: 'ygg', type: 'service', position: { x: 350, y: 700 },
    data: { label: 'Ygg (HoundArcade)', port: '2480', color: 'var(--t3)' } },

  // Frigg — separate access paths
  // IDE: localhost:2481 (host-mapped from Frigg container)
  // Docker: frigg:2480 (internal container port, no offset inside network)
  { id: 'frigg-dev',    type: 'service', position: { x: 130, y: 700 },
    data: { label: 'Frigg',  port: '2481', mode: 'dev',    color: 'var(--t3)' } },
  { id: 'frigg-docker', type: 'service', position: { x: 600, y: 700 },
    data: { label: 'Frigg',  port: '2480', extPort: '2481', mode: 'docker', color: 'var(--t3)' } },
];

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
  e('cd-kd',    'chur-dev',     'keycloak-dev',    'OAuth2',     'var(--wrn)'),
  e('cd-std',   'chur-dev',     'shuttle-dev',     'GraphQL',    'var(--acc)'),
  e('cd-hbd',   'chur-dev',     'hb-dev',          'events',     'var(--t3)'),
  e('std-ygg',   'shuttle-dev',  'ygg',             'ArcadeDB',   'var(--t3)'),
  e('hbd-fri',   'hb-dev',       'frigg-dev',       'ArcadeDB',   'var(--t3)'),
  e('cd-dali',   'chur-dev',     'dali-dev',        'REST',       'var(--acc)'),
  e('dali-fri',  'dali-dev',     'frigg-dev',       'JobRunr',    'var(--t3)'),

  // ── Docker lane (internal ports, docker network) ──────────────────────────
  e('sk-vk',     'shell-docker',    'verdandi-docker',    'MF',         'var(--inf)'),
  e('sk-hfk',    'shell-docker',    'hf-docker',          'MF',         'var(--inf)'),
  e('vk-ck',     'verdandi-docker', 'chur-docker',        'auth/proxy', 'var(--suc)'),
  e('hfk-hbk',   'hf-docker',      'hb-docker',          'REST/WS',    'var(--suc)', true),
  e('ck-kk',     'chur-docker',     'keycloak-docker',    'OAuth2',     'var(--wrn)'),
  e('ck-stk',    'chur-docker',     'shuttle-docker',     'GraphQL',    'var(--acc)'),
  e('ck-hbk',    'chur-docker',     'hb-docker',          'events',     'var(--t3)'),
  e('stk-ygg',   'shuttle-docker',  'ygg',                'ArcadeDB',   'var(--t3)'),
  e('hbk-frik',  'hb-docker',       'frigg-docker',       'ArcadeDB',   'var(--t3)'),
  e('ck-dalik',  'chur-docker',     'dali-docker',        'REST',       'var(--acc)'),
  e('dalik-frik','dali-docker',     'frigg-docker',       'JobRunr',    'var(--t3)'),
];

// ── Component ─────────────────────────────────────────────────────────────────
export function ServiceTopology() {
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
      }}>
        Service Topology
      </div>
      <div style={{ height: 860 }}>
        <ReactFlow
          defaultNodes={NODES}
          defaultEdges={EDGES}
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
