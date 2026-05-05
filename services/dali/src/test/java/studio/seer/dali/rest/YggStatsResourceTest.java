package studio.seer.dali.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.tenantrouting.ArcadeConnection;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for YggStatsResource — GET /api/stats.
 *
 * <p>Uses a hand-rolled {@link ArcadeConnection} fake that dispatches SQL
 * responses by query content, avoiding Mockito stub-ordering ambiguity
 * across the eight SQL calls that share {@code DaliAtom} in their text.
 *
 * <p>TenantContext is populated naturally from the {@code X-Seer-Tenant-Alias}
 * request header via TenantContextFilter → TenantContextHolder → TenantContextProducer.
 */
@QuarkusTest
class YggStatsResourceTest {

    @InjectMock YggLineageRegistry lineageRegistry;

    private static RequestSpecification withTenant() {
        return given().header("X-Seer-Tenant-Alias", "default");
    }

    /** Fake ArcadeConnection that returns canned counts based on SQL content. */
    private static ArcadeConnection buildFakeConn(
            long tables, long columns, long sessions, long statements, long routines,
            long atomsResolved, long atomsUnresolved,
            List<Map<String, Object>> atomsByStatus) {
        return new ArcadeConnection() {
            @Override
            public List<Map<String, Object>> sql(String query, Map<String, Object> params) {
                if (query.contains("DaliTable"))     return List.of(Map.of("cnt", tables));
                if (query.contains("DaliColumn"))    return List.of(Map.of("cnt", columns));
                if (query.contains("DaliSession"))   return List.of(Map.of("cnt", sessions));
                if (query.contains("DaliStatement")) return List.of(Map.of("cnt", statements));
                if (query.contains("DaliRoutine"))   return List.of(Map.of("cnt", routines));
                // atomCounts GROUP BY
                if (query.contains("GROUP BY")) return atomsByStatus;
                // countAtoms resolved — coalesce(primary_status, status) in (...)
                if (query.contains("'RESOLVED'") && !query.contains("NOT IN")) return List.of(Map.of("cnt", atomsResolved));
                // countAtoms unresolved
                if (query.contains("NOT IN")) return List.of(Map.of("cnt", atomsUnresolved));
                return List.of();
            }
            @Override
            public List<Map<String, Object>> cypher(String q, Map<String, Object> p) {
                return List.of();
            }
            @Override
            public String databaseName() { return "hound_default"; }
        };
    }

    @BeforeEach
    void setUp() {
        ArcadeConnection conn = buildFakeConn(
                5L, 20L, 3L, 15L, 7L,
                10L, 2L,
                List.of(
                        Map.of("ps", "RESOLVED", "cnt", 8L),
                        Map.of("ps", "CONSTANT",   "cnt", 2L)
                ));
        when(lineageRegistry.resourceFor("default")).thenReturn(conn);
    }

    // ── success path ──────────────────────────────────────────────────────────

    @Test
    void get_success_returnsVertexCounts() {
        withTenant()
        .when().get("/api/stats")
        .then()
            .statusCode(200)
            .body("tables",     equalTo(5))
            .body("columns",    equalTo(20))
            .body("sessions",   equalTo(3))
            .body("statements", equalTo(15))
            .body("routines",   equalTo(7));
    }

    @Test
    void get_success_returnsAtomStats() {
        withTenant()
        .when().get("/api/stats")
        .then()
            .statusCode(200)
            .body("atomsTotal",      equalTo(10))  // 8 + 2
            .body("atomsResolved",   equalTo(10))
            .body("atomsConstant",   equalTo(2))
            .body("atomsUnresolved", equalTo(2));
    }

    @Test
    void get_emptyGraph_returnsAllZeros() {
        ArcadeConnection empty = buildFakeConn(0L, 0L, 0L, 0L, 0L, 0L, 0L, List.of());
        when(lineageRegistry.resourceFor("default")).thenReturn(empty);

        withTenant()
        .when().get("/api/stats")
        .then()
            .statusCode(200)
            .body("tables",     equalTo(0))
            .body("atomsTotal", equalTo(0));
    }

    // ── failure path ──────────────────────────────────────────────────────────

    @Test
    void get_yggUnavailable_returns503WithErrorBody() {
        when(lineageRegistry.resourceFor("default"))
                .thenThrow(new RuntimeException("hound_default not reachable"));

        withTenant()
        .when().get("/api/stats")
        .then()
            .statusCode(503)
            .body("error", containsString("YGG unavailable"));
    }

    @Test
    void get_connectionSqlThrows_gracefullyReturns200WithZeros() {
        // Private count/countAtoms/atomCounts helpers each catch Exception and return 0.
        // So even when every SQL call throws, the response is 200 with all-zero fields.
        ArcadeConnection broken = new ArcadeConnection() {
            @Override
            public List<Map<String, Object>> sql(String q, Map<String, Object> p) {
                throw new RuntimeException("ArcadeDB socket timeout");
            }
            @Override
            public List<Map<String, Object>> cypher(String q, Map<String, Object> p) {
                return List.of();
            }
            @Override
            public String databaseName() { return "hound_default"; }
        };
        when(lineageRegistry.resourceFor("default")).thenReturn(broken);

        withTenant()
        .when().get("/api/stats")
        .then()
            .statusCode(200)
            .body("tables",     equalTo(0))
            .body("atomsTotal", equalTo(0));
    }
}
