package com.mimir.byok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Per-tenant LLM provider configuration stored encrypted in FRIGG
 * (DaliTenantConfig.llmConfig). The {@code encryptedApiKey} field carries an
 * AES-256-GCM envelope produced by {@link CredentialEncryptor}.
 *
 * <p>{@link #toString()} is overridden so the encrypted key never leaks into
 * logs by accident, even though the value is already ciphertext.
 *
 * @param tenantAlias    canonical tenant identifier (e.g. "demo", "acme-prod")
 * @param provider       lower-case provider name: "deepseek" | "anthropic" | "openai" | "ollama-cloud"
 * @param encryptedApiKey AES-256-GCM envelope: {@code base64(IV || ciphertext || tag)}
 * @param baseUrl        optional override (Ollama-cloud, custom OpenAI proxy)
 * @param modelName      optional override (e.g. "claude-sonnet-4-6", "gpt-4o-mini")
 * @param updatedAt      ISO-8601 timestamp of last admin update
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TenantLlmConfig(
        String tenantAlias,
        String provider,
        String encryptedApiKey,
        String baseUrl,
        String modelName,
        String updatedAt
) {

    @Override
    public String toString() {
        return "TenantLlmConfig{tenantAlias=" + tenantAlias
                + ", provider=" + provider
                + ", encryptedApiKey=***"
                + ", baseUrl=" + baseUrl
                + ", modelName=" + modelName
                + ", updatedAt=" + updatedAt + "}";
    }
}
