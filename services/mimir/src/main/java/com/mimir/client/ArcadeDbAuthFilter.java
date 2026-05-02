package com.mimir.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HTTP Basic auth filter for ArcadeDB REST client.
 *
 * <p>Credentials: {@code mimir.ygg.user} / {@code mimir.ygg.password}
 * (defaults: root / playwithdata for dev — overridden in prod via env vars).
 */
public class ArcadeDbAuthFilter implements ClientRequestFilter {

    @ConfigProperty(name = "mimir.ygg.user", defaultValue = "root")
    String user;

    @ConfigProperty(name = "mimir.ygg.password", defaultValue = "playwithdata")
    String password;

    @Override
    public void filter(ClientRequestContext requestContext) {
        String credentials = user + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        requestContext.getHeaders().putSingle("Authorization", "Basic " + encoded);
    }
}
