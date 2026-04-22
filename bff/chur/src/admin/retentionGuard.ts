/**
 * MTN-42 — Retention-downgrade attack defense (legal hold bypass).
 *
 * Scenario: superadmin lowers `dataRetentionUntil` to a past timestamp on a
 * tenant with active `legalHoldUntil`. Next hard-delete scheduler run purges
 * data that was supposed to be preserved by the hold. Admin denies ever
 * shortening retention — audit trail didn't capture before/after.
 *
 * Defense:
 *   1. Block UPDATE that lowers `dataRetentionUntil` below `legalHoldUntil`
 *      (whether the hold was set before or after).
 *   2. Block UPDATE that lowers `archiveRetentionUntil` below its current
 *      value unless superadmin supplies explicit `--force-downgrade` flag
 *      (captured in audit).
 *   3. Audit every retention change with before/after JSON.
 *
 * This helper is a pure function — call from route handlers after reading
 * current state and before issuing UPDATE.
 */

export interface RetentionChange {
  currentDataRetentionUntil:    number | null;
  currentArchiveRetentionUntil: number | null;
  currentLegalHoldUntil:        number | null;
  /** Proposed new value for dataRetentionUntil (null = no change). */
  newDataRetentionUntil?:       number | null;
  /** Proposed new value for archiveRetentionUntil (null = no change). */
  newArchiveRetentionUntil?:    number | null;
  /** Explicit superadmin override to allow archive-retention downgrade. */
  forceDowngrade?:              boolean;
}

export type RetentionCheck =
  | { ok: true }
  | { ok: false; status: 403; error: string; details: Record<string, unknown> };

export function checkRetentionChange(chg: RetentionChange): RetentionCheck {
  // 1. Legal hold floor for dataRetentionUntil
  if (chg.newDataRetentionUntil !== undefined && chg.newDataRetentionUntil !== null) {
    if (chg.currentLegalHoldUntil !== null
        && chg.currentLegalHoldUntil !== undefined
        && chg.newDataRetentionUntil < chg.currentLegalHoldUntil) {
      return {
        ok: false, status: 403,
        error: 'retention_below_legal_hold',
        details: {
          legalHoldUntil:            chg.currentLegalHoldUntil,
          requestedDataRetentionUntil: chg.newDataRetentionUntil,
          hint: 'dataRetentionUntil cannot be set earlier than legalHoldUntil',
        },
      };
    }
  }
  // 2. Archive retention downgrade (unless force-downgrade)
  if (chg.newArchiveRetentionUntil !== undefined && chg.newArchiveRetentionUntil !== null) {
    if (chg.currentArchiveRetentionUntil !== null
        && chg.currentArchiveRetentionUntil !== undefined
        && chg.newArchiveRetentionUntil < chg.currentArchiveRetentionUntil
        && !chg.forceDowngrade) {
      return {
        ok: false, status: 403,
        error: 'archive_retention_downgrade_blocked',
        details: {
          current:   chg.currentArchiveRetentionUntil,
          requested: chg.newArchiveRetentionUntil,
          hint: 'archiveRetentionUntil can only be lowered with forceDowngrade=true (superadmin). Change is audited.',
        },
      };
    }
  }
  return { ok: true };
}

/** Build an audit-safe before/after record for retention changes. */
export function retentionChangeAudit(chg: RetentionChange): Record<string, unknown> {
  return {
    before: {
      dataRetentionUntil:    chg.currentDataRetentionUntil,
      archiveRetentionUntil: chg.currentArchiveRetentionUntil,
      legalHoldUntil:        chg.currentLegalHoldUntil,
    },
    after: {
      dataRetentionUntil:    chg.newDataRetentionUntil    ?? chg.currentDataRetentionUntil,
      archiveRetentionUntil: chg.newArchiveRetentionUntil ?? chg.currentArchiveRetentionUntil,
    },
    forceDowngrade: chg.forceDowngrade ?? false,
  };
}
