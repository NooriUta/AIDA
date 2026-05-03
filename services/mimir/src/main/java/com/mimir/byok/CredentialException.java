package com.mimir.byok;

/**
 * Wraps any failure in BYOK crypto / storage layer so callers and audit logs
 * never see raw {@link java.security.GeneralSecurityException} messages —
 * those can leak detail (e.g. tag verification failures) that aid attackers.
 */
public class CredentialException extends RuntimeException {

    public CredentialException(String message) {
        super(message);
    }

    public CredentialException(String message, Throwable cause) {
        super(message, cause);
    }
}
