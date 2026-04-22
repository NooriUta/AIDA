package studio.seer.heimdall.scheduler;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Typed config for HEIMDALL's JobRunr scheduler role.
 *
 * jobrunr.db — shared ArcadeDB database on FRIGG for all JobRunr state
 * jobrunr.worker-threads — low (1) since HEIMDALL only processes archive/restore/purge jobs
 * jobrunr.internal-secret — guards /api/internal/scheduler endpoint
 */
@ConfigMapping(prefix = "heimdall.scheduler")
public interface SchedulerConfig {

    Jobrunr jobrunr();

    interface Jobrunr {
        @WithDefault("frigg-jobrunr")
        String db();

        @WithDefault("1")
        int workerThreads();

        @WithDefault("true")
        boolean dashboardEnabled();

        @WithDefault("29092")
        int dashboardPort();
    }

    @WithDefault("changeme-internal-secret")
    String internalSecret();
}
