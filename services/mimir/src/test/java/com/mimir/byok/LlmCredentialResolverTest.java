package com.mimir.byok;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
class LlmCredentialResolverTest {

    @Inject LlmCredentialResolver resolver;
    @InjectMock TenantLlmConfigStore store;
    @Inject CredentialEncryptor encryptor;

    @BeforeEach
    void clearCache() {
        resolver.invalidateAll();
        reset(store);
    }

    @Test
    void emptyAliasReturnsEmpty() {
        assertThat(resolver.resolveForTenant(null)).isEmpty();
        assertThat(resolver.resolveForTenant("")).isEmpty();
        assertThat(resolver.resolveForTenant("  ")).isEmpty();
        verifyNoInteractions(store);
    }

    @Test
    void missingTenantConfigReturnsEmpty() {
        when(store.findByTenant("ghost")).thenReturn(Optional.empty());

        assertThat(resolver.resolveForTenant("ghost")).isEmpty();
    }

    @Test
    void cachedAfterFirstHit() {
        String envelope = encryptor.encrypt("sk-real-key-1234");
        when(store.findByTenant("acme"))
                .thenReturn(Optional.of(new TenantLlmConfig(
                        "acme", "deepseek", envelope, null, null, "2026-05-02T20:00:00Z")));

        Optional<ResolvedKey> first = resolver.resolveForTenant("acme");
        Optional<ResolvedKey> second = resolver.resolveForTenant("acme");

        assertThat(first).isPresent();
        assertThat(first.get().key().reveal()).isEqualTo("sk-real-key-1234");
        assertThat(first.get().provider()).isEqualTo("deepseek");
        assertThat(second).isPresent();
        // store consulted only once — cache served second call
        verify(store, times(1)).findByTenant("acme");
    }

    @Test
    void invalidateForcesReload() {
        String envelope = encryptor.encrypt("sk-key-1");
        when(store.findByTenant("acme"))
                .thenReturn(Optional.of(new TenantLlmConfig(
                        "acme", "deepseek", envelope, null, null, "2026-05-02T20:00:00Z")));

        resolver.resolveForTenant("acme");
        resolver.invalidate("acme");
        resolver.resolveForTenant("acme");

        verify(store, times(2)).findByTenant("acme");
    }

    @Test
    void corruptCiphertextReturnsEmptyNoThrow() {
        when(store.findByTenant("broken"))
                .thenReturn(Optional.of(new TenantLlmConfig(
                        "broken", "deepseek", "not-base64!@#", null, null, "2026-05-02T20:00:00Z")));

        assertThat(resolver.resolveForTenant("broken")).isEmpty();
    }

    @Test
    void resolvedKeyDoesNotLeakInToString() {
        String envelope = encryptor.encrypt("sk-secret-xyz789");
        when(store.findByTenant("acme"))
                .thenReturn(Optional.of(new TenantLlmConfig(
                        "acme", "deepseek", envelope, "https://x.example", "deepseek-chat",
                        "2026-05-02T20:00:00Z")));

        ResolvedKey rk = resolver.resolveForTenant("acme").orElseThrow();

        assertThat(rk.toString()).doesNotContain("xyz789");
        assertThat(rk.toString()).contains("***");
    }
}
