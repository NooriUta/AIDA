package com.mimir.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialMaskingFilterTest {

    @Test
    void deepseekKeyInMessageIsMasked() {
        String input = "Calling DeepSeek with key sk-prod-abc123def456 and base-url ...";
        String out = CredentialMaskingFilter.mask(input);
        assertThat(out).doesNotContain("sk-prod-abc123def456");
        assertThat(out).contains("sk-***-f456");
    }

    @Test
    void anthropicKeyIsMasked() {
        String out = CredentialMaskingFilter.mask("Authorization=sk-ant-api03-1234567890abcdef now");
        assertThat(out).doesNotContain("sk-ant-api03-1234567890abcdef");
        assertThat(out).contains("sk-***");
    }

    @Test
    void bearerTokenIsMasked() {
        String out = CredentialMaskingFilter.mask("Authorization: Bearer abcdef0123456789ghijklm now");
        assertThat(out).doesNotContain("abcdef0123456789ghijklm");
        assertThat(out).contains("Bearer ");
        assertThat(out).contains("***");
    }

    @Test
    void cleanLineIsUntouched() {
        String input = "session=abc123 elapsed=42ms — no secrets here";
        assertThat(CredentialMaskingFilter.mask(input)).isEqualTo(input);
    }

    @Test
    void multipleKeysOnSameLineAllMasked() {
        String input = "primary sk-prod-aaaa1111bbbb2222 fallback sk-prod-cccc3333dddd4444";
        String out = CredentialMaskingFilter.mask(input);
        assertThat(out).doesNotContain("aaaa1111");
        assertThat(out).doesNotContain("cccc3333");
        assertThat(out).contains("sk-***-2222");
        assertThat(out).contains("sk-***-4444");
    }
}
