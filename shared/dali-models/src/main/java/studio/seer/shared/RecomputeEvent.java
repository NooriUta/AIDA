package studio.seer.shared;

/**
 * HAL2-05 — Event requesting async recomputation of atom lineage.
 *
 * <p>Emitted by Hound when an atom transitions from PENDING_INJECT to a
 * resolved state (RECONSTRUCT_INVERSE, RESOLVED). The Dali RecomputeWorker
 * consumes these events to cascade status changes to parent statements.
 *
 * @param atomGeoid        geoid of the atom that changed
 * @param newPrimaryStatus new primary_status after resolution
 * @param newConfidence    new confidence after resolution (nullable)
 * @param sessionId        session that triggered the change
 * @param tenantAlias      tenant database to target
 * @param timestamp        unix ms at emit time
 */
public record RecomputeEvent(
        String atomGeoid,
        String newPrimaryStatus,
        String newConfidence,
        String sessionId,
        String tenantAlias,
        long   timestamp) {
}
