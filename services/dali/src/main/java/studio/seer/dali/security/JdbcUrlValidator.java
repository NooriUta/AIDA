package studio.seer.dali.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * MTN-43 🔴 CRIT — SSRF defense for JDBC harvest source URLs.
 *
 * <p>A tenant admin who controls a harvest source URL ({@code jdbc:mysql://…})
 * can point the URL at the cluster's internal metadata endpoint
 * ({@code 169.254.169.254}, AWS/GCP/Azure IMDS) and trick Dali into making
 * requests that return machine-scoped IAM credentials in the error body, or
 * into probing the internal RFC1918 network for reachable services. SSH/HTTP
 * response fingerprints are enough for lateral movement.
 *
 * <p>This validator blocks exactly that class of request:
 * <ul>
 *   <li>Parse {@code jdbc:...} into {@code host:port}.</li>
 *   <li>Resolve host DNS → one or more {@code InetAddress}.</li>
 *   <li>Reject if any resolved address falls inside a forbidden CIDR:
 *       loopback ({@code 127.0.0.0/8}, {@code ::1}), link-local
 *       ({@code 169.254.0.0/16}, {@code fe80::/10}), RFC1918 private
 *       ({@code 10/8}, {@code 172.16/12}, {@code 192.168/16}).</li>
 *   <li>Allow-list override {@code dali.harvest.source.allowed-hosts} (CSV of
 *       explicit hostnames) — for on-prem DBs behind private DNS.</li>
 * </ul>
 *
 * <p>DNS-rebinding mitigation: callers call {@link #validate} immediately
 * before opening the JDBC connection (not at {@code POST /sources} time only)
 * so an attacker cannot flip the DNS answer between validation and use.
 *
 * <p>IPv6-aware: forbidden ranges are expressed in {@link InetAddress#getAddress}
 * byte form, so we don't care about textual representation.
 */
@ApplicationScoped
public class JdbcUrlValidator {

    private static final Logger log = LoggerFactory.getLogger(JdbcUrlValidator.class);

    @ConfigProperty(name = "dali.harvest.source.allowed-hosts")
    Optional<String> allowedHostsCsv;

    /** Comma-separated override allowlist resolved lazily. */
    private volatile Set<String> allowedHostsCache;

    // ── Public API ────────────────────────────────────────────────────────────

    public record ValidationResult(boolean allowed, String reason) {
        public static ValidationResult ok() { return new ValidationResult(true, null); }
        public static ValidationResult reject(String reason) { return new ValidationResult(false, reason); }
    }

    /**
     * Validate that {@code jdbcUrl} points to a host we're willing to contact.
     * Does a live DNS lookup; callers should treat this as authoritative at
     * the moment of call (DNS answers can flip).
     */
    public ValidationResult validate(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return ValidationResult.reject("jdbcUrl is empty");
        }
        String host = extractHost(jdbcUrl);
        if (host == null) {
            return ValidationResult.reject("cannot extract host from jdbc url: " + jdbcUrl);
        }
        if (allowedHosts().contains(host.toLowerCase())) {
            return ValidationResult.ok();
        }
        // DNS resolve — any address within the forbidden ranges = reject.
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            return ValidationResult.reject("host DNS lookup failed: " + host);
        }
        for (InetAddress addr : addresses) {
            String rejection = forbiddenReason(addr);
            if (rejection != null) {
                log.warn("[MTN-43] blocked jdbc url: host='{}' addr='{}' reason='{}'",
                        host, addr.getHostAddress(), rejection);
                return ValidationResult.reject(rejection + " (addr=" + addr.getHostAddress() + ")");
            }
        }
        return ValidationResult.ok();
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    /**
     * Extract the host portion of a JDBC URL. Supported formats:
     *   jdbc:mysql://host:port/db?…
     *   jdbc:postgresql://host:5432/db
     *   jdbc:oracle:thin:@host:1521/xe
     *   jdbc:sqlserver://host:1433;databaseName=…
     *   jdbc:mariadb://host:3306/db
     *
     * @return host (lowercase) or {@code null} if we can't parse.
     */
    static String extractHost(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:")) return null;
        String tail = jdbcUrl.substring(5);

        // Oracle thin driver: jdbc:oracle:thin:@host:port/sid
        if (tail.startsWith("oracle:thin:@")) {
            String rest = tail.substring("oracle:thin:@".length());
            int colon = rest.indexOf(':');
            int slash = rest.indexOf('/');
            int end = Math.min(colon < 0 ? Integer.MAX_VALUE : colon,
                               slash < 0 ? Integer.MAX_VALUE : slash);
            if (end == Integer.MAX_VALUE) end = rest.length();
            return rest.substring(0, end).toLowerCase();
        }

        // Standard "jdbc:<dialect>://<host>[:port][/...][?...]"
        int schemeEnd = tail.indexOf("://");
        if (schemeEnd < 0) return null;
        String authorityAndPath = tail.substring(schemeEnd + 3);
        // Strip path / query / jtds-style semicolon params
        int cut = firstIndex(authorityAndPath, '/', '?', ';', ',');
        String authority = cut < 0 ? authorityAndPath : authorityAndPath.substring(0, cut);
        // Handle IPv6 literal [::1]:port
        if (authority.startsWith("[")) {
            int closeBracket = authority.indexOf(']');
            if (closeBracket < 0) return null;
            return authority.substring(1, closeBracket).toLowerCase();
        }
        int portColon = authority.indexOf(':');
        String host = portColon < 0 ? authority : authority.substring(0, portColon);
        return host.isBlank() ? null : host.toLowerCase();
    }

    private static int firstIndex(String s, char... delims) {
        int min = -1;
        for (char d : delims) {
            int i = s.indexOf(d);
            if (i >= 0 && (min < 0 || i < min)) min = i;
        }
        return min;
    }

    /** Return rejection reason string if {@code addr} falls in a forbidden range, else null. */
    private static String forbiddenReason(InetAddress addr) {
        if (addr.isLoopbackAddress())            return "loopback address";
        if (addr.isLinkLocalAddress())           return "link-local address (cloud metadata range)";
        if (addr.isSiteLocalAddress())           return "RFC1918 private address";
        if (addr.isAnyLocalAddress())            return "wildcard (0.0.0.0 / ::) address";
        if (addr.isMulticastAddress())           return "multicast address";
        byte[] ip = addr.getAddress();
        // Explicit 169.254.0.0/16 guard (isLinkLocalAddress only flags IPv4 169.254/16 in newer JDKs)
        if (ip.length == 4 && (ip[0] & 0xff) == 169 && (ip[1] & 0xff) == 254) {
            return "AWS/GCP/Azure metadata endpoint (169.254.169.254 family)";
        }
        return null;
    }

    private Set<String> allowedHosts() {
        Set<String> c = allowedHostsCache;
        if (c != null) return c;
        String csv = (allowedHostsCsv == null) ? "" : allowedHostsCsv.orElse("");
        if (csv.isBlank()) {
            c = Collections.emptySet();
        } else {
            c = new HashSet<>();
            for (String h : csv.split(",")) {
                String trimmed = h.trim().toLowerCase();
                if (!trimmed.isEmpty()) c.add(trimmed);
            }
            c = Collections.unmodifiableSet(c);
        }
        allowedHostsCache = c;
        return c;
    }

    /** @internal test hook — inject allowlist. */
    void setAllowedHostsForTests(String... hosts) {
        this.allowedHostsCache = new HashSet<>(Arrays.asList(hosts));
    }
}
