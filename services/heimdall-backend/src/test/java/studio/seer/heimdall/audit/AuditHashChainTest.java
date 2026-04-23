package studio.seer.heimdall.audit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditHashChainTest {

    private static final String JSON_1 = "{\"action\":\"USER_CREATE\",\"actor\":\"admin\",\"target\":\"user-1\",\"ts\":1700000000000}";
    private static final String JSON_2 = "{\"action\":\"USER_DELETE\",\"actor\":\"admin\",\"target\":\"user-1\",\"ts\":1700000001000}";

    @Test
    void computeHash_is_deterministic() {
        String h1 = AuditHashChain.computeHash(JSON_1, AuditHashChain.GENESIS_HASH);
        String h2 = AuditHashChain.computeHash(JSON_1, AuditHashChain.GENESIS_HASH);
        assertEquals(h1, h2);
    }

    @Test
    void computeHash_returns_64_char_lowercase_hex() {
        String h = AuditHashChain.computeHash(JSON_1, AuditHashChain.GENESIS_HASH);
        assertEquals(64, h.length());
        assertTrue(h.matches("[0-9a-f]{64}"), "hash must be lowercase hex");
    }

    @Test
    void different_payload_produces_different_hash() {
        String h1 = AuditHashChain.computeHash(JSON_1, AuditHashChain.GENESIS_HASH);
        String h2 = AuditHashChain.computeHash(JSON_2, AuditHashChain.GENESIS_HASH);
        assertNotEquals(h1, h2);
    }

    @Test
    void different_prevHash_produces_different_hash() {
        String h1 = AuditHashChain.computeHash(JSON_1, AuditHashChain.GENESIS_HASH);
        String h2 = AuditHashChain.computeHash(JSON_1, "a".repeat(64));
        assertNotEquals(h1, h2);
    }

    @Test
    void verify_returns_true_for_valid_hash() {
        String h = AuditHashChain.computeHash(JSON_1, AuditHashChain.GENESIS_HASH);
        assertTrue(AuditHashChain.verify(JSON_1, AuditHashChain.GENESIS_HASH, h));
    }

    @Test
    void verify_returns_false_on_tampered_payload() {
        String original = AuditHashChain.computeHash(JSON_1, AuditHashChain.GENESIS_HASH);
        assertFalse(AuditHashChain.verify(JSON_2, AuditHashChain.GENESIS_HASH, original));
    }

    @Test
    void chain_breaks_propagate_forward() {
        // Build a 3-event chain
        String h1 = AuditHashChain.computeHash(JSON_1, AuditHashChain.GENESIS_HASH);
        String h2 = AuditHashChain.computeHash(JSON_2, h1);
        String h3 = AuditHashChain.computeHash("{\"action\":\"LOGOUT\"}", h2);

        // Now tamper with the 2nd event payload; recomputing h2 should differ,
        // and h3 (computed over h2) will no longer match the stored value.
        String h2Tampered = AuditHashChain.computeHash("{\"action\":\"DIFFERENT\"}", h1);
        assertNotEquals(h2, h2Tampered);
        String h3OverTampered = AuditHashChain.computeHash("{\"action\":\"LOGOUT\"}", h2Tampered);
        assertNotEquals(h3, h3OverTampered);
    }

    @Test
    void computeHash_rejects_null_or_empty_json() {
        assertThrows(IllegalArgumentException.class,
                () -> AuditHashChain.computeHash(null, AuditHashChain.GENESIS_HASH));
        assertThrows(IllegalArgumentException.class,
                () -> AuditHashChain.computeHash("", AuditHashChain.GENESIS_HASH));
    }

    @Test
    void computeHash_rejects_malformed_prevHash() {
        assertThrows(IllegalArgumentException.class,
                () -> AuditHashChain.computeHash(JSON_1, "tooShort"));
        assertThrows(IllegalArgumentException.class,
                () -> AuditHashChain.computeHash(JSON_1, "Z".repeat(64))); // non-hex
        assertThrows(IllegalArgumentException.class,
                () -> AuditHashChain.computeHash(JSON_1, "A".repeat(64))); // uppercase
    }

    @Test
    void genesis_hash_is_64_zeroes() {
        assertEquals(64, AuditHashChain.GENESIS_HASH.length());
        assertTrue(AuditHashChain.GENESIS_HASH.chars().allMatch(c -> c == '0'));
    }

    @Test
    void verify_rejects_malformed_claimedHash() {
        assertThrows(IllegalArgumentException.class,
                () -> AuditHashChain.verify(JSON_1, AuditHashChain.GENESIS_HASH, "short"));
    }
}
