package com.mimir.tools;

import com.mimir.client.ArcadeDbClient;
import com.mimir.heimdall.MimirEventEmitter;
import com.mimir.tenant.DbNameResolver;
import com.mimir.tenant.TenantContext;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class ShuttleToolsTest {

    @Inject ShuttleTools tools;

    @InjectMock @RestClient ArcadeDbClient arcade;
    @InjectMock TenantContext tenantContext;
    @InjectMock DbNameResolver dbNameResolver;
    @InjectMock MimirEventEmitter emitter;

    @BeforeEach
    void setup() {
        when(tenantContext.alias()).thenReturn("acme");
        when(tenantContext.sessionId()).thenReturn("session-1");
        when(dbNameResolver.forTenant("acme")).thenReturn("hound_acme");
    }

    @Test
    void searchNodesHappyPath() {
        when(arcade.query(eq("hound_acme"), any())).thenReturn(
                new ArcadeDbClient.QueryResult(List.of(
                        Map.of("geoid", "HR.ORDERS", "qualifiedName", "HR.ORDERS",
                               "type", "DaliTable", "schema", "HR"),
                        Map.of("geoid", "HR.ORDERS_HIST", "qualifiedName", "HR.ORDERS_HIST",
                               "type", "DaliTable", "schema", "HR"))));

        List<Map<String, String>> result = tools.search_nodes("ORDERS", "TABLE", 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsEntry("geoid", "HR.ORDERS");
        assertThat(result.get(0)).containsEntry("type", "DaliTable");

        verify(emitter).toolCallStarted(eq("session-1"), eq("search_nodes"), any());
        verify(emitter).toolCallCompleted(eq("session-1"), eq("search_nodes"), anyLong(), eq(2));
    }

    @Test
    void searchNodesUsesCorrectDbName() {
        when(arcade.query(anyString(), any())).thenReturn(new ArcadeDbClient.QueryResult(List.of()));

        tools.search_nodes("anything", "TABLE", 5);

        ArgumentCaptor<String> dbCaptor = ArgumentCaptor.forClass(String.class);
        verify(arcade).query(dbCaptor.capture(), any());
        assertThat(dbCaptor.getValue()).isEqualTo("hound_acme");
    }

    @Test
    void searchNodesLimitBoundedToMax50() {
        when(arcade.query(anyString(), any())).thenReturn(new ArcadeDbClient.QueryResult(List.of()));

        tools.search_nodes("x", "TABLE", 9999);

        ArgumentCaptor<ArcadeDbClient.ArcadeQuery> queryCaptor =
                ArgumentCaptor.forClass(ArcadeDbClient.ArcadeQuery.class);
        verify(arcade).query(anyString(), queryCaptor.capture());
        assertThat(queryCaptor.getValue().params()).containsEntry("lim", 50);
    }

    @Test
    void searchNodesReturnsEmptyOnArcadeFailure() {
        when(arcade.query(anyString(), any())).thenThrow(new RuntimeException("connection refused"));

        List<Map<String, String>> result = tools.search_nodes("ORDERS", "TABLE", 5);

        assertThat(result).isEmpty();
        verify(emitter).toolCallCompleted(eq("session-1"), eq("search_nodes"), anyLong(), eq(0));
    }

    @Test
    void searchNodesNullPatternHandled() {
        when(arcade.query(anyString(), any())).thenReturn(new ArcadeDbClient.QueryResult(List.of()));

        // Should not throw NPE
        List<Map<String, String>> result = tools.search_nodes(null, "TABLE", 5);

        assertThat(result).isNotNull();
    }
}
