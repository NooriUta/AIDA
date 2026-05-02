package com.mimir.byok;

/**
 * Static helper for masking BYOK API keys in logs/audit/UI.
 *
 * <p>Format: {@code "sk-prod-abc123def456" → "sk-***-f456"} — first 2 chars +
 * {@code "-***-"} + last 4 chars. Works for any provider prefix (sk-, sk-ant-,
 * Bearer …, etc.) because it only inspects character positions, not content.
 *
 * <p>For values shorter than 8 chars returns {@code "***"} to avoid leaking
 * a meaningful fraction of the secret.
 */
public final class CredentialMasker {

    private CredentialMasker() {}

    public static String mask(String key) {
        if (key == null) return "***";
        String trimmed = key.trim();
        if (trimmed.length() < 8) return "***";
        return trimmed.substring(0, 2) + "-***-" + trimmed.substring(trimmed.length() - 4);
    }
}
