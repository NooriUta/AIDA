package com.mimir.byok;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM symmetric encryption for tenant API keys at rest in FRIGG.
 *
 * <p>Master key is sourced from env {@code MIMIR_KEY_ENCRYPTION_KEY} (base64 of
 * 32 raw bytes). Rotation policy: yearly (see security feedback). The same key
 * decrypts every tenant's envelope, so rotation requires re-encrypting all
 * stored configs (out of scope for MT-06).
 *
 * <p>Envelope layout — {@code base64(IV || ciphertext || tag(16))}:
 * <ul>
 *   <li>IV: 12 random bytes per call (NIST SP 800-38D recommendation)</li>
 *   <li>ciphertext: AES-256-GCM of UTF-8 plain</li>
 *   <li>tag: 16 bytes appended by the GCM cipher provider</li>
 * </ul>
 *
 * <p>Failures throw {@link CredentialException} with a generic message — never
 * leak whether tag verification failed (oracle attack) versus truncated input.
 */
@ApplicationScoped
public class CredentialEncryptor {

    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;

    @ConfigProperty(name = "MIMIR_KEY_ENCRYPTION_KEY", defaultValue = "")
    String masterKeyBase64;

    private SecretKeySpec key;
    private final SecureRandom rng = new SecureRandom();

    private synchronized SecretKeySpec key() {
        if (key != null) return key;
        if (masterKeyBase64 == null || masterKeyBase64.isBlank()) {
            throw new CredentialException("MIMIR_KEY_ENCRYPTION_KEY is not configured");
        }
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(masterKeyBase64);
        } catch (IllegalArgumentException e) {
            throw new CredentialException("MIMIR_KEY_ENCRYPTION_KEY is not valid base64");
        }
        if (raw.length != 32) {
            throw new CredentialException("MIMIR_KEY_ENCRYPTION_KEY must decode to 32 bytes (got " + raw.length + ")");
        }
        key = new SecretKeySpec(raw, "AES");
        return key;
    }

    public String encrypt(String plain) {
        if (plain == null) throw new CredentialException("plain is null");
        try {
            byte[] iv = new byte[IV_LEN];
            rng.nextBytes(iv);
            Cipher c = Cipher.getInstance(TRANSFORM);
            c.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] envelope = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, envelope, 0, iv.length);
            System.arraycopy(ct, 0, envelope, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(envelope);
        } catch (CredentialException e) {
            throw e;
        } catch (Exception e) {
            throw new CredentialException("encrypt failed");
        }
    }

    public String decrypt(String envelopeBase64) {
        if (envelopeBase64 == null || envelopeBase64.isBlank()) {
            throw new CredentialException("envelope is empty");
        }
        try {
            byte[] envelope = Base64.getDecoder().decode(envelopeBase64);
            if (envelope.length < IV_LEN + 16) {
                throw new CredentialException("envelope too short");
            }
            byte[] iv = new byte[IV_LEN];
            System.arraycopy(envelope, 0, iv, 0, IV_LEN);
            Cipher c = Cipher.getInstance(TRANSFORM);
            c.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] plain = c.doFinal(envelope, IV_LEN, envelope.length - IV_LEN);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (CredentialException e) {
            throw e;
        } catch (Exception e) {
            throw new CredentialException("decrypt failed");
        }
    }
}
