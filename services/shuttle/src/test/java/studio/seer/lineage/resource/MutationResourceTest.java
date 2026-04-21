package studio.seer.lineage.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import studio.seer.lineage.client.anvil.AnvilClient;
import studio.seer.lineage.client.anvil.model.*;
import studio.seer.lineage.client.mimir.MimirClient;
import studio.seer.lineage.client.mimir.model.AskInput;
import studio.seer.lineage.client.mimir.model.MimirAnswer;
import studio.seer.lineage.security.SeerIdentity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * SC-04 — tests for MimirClient (SC-02) and AnvilClient (SC-03) mutations.
 *
 * Calls MutationResource methods directly (no HTTP overhead) using CDI injection.
 * MimirClient and AnvilClient are mocked via @InjectMock so no live services needed.
 */
@QuarkusTest
class MutationResourceTest {

    @InjectMock @RestClient MimirClient mimirClient;
    @InjectMock @RestClient AnvilClient anvilClient;
    @InjectMock             SeerIdentity seerIdentity;

    @Inject MutationResource resource;

    @BeforeEach
    void stubIdentity() {
        Mockito.when(seerIdentity.tenantAlias()).thenReturn("default");
    }

    // ── askMimir ──────────────────────────────────────────────────────────────

    @Test
    void askMimir_success_returnsMimirAnswer() {
        Mockito.when(mimirClient.ask(anyString(), any(AskInput.class)))
               .thenReturn(new MimirAnswer(
                       "HR.ORDERS has 7 downstream tables.",
                       List.of("search_nodes"),
                       List.of("#16:100"),
                       "high",
                       1234L));

        MimirAnswer result = resource.askMimir("test question", null, 5)
                .await().indefinitely();

        assertEquals("HR.ORDERS has 7 downstream tables.", result.answer());
        assertEquals("high", result.confidence());
        assertEquals(1234L, result.durationMs());
        assertEquals(1, result.toolCallsUsed().size());
    }

    @Test
    void askMimir_mimirUnavailable_returnsFallback() {
        Mockito.when(mimirClient.ask(anyString(), any(AskInput.class)))
               .thenThrow(new RuntimeException("Connection refused"));

        MimirAnswer result = resource.askMimir("test", null, 5)
                .await().indefinitely();

        assertNotNull(result);
        assertEquals("unavailable", result.confidence());
        assertTrue(result.answer().contains("unavailable"));
    }

    // ── findImpact ────────────────────────────────────────────────────────────

    @Test
    void findImpact_downstream_success() {
        ImpactNode root   = new ImpactNode("HR.ORDERS", "DaliTable", "HR.ORDERS", 0);
        ImpactNode child1 = new ImpactNode("HR.ORDERS.ID", "DaliColumn", "HR.ORDERS.ID", 1);
        ImpactNode child2 = new ImpactNode("HR.ORDERS.STATUS", "DaliColumn", "HR.ORDERS.STATUS", 1);
        ImpactNode child3 = new ImpactNode("PKG_ORDERS.GET", "DaliRoutine", "PKG_ORDERS.GET", 2);

        Mockito.when(anvilClient.findImpact(anyString(), any(ImpactRequest.class)))
               .thenReturn(new ImpactResult(root, List.of(child1, child2, child3), List.of(), 3, false, false, 42L));

        ImpactResult result = resource.findImpact("HR.ORDERS", "downstream", 5)
                .await().indefinitely();

        assertEquals(3, result.totalAffected());
        assertFalse(result.hasMore());
        assertEquals(3, result.nodes().size());
        assertEquals("HR.ORDERS", result.rootNode().id());
    }

    @Test
    void findImpact_anvilUnavailable_returnsFallback() {
        Mockito.when(anvilClient.findImpact(anyString(), any(ImpactRequest.class)))
               .thenThrow(new RuntimeException("ANVIL down"));

        ImpactResult result = resource.findImpact("HR.ORDERS", "downstream", 5)
                .await().indefinitely();

        assertNotNull(result);
        assertEquals(0, result.totalAffected());
        assertTrue(result.nodes().isEmpty());
    }

    @Test
    void findImpact_defaultDirection_isDownstream() {
        Mockito.when(anvilClient.findImpact(anyString(), any(ImpactRequest.class)))
               .thenAnswer(inv -> {
                   ImpactRequest req = inv.getArgument(1);
                   assertEquals("downstream", req.direction());
                   return new ImpactResult(null, List.of(), List.of(), 0, false, false, 5L);
               });

        ImpactResult result = resource.findImpact("X", "downstream", 5)
                .await().indefinitely();

        assertEquals(0, result.totalAffected());
    }

    // ── executeQuery ──────────────────────────────────────────────────────────

    @Test
    void executeQuery_cypher_success() {
        AnvilQueryResponse resp = new AnvilQueryResponse(
                "cypher",
                List.of(
                        Map.of("qualifiedName", "HR.ORDERS",    "@type", "DaliTable"),
                        Map.of("qualifiedName", "HR.EMPLOYEES", "@type", "DaliTable")
                ),
                2, false, 18L, "qid-001");

        Mockito.when(anvilClient.executeQuery(anyString(), any(QueryRequest.class)))
               .thenReturn(resp);

        QueryResult result = resource.executeQuery(
                "MATCH (t:DaliTable) RETURN t LIMIT 2", "cypher", "hound_default")
                .await().indefinitely();

        assertEquals("cypher", result.language());
        assertEquals(2, result.totalRows());
        assertTrue(result.rowsJson().contains("HR.ORDERS"));
        assertEquals("qid-001", result.queryId());
    }

    @Test
    void executeQuery_anvilUnavailable_returnsFallback() {
        Mockito.when(anvilClient.executeQuery(anyString(), any(QueryRequest.class)))
               .thenThrow(new RuntimeException("ANVIL down"));

        QueryResult result = resource.executeQuery("MATCH (t) RETURN t", "cypher", "hound_default")
                .await().indefinitely();

        assertNotNull(result);
        assertEquals(0, result.totalRows());
        assertEquals("[]", result.rowsJson());
    }

    @Test
    void executeQuery_sqlLanguage_passedThrough() {
        Mockito.when(anvilClient.executeQuery(anyString(), any(QueryRequest.class)))
               .thenAnswer(inv -> {
                   QueryRequest req = inv.getArgument(1);
                   assertEquals("sql", req.language());
                   return new AnvilQueryResponse("sql", List.of(), 0, false, 5L, "qid-sql");
               });

        QueryResult result = resource.executeQuery("SELECT 1", "sql", "hound_default")
                .await().indefinitely();

        assertEquals("sql", result.language());
        assertEquals(0, result.totalRows());
    }
}
