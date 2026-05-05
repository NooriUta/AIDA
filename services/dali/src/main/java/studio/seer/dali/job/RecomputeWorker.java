package studio.seer.dali.job;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.storage.FriggGateway;

import java.util.*;

/**
 * HAL2-04 / HAL2-05: Async cascade recomputation of atom/statement statuses.
 *
 * <p>Scheduled via {@link RecomputeJobRequest} after a parse session resolves
 * PENDING_INJECT atoms. Walks the graph in the tenant database to propagate
 * status changes to parent statements.
 *
 * <p>Cascade rules:
 * <ul>
 *   <li>All child atoms RESOLVED/RECONSTRUCT_* → parent statement RESOLVED
 *   <li>Any child PENDING_INJECT → parent PENDING_INJECT
 *   <li>Mixed → parent PARTIAL
 * </ul>
 */
@Unremovable
@ApplicationScoped
public class RecomputeWorker implements JobRequestHandler<RecomputeJobRequest> {

    private static final Logger log = LoggerFactory.getLogger(RecomputeWorker.class);

    @Inject FriggGateway frigg;

    @Override
    public void run(RecomputeJobRequest req) throws Exception {
        execute(req.sessionId(), req.tenantAlias());
    }

    @Job(name = "Recompute atom lineage", retries = 3)
    public void execute(String sessionId, String tenantAlias) {
        log.info("[{}] recompute start tenant={}", sessionId, tenantAlias);

        // Phase 1: TTL expiry — transition expired PENDING_INJECT → UNRESOLVED
        int expired = expirePendingAtoms(tenantAlias, sessionId);

        // Phase 2: Cascade — propagate child atom status changes to parent statements
        int cascaded = cascadeParentStatuses(tenantAlias, sessionId);

        log.info("[{}] recompute done tenant={} expired={} cascaded={}",
                sessionId, tenantAlias, expired, cascaded);
    }

    int expirePendingAtoms(String tenantAlias, String sessionId) {
        String query = """
                UPDATE DaliAtom SET primary_status = 'UNRESOLVED',
                    status = 'UNRESOLVED', pending_kind = NULL, pending_snapshot = NULL
                WHERE primary_status = 'PENDING_INJECT'
                  AND pending_since IS NOT NULL
                  AND pending_since < :cutoff
                """;
        long cutoffMs = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        try {
            var result = frigg.sqlIn(tenantAlias, query, Map.of("cutoff", cutoffMs));
            int count = result.isEmpty() ? 0 : extractCount(result);
            if (count > 0) log.info("[{}] TTL expired {} PENDING_INJECT atoms", sessionId, count);
            return count;
        } catch (Exception e) {
            log.warn("[{}] TTL expiry failed: {}", sessionId, e.getMessage());
            return 0;
        }
    }

    int cascadeParentStatuses(String tenantAlias, String sessionId) {
        String findParents = """
                SELECT stmt_geoid FROM DaliStatement
                WHERE @rid IN (
                    SELECT out FROM HAS_ATOM
                    WHERE in.primary_status IN ['PENDING_INJECT', 'RECONSTRUCT_INVERSE', 'RECONSTRUCT_DIRECT']
                )
                AND session_id = :sessionId
                """;
        try {
            var stmts = frigg.sqlIn(tenantAlias, findParents, Map.of("sessionId", sessionId));
            int cascaded = 0;
            Set<String> visited = new HashSet<>();
            for (var row : stmts) {
                String stmtGeoid = (String) row.get("stmt_geoid");
                if (stmtGeoid == null || !visited.add(stmtGeoid)) continue;
                cascaded += cascadeStatement(tenantAlias, stmtGeoid, visited);
            }
            return cascaded;
        } catch (Exception e) {
            log.warn("[{}] cascade failed: {}", sessionId, e.getMessage());
            return 0;
        }
    }

    private int cascadeStatement(String tenantAlias, String stmtGeoid, Set<String> visited) {
        String childQuery = """
                SELECT primary_status FROM (
                    SELECT expand(outE('HAS_ATOM').in) FROM DaliStatement
                    WHERE stmt_geoid = :stmtGeoid
                )
                """;
        var children = frigg.sqlIn(tenantAlias, childQuery, Map.of("stmtGeoid", stmtGeoid));
        if (children.isEmpty()) return 0;

        boolean allResolved = true;
        boolean anyPending = false;
        for (var child : children) {
            String status = (String) child.get("primary_status");
            if ("PENDING_INJECT".equals(status)) {
                anyPending = true;
                allResolved = false;
            } else if ("UNRESOLVED".equals(status) || "PARTIAL".equals(status)) {
                allResolved = false;
            }
        }

        String newStatus;
        if (allResolved) newStatus = "RESOLVED";
        else if (anyPending) newStatus = "PENDING_INJECT";
        else newStatus = "PARTIAL";

        String update = """
                UPDATE DaliStatement SET aggregate_status = :newStatus
                WHERE stmt_geoid = :stmtGeoid
                """;
        frigg.sqlIn(tenantAlias, update, Map.of("newStatus", newStatus, "stmtGeoid", stmtGeoid));
        return 1;
    }

    private static int extractCount(List<Map<String, Object>> result) {
        if (result.isEmpty()) return 0;
        Object val = result.get(0).getOrDefault("count", result.get(0).get("value"));
        if (val instanceof Number n) return n.intValue();
        return result.size();
    }
}
