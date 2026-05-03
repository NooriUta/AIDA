package com.mimir.client;

import com.mimir.security.Secret;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * HTTP Basic auth filter for the read-only ArcadeDB connection used by all
 * MIMIR @Tool methods that read the metadata graph. The credentials map to an
 * ArcadeDB user with the {@code reader} role on the per-tenant {@code hound_*}
 * databases. Defence-in-depth: even if a tool builds a destructive query string
 * (or the LLM coaxes it into one) the database refuses to execute it.
 *
 * <p>Falls back to the elevated {@code mimir.ygg.user/password} pair when the
 * read-only ones aren't configured — that keeps standalone dev installs that
 * never created the {@code mimir_ro} account bootable.
 *
 * <p>Optional + no default — SmallRye Config 3.x rejects defaultValue="" on
 * a required String, so an unset env in dev/test must keep MIMIR bootable.
 */
public class ArcadeDbReadOnlyAuthFilter implements ClientRequestFilter {

    @ConfigProperty(name = "mimir.ygg.ro.user")
    Optional<String> roUserOpt;

    @ConfigProperty(name = "mimir.ygg.ro.password")
    Optional<String> roPasswordOpt;

    @ConfigProperty(name = "mimir.ygg.user", defaultValue = "root")
    String fallbackUser;

    @ConfigProperty(name = "mimir.ygg.password", defaultValue = "playwithdata")
    String fallbackPasswordRaw;

    @Override
    public void filter(ClientRequestContext requestContext) {
        String roUser = roUserOpt == null ? null : roUserOpt.orElse(null);
        String roPwd  = roPasswordOpt == null ? null : roPasswordOpt.orElse(null);
        boolean haveRo = roUser != null && !roUser.isBlank()
                      && roPwd  != null && !roPwd.isBlank();
        String user = haveRo ? roUser : fallbackUser;
        Secret pw   = haveRo ? Secret.of(roPwd) : Secret.of(fallbackPasswordRaw);

        String credentials = user + ":" + pw.reveal();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        requestContext.getHeaders().putSingle("Authorization", "Basic " + encoded);
    }
}
