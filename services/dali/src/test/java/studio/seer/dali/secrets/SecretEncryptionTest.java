package studio.seer.dali.secrets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SecretEncryptionTest {

    private SecretEncryption crypto;

    @BeforeEach
    void setUp() {
        crypto = new SecretEncryption();
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        crypto.setKeyForTests(key);
    }

    @Test
    void roundtrip_returnsOriginalPlaintext() {
        String pt = "hunter2";
        String ct = crypto.encrypt(pt);
        assertNotEquals(pt, ct);
        assertTrue(SecretEncryption.isCiphertext(ct));
        assertEquals(pt, crypto.decrypt(ct));
    }

    @Test
    void differentCiphertext_forIdenticalPlaintext_randomIv() {
        String a = crypto.encrypt("password");
        String b = crypto.encrypt("password");
        assertNotEquals(a, b);
        assertEquals("password", crypto.decrypt(a));
        assertEquals("password", crypto.decrypt(b));
    }

    @Test
    void tamperedCiphertext_rejected() {
        String ct = crypto.encrypt("secret-jdbc-password");
        String[] parts = ct.split("\\.");
        String flipped = parts[2].charAt(0) == 'A' ? "B" + parts[2].substring(1) : "A" + parts[2].substring(1);
        String tampered = parts[0] + "." + parts[1] + "." + flipped + "." + parts[3];
        assertThrows(RuntimeException.class, () -> crypto.decrypt(tampered));
    }

    @Test
    void unsupportedVersion_rejected() {
        String ct = crypto.encrypt("secret");
        String[] parts = ct.split("\\.");
        String bad = "99." + parts[1] + "." + parts[2] + "." + parts[3];
        assertThrows(RuntimeException.class, () -> crypto.decrypt(bad));
    }

    @Test
    void legacyPlaintext_passesThroughUnchanged() {
        String legacy = "plain-jdbc:mysql://db/foo";
        assertFalse(SecretEncryption.isCiphertext(legacy));
        assertEquals(legacy, crypto.decrypt(legacy));
    }

    @Test
    void emptyInput_passesThrough() {
        assertEquals("", crypto.encrypt(""));
        assertEquals("", crypto.decrypt(""));
        assertNull(crypto.encrypt(null));
        assertNull(crypto.decrypt(null));
    }

    @Test
    void missingKey_encryptReturnsPlaintext_devMode() {
        SecretEncryption c = new SecretEncryption();
        c.dekBase64 = Optional.empty();  // no key — dev fallback
        assertEquals("secret", c.encrypt("secret"));
        // And decrypt of plaintext also no-op
        assertEquals("secret", c.decrypt("secret"));
    }

    @Test
    void missingKey_decryptOfBlobFails() {
        SecretEncryption withKey = new SecretEncryption();
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        withKey.setKeyForTests(key);
        String blob = withKey.encrypt("secret");

        SecretEncryption withoutKey = new SecretEncryption();
        withoutKey.dekBase64 = Optional.empty();
        assertThrows(RuntimeException.class, () -> withoutKey.decrypt(blob));
    }

    @Test
    void isCiphertext_acceptsWellFormedBlobs() {
        assertTrue(SecretEncryption.isCiphertext("1.aaaaaaaa.bbbbbbbbbb.cccccccc"));
    }

    @Test
    void isCiphertext_rejectsPlaintext() {
        assertFalse(SecretEncryption.isCiphertext(""));
        assertFalse(SecretEncryption.isCiphertext("short"));
        assertFalse(SecretEncryption.isCiphertext("hunter2"));
        assertFalse(SecretEncryption.isCiphertext("jdbc:mysql://db/foo"));
    }
}
