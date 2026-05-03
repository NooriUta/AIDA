package com.mimir.byok;

import com.mimir.security.Secret;

/**
 * Decrypted, ready-to-use credential bundle.
 *
 * <p>The plaintext key is held inside a {@link Secret} so the only way to
 * obtain the raw value is {@link Secret#reveal()} at the call-site that hands
 * it to the LangChain4j model builder. Callers must NEVER log {@link #key()}
 * directly — use {@link Secret#mask()} for any diagnostic.
 *
 * <p>Lives in {@link LlmCredentialResolver} cache for at most TTL minutes.
 */
public record ResolvedKey(
        String tenantAlias,
        String provider,
        Secret key,
        String baseUrl,
        String modelName
) {

    public String keyMask() {
        return key == null ? "***" : key.mask();
    }

    @Override
    public String toString() {
        return "ResolvedKey{tenantAlias=" + tenantAlias
                + ", provider=" + provider
                + ", key=" + keyMask()
                + ", baseUrl=" + baseUrl
                + ", modelName=" + modelName + "}";
    }
}
