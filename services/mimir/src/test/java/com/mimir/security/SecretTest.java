package com.mimir.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecretTest {

    @Test
    void toStringNeverRevealsRaw() {
        Secret s = Secret.of("super-secret-password-12345");
        assertThat(s.toString()).isEqualTo("***");
        assertThat(s.toString()).doesNotContain("super");
        assertThat(s.toString()).doesNotContain("password");
    }

    @Test
    void revealReturnsRawValue() {
        Secret s = Secret.of("my-key-1234");
        assertThat(s.reveal()).isEqualTo("my-key-1234");
    }

    @Test
    void emptySecretIsSingleton() {
        assertThat(Secret.of("")).isSameAs(Secret.empty());
        assertThat(Secret.of(null)).isSameAs(Secret.empty());
        assertThat(Secret.empty().isEmpty()).isTrue();
        assertThat(Secret.empty().reveal()).isEmpty();
    }

    @Test
    void maskShortStringReturnsTripleStar() {
        assertThat(Secret.of("short").mask()).isEqualTo("***");
        assertThat(Secret.of("").mask()).isEqualTo("***");
    }

    @Test
    void maskLongStringShowsPrefixSuffix() {
        // sk-prod-abc123def456 (21 chars) → first 3 + "***" + last 4
        assertThat(Secret.of("sk-prod-abc123def456").mask()).isEqualTo("sk-***-f456");
        assertThat(Secret.of("sk-test-fake-key-1234").mask()).isEqualTo("sk-***-1234");
    }

    @Test
    void equalsTimeConstant() {
        Secret a = Secret.of("password");
        Secret b = Secret.of("password");
        Secret c = Secret.of("different");
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("password"); // не равно raw String
    }

    @Test
    void hashCodeDoesNotLeakContent() {
        // Hash должен быть основан на длине, не содержании — равные длины → одинаковый hash
        Secret a = Secret.of("aaaaaaaa");
        Secret b = Secret.of("bbbbbbbb");
        assertThat(a.hashCode()).isEqualTo(b.hashCode()); // same length = same hash
    }

    @Test
    void wipeMakesValueEmptyString() {
        Secret s = Secret.of("secret");
        // Cannot inspect internal char[] but can check that reveal() returns null bytes after wipe
        s.wipe();
        // wipe overwrites with '\0'; reveal returns String with null chars (length unchanged)
        assertThat(s.reveal()).hasSize(6);
        assertThat(s.reveal()).isEqualTo("\0\0\0\0\0\0");
    }

    @Test
    void lengthExposed() {
        assertThat(Secret.of("12345").length()).isEqualTo(5);
        assertThat(Secret.empty().length()).isZero();
    }
}
