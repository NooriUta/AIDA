package studio.seer.dali.secrets;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * MTN-47 — AES-256-GCM envelope for FRIGG DaliSource credentials at rest.
 *
 * <p>Mirrors the wire format and plaintext-fallback semantics of
 * {@code bff/chur/src/session/encryption.ts} (MTN-59) so both sides encrypt
 * and decrypt compatibly:
 *   <pre>&lt;version&gt;.&lt;ivB64url&gt;.&lt;ctB64url&gt;.&lt;tagB64url&gt;</pre>
 *
 * <p>Activation: set {@code dali.source.dek} (ADR-MT-008 will later redirect
 * this through {@code SecretProvider}). When missing, encrypt/decrypt are
 * no-ops — dev stays plaintext with a single loud warning; legacy rows stay
 * readable.
 */
@ApplicationScoped
public class SecretEncryption {

    private static final Logger log = LoggerFactory.getLogger(SecretEncryption.class);

    private static final int VERSION  = 1;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    @ConfigProperty(name = "dali.source.dek")
    Optional<String> dekBase64;

    /** Parsed 32-byte key, or null when plaintext mode is on. */
    private volatile byte[] keyCached;
    private volatile boolean plaintextWarningEmitted;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Encrypt {@code plaintext}. When no DEK is configured (dev), returns
     * {@code plaintext} unchanged and logs a single warning.
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) return plaintext;
        byte[] key = key();
        if (key == null) return plaintext;

        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            // GCM appends tag to ct in Java — split into ct + tag for wire compat
            int ctLen = ct.length - (TAG_BITS / 8);
            byte[] ciphertext = new byte[ctLen];
            byte[] tag = new byte[TAG_BITS / 8];
            System.arraycopy(ct, 0,      ciphertext, 0, ctLen);
            System.arraycopy(ct, ctLen,  tag,        0, tag.length);
            return VERSION + "." + b64url(iv) + "." + b64url(ciphertext) + "." + b64url(tag);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM encrypt failed", e);
        }
    }

    /**
     * Decrypt a blob produced by {@link #encrypt}. If {@code value} is not in
     * the versioned format, returns it unchanged (legacy plaintext row).
     *
     * @throws RuntimeException when value is a blob but DEK missing or blob tampered
     */
    public String decrypt(String value) {
        if (value == null || value.isEmpty()) return value;
        if (!isCiphertext(value)) return value;
        byte[] key = key();
        if (key == null) {
            throw new RuntimeException("dali.source.dek missing, cannot decrypt ciphertext blob");
        }
        String[] parts = value.split("\\.");
        if (parts.length != 4) {
            throw new RuntimeException("malformed ciphertext (expected 4 dot-separated segments)");
        }
        if (Integer.parseInt(parts[0]) != VERSION) {
            throw new RuntimeException("unsupported ciphertext version: " + parts[0]);
        }
        byte[] iv  = fromB64url(parts[1]);
        byte[] ct  = fromB64url(parts[2]);
        byte[] tag = fromB64url(parts[3]);
        if (iv.length != IV_BYTES || tag.length != TAG_BITS / 8) {
            throw new RuntimeException("malformed ciphertext: iv=" + iv.length + " tag=" + tag.length);
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(TAG_BITS, iv));
            // Java expects ct + tag concatenated
            byte[] joined = new byte[ct.length + tag.length];
            System.arraycopy(ct,  0, joined, 0,         ct.length);
            System.arraycopy(tag, 0, joined, ct.length, tag.length);
            byte[] plaintext = cipher.doFinal(joined);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (javax.crypto.AEADBadTagException e) {
            throw new RuntimeException("ciphertext tampered or wrong key", e);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM decrypt failed", e);
        }
    }

    /** Cheap probe: does {@code value} look like a MTN-47 ciphertext blob? */
    public static boolean isCiphertext(String value) {
        if (value == null || value.length() < 16) return false;
        return value.matches("^\\d+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");
    }

    // ── Internals ──────────────────────────────────────────────────────────────

    private byte[] key() {
        byte[] c = keyCached;
        if (c != null) return c;
        if (dekBase64 == null || dekBase64.isEmpty() || dekBase64.get().isBlank()) {
            if (!plaintextWarningEmitted) {
                plaintextWarningEmitted = true;
                log.warn("[MTN-47] dali.source.dek not set — DaliSource credentials stored in PLAINTEXT. DEV ONLY.");
            }
            return null;
        }
        byte[] decoded = Base64.getDecoder().decode(dekBase64.get());
        if (decoded.length != 32) {
            throw new IllegalStateException("dali.source.dek must be 32 bytes (256-bit) base64-encoded, got " + decoded.length);
        }
        keyCached = decoded;
        return decoded;
    }

    private static String b64url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private static byte[] fromB64url(String s) {
        return Base64.getUrlDecoder().decode(s);
    }

    /** @internal test helper. */
    void setKeyForTests(byte[] key) {
        this.keyCached = key;
    }
}
