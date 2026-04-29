import type { HeimdallEvent } from 'aida-shared';

export const EVENT_LABELS: Record<string, string> = {
  FILE_PARSING_STARTED:   'Parsing started',
  FILE_PARSING_COMPLETED: 'Parsing completed',
  FILE_PARSING_FAILED:    'Parsing failed',
  PARSE_ERROR:            'Parse error',
  PARSE_WARNING:          'Grammar notice',
  ATOM_EXTRACTED:         'Atoms extracted',
  RESOLUTION_COMPLETED:   'Names resolved',
  SESSION_STARTED:        'Session started',
  SESSION_COMPLETED:      'Session completed',
  WORKER_ASSIGNED:        'Worker assigned',
  JOB_COMPLETED:          'Job completed',
  QUERY_RECEIVED:         'Query received',
  TOOL_CALL_STARTED:      'Tool call',
  TOOL_CALL_COMPLETED:    'Tool completed',
  LLM_RESPONSE_READY:     'LLM response',
  TRAVERSAL_STARTED:      'Traversal started',
  TRAVERSAL_COMPLETED:    'Traversal done',
  REQUEST_RECEIVED:       'GraphQL request',
  REQUEST_COMPLETED:      'GraphQL done',
  SUBSCRIPTION_OPENED:    'Subscription',
  // EV-02/03: YGG write events
  YGG_WRITE_COMPLETED:    'YGG write done',
  YGG_WRITE_FAILED:       'YGG write failed',
  YGG_CLEAR_COMPLETED:    'YGG cleared',
  // EV-04/05: Shuttle performance + DB health
  CYPHER_QUERY_SLOW:      'Slow query',
  DB_CONNECTION_ERROR:    'DB error',
  // EV-06: Dali source config
  SOURCE_CREATED:         'Source added',
  SOURCE_DELETED:         'Source removed',
  // EV-09: LOOM UX
  LOOM_NODE_SELECTED:     'Node selected',
  LOOM_VIEW_LOADED:       'LOOM loaded',
  LOOM_VIEW_SLOW:         'LOOM slow render',
  // EV-10: Auth audit
  AUTH_LOGIN:             'Login',
  AUTH_LOGIN_FAILED:      'Login failed',
  AUTH_LOGOUT:            'Logout',
  RATE_LIMIT_EXCEEDED:    'Rate limit hit',
  DEMO_RESET:             'Demo reset',
};

export function formatPayload(event: HeimdallEvent): string {
  const p = event.payload ?? {};
  switch (event.eventType) {
    case 'SESSION_STARTED': {
      const thr = p['threads'] != null ? ` threads:${p['threads']}` : '';
      const prev = p['preview'] ? ' preview' : '';
      return `${p['dialect'] ?? ''} ${p['source'] ?? ''}${thr}${prev}`;
    }
    case 'FILE_PARSING_STARTED':
      return `file:"${p['file'] ?? ''}"`;
    case 'FILE_PARSING_COMPLETED':
      return `file:"${p['file'] ?? ''}" atoms:${p['atomCount'] ?? 0} ${event.durationMs}ms`;
    case 'FILE_PARSING_FAILED':
      return `file:"${p['file'] ?? ''}" error:${p['error'] ?? 'unknown'}`;
    case 'PARSE_ERROR':
    case 'PARSE_WARNING':
      return `${p['file'] ?? ''} line ${p['line'] ?? '?'}:${p['col'] ?? '?'} — ${p['msg'] ?? ''}`;
    case 'ATOM_EXTRACTED':
      return `${p['atomCount'] ?? 0} atoms · ${p['file'] ?? ''}`;
    case 'RESOLUTION_COMPLETED': {
      const rate = Number(p['resolutionRate'] ?? 0);
      return `${p['resolved'] ?? 0}/${p['total'] ?? 0} (${Math.round(rate * 100)}%)`;
    }
    case 'TOOL_CALL_COMPLETED':
      return `tool:"${p['tool'] ?? ''}" nodes:${p['nodes'] ?? 0} ${event.durationMs}ms`;
    case 'TRAVERSAL_COMPLETED':
      return `nodes:${p['nodes'] ?? 0} edges:${p['edges'] ?? 0} ${event.durationMs}ms`;
    case 'LLM_RESPONSE_READY':
      return `${p['tokens_in'] ?? 0}→${p['tokens_out'] ?? 0} tokens ${event.durationMs}ms`;
    case 'REQUEST_RECEIVED':
    case 'REQUEST_COMPLETED':
      return `${p['op'] ?? 'query'} ${event.durationMs > 0 ? `${event.durationMs}ms` : ''}`.trim();
    case 'YGG_WRITE_COMPLETED':
      return `v:${p['vertices_written'] ?? 0} e:${p['edges_written'] ?? 0} ${event.durationMs}ms`;
    case 'YGG_WRITE_FAILED':
      return `error:${p['error_code'] ?? 'unknown'}`;
    case 'YGG_CLEAR_COMPLETED':
      return `cleared in ${p['duration_ms'] ?? event.durationMs}ms`;
    case 'CYPHER_QUERY_SLOW':
      return `${p['query_type'] ?? 'query'} ${p['duration_ms'] ?? event.durationMs}ms > ${p['threshold_ms'] ?? 500}ms`;
    case 'DB_CONNECTION_ERROR':
      return `db:${p['db'] ?? '?'} host:${p['host'] ?? '?'} ${p['error'] ?? ''}`;
    case 'SOURCE_CREATED':
    case 'SOURCE_DELETED':
      return `id:${p['source_id'] ?? '?'} dialect:${p['dialect'] ?? '?'}`;
    case 'LOOM_NODE_SELECTED':
      return `${p['node_type'] ?? '?'} id:${p['node_id'] ?? '?'}`;
    case 'LOOM_VIEW_SLOW':
      return `${p['nodes_count'] ?? 0} nodes ${p['render_time_ms'] ?? 0}ms`;
    case 'RATE_LIMIT_EXCEEDED':
      return `${p['endpoint'] ?? '?'} ip:${p['ip'] ?? '?'}`;
    case 'AUTH_LOGIN':
      return `${p['username'] ?? ''} (${p['role'] ?? ''})`;
    case 'AUTH_LOGIN_FAILED':
      return `${p['username'] ?? 'unknown'} · invalid credentials`;
    case 'AUTH_LOGOUT':
      return `${p['username'] ?? ''}`;
    default: {
      const keys = Object.keys(p).slice(0, 3);
      return keys.length
        ? keys.map(k => `${k}:${JSON.stringify(p[k])}`).join(' ')
        : '—';
    }
  }
}

export function levelClass(level: string): string {
  switch (level) {
    case 'INFO':  return 'badge-info';
    case 'WARN':  return 'badge-warn';
    case 'ERROR': return 'badge-err';
    default:      return 'badge-neutral';
  }
}
