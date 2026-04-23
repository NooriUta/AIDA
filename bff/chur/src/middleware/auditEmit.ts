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
  | 'seer.audit.tenant_spoof_attempt'
  // MTN-61 user lifecycle events
  | 'seer.audit.user_soft_deleted'
  | 'seer.audit.user_restored'
  | 'seer.audit.user_legal_hold_set'
  // MTN-12 feature flags
  | 'seer.audit.tenant_feature_flags_updated'
  // tenant config general update
  | 'seer.audit.tenant_config_updated';

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
