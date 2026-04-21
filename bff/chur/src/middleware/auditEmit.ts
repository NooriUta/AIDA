/**
 * CAP-11: Tenant audit event emission via Heimdall.
 * Fire-and-forget — never throws.
 */
import { emitToHeimdall } from './heimdallEmit';

export type TenantAuditEvent =
  | 'seer.audit.tenant_created'
  | 'seer.audit.tenant_suspended'
  | 'seer.audit.tenant_archived'
  | 'seer.audit.tenant_restored'
  | 'seer.audit.tenant_purged'
  | 'seer.audit.member_added'
  | 'seer.audit.member_removed'
  | 'seer.audit.member_role_changed'
  | 'seer.audit.tenant_spoof_attempt';

export function emitTenantAudit(
  event:       TenantAuditEvent,
  actor:       string,
  tenantAlias: string,
  extra?:      Record<string, unknown>,
  sessionId?:  string,
): void {
  emitToHeimdall(event, 'INFO', { actor, tenantAlias, ...extra }, sessionId);
}

export function emitSpoofAttempt(
  actor:          string,
  headerAlias:    string,
  jwtAlias:       string,
  sessionId?:     string,
): void {
  emitToHeimdall('seer.audit.tenant_spoof_attempt', 'WARN', {
    actor,
    headerAlias,
    jwtAlias,
  }, sessionId);
}
