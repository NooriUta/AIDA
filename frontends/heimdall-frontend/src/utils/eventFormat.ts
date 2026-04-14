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
};

export function formatPayload(event: HeimdallEvent): string {
  const p = event.payload ?? {};
  switch (event.eventType) {
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
    case 'AUTH_LOGIN_SUCCESS':
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
