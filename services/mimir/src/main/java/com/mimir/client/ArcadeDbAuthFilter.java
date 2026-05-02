package com.mimir.client;

import com.mimir.security.Secret;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HTTP Basic auth filter for ArcadeDB REST client.
 *
 * <p>Credentials: {@code mimir.ygg.user} (String) + {@code mimir.ygg.password}
 * (wrapped in {@link Secret} at use site).
 *
 * <p>Defaults: root / playwithdata for dev — overridden in prod via env vars
 * {@code YGG_USER} / {@code YGG_PASSWORD} (Lockbox-injected).
 *
 * <p>Why Secret wrapper: prevents accidental leakage in logs / stacktraces / heap dumps.
 * {@link Secret#toString()} returns {@code "***"}, raw value extracted ONLY at this
 * single call site via {@link Secret#reveal()} for HTTP Basic Auth header construction.
 */
public class ArcadeDbAuthFilter implements ClientRequestFilter {

    @ConfigProperty(name = "mimir.ygg.user", defaultValue = "root")
    String user;

    /**
     * Raw password from MicroProfile Config — Quarkiverse can't read {@link Secret} directly.
     * Wrapped via {@link #password()} at use site to minimize surface area.
     */
    @ConfigProperty(name = "mimir.ygg.password", defaultValue = "playwithdata")
    String passwordRaw;

    private Secret password() {
        return Secret.of(passwordRaw);
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        Secret pw = password();
        // reveal() ONLY here — single audit point for code review
        String credentials = user + ":" + pw.reveal();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        requestContext.getHeaders().putSingle("Authorization", "Basic " + encoded);
    }
}
