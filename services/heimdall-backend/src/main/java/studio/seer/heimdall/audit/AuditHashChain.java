package studio.seer.heimdall.audit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * MTN-37 — Tamper-evident hash chain for {@code seer.audit.*} events.
 *
 * <p>Each persisted event carries two hashes:
 * <ul>
 *   <li>{@code prevHash}  — {@link #GENESIS_HASH} for the first event,
 *       otherwise the {@code eventHash} of the immediately preceding event
 *       (by monotonic {@code id} / insertion order);</li>
 *   <li>{@code eventHash} — {@code SHA-256(canonicalJson + prevHash)}.</li>
 * </ul>
 *
 * <p>Storage is append-only: the ArcadeDB {@code AuditEvent} vertex rejects
 * UPDATE/DELETE via a trigger (configured at schema bootstrap time). A
 * standalone verifier re-computes the chain and reports the first divergence
 * — any tampering produces a cascade mismatch from that row onward.
 *
 * <p>This class is deliberately stateless — it does not read or write to the
 * DB. Callers compose it with a persistence layer. Canonical JSON is the
 * caller's responsibility (sorted keys, stable number/whitespace rendering)
 * so that the hash is reproducible by an independent verifier.
 */
public final class AuditHashChain {

    /** Sentinel value recorded as {@code prevHash} for the genesis event. */
    public static final String GENESIS_HASH = "0".repeat(64);

    private AuditHashChain() {
        throw new AssertionError("utility class");
    }

    /**
     * Compute the hash for an event given its canonical JSON and the preceding
     * event's {@code eventHash} (or {@link #GENESIS_HASH}).
     *
     * @param canonicalJson canonical serialization of the event payload (sorted
     *                      keys, no whitespace variation, UTF-8 encodeable)
     * @param prevHash      64-char lowercase hex of the previous event hash,
     *                      or {@link #GENESIS_HASH} for the chain head
     * @return 64-char lowercase hex SHA-256 digest
     */
    public static String computeHash(String canonicalJson, String prevHash) {
        requireNonBlank(canonicalJson, "canonicalJson");
        validateHash(prevHash, "prevHash");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(canonicalJson.getBytes(StandardCharsets.UTF_8));
            md.update(prevHash.getBytes(StandardCharsets.US_ASCII));
            byte[] digest = md.digest();
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandatory in every JRE
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Verify that {@code claimedHash} matches {@link #computeHash} for the
     * given canonical JSON and {@code prevHash}. Uses a constant-time compare
     * to avoid leaking position information when chains are verified in bulk.
     */
    public static boolean verify(String canonicalJson, String prevHash, String claimedHash) {
        validateHash(claimedHash, "claimedHash");
        String expected = computeHash(canonicalJson, prevHash);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                claimedHash.getBytes(StandardCharsets.US_ASCII));
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(field + " must be non-empty");
        }
    }

    private static void validateHash(String value, String field) {
        if (value == null || value.length() != 64) {
            throw new IllegalArgumentException(field + " must be 64 hex chars, got "
                    + (value == null ? "null" : value.length()));
        }
        for (int i = 0; i < 64; i++) {
            char c = value.charAt(i);
            boolean hex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
            if (!hex) {
                throw new IllegalArgumentException(field + " must be lowercase hex, got '" + c + "' at " + i);
            }
        }
    }
}
