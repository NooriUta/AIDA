package com.mimir.byok;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
class CredentialEncryptorTest {

    @Inject CredentialEncryptor encryptor;

    @Test
    void roundTripPreservesPlaintext() {
        String plain = "sk-prod-abc123def456";
        String envelope = encryptor.encrypt(plain);

        assertThat(envelope).isNotEqualTo(plain);
        assertThat(encryptor.decrypt(envelope)).isEqualTo(plain);
    }

    @Test
    void emptyPlaintextRoundTrips() {
        String envelope = encryptor.encrypt("");
        assertThat(encryptor.decrypt(envelope)).isEqualTo("");
    }

    @Test
    void nullPlaintextThrows() {
        assertThatThrownBy(() -> encryptor.encrypt(null))
                .isInstanceOf(CredentialException.class);
    }

    @Test
    void ivIsRandomPerEncrypt() {
        // Same plaintext encrypted 20 times must yield 20 distinct envelopes
        Set<String> envelopes = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            envelopes.add(encryptor.encrypt("sk-test"));
        }
        assertThat(envelopes).hasSize(20);
    }

    @Test
    void truncatedEnvelopeIsRejected() {
        String envelope = encryptor.encrypt("sk-test");
        // Cut envelope below IV+tag minimum
        String truncated = envelope.substring(0, 4);
        assertThatThrownBy(() -> encryptor.decrypt(truncated))
                .isInstanceOf(CredentialException.class);
    }

    @Test
    void invalidBase64IsRejected() {
        assertThatThrownBy(() -> encryptor.decrypt("not-base64!@#"))
                .isInstanceOf(CredentialException.class);
    }

    @Test
    void emptyEnvelopeIsRejected() {
        assertThatThrownBy(() -> encryptor.decrypt(""))
                .isInstanceOf(CredentialException.class);
        assertThatThrownBy(() -> encryptor.decrypt(null))
                .isInstanceOf(CredentialException.class);
    }

    @Test
    void tagTamperingIsDetected() {
        String envelope = encryptor.encrypt("sk-real-secret");
        // Flip last char (tag area) — GCM must reject the tampered envelope
        char[] chars = envelope.toCharArray();
        chars[chars.length - 2] = chars[chars.length - 2] == 'A' ? 'B' : 'A';
        String tampered = new String(chars);
        assertThatThrownBy(() -> encryptor.decrypt(tampered))
                .isInstanceOf(CredentialException.class);
    }
}
