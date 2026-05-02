package com.mimir.tools;

import com.mimir.client.AnvilClient;
import com.mimir.heimdall.MimirEventEmitter;
import com.mimir.model.anvil.ImpactRequest;
import com.mimir.model.anvil.ImpactResult;
import com.mimir.model.anvil.LineageRequest;
import com.mimir.model.anvil.LineageResult;
import com.mimir.tenant.DbNameResolver;
import com.mimir.tenant.TenantContext;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Tools backed by ANVIL :9095 — lineage impact analysis (MC-01).
 *
 * <p>Real implementation via {@link AnvilClient} REST client with {@code @Timeout(10s)},
 * {@code @Retry(maxRetries=2)}, {@code @Fallback} for graceful degradation.
 *
 * <p>Tenant isolation: dbName = {@code hound_{alias}} via {@link DbNameResolver}.
 *
 * <p>Q-MC7 deferred: {@code query_lineage} uses ANVIL's current single-nodeId+direction
 * contract (not source→target path-between). Path-between requires ANVIL endpoint design
 * — separate session after Hound work (per Decision #75 timeline).
 */
@ApplicationScoped
public class AnvilTools {

    private static final Logger LOG = Logger.getLogger(AnvilTools.class);

    @Inject @RestClient AnvilClient anvil;
    @Inject TenantContext tenantContext;
    @Inject DbNameResolver dbNameResolver;
    @Inject MimirEventEmitter eventEmitter;

    @Tool("Find downstream or upstream objects affected by changes to the given node. " +
          "Use this when the user asks 'what breaks if I change X?' or 'what depends on X?'. " +
          "Returns nodes (with id, type, label, depth), edges, and total affected count.")
    public Map<String, Object> find_impact(
            @P("Node geoid (e.g. HR.ORDERS.STATUS) — get from search_nodes if unsure") String nodeId,
            @P("Direction: 'downstream' (what it affects) or 'upstream' (what affects it)") String direction,
            @P("Maximum hops to traverse, 1..10, default 5") int maxHops
    ) {
        long start = System.currentTimeMillis();
        String alias = tenantContext.alias();
        String dbName = dbNameResolver.forTenant(alias);
        String sessionId = tenantContext.sessionId();
        int boundedHops = Math.min(Math.max(maxHops, 1), 10);
        String dir = direction == null ? "downstream" : direction.toLowerCase().trim();

        Map<String, Object> startArgs = new HashMap<>();
        startArgs.put("nodeId",    nodeId == null ? "" : nodeId);
        startArgs.put("direction", dir);
        startArgs.put("maxHops",   boundedHops);
        eventEmitter.toolCallStarted(sessionId, "find_impact", startArgs);

        try {
            ImpactResult r = anvil.impact(alias, new ImpactRequest(nodeId, dir, boundedHops, dbName, null));
            int nodeCount = r.nodes() == null ? 0 : r.nodes().size();
            eventEmitter.toolCallCompleted(sessionId, "find_impact",
                    System.currentTimeMillis() - start, nodeCount);

            Map<String, Object> result = new HashMap<>();
            result.put("rootNode",      r.rootNode());
            result.put("nodes",         r.nodes());
            result.put("edges",         r.edges());
            result.put("totalAffected", r.totalAffected());
            result.put("hasMore",       r.hasMore());
            result.put("cached",        r.cached());
            result.put("executionMs",   r.executionMs());
            if (r.executionMs() == -1L) {
                result.put("warning", "ANVIL unreachable — empty result returned");
            }
            return result;
        } catch (Exception e) {
            LOG.warnf(e, "find_impact failed for tenant=%s nodeId=%s", alias, nodeId);
            eventEmitter.toolCallCompleted(sessionId, "find_impact",
                    System.currentTimeMillis() - start, 0);
            return Map.of(
                    "error",     "anvil_call_failed",
                    "message",   e.getMessage() == null ? "" : e.getMessage(),
                    "nodeId",    nodeId == null ? "" : nodeId,
                    "direction", dir);
        }
    }

    @Tool("Query data lineage (DATA_FLOW edges) for a node — use when the user asks " +
          "'where does this column come from?' or 'where is this data used?'. " +
          "Returns nodes and edges along the data-flow path.")
    public Map<String, Object> query_lineage(
            @P("Node geoid (e.g. CRM.CA_DATA_EXPORTS.EXPORT_DATE)") String nodeId,
            @P("Direction: 'upstream' (where it comes from), 'downstream' (where it goes), or 'both'") String direction,
            @P("Maximum hops, 1..15, default 10") int maxHops
    ) {
        long start = System.currentTimeMillis();
        String alias = tenantContext.alias();
        String dbName = dbNameResolver.forTenant(alias);
        String sessionId = tenantContext.sessionId();
        int boundedHops = Math.min(Math.max(maxHops, 1), 15);
        String dir = direction == null ? "both" : direction.toLowerCase().trim();

        Map<String, Object> startArgs = new HashMap<>();
        startArgs.put("nodeId",    nodeId == null ? "" : nodeId);
        startArgs.put("direction", dir);
        startArgs.put("maxHops",   boundedHops);
        eventEmitter.toolCallStarted(sessionId, "query_lineage", startArgs);

        try {
            LineageResult r = anvil.lineage(alias, new LineageRequest(nodeId, dir, dbName, boundedHops));
            int nodeCount = r.nodes() == null ? 0 : r.nodes().size();
            eventEmitter.toolCallCompleted(sessionId, "query_lineage",
                    System.currentTimeMillis() - start, nodeCount);

            Map<String, Object> result = new HashMap<>();
            result.put("nodes",       r.nodes());
            result.put("edges",       r.edges());
            result.put("executionMs", r.executionMs());
            if (r.executionMs() == -1L) {
                result.put("warning", "ANVIL unreachable — empty lineage returned");
            }
            return result;
        } catch (Exception e) {
            LOG.warnf(e, "query_lineage failed for tenant=%s nodeId=%s", alias, nodeId);
            eventEmitter.toolCallCompleted(sessionId, "query_lineage",
                    System.currentTimeMillis() - start, 0);
            return Map.of(
                    "error",     "anvil_call_failed",
                    "message",   e.getMessage() == null ? "" : e.getMessage(),
                    "nodeId",    nodeId == null ? "" : nodeId,
                    "direction", dir);
        }
    }
}
