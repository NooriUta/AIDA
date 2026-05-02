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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class YggToolsTest {

    @Inject YggTools tools;

    @InjectMock @RestClient ArcadeDbClient arcade;
    @InjectMock TenantContext tenantContext;
    @InjectMock DbNameResolver dbNameResolver;
    @InjectMock MimirEventEmitter emitter;

    @BeforeEach
    void setup() {
        when(tenantContext.alias()).thenReturn("acme");
        when(tenantContext.sessionId()).thenReturn("s1");
        when(dbNameResolver.forTenant("acme")).thenReturn("hound_acme");
    }

    @Test
    void getProcedureSourceHappyPath() {
        when(arcade.query(eq("hound_acme"), any())).thenReturn(
                new ArcadeDbClient.QueryResult(List.of(Map.of(
                        "qualifiedName", "HR.PROC_X",
                        "sourceText",    "BEGIN ... END",
                        "language",      "PLSQL",
                        "schema",        "HR",
                        "name",          "PROC_X",
                        "type",          "DaliRoutine"))));

        Map<String, Object> result = tools.get_procedure_source("HR.PROC_X");

        assertThat(result).containsEntry("qualifiedName", "HR.PROC_X");
        assertThat(result).containsEntry("sourceText", "BEGIN ... END");
        assertThat(result).containsEntry("language", "PLSQL");
        verify(emitter).toolCallStarted(eq("s1"), eq("get_procedure_source"), any());
        verify(emitter).toolCallCompleted(eq("s1"), eq("get_procedure_source"), anyLong(), eq(1));
    }

    @Test
    void getProcedureSourceNotFoundReturnsErrorMap() {
        when(arcade.query(anyString(), any())).thenReturn(new ArcadeDbClient.QueryResult(List.of()));

        Map<String, Object> result = tools.get_procedure_source("HR.MISSING");

        assertThat(result).containsEntry("error", "not_found");
        assertThat(result).containsEntry("qualifiedName", "HR.MISSING");
    }

    @Test
    void getProcedureSourceFailureReturnsQueryFailed() {
        when(arcade.query(anyString(), any())).thenThrow(new RuntimeException("ArcadeDB down"));

        Map<String, Object> result = tools.get_procedure_source("HR.PROC_X");

        assertThat(result).containsEntry("error", "query_failed");
    }

    @Test
    void countDependenciesIn() {
        when(arcade.query(eq("hound_acme"), any())).thenReturn(
                new ArcadeDbClient.QueryResult(List.of(Map.of("cnt", 42))));

        Map<String, Object> result = tools.count_dependencies("HR.ORDERS", "in");

        assertThat(result).containsEntry("nodeId", "HR.ORDERS");
        assertThat(result).containsEntry("direction", "in");
        assertThat(result).containsEntry("count", 42L);
    }

    @Test
    void countDependenciesOut() {
        when(arcade.query(anyString(), any())).thenReturn(
                new ArcadeDbClient.QueryResult(List.of(Map.of("cnt", 7))));

        Map<String, Object> result = tools.count_dependencies("HR.ORDERS", "out");

        assertThat(result).containsEntry("count", 7L);
    }

    @Test
    void countDependenciesBothIsDefault() {
        when(arcade.query(anyString(), any())).thenReturn(
                new ArcadeDbClient.QueryResult(List.of(Map.of("cnt", 10))));

        Map<String, Object> result = tools.count_dependencies("HR.ORDERS", null);

        assertThat(result).containsEntry("direction", "both");
        assertThat(result).containsEntry("count", 10L);
    }

    @Test
    void countDependenciesEmptyResultReturnsZero() {
        when(arcade.query(anyString(), any())).thenReturn(new ArcadeDbClient.QueryResult(List.of()));

        Map<String, Object> result = tools.count_dependencies("HR.UNKNOWN", "in");

        assertThat(result).containsEntry("count", 0L);
    }

    @Test
    void describeTableColumnsHappyPath() {
        when(arcade.query(eq("hound_acme"), any())).thenReturn(
                new ArcadeDbClient.QueryResult(List.of(
                        Map.of("column_name", "ID",   "data_type", "NUMBER(19)",
                               "is_pk", true, "is_fk", false,
                               "is_required", true, "ordinal_position", 1),
                        Map.of("column_name", "NAME", "data_type", "VARCHAR2(200)",
                               "is_pk", false, "is_fk", false,
                               "is_required", true, "ordinal_position", 2))));

        Map<String, Object> result = tools.describe_table_columns("CRM.COUNTRIES");

        assertThat(result).containsEntry("tableGeoid", "CRM.COUNTRIES");
        assertThat(result).containsEntry("columnCount", 2);
        assertThat((List<?>) result.get("columns")).hasSize(2);
        verify(emitter).toolCallStarted(eq("s1"), eq("describe_table_columns"), any());
        verify(emitter).toolCallCompleted(eq("s1"), eq("describe_table_columns"), anyLong(), eq(2));
    }

    @Test
    void describeTableColumnsArcadeFailureReturnsErrorMap() {
        when(arcade.query(anyString(), any())).thenThrow(new RuntimeException("ArcadeDB down"));

        Map<String, Object> result = tools.describe_table_columns("CRM.COUNTRIES");

        assertThat(result).containsEntry("error", "query_failed");
        assertThat(result).containsEntry("columnCount", 0);
    }

    @Test
    void describeTableColumnsEmptyResult() {
        when(arcade.query(anyString(), any())).thenReturn(new ArcadeDbClient.QueryResult(List.of()));

        Map<String, Object> result = tools.describe_table_columns("HR.NONEXISTENT");

        assertThat(result).containsEntry("columnCount", 0);
        assertThat((List<?>) result.get("columns")).isEmpty();
    }
}
