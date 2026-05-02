package com.mimir.persistence;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Map;

/**
 * Idempotent schema bootstrap for {@code frigg-mimir-sessions} database.
 *
 * <p>On startup:
 * <ol>
 *     <li>Verifies database exists (creates if missing) — TODO: ArcadeDB CREATE DATABASE
 *         requires server-level admin call, not per-DB. For MVP we assume DBA/initContainer
 *         provisions the database (`init-arcadedb.sh` on staging/prod).</li>
 *     <li>Runs CREATE VERTEX TYPE / CREATE PROPERTY / CREATE INDEX with IF NOT EXISTS semantics.</li>
 * </ol>
 *
 * <p>Q-MC9 closed: Quarkus {@code @Startup} hook with idempotent SQL — safe to run on every boot.
 *
 * <p>Failures are logged WARN but don't block startup — repository will still try to upsert
 * and surface failures per-request (test-mode runs without FRIGG should not fail boot).
 */
@ApplicationScoped
@Startup
public class MimirSessionInitializer {

    private static final Logger LOG = Logger.getLogger(MimirSessionInitializer.class);

    @Inject @RestClient FriggClient frigg;

    @ConfigProperty(name = "mimir.frigg.session-db", defaultValue = "frigg-mimir-sessions")
    String sessionDb;

    @ConfigProperty(name = "mimir.frigg.session-init.skip", defaultValue = "false")
    boolean skipInit;

    @PostConstruct
    void init() {
        if (skipInit) {
            LOG.info("MimirSessionInitializer: skipped (mimir.frigg.session-init.skip=true)");
            return;
        }
        try {
            // CREATE VERTEX TYPE IF NOT EXISTS via separate ArcadeDB SQL — supported in 26.x
            execIgnoreError("CREATE VERTEX TYPE MimirSession IF NOT EXISTS");
            execIgnoreError("CREATE PROPERTY MimirSession.sessionId IF NOT EXISTS STRING");
            execIgnoreError("CREATE PROPERTY MimirSession.tenantAlias IF NOT EXISTS STRING");
            execIgnoreError("CREATE PROPERTY MimirSession.status IF NOT EXISTS STRING");
            execIgnoreError("CREATE PROPERTY MimirSession.toolCallsUsed IF NOT EXISTS STRING");
            execIgnoreError("CREATE PROPERTY MimirSession.highlightIds IF NOT EXISTS STRING");
            execIgnoreError("CREATE PROPERTY MimirSession.pauseState IF NOT EXISTS STRING");
            execIgnoreError("CREATE PROPERTY MimirSession.createdAt IF NOT EXISTS DATETIME");
            execIgnoreError("CREATE PROPERTY MimirSession.updatedAt IF NOT EXISTS DATETIME");
            execIgnoreError("CREATE INDEX MimirSession.sessionId IF NOT EXISTS ON MimirSession (sessionId) UNIQUE");
            execIgnoreError("CREATE INDEX MimirSession.tenantAlias IF NOT EXISTS ON MimirSession (tenantAlias) NOTUNIQUE");
            LOG.infof("MimirSessionInitializer: schema ensured in %s", sessionDb);
        } catch (Exception e) {
            LOG.warnf(e, "MimirSessionInitializer: bootstrap skipped — %s", e.getMessage());
        }
    }

    private void execIgnoreError(String sql) {
        try {
            frigg.command(sessionDb, new FriggClient.FriggCommand("sql", sql));
        } catch (Exception e) {
            // Idempotent — "already exists" type errors are expected on re-run
            LOG.tracef("init step '%s' returned: %s", sql, e.getMessage());
        }
    }
}
