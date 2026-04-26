import {
  ReactFlow, Background, Controls,
  type Node, type Edge, MarkerType,
  Handle, Position, useNodesState, useReactFlow, ReactFlowProvider,
} from '@xyflow/react';
import { useEffect, useMemo, useState } from 'react';
import ELK, { type ELK as ELKApi } from 'elkjs/lib/elk.bundled.js';
import elkWorkerUrl from 'elkjs/lib/elk-worker.min.js?url';
import '@xyflow/react/dist/style.css';
import { SERVICES, STORAGE, type ServiceSpec } from '../config/services';

// ── Docker SVG icon ────────────────────────────────────────────────────────────
function DockerIcon({ size = 10 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="#2496ED" aria-hidden="true">
      <path d="M13.983 11.078h2.119a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.119a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185m-2.954-5.43h2.118a.186.186 0 0 0 .186-.186V3.574a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m0 2.716h2.118a.187.187 0 0 0 .186-.186V6.29a.186.186 0 0 0-.186-.185h-2.118a.185.185 0 0 0-.185.185v1.887c0 .102.082.186.185.186m-2.93 0h2.12a.186.186 0 0 0 .184-.186V6.29a.185.185 0 0 0-.185-.185H8.1a.185.185 0 0 0-.185.185v1.887c0 .102.083.186.185.186m-2.964 0h2.119a.186.186 0 0 0 .185-.186V6.29a.185.185 0 0 0-.185-.185H5.136a.186.186 0 0 0-.186.185v1.887c0 .102.084.186.186.186m5.893 2.715h2.118a.186.186 0 0 0 .186-.185V9.006a.186.186 0 0 0-.186-.186h-2.118a.185.185 0 0 0-.185.185v1.888c0 .102.082.185.185.185m-2.93 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.184.185v1.888c0 .102.083.185.185.185m-2.964 0h2.119a.185.185 0 0 0 .185-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.186.186 0 0 0-.186.185v1.888c0 .102.084.185.186.185m-2.92 0h2.12a.185.185 0 0 0 .184-.185V9.006a.185.185 0 0 0-.184-.186h-2.12a.185.185 0 0 0-.185.185v1.888c0 .102.083.185.185.185M23.763 9.89c-.065-.051-.672-.51-1.954-.51-.338.001-.676.03-1.01.087-.248-1.7-1.653-2.53-1.716-2.566l-.344-.199-.226.327c-.284.438-.49.922-.612 1.43-.23.97-.09 1.882.403 2.661-.595.332-1.55.413-1.744.42H.751a.751.751 0 0 0-.75.748 11.376 11.376 0 0 0 .692 4.062c.545 1.428 1.355 2.48 2.41 3.124 1.18.723 3.1 1.137 5.275 1.137.983.003 1.963-.086 2.93-.266a12.248 12.248 0 0 0 3.823-1.389c.98-.567 1.86-1.288 2.61-2.136 1.252-1.418 1.998-2.997 2.553-4.4h.221c1.372 0 2.215-.549 2.68-1.009.309-.293.55-.65.707-1.046l.098-.288Z"/>
    </svg>
  );
}

// ── Types ─────────────────────────────────────────────────────────────────────
export interface ServiceHealth {
  name:   string;
  mode:   'dev' | 'docker';
  status: 'up' | 'degraded' | 'down' | 'self';
}

interface ServiceNodeData {
  label:        string;
  port?:        number;
  extPort?:     number;
  color?:       string;
  statusColor?: string;
  isStorage?:   boolean;
  isDocker?:    boolean;
  [key: string]: unknown;
}

// ── Node component ────────────────────────────────────────────────────────────
// Handles on left (target) and right (source) — matches ELK direction = RIGHT.
function ServiceNode({ data }: { data: ServiceNodeData }) {
  const borderColor = data.statusColor ?? data.color ?? 'var(--bd)';
  const openUrl = data.extPort ? `http://localhost:${data.extPort}` : undefined;

  return (
    <div
      onDoubleClick={() => openUrl && window.open(openUrl, '_blank', 'noopener,noreferrer')}
      title={openUrl ? `Double-click to open ${openUrl}` : undefined}
      style={{
        background:   'var(--bg1)',
        border:       `1px solid ${borderColor}`,
        borderRadius: 'var(--seer-radius-md)',
        padding:      '5px 10px',
        width:        NODE_W,
        height:       NODE_H,
        textAlign:    'center',
        cursor:       openUrl ? 'pointer' : 'default',
        boxShadow: data.statusColor
          ? `0 0 6px color-mix(in srgb, ${data.statusColor} 40%, transparent)`
          : undefined,
        transition: 'border-color 0.4s, box-shadow 0.4s',
        display:        'flex',
        flexDirection:  'column',
        justifyContent: 'center',
        alignItems:     'center',
        gap:            2,
      }}>
      <Handle id="t" type="target" position={Position.Left}  style={{ background: 'var(--bd)' }} />

      <div style={{
        fontSize: '11px', fontWeight: 600, color: 'var(--t1)',
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4,
        whiteSpace: 'nowrap',
      }}>
        {data.isDocker && !data.isStorage && <DockerIcon size={10} />}
        {data.label}
      </div>

      {data.statusColor && (
        <div style={{
          width: 5, height: 5, borderRadius: '50%',
          background: data.statusColor,
        }} />
      )}

      {data.port && (
        <div style={{
          fontSize: '10px', fontFamily: 'var(--mono)',
          color: data.isStorage ? 'var(--t3)' : '#2496ED',
        }}>
          :{data.port}
        </div>
      )}

      <Handle id="b" type="source" position={Position.Right} style={{ background: 'var(--bd)' }} />
    </div>
  );
}

const NODE_TYPES = { service: ServiceNode };

// ── Layout constants ──────────────────────────────────────────────────────────
const NODE_W = 130;   // node width fed to ELK
const NODE_H = 60;    // node height fed to ELK

// ── ELK setup (singleton) ─────────────────────────────────────────────────────
// Bundled build; Worker hosted via Vite's ?url import. Same-origin in dev
// (heimdall-frontend serves at :5174); MF/cross-origin scenarios re-fetch
// the script via blob URL — but for this small static graph we never run
// inside an MF remote, so the simple direct-URL workerFactory suffices.
let _elk: ELKApi | null = null;
function getElk(): ELKApi {
  if (_elk) return _elk;
  _elk = new ELK({
    workerFactory: (_url?: string) => new Worker(elkWorkerUrl),
  });
  return _elk;
}

// ── Build raw node + edge spec from SERVICES + STORAGE ────────────────────────
interface RawNode {
  id: string; label: string; port?: number; extPort?: number;
  color?: string; isStorage?: boolean; isDocker?: boolean;
  layer: number;
}

function buildRaw(): { nodes: RawNode[]; edges: Edge[] } {
  const nodes: RawNode[] = [];
  const allIds = new Set<string>();

  for (const svc of SERVICES) {
    if (svc.layer == null || !svc.portDocker) continue;
    nodes.push({
      id: svc.id, label: svc.label,
      port: svc.portDocker, extPort: svc.portDocker,
      color: svc.color, isDocker: true, layer: svc.layer,
    });
    allIds.add(svc.id);
  }
  for (const s of STORAGE) {
    nodes.push({
      id: s.id, label: s.label,
      port: s.portExt, color: s.color, isStorage: true, layer: s.layer,
    });
    allIds.add(s.id);
  }

  const edges: Edge[] = [];
  const edgeColor = (src: ServiceSpec): string => src.color ?? 'var(--bd)';
  for (const svc of SERVICES) {
    if (!svc.deps || !allIds.has(svc.id)) continue;
    svc.deps.forEach((dep, i) => {
      if (!allIds.has(dep)) return;
      const label = svc.depLabels?.[i] ?? '→';
      const color = edgeColor(svc);
      edges.push({
        id:           `${svc.id}->${dep}`,
        source:       svc.id,
        target:       dep,
        label,
        markerEnd:    { type: MarkerType.ArrowClosed, color },
        style:        { stroke: color, strokeWidth: 1.5 },
        labelStyle:   { fill: 'var(--t3)', fontSize: 9, fontFamily: 'var(--mono)' },
        labelBgStyle: { fill: 'var(--bg0)', opacity: 0.85 },
      });
    });
  }
  return { nodes, edges };
}

// ── Run ELK layered DOWN with explicit layer constraints ──────────────────────
// Each service has a fixed layer (0 = nginx edge, 6 = storage). We feed those
// to ELK via `layerChoiceConstraint` so the L0..L6 stacking is preserved
// regardless of edge directions; ELK only needs to decide horizontal order
// within each layer to minimize crossings.
async function runLayout(raw: ReturnType<typeof buildRaw>): Promise<{
  nodes:  Node[];
  width:  number;
  height: number;
}> {
  const elkGraph = {
    id: 'root',
    layoutOptions: {
      'elk.algorithm':                             'layered',
      'elk.direction':                             'RIGHT',
      'elk.layered.spacing.nodeNodeBetweenLayers': '50',
      'elk.spacing.nodeNode':                      '34',
      'elk.layered.crossingMinimization.strategy': 'LAYER_SWEEP',
      'elk.layered.nodePlacement.strategy':        'BRANDES_KOEPF',
      'elk.layered.unnecessaryBendpoints':         'false',
      'elk.padding':                               '[top=12,left=12,bottom=12,right=12]',
    },
    children: raw.nodes.map(n => ({
      id:     n.id,
      width:  NODE_W,
      height: NODE_H,
      layoutOptions: {
        'elk.layered.layering.layerChoiceConstraint': String(n.layer),
      },
    })),
    edges: raw.edges.map(e => ({
      id:      e.id,
      sources: [e.source],
      targets: [e.target],
    })),
  };

  const result = await getElk().layout(elkGraph);
  const posMap = new Map<string, { x: number; y: number }>();
  for (const c of result.children ?? []) {
    posMap.set(c.id, { x: c.x ?? 0, y: c.y ?? 0 });
  }

  const nodes: Node[] = raw.nodes.map(n => ({
    id:       n.id,
    type:     'service',
    position: posMap.get(n.id) ?? { x: 0, y: 0 },
    data: {
      label:     n.label,
      port:      n.port,
      extPort:   n.extPort,
      color:     n.color,
      isStorage: n.isStorage,
      isDocker:  n.isDocker,
    },
  }));

  const r = result as unknown as { width?: number; height?: number };
  return {
    nodes,
    width:  Math.ceil(r.width  ?? 0),
    height: Math.ceil(r.height ?? 0),
  };
}

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

// ── Inner — assumes ReactFlowProvider context ─────────────────────────────────
function ServiceTopologyInner({ serviceStatuses }: { serviceStatuses: ServiceHealth[] }) {
  const [collapsed, setCollapsed] = useState(true);
  const raw = useMemo(buildRaw, []);

  // ELK runs once on mount; layout is static (driven by services.ts config).
  const [layout, setLayout] = useState<{ nodes: Node[]; width: number; height: number } | null>(null);
  const [layoutErr, setLayoutErr] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    runLayout(raw)
      .then(res => { if (!cancelled) setLayout(res); })
      .catch(err => { if (!cancelled) setLayoutErr(err instanceof Error ? err.message : String(err)); });
    return () => { cancelled = true; };
  }, [raw]);

  const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
  const { fitView } = useReactFlow();

  // Apply ELK output to ReactFlow state + re-fit viewport.
  useEffect(() => {
    if (!layout) return;
    setNodes(layout.nodes);
    requestAnimationFrame(() => fitView({ padding: 0.08, duration: 250 }));
  }, [layout, setNodes, fitView]);

  // Live health colour updates — touch only `data.statusColor`, preserve positions.
  const statusLookup = useMemo(() => {
    const m = new Map<string, ServiceHealth['status']>();
    for (const s of serviceStatuses) m.set(s.name, s.status);
    return m;
  }, [serviceStatuses]);

  useEffect(() => {
    setNodes(prev => prev.map(n => {
      const status = statusLookup.get(n.id);
      const hc = healthColor(status);
      const prevHc = (n.data as ServiceNodeData).statusColor;
      if (hc === prevHc) return n;
      return { ...n, data: { ...n.data, statusColor: hc } };
    }));
  }, [statusLookup, setNodes]);

  const edges = useMemo(() => buildRaw().edges, []);

  // Canvas height — derive from ELK output, clamped to a sensible band.
  const canvasHeight = layout ? Math.max(360, Math.min(640, layout.height + 60)) : 420;

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
        <span style={{ textTransform: 'none', fontFamily: 'var(--mono)', color: 'var(--t3)', opacity: 0.7 }}>
          · ELK layered · derived from config/services.ts
        </span>
        {layoutErr && (
          <span style={{ textTransform: 'none', color: 'var(--danger)', fontFamily: 'var(--mono)' }}>
            layout: {layoutErr}
          </span>
        )}
        <span style={{ display: 'flex', alignItems: 'center', gap: 6, marginLeft: 'auto' }}>
          {!collapsed && serviceStatuses.length > 0 && (
            <>
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
            </>
          )}
          <button
            onClick={() => setCollapsed(c => !c)}
            style={{
              background: 'none', border: '1px solid var(--bd)', borderRadius: 3,
              cursor: 'pointer', color: 'var(--t2)', fontSize: 10,
              padding: '1px 6px', fontFamily: 'var(--mono)', lineHeight: '16px',
              textTransform: 'none', letterSpacing: 0,
            }}
          >
            {collapsed ? '▶ expand' : '▼ collapse'}
          </button>
        </span>
      </div>
      {!collapsed && <div style={{ height: canvasHeight }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          nodeTypes={NODE_TYPES}
          fitView
          fitViewOptions={{ padding: 0.08 }}
          proOptions={{ hideAttribution: true }}
          style={{ background: 'var(--bg0)' }}
        >
          <Background color="var(--bd)" gap={20} />
          <Controls style={{ background: 'var(--bg1)', border: '1px solid var(--bd)' }} />
        </ReactFlow>
      </div>}
    </div>
  );
}

// ── Public component — wraps inner in ReactFlowProvider so useReactFlow works ─
export function ServiceTopology({ serviceStatuses = [] }: { serviceStatuses?: ServiceHealth[] }) {
  return (
    <ReactFlowProvider>
      <ServiceTopologyInner serviceStatuses={serviceStatuses} />
    </ReactFlowProvider>
  );
}
