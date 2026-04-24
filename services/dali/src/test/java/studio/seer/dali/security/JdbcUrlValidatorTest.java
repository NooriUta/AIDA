package studio.seer.dali.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcUrlValidatorTest {

    private JdbcUrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JdbcUrlValidator();
        validator.allowedHostsCsv = Optional.empty();
    }

    // ── Host extraction (pure — no DNS) ───────────────────────────────────────

    @Test
    void extractHost_mysql() {
        assertEquals("db.example.com",
                JdbcUrlValidator.extractHost("jdbc:mysql://db.example.com:3306/mydb"));
    }

    @Test
    void extractHost_postgresql_withQuery() {
        assertEquals("db.example.com",
                JdbcUrlValidator.extractHost("jdbc:postgresql://db.example.com:5432/mydb?sslmode=require"));
    }

    @Test
    void extractHost_oracle_thin_at_form() {
        assertEquals("oracle.corp",
                JdbcUrlValidator.extractHost("jdbc:oracle:thin:@oracle.corp:1521:xe"));
        assertEquals("oracle.corp",
                JdbcUrlValidator.extractHost("jdbc:oracle:thin:@oracle.corp:1521/xe"));
    }

    @Test
    void extractHost_sqlserver_semicolonParams() {
        assertEquals("sqlsvr.corp",
                JdbcUrlValidator.extractHost("jdbc:sqlserver://sqlsvr.corp:1433;databaseName=prod;encrypt=true"));
    }

    @Test
    void extractHost_ipv6_literal() {
        assertEquals("::1",
                JdbcUrlValidator.extractHost("jdbc:mysql://[::1]:3306/db"));
        assertEquals("2001:db8::1",
                JdbcUrlValidator.extractHost("jdbc:mysql://[2001:db8::1]:3306/db"));
    }

    @Test
    void extractHost_withoutPort() {
        assertEquals("db",
                JdbcUrlValidator.extractHost("jdbc:mysql://db/mydb"));
    }

    @Test
    void extractHost_invalid_returnsNull() {
        assertNull(JdbcUrlValidator.extractHost("not-a-jdbc-url"));
        assertNull(JdbcUrlValidator.extractHost("jdbc:mysql:noSlashes/mydb"));
        assertNull(JdbcUrlValidator.extractHost("jdbc:mysql://:3306/mydb"));  // blank host
    }

    // ── Forbidden-range rejection ─────────────────────────────────────────────

    @Test
    void validate_rejects_awsMetadataEndpoint() {
        var r = validator.validate("jdbc:mysql://169.254.169.254:3306/db");
        assertFalse(r.allowed());
        assertTrue(r.reason().toLowerCase().contains("metadata") || r.reason().toLowerCase().contains("link-local"));
    }

    @Test
    void validate_rejects_loopback_ipv4() {
        var r = validator.validate("jdbc:mysql://127.0.0.1:3306/db");
        assertFalse(r.allowed());
        assertTrue(r.reason().toLowerCase().contains("loopback"));
    }

    @Test
    void validate_rejects_loopback_ipv6() {
        var r = validator.validate("jdbc:mysql://[::1]:3306/db");
        assertFalse(r.allowed());
        assertTrue(r.reason().toLowerCase().contains("loopback"));
    }

    @Test
    void validate_rejects_rfc1918_tenEight() {
        var r = validator.validate("jdbc:mysql://10.0.0.1:3306/db");
        assertFalse(r.allowed());
        assertTrue(r.reason().toLowerCase().contains("private"));
    }

    @Test
    void validate_rejects_rfc1918_192168() {
        var r = validator.validate("jdbc:mysql://192.168.1.1:3306/db");
        assertFalse(r.allowed());
    }

    @Test
    void validate_rejects_rfc1918_172_16() {
        var r = validator.validate("jdbc:mysql://172.16.0.5:3306/db");
        assertFalse(r.allowed());
    }

    @Test
    void validate_rejects_wildcard() {
        var r = validator.validate("jdbc:mysql://0.0.0.0:3306/db");
        assertFalse(r.allowed());
    }

    @Test
    void validate_rejects_emptyUrl() {
        var r = validator.validate("");
        assertFalse(r.allowed());
        var r2 = validator.validate(null);
        assertFalse(r2.allowed());
    }

    @Test
    void validate_rejects_unparseable() {
        var r = validator.validate("not-a-jdbc-url");
        assertFalse(r.allowed());
    }

    // ── Allow-list override (on-prem DB behind private DNS) ───────────────────

    @Test
    void validate_allowsPrivateHost_whenOnAllowList() {
        validator.setAllowedHostsForTests("onprem.corp.local");
        // Allow-list matches by hostname (no DNS lookup done)
        var r = validator.validate("jdbc:mysql://onprem.corp.local:3306/db");
        assertTrue(r.allowed(), r.reason());
    }

    @Test
    void validate_allowListIsCaseInsensitive() {
        validator.setAllowedHostsForTests("onprem.corp.local");
        var r = validator.validate("jdbc:mysql://ONPREM.CORP.LOCAL:3306/db");
        assertTrue(r.allowed(), r.reason());
    }

    // ── Public hosts (DNS-dependent, safe to run in CI since we don't connect) ─

    @Test
    void validate_allows_publicHost() {
        // Use a stable well-known public host; result depends on DNS but all public resolvers
        // return a routable public address for this one.
        var r = validator.validate("jdbc:mysql://example.com:3306/db");
        assertTrue(r.allowed(), "expected public host to pass, got: " + r.reason());
    }
}
