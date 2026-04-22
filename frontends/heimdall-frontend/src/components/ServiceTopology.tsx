import {
  ReactFlow, Background, Controls,
  type Node, type Edge, MarkerType,
  Handle, Position, useNodesState,
} from '@xyflow/react';
import { useEffect, useMemo } from 'react';
import '@xyflow/react/dist/style.css';
import { SERVICES, STORAGE, type ServiceSpec, type TopologyLayer } from '../config/services';

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

// ── Nodes ──────────────────────────────────────────────────────────────────────
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
        minWidth:     118,
        textAlign:    'center',
        cursor:       openUrl ? 'pointer' : 'default',
        boxShadow: data.statusColor
          ? `0 0 6px color-mix(in srgb, ${data.statusColor} 40%, transparent)`
          : undefined,
        transition: 'border-color 0.4s, box-shadow 0.4s',
      }}>
      <Handle id="t" type="target" position={Position.Top}    style={{ background: 'var(--bd)' }} />
      <Handle id="l" type="target" position={Position.Left}   style={{ background: 'var(--bd)' }} />

      <div style={{
        fontSize: '11px', fontWeight: 600, color: 'var(--t1)',
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4,
      }}>
        {data.isDocker && !data.isStorage && <DockerIcon size={10} />}
        {data.label}
      </div>

      {data.statusColor && (
        <div style={{
          width: 5, height: 5, borderRadius: '50%',
          background: data.statusColor,
          margin: '2px auto 0',
        }} />
      )}

      {data.port && (
        <div style={{
          fontSize: '10px', fontFamily: 'var(--mono)',
          color: data.isStorage ? 'var(--t3)' : '#2496ED',
          marginTop: data.statusColor ? 1 : 2,
        }}>
          :{data.port}
        </div>
      )}

      <Handle id="b" type="source" position={Position.Bottom} style={{ background: 'var(--bd)' }} />
      <Handle id="r" type="source" position={Position.Right}  style={{ background: 'var(--bd)' }} />
    </div>
  );
}

const NODE_TYPES = { service: ServiceNode };

// ── Auto-layout (horizontal) ───────────────────────────────────────────────────
// Layers flow left-to-right: L0 (nginx edge) → L1 (shell) → … → L6 (storage).
// Within each layer services stack vertically, centred around CENTER_Y.
const COL_W    = 190;   // distance between layers (horizontal step)
const ROW_H    = 95;    // distance between services in the same layer
const CENTER_Y = 320;   // vertical centre for each column

/** Build nodes from SERVICES (docker-mode) + STORAGE. Layer ⇒ x; index within layer ⇒ y. */
function buildNodes(): Node[] {
  // Group by layer
  const byLayer = new Map<TopologyLayer, Array<{ id: string; label: string; port?: number; extPort?: number; color?: string; isStorage?: boolean; isDocker?: boolean }>>();
  const push = (L: TopologyLayer, n: { id: string; label: string; port?: number; extPort?: number; color?: string; isStorage?: boolean; isDocker?: boolean }) => {
    const arr = byLayer.get(L) ?? [];
    arr.push(n);
    byLayer.set(L, arr);
  };

  for (const svc of SERVICES) {
    if (svc.layer == null) continue;            // undefined — not topology-visible
    if (!svc.portDocker) continue;               // docker-only topology
    push(svc.layer, {
      id: svc.id, label: svc.label,
      port: svc.portDocker, extPort: svc.portDocker,
      color: svc.color, isDocker: true,
    });
  }
  for (const s of STORAGE) {
    push(s.layer, { id: s.id, label: s.label, port: s.portExt, color: s.color, isStorage: true });
  }

  const nodes: Node[] = [];
  for (const [layer, items] of byLayer) {
    const n = items.length;
    const startY = CENTER_Y - ((n - 1) * ROW_H) / 2;
    items.forEach((item, idx) => {
      nodes.push({
        id:       item.id,
        type:     'service',
        position: { x: layer * COL_W, y: startY + idx * ROW_H },
        data: {
          label:     item.label,
          port:      item.port,
          extPort:   item.extPort,
          color:     item.color,
          isStorage: item.isStorage,
          isDocker:  item.isDocker,
        },
      });
    });
  }
  return nodes;
}

/** Derive edges from `deps` + `depLabels` on each ServiceSpec. */
function buildEdges(): Edge[] {
  const edges: Edge[] = [];
  const allIds = new Set<string>([
    ...SERVICES.filter(s => s.portDocker).map(s => s.id),
    ...STORAGE.map(s => s.id),
  ]);

  const edgeColor = (source: ServiceSpec, _targetId: string): string => {
    // Colour by source service accent (fallback to border)
    return source.color ?? 'var(--bd)';
  };

  for (const svc of SERVICES) {
    if (!svc.deps) continue;
    if (!svc.portDocker && !allIds.has(svc.id)) continue;
    svc.deps.forEach((dep, i) => {
      if (!allIds.has(dep)) return;
      const label = svc.depLabels?.[i] ?? '→';
      const color = edgeColor(svc, dep);
      edges.push({
        id:        `${svc.id}->${dep}`,
        source:    svc.id,
        target:    dep,
        label,
        markerEnd:    { type: MarkerType.ArrowClosed, color },
        style:        { stroke: color, strokeWidth: 1.5 },
        labelStyle:   { fill: 'var(--t3)', fontSize: 9, fontFamily: 'var(--mono)' },
        labelBgStyle: { fill: 'var(--bg0)', opacity: 0.85 },
      });
    });
  }
  return edges;
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

// ── Component ─────────────────────────────────────────────────────────────────
export function ServiceTopology({ serviceStatuses = [] }: { serviceStatuses?: ServiceHealth[] }) {
  const statusLookup = useMemo(() => {
    const m = new Map<string, ServiceHealth['status']>();
    for (const s of serviceStatuses) m.set(s.name, s.status);
    return m;
  }, [serviceStatuses]);

  const initialNodes = useMemo(() => buildNodes(), []);
  const edges        = useMemo(() => buildEdges(), []);

  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);

  useEffect(() => {
    setNodes(prev => prev.map(n => {
      const status = statusLookup.get(n.id);
      const hc = healthColor(status);
      const prevHc = (n.data as ServiceNodeData).statusColor;
      if (hc === prevHc) return n;
      return { ...n, data: { ...n.data, statusColor: hc } };
    }));
  }, [statusLookup, setNodes]);

  // Canvas height — horizontal layout ⇒ depends on the tallest column
  // (layer with most services), not the number of layers.
  const layerCounts = new Map<number, number>();
  for (const s of SERVICES) if (s.portDocker && s.layer != null)
    layerCounts.set(s.layer, (layerCounts.get(s.layer) ?? 0) + 1);
  for (const s of STORAGE)
    layerCounts.set(s.layer, (layerCounts.get(s.layer) ?? 0) + 1);
  const maxCol = Math.max(...layerCounts.values(), 1);
  const canvasHeight = Math.max(420, maxCol * ROW_H + 180);

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
          · derived from config/services.ts
        </span>
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
      <div style={{ height: canvasHeight }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
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

