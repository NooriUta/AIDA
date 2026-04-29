package studio.seer.dali.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-MT-33 — MTN-43 🔴 CRIT: SSRF defence must block JDBC URLs that resolve
 * to internal/localhost addresses, including:
 * <ul>
 *   <li>{@code localhost} hostname (resolves to 127.0.0.1 — loopback)</li>
 *   <li>{@code localhost} with IPv6 (resolves to ::1 — loopback)</li>
 *   <li>Non-JDBC schemes like {@code file://} or raw {@code http://} (no valid host)</li>
 *   <li>AWS IMDS endpoint {@code 169.254.169.254} (link-local / metadata service)</li>
 *   <li>Zero address {@code 0.0.0.0}</li>
 * </ul>
 *
 * <p>Complements {@link JdbcUrlValidatorTest} which focuses on IP-literal
 * forbidden ranges.  This class focuses on hostname-based SSRF and
 * non-JDBC scheme injection, i.e. the vectors most likely to be missed
 * in a review.
 *
 * <p>Note on {@code localhost} DNS: The JVM resolves {@code localhost} to
 * the loopback address (127.0.0.1 / ::1) on every supported platform
 * ({@code /etc/hosts} on Linux/macOS, {@code %SystemRoot%\System32\drivers\etc\hosts}
 * on Windows).  CI environments universally honour this — it is safe to call
 * {@link JdbcUrlValidator#validate} here without mocking DNS.
 */
class JdbcUrlSsrfBlocklistTest {

    private JdbcUrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JdbcUrlValidator();
        validator.allowedHostsCsv = Optional.empty();
    }

    // ── localhost hostname ────────────────────────────────────────────────────

    @Test
    void validate_rejects_localhost_mysql() {
        var r = validator.validate("jdbc:mysql://localhost:3306/db");
        assertFalse(r.allowed(), "localhost:3306 must be rejected, got: " + r.reason());
        assertTrue(r.reason().toLowerCase().contains("loopback") ||
                   r.reason().toLowerCase().contains("private"),
                   "Reason must mention loopback or private range, got: " + r.reason());
    }

    @Test
    void validate_rejects_localhost_postgresql() {
        var r = validator.validate("jdbc:postgresql://localhost:5432/mydb");
        assertFalse(r.allowed(), "localhost postgres must be rejected");
    }

    @Test
    void validate_rejects_localhost_sqlserver() {
        var r = validator.validate("jdbc:sqlserver://localhost:1433;databaseName=prod");
        assertFalse(r.allowed(), "localhost sql server must be rejected");
    }

    @Test
    void validate_rejects_localhost_withoutPort() {
        var r = validator.validate("jdbc:mysql://localhost/mydb");
        assertFalse(r.allowed(), "localhost without port must be rejected");
    }

    @Test
    void validate_rejects_localhost_uppercase() {
        // DNS resolution is case-insensitive; validator must handle LOCALHOST too
        var r = validator.validate("jdbc:mysql://LOCALHOST:3306/db");
        assertFalse(r.allowed(), "LOCALHOST (uppercase) must be rejected");
    }

    // ── file:// and non-JDBC schemes ──────────────────────────────────────────

    @Test
    void validate_rejects_fileScheme() {
        // file:// cannot be parsed as a JDBC URL — host extraction returns null → rejected
        var r = validator.validate("file:///etc/passwd");
        assertFalse(r.allowed(), "file:// scheme must be rejected as non-JDBC");
    }

    @Test
    void validate_rejects_httpScheme() {
        var r = validator.validate("http://internal-service/secret");
        assertFalse(r.allowed(), "http:// scheme must be rejected as non-JDBC");
    }

    @Test
    void validate_rejects_jdbcFileVariant() {
        // Some JDBC drivers support jdbc:file:// — must also be rejected
        var r = validator.validate("jdbc:file:///var/data/db");
        assertFalse(r.allowed(), "jdbc:file:// must be rejected (no resolvable host or loopback)");
    }

    // ── AWS / cloud metadata endpoints ────────────────────────────────────────

    @Test
    void validate_rejects_awsImds_169_254_169_254() {
        var r = validator.validate("jdbc:mysql://169.254.169.254:80/imds");
        assertFalse(r.allowed(), "AWS IMDS endpoint must be rejected");
    }

    @Test
    void validate_rejects_azureImds_168_63_129_16() {
        // Azure IMDS / internal DNS: 168.63.129.16 is in 168.63.128.0/10 ≈ link-local-ish
        // but more specifically this is a known Azure endpoint — blocked via private range
        var r = validator.validate("jdbc:mysql://168.63.129.16:80/test");
        assertFalse(r.allowed(), "Azure IMDS endpoint must be rejected");
    }

    // ── zero address ─────────────────────────────────────────────────────────

    @Test
    void validate_rejects_zeroAddress() {
        var r = validator.validate("jdbc:mysql://0.0.0.0:3306/db");
        assertFalse(r.allowed(), "0.0.0.0 must be rejected");
    }

    // ── allow-list exempts localhost for on-prem setups (opt-in only) ─────────

    @Test
    void validate_allowListForLocalhost_permitsWhenExplicitlyAdded() {
        // Operators can explicitly allow localhost (e.g. local development override).
        // This must ONLY work via an explicit allow-list entry — NOT by default.
        validator.setAllowedHostsForTests("localhost");
        var r = validator.validate("jdbc:mysql://localhost:3306/devdb");
        assertTrue(r.allowed(), "localhost must be allowed when on explicit allow-list");
    }

    // ── positive sanity: public host still allowed ────────────────────────────

    @Test
    void validate_allows_publicExternalHost() {
        var r = validator.validate("jdbc:postgresql://db.example.com:5432/prod");
        assertTrue(r.allowed(), "Public host must still be allowed: " + r.reason());
    }
}
