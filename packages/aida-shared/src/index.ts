export type UserRole =
  | 'viewer'
  | 'editor'
  | 'operator'
  | 'auditor'
  | 'local-admin'
  | 'tenant-owner'
  | 'admin'
  | 'super-admin';

export interface AuthUser {
  id:                 string;
  username:           string;
  role:               UserRole;
  email?:             string;
  firstName?:         string;
  lastName?:          string;
  activeTenantAlias?: string;
}

export type EventLevel = 'INFO' | 'WARN' | 'ERROR';

export interface HeimdallEvent {
  timestamp: number;
  sourceComponent: string;
  eventType: string;
  level: EventLevel;
  sessionId: string | null;
  userId: string | null;
  correlationId: string | null;
  durationMs: number;
  payload: Record<string, unknown>;
}

export interface MetricsSnapshot {
  atomsExtracted: number;
  filesParsed: number;
  toolCallsTotal: number;
  activeWorkers: number;
  queueDepth: number;
  resolutionRate: number; // NaN если filesParsed === 0
}

export interface SnapshotInfo {
  id: string;
  name: string;
  timestamp: number;
  eventCount: number;
}

export interface EventFilter {
  component?: string;
  sessionId?: string;
  level?: EventLevel;
  type?: string;
}

export interface ShellStore {
  currentApp: string;
  navigateTo(app: string, context?: AppContext): void;
}

// ADR-DA-013: стандартный набор URL-контекстных параметров
export interface AppContext {
  nodeId?: string;    // ArcadeDB geoid: "DaliTable:prod.orders"
  schema?: string;
  returnTo?: string;  // путь возврата
  highlight?: string;
  sessionId?: string;
}

// Утилиты ADR-DA-013 (экспортируются из aida-shared)
export function buildAppUrl(app: string, context?: AppContext): string {
  const base = `/${app}`;
  if (!context) return base;
  const params = new URLSearchParams();
  if (context.nodeId)    params.set('nodeId',    context.nodeId);
  if (context.schema)    params.set('schema',    context.schema);
  if (context.returnTo)  params.set('returnTo',  context.returnTo);
  if (context.highlight) params.set('highlight', context.highlight);
  if (context.sessionId) params.set('sessionId', context.sessionId);
  const qs = params.toString();
  return qs ? `${base}?${qs}` : base;
}

export function useAppContext(): AppContext {
  // Reads current URL search params.
  // Реализуется в каждом remote через useSearchParams() из react-router-dom.
  return {} as AppContext; // placeholder
}

export { sharedPrefsStore, applyPrefs, initSharedPrefs } from './stores/sharedPrefsStore';
export type { SharedPrefs } from './stores/sharedPrefsStore';
