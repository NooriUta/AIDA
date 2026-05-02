package com.mimir.tools;

import com.mimir.client.AnvilClient;
import com.mimir.heimdall.MimirEventEmitter;
import com.mimir.model.anvil.ImpactEdge;
import com.mimir.model.anvil.ImpactNode;
import com.mimir.model.anvil.ImpactRequest;
import com.mimir.model.anvil.ImpactResult;
import com.mimir.model.anvil.LineageRequest;
import com.mimir.model.anvil.LineageResult;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class AnvilToolsTest {

    @Inject AnvilTools tools;

    @InjectMock @RestClient AnvilClient anvil;
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
    void findImpactHappyPath() {
        ImpactNode root = new ImpactNode("HR.ORDERS.STATUS", "DaliColumn", "STATUS", 0);
        ImpactNode dep = new ImpactNode("PROC_X", "DaliRoutine", "PROC_X", 1);
        ImpactEdge edge = new ImpactEdge("HR.ORDERS.STATUS", "PROC_X", "READS_FROM");
        when(anvil.impact(eq("acme"), any(ImpactRequest.class))).thenReturn(
                new ImpactResult(root, List.of(dep), List.of(edge), 47, false, false, 145L));

        Map<String, Object> result = tools.find_impact("HR.ORDERS.STATUS", "downstream", 5);

        assertThat(result).containsEntry("totalAffected", 47);
        assertThat(result).containsEntry("hasMore", false);
        assertThat((List<?>) result.get("nodes")).hasSize(1);
        verify(emitter).toolCallStarted(eq("session-1"), eq("find_impact"), any());
        verify(emitter).toolCallCompleted(eq("session-1"), eq("find_impact"), anyLong(), eq(1));
    }

    @Test
    void findImpactSendsCorrectRequest() {
        when(anvil.impact(anyString(), any(ImpactRequest.class)))
                .thenReturn(new ImpactResult(null, List.of(), List.of(), 0, false, false, 0L));

        tools.find_impact("HR.ORDERS", "downstream", 7);

        ArgumentCaptor<ImpactRequest> captor = ArgumentCaptor.forClass(ImpactRequest.class);
        verify(anvil).impact(eq("acme"), captor.capture());
        ImpactRequest req = captor.getValue();
        assertThat(req.nodeId()).isEqualTo("HR.ORDERS");
        assertThat(req.direction()).isEqualTo("downstream");
        assertThat(req.maxHops()).isEqualTo(7);
        assertThat(req.dbName()).isEqualTo("hound_acme");
    }

    @Test
    void findImpactBoundsHopsTo10Max() {
        when(anvil.impact(anyString(), any(ImpactRequest.class)))
                .thenReturn(new ImpactResult(null, List.of(), List.of(), 0, false, false, 0L));

        tools.find_impact("X", "downstream", 99);

        ArgumentCaptor<ImpactRequest> captor = ArgumentCaptor.forClass(ImpactRequest.class);
        verify(anvil).impact(anyString(), captor.capture());
        assertThat(captor.getValue().maxHops()).isEqualTo(10);
    }

    @Test
    void findImpactDefaultsDirectionToDownstream() {
        when(anvil.impact(anyString(), any(ImpactRequest.class)))
                .thenReturn(new ImpactResult(null, List.of(), List.of(), 0, false, false, 0L));

        tools.find_impact("X", null, 3);

        ArgumentCaptor<ImpactRequest> captor = ArgumentCaptor.forClass(ImpactRequest.class);
        verify(anvil).impact(anyString(), captor.capture());
        assertThat(captor.getValue().direction()).isEqualTo("downstream");
    }

    @Test
    void findImpactFallbackHandled() {
        // Fallback returns executionMs=-1 — surfaced as warning
        when(anvil.impact(anyString(), any(ImpactRequest.class))).thenReturn(
                new ImpactResult(null, List.of(), List.of(), 0, false, false, -1L));

        Map<String, Object> result = tools.find_impact("X", "downstream", 5);

        assertThat(result).containsEntry("warning", "ANVIL unreachable — empty result returned");
        assertThat(result).containsEntry("executionMs", -1L);
    }

    @Test
    void findImpactExceptionGracefullyHandledByFallback() {
        // @Fallback interceptor wraps mock — exception → fallback returns executionMs=-1
        when(anvil.impact(anyString(), any(ImpactRequest.class)))
                .thenThrow(new RuntimeException("connection refused"));

        Map<String, Object> result = tools.find_impact("X", "downstream", 5);

        // Graceful degradation surfaces as warning, not error
        assertThat(result).containsEntry("warning", "ANVIL unreachable — empty result returned");
        assertThat(result).containsEntry("executionMs", -1L);
    }

    @Test
    void queryLineageHappyPath() {
        ImpactNode src = new ImpactNode("HR.RAW", "DaliTable", "RAW", 1);
        ImpactNode dst = new ImpactNode("HR.EMPLOYEES.SALARY", "DaliColumn", "SALARY", 0);
        ImpactEdge edge = new ImpactEdge("HR.RAW", "HR.EMPLOYEES.SALARY", "DATA_FLOW");
        when(anvil.lineage(eq("acme"), any(LineageRequest.class))).thenReturn(
                new LineageResult(List.of(src, dst), List.of(edge), 230L));

        Map<String, Object> result = tools.query_lineage("HR.EMPLOYEES.SALARY", "upstream", 10);

        assertThat((List<?>) result.get("nodes")).hasSize(2);
        assertThat((List<?>) result.get("edges")).hasSize(1);
        assertThat(result).containsEntry("executionMs", 230L);
        verify(emitter).toolCallCompleted(eq("session-1"), eq("query_lineage"), anyLong(), eq(2));
    }

    @Test
    void queryLineageDefaultsDirectionToBoth() {
        when(anvil.lineage(anyString(), any(LineageRequest.class)))
                .thenReturn(new LineageResult(List.of(), List.of(), 0L));

        tools.query_lineage("X", null, 5);

        ArgumentCaptor<LineageRequest> captor = ArgumentCaptor.forClass(LineageRequest.class);
        verify(anvil).lineage(anyString(), captor.capture());
        assertThat(captor.getValue().direction()).isEqualTo("both");
    }

    @Test
    void queryLineageBoundsHopsTo15Max() {
        when(anvil.lineage(anyString(), any(LineageRequest.class)))
                .thenReturn(new LineageResult(List.of(), List.of(), 0L));

        tools.query_lineage("X", "upstream", 999);

        ArgumentCaptor<LineageRequest> captor = ArgumentCaptor.forClass(LineageRequest.class);
        verify(anvil).lineage(anyString(), captor.capture());
        assertThat(captor.getValue().maxHops()).isEqualTo(15);
    }

    @Test
    void queryLineageExceptionGracefullyHandledByFallback() {
        when(anvil.lineage(anyString(), any(LineageRequest.class)))
                .thenThrow(new RuntimeException("ANVIL down"));

        Map<String, Object> result = tools.query_lineage("X", "upstream", 5);

        assertThat(result).containsEntry("warning", "ANVIL unreachable — empty lineage returned");
        assertThat(result).containsEntry("executionMs", -1L);
    }
}
