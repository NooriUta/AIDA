package com.mimir.byok;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialMaskerTest {

    @Test
    void deepseekStyleKeyIsMasked() {
        assertThat(CredentialMasker.mask("sk-prod-abc123def456")).isEqualTo("sk-***-f456");
    }

    @Test
    void anthropicKeyIsMasked() {
        assertThat(CredentialMasker.mask("sk-ant-api03-1234567890abcdef")).isEqualTo("sk-***-cdef");
    }

    @Test
    void shortKeyCollapsesToStars() {
        assertThat(CredentialMasker.mask("sk-1234")).isEqualTo("***");
        assertThat(CredentialMasker.mask("a")).isEqualTo("***");
        assertThat(CredentialMasker.mask("")).isEqualTo("***");
    }

    @Test
    void nullReturnsStars() {
        assertThat(CredentialMasker.mask(null)).isEqualTo("***");
    }

    @Test
    void whitespaceTrimmedBeforeMasking() {
        assertThat(CredentialMasker.mask("  sk-prod-abc123def456  ")).isEqualTo("sk-***-f456");
    }
}
