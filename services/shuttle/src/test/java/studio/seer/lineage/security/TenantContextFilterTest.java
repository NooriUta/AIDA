package studio.seer.lineage.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SHT-02: Unit tests for TenantContextFilter alias validation and JWT extraction.
 */
class TenantContextFilterTest {

    @Test
    void validAlias_passes() {
        assertThat(TenantContextFilter.validateAlias("acme-corp")).isNull();
        assertThat(TenantContextFilter.validateAlias("default")).isNull();
        assertThat(TenantContextFilter.validateAlias("test1234")).isNull();
    }

    @Test
    void nullOrBlankAlias_fails() {
        assertThat(TenantContextFilter.validateAlias(null)).isNotNull();
        assertThat(TenantContextFilter.validateAlias("")).isNotNull();
        assertThat(TenantContextFilter.validateAlias("   ")).isNotNull();
    }

    @Test
    void tooShortAlias_fails() {
        assertThat(TenantContextFilter.validateAlias("ab")).isNotNull();
        assertThat(TenantContextFilter.validateAlias("abc")).isNotNull(); // 3 chars, too short (min=4 total)
    }

    @Test
    void uppercaseAlias_fails() {
        assertThat(TenantContextFilter.validateAlias("ACME")).isNotNull();
        assertThat(TenantContextFilter.validateAlias("Acme")).isNotNull();
    }

    @Test
    void startsWithDigit_fails() {
        assertThat(TenantContextFilter.validateAlias("1acme")).isNotNull();
    }

    @Test
    void validJwt_extractsOrgAlias() {
        // Build a JWT with organization.alias claim
        String payload = java.util.Base64.getUrlEncoder().encodeToString(
                "{\"sub\":\"user\",\"organization\":{\"alias\":\"acme-corp\"}}".getBytes()
        );
        String jwt = "header." + payload + ".sig";
        assertThat(TenantContextFilter.extractJwtOrgAlias("Bearer " + jwt)).isEqualTo("acme-corp");
    }

    @Test
    void jwtWithoutOrg_returnsNull() {
        String payload = java.util.Base64.getUrlEncoder().encodeToString(
                "{\"sub\":\"user\",\"preferred_username\":\"alice\"}".getBytes()
        );
        String jwt = "header." + payload + ".sig";
        assertThat(TenantContextFilter.extractJwtOrgAlias("Bearer " + jwt)).isNull();
    }

    @Test
    void missingBearer_returnsNull() {
        assertThat(TenantContextFilter.extractJwtOrgAlias(null)).isNull();
        assertThat(TenantContextFilter.extractJwtOrgAlias("Basic xyz")).isNull();
    }
}
