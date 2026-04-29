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
  AUTH_LOGIN_SUCCESS:     'Login',
  AUTH_LOGIN_FAILED:      'Login failed',
  AUTH_LOGOUT:            'Logout',
  DEMO_RESET:             'Demo reset',
  LOOM_NODE_SELECTED:     'Node selected',
  LOOM_VIEW_SLOW:         'Slow render',
  CYPHER_QUERY_SLOW:      'Slow YGG query',
};

/** Fields rendered in dedicated columns — exclude from payload summary to avoid duplication. */
const DEDICATED_COLUMNS = new Set(['tenantAlias', 'db']);

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
    case 'REQUEST_COMPLETED': {
      // Show full call signature + db pointer. Example:
      // [hound_acme] exploreRoutineAggregate(scope=schema-PROD) → 42 nodes, 18 edges  120ms
      const call = p['call'] as string | undefined;
      const db   = p['db']   as string | undefined;
      const op   = call ?? (p['op'] as string | undefined) ?? 'query';
      const dur  = event.durationMs > 0 ? ` ${event.durationMs}ms` : '';
      return db ? `[${db}] ${op}${dur}` : `${op}${dur}`;
    }
    case 'AUTH_LOGIN_SUCCESS':
      return `${p['username'] ?? ''} (${p['role'] ?? ''})`;
    case 'AUTH_LOGIN_FAILED':
      return `${p['username'] ?? 'unknown'} · invalid credentials`;
    case 'AUTH_LOGOUT':
      return `${p['username'] ?? ''}`;
    case 'LOOM_NODE_SELECTED':
      return `${p['nodeType'] ?? ''} ${p['nodeLabel'] ?? ''} @ ${p['viewLevel'] ?? ''}`;
    case 'LOOM_VIEW_SLOW': {
      const nc = p['nodeCount'] ?? 0;
      const ec = p['edgeCount'] ?? 0;
      return `${nc} nodes · ${ec} edges · ${event.durationMs}ms`;
    }
    case 'CYPHER_QUERY_SLOW': {
      const lang = p['language'] === 'cypher' ? 'Cypher' : 'SQL';
      const q    = (p['query'] as string | undefined) ?? '';
      const preview = q.length > 80 ? q.substring(0, 80) + '…' : q;
      return `[${p['db'] ?? '?'}] ${lang} ${event.durationMs}ms — ${preview}`;
    }
    default: {
      // Skip fields that have their own dedicated table columns (tenantAlias, db)
      const keys = Object.keys(p)
        .filter(k => !DEDICATED_COLUMNS.has(k))
        .slice(0, 3);
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
