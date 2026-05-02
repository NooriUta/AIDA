package com.mimir.tenant;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class DbNameResolverTest {

    @Inject DbNameResolver resolver;

    @Test
    void plainAliasMapsToHoundPrefix() {
        assertThat(resolver.forTenant("acme")).isEqualTo("hound_acme");
    }

    @Test
    void hyphenReplacedWithUnderscore() {
        assertThat(resolver.forTenant("acme-corp")).isEqualTo("hound_acme_corp");
    }

    @Test
    void uppercaseNormalizedToLowercase() {
        assertThat(resolver.forTenant("ACME")).isEqualTo("hound_acme");
    }

    @Test
    void nullAliasReturnsDefault() {
        assertThat(resolver.forTenant(null)).isEqualTo("hound_default");
    }

    @Test
    void blankAliasReturnsDefault() {
        assertThat(resolver.forTenant("")).isEqualTo("hound_default");
        assertThat(resolver.forTenant("   ")).isEqualTo("hound_default");
    }
}
