package studio.seer.dali.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.tenantrouting.ArcadeConnection;
import studio.seer.tenantrouting.TenantContext;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * YGG graph stats endpoint — exposes aggregate counts from the tenant's
 * ArcadeDB lineage database ({@code hound_{tenantAlias}}).
 *
 * <pre>
 * GET /api/stats  →  200 OK + YggStats JSON
 * </pre>
 */
@RequestScoped
@Path("/api/stats")
@Produces(MediaType.APPLICATION_JSON)
public class YggStatsResource {

    private static final Logger log = LoggerFactory.getLogger(YggStatsResource.class);

    @Inject TenantContext      tenantCtx;
    @Inject YggLineageRegistry lineageRegistry;

    @GET
    public Response get() {
        try {
            ArcadeConnection conn = lineageRegistry.resourceFor(tenantCtx.tenantAlias());

            long tables     = count(conn, "DaliTable");
            long columns    = count(conn, "DaliColumn");
            long sessions   = count(conn, "DaliSession");
            long statements = count(conn, "DaliStatement");
            long routines   = count(conn, "DaliRoutine");

            Map<String, Long> atomsByStatus = atomCounts(conn);

            long atomsResolved   = countAtoms(conn,
                    "coalesce(primary_status, status) in ('RESOLVED', 'CONSTANT', 'Обработано', 'constant')");
            long atomsUnresolved = countAtoms(conn,
                    "primary_status is null AND status is null OR coalesce(primary_status, status) NOT IN ['RESOLVED', 'CONSTANT', 'Обработано', 'constant'] OR statement_geoid = 'unattached'");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tables",          tables);
            result.put("columns",         columns);
            result.put("sessions",        sessions);
            result.put("statements",      statements);
            result.put("routines",        routines);
            result.put("atomsTotal",      atomsByStatus.values().stream().mapToLong(Long::longValue).sum());
            result.put("atomsResolved",   atomsResolved);
            result.put("atomsConstant",   atomsByStatus.getOrDefault("CONSTANT",  0L) + atomsByStatus.getOrDefault("constant", 0L));
            result.put("atomsUnresolved", atomsUnresolved);
            result.put("atomsPending",    atomsByStatus.getOrDefault("PENDING_INJECT", 0L) + atomsByStatus.getOrDefault("pending", 0L));

            // HAL-05: quality_true + quality_syntax (ADR-HND-005)
            long qualityDenom = countAtoms(conn,
                    "coalesce(primary_status, status) NOT IN ['CONSTANT_ORPHAN', 'PARTIAL'] AND coalesce(qualifier, '') != 'CTRL_FLOW'");
            long qualityTrueNum = countAtoms(conn,
                    "coalesce(primary_status, status) = 'RESOLVED' AND confidence IN ['HIGH', 'MEDIUM'] AND coalesce(qualifier, '') != 'CTRL_FLOW'");
            long qualitySyntaxNum = countAtoms(conn,
                    "coalesce(primary_status, status) IN ['RESOLVED', 'CONSTANT', 'FUNCTION_CALL', 'RECONSTRUCT_DIRECT', 'RECONSTRUCT_INVERSE'] AND coalesce(qualifier, '') NOT IN ['FN_UNVERIFIED', 'CTRL_FLOW']");

            Map<String, Object> quality = new LinkedHashMap<>();
            quality.put("atom_total_for_quality", qualityDenom);
            quality.put("atom_resolved_high_medium", qualityTrueNum);
            quality.put("quality_true", qualityDenom > 0 ? Math.round(qualityTrueNum * 10000.0 / qualityDenom) / 100.0 : 0.0);
            quality.put("quality_syntax", qualityDenom > 0 ? Math.round(qualitySyntaxNum * 10000.0 / qualityDenom) / 100.0 : 0.0);
            result.put("quality", quality);

            return Response.ok(result).build();
        } catch (Exception e) {
            log.warn("YGG stats failed for tenant {}: {}", tenantCtx.tenantAlias(), e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("error", "YGG unavailable: " + e.getMessage()))
                    .build();
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private long count(ArcadeConnection conn, String type) {
        try {
            List<Map<String, Object>> rows = conn.sql(
                    "SELECT count(*) as cnt FROM `" + type + "`", Map.of());
            if (rows == null || rows.isEmpty()) return 0L;
            Object v = rows.get(0).get("cnt");
            return v instanceof Number n ? n.longValue() : 0L;
        } catch (Exception e) {
            log.debug("count({}) failed: {}", type, e.getMessage());
            return 0L;
        }
    }

    private long countAtoms(ArcadeConnection conn, String where) {
        try {
            List<Map<String, Object>> rows = conn.sql(
                    "SELECT count(*) as cnt FROM `DaliAtom` WHERE " + where, Map.of());
            if (rows == null || rows.isEmpty()) return 0L;
            Object v = rows.get(0).get("cnt");
            return v instanceof Number n ? n.longValue() : 0L;
        } catch (Exception e) {
            log.debug("countAtoms({}) failed: {}", where, e.getMessage());
            return 0L;
        }
    }

    private Map<String, Long> atomCounts(ArcadeConnection conn) {
        Map<String, Long> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> rows = conn.sql(
                    "SELECT coalesce(primary_status, status) as ps, count(*) as cnt FROM `DaliAtom` GROUP BY coalesce(primary_status, status)", Map.of());
            if (rows == null) return result;
            for (Map<String, Object> row : rows) {
                Object statusObj = row.get("ps");
                Object cntObj    = row.get("cnt");
                String status = statusObj != null ? statusObj.toString() : "pending";
                long   cnt    = cntObj instanceof Number n ? n.longValue() : 0L;
                result.put(status, cnt);
            }
        } catch (Exception e) {
            log.debug("atomCounts failed: {}", e.getMessage());
        }
        return result;
    }
}
