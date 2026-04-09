package studio.seer.lineage.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import studio.seer.lineage.client.ArcadeGateway;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LineageService.expandDeep depth-capping behaviour.
 *
 * Verifies that the depth parameter is always clamped to [1..10] before
 * being interpolated into the Cypher query string.  Using a mock ArcadeGateway
 * so no live database is needed.
 */
@QuarkusTest
class LineageServiceTest {

    @Inject
    LineageService lineageService;

    @InjectMock
    ArcadeGateway arcade;

    @BeforeEach
    void setUp() {
        // Return empty result for every cypher call — we only care about the query string
        when(arcade.cypher(anyString(), anyMap()))
            .thenReturn(Uni.createFrom().item(List.of()));
    }

    @Test
    void expandDeep_depthOne_usesOneInQuery() {
        lineageService.expandDeep("#1:0", 1).await().indefinitely();

        String query = captureQuery();
        assertTrue(query.contains("*1..1"),
            "depth=1 should produce *1..1, got: " + query);
    }

    @Test
    void expandDeep_depthFive_usesFiveInQuery() {
        lineageService.expandDeep("#1:0", 5).await().indefinitely();

        String query = captureQuery();
        assertTrue(query.contains("*1..5"),
            "depth=5 should produce *1..5, got: " + query);
    }

    @Test
    void expandDeep_depthTen_usesTenInQuery() {
        lineageService.expandDeep("#1:0", 10).await().indefinitely();

        String query = captureQuery();
        assertTrue(query.contains("*1..10"),
            "depth=10 should produce *1..10, got: " + query);
    }

    @Test
    void expandDeep_depthAboveCap_clampedToTen() {
        lineageService.expandDeep("#1:0", 99).await().indefinitely();

        String query = captureQuery();
        assertTrue(query.contains("*1..10"),
            "depth=99 must be clamped to 10, got: " + query);
        assertFalse(query.contains("*1..99"),
            "depth=99 must NOT appear in query");
    }

    @Test
    void expandDeep_depthZero_clampedToOne() {
        lineageService.expandDeep("#1:0", 0).await().indefinitely();

        String query = captureQuery();
        assertTrue(query.contains("*1..1"),
            "depth=0 must be clamped to 1, got: " + query);
    }

    @Test
    void expandDeep_negativeDepth_clampedToOne() {
        lineageService.expandDeep("#1:0", -5).await().indefinitely();

        String query = captureQuery();
        assertTrue(query.contains("*1..1"),
            "negative depth must be clamped to 1, got: " + query);
    }

    @Test
    void expandDeep_queryPassesNodeIdAsParam() {
        lineageService.expandDeep("#42:7", 3).await().indefinitely();

        ArgumentCaptor<Map<String, Object>> paramsCaptor = paramsCaptor();
        verify(arcade).cypher(anyString(), paramsCaptor.capture());
        assertEquals("#42:7", paramsCaptor.getValue().get("nodeId"),
            "nodeId must be passed as a named parameter, not interpolated");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String captureQuery() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(arcade, atLeastOnce()).cypher(captor.capture(), anyMap());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> paramsCaptor() {
        return ArgumentCaptor.forClass((Class) Map.class);
    }
}
