package com.mimir.persistence;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class MimirSessionRepositoryTest {

    @Inject MimirSessionRepository repo;

    @InjectMock @RestClient FriggClient frigg;

    @Test
    void saveSendsUpsertCommand() {
        when(frigg.command(anyString(), any())).thenReturn(new FriggClient.QueryResult(List.of()));

        MimirSession session = MimirSession.completed("s1", "acme",
                List.of("search_nodes", "find_impact"),
                List.of("HR.ORDERS"));

        boolean ok = repo.save(session);

        assertThat(ok).isTrue();
        ArgumentCaptor<FriggClient.FriggCommand> cmd = ArgumentCaptor.forClass(FriggClient.FriggCommand.class);
        verify(frigg).command(eq("frigg-mimir-sessions"), cmd.capture());
        assertThat(cmd.getValue().command()).contains("UPSERT WHERE sessionId");
        assertThat(cmd.getValue().params()).containsEntry("sessionId", "s1");
        assertThat(cmd.getValue().params()).containsEntry("tenantAlias", "acme");
        assertThat(cmd.getValue().params()).containsEntry("status", "completed");
    }

    @Test
    void saveFriggDownReturnsFalse() {
        when(frigg.command(anyString(), any())).thenThrow(new RuntimeException("FRIGG down"));

        boolean ok = repo.save(MimirSession.completed("s1", "acme", List.of(), List.of()));

        assertThat(ok).isFalse();
    }

    @Test
    void findByIdHappyPath() {
        Map<String, Object> row = Map.of(
                "sessionId",     "s1",
                "tenantAlias",   "acme",
                "status",        "completed",
                "toolCallsUsed", "[\"search_nodes\"]",
                "highlightIds",  "[\"HR.ORDERS\"]",
                "createdAt",     "2026-05-02T10:00:00Z",
                "updatedAt",     "2026-05-02T10:00:05Z"
        );
        when(frigg.query(anyString(), any())).thenReturn(new FriggClient.QueryResult(List.of(row)));

        Optional<MimirSession> result = repo.findById("s1");

        assertThat(result).isPresent();
        assertThat(result.get().sessionId()).isEqualTo("s1");
        assertThat(result.get().tenantAlias()).isEqualTo("acme");
        assertThat(result.get().toolCallsUsed()).containsExactly("search_nodes");
        assertThat(result.get().highlightIds()).containsExactly("HR.ORDERS");
    }

    @Test
    void findByIdNotFoundReturnsEmpty() {
        when(frigg.query(anyString(), any())).thenReturn(new FriggClient.QueryResult(List.of()));

        Optional<MimirSession> result = repo.findById("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdFriggErrorReturnsEmpty() {
        when(frigg.query(anyString(), any())).thenThrow(new RuntimeException("FRIGG down"));

        Optional<MimirSession> result = repo.findById("s1");

        assertThat(result).isEmpty();
    }

    @Test
    void findByTenantAppliesLimitBound() {
        when(frigg.query(anyString(), any())).thenReturn(new FriggClient.QueryResult(List.of()));

        repo.findByTenant("acme", 9999); // → bounded to 100

        ArgumentCaptor<FriggClient.FriggQuery> q = ArgumentCaptor.forClass(FriggClient.FriggQuery.class);
        verify(frigg).query(anyString(), q.capture());
        assertThat(q.getValue().params()).containsEntry("lim", 100);
        assertThat(q.getValue().params()).containsEntry("alias", "acme");
    }

    @Test
    void completedFactorySetsStatus() {
        MimirSession s = MimirSession.completed("s1", "acme",
                List.of("a"), List.of("b"));
        assertThat(s.status()).isEqualTo("completed");
        assertThat(s.toolCallsUsed()).containsExactly("a");
        assertThat(s.highlightIds()).containsExactly("b");
        assertThat(s.pauseState()).isNull();
        assertThat(s.createdAt()).isNotNull();
        assertThat(s.updatedAt()).isNotNull();
    }

    @Test
    void failedFactorySetsStatus() {
        MimirSession s = MimirSession.failed("s2", "acme");
        assertThat(s.status()).isEqualTo("failed");
        assertThat(s.toolCallsUsed()).isEmpty();
        assertThat(s.highlightIds()).isEmpty();
    }
}
