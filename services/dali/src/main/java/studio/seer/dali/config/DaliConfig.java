package studio.seer.dali.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.List;
import java.util.Optional;

/**
 * Typed configuration for Dali service (DS-03).
 *
 * <p>JDBC sources are configured via indexed application.properties keys:
 * <pre>
 *   dali.sources[0].name=oracle-prod
 *   dali.sources[0].dialect=oracle
 *   dali.sources[0].jdbc-url=jdbc:oracle:thin:@host:1521:XE
 *   dali.sources[0].username=dali_harvest
 *   dali.sources[0].password=secret
 *   dali.sources[0].schema=HR          # optional
 * </pre>
 *
 * <p>If no sources are configured the list is empty and HarvestJob is a no-op.
 */
@ConfigMapping(prefix = "dali")
public interface DaliConfig {

    /** JDBC sources for nightly harvest. Empty list = HarvestJob is a no-op. */
    List<Source> sources();

    /** Session retention settings. */
    Session session();

    /** Per-instance identity for multi-Dali setups. */
    Instance instance();

    /** JobRunr configuration. */
    Jobrunr jobrunr();

    /** Harvest scheduling. */
    Harvest harvest();

    interface Session {
        @WithName("retention-days")
        @WithDefault("30")
        int retentionDays();
    }

    interface Instance {
        Optional<String> id();
    }

    interface Jobrunr {
        @WithName("background-job-server")
        BackgroundJobServer backgroundJobServer();

        @WithName("worker-threads")
        @WithDefault("4")
        int workerThreads();

        Dashboard dashboard();

        interface BackgroundJobServer {
            @WithDefault("true")
            boolean enabled();
        }

        interface Dashboard {
            /** Start the JobRunr HTTP dashboard. Disable in test profile. */
            @WithDefault("true")
            boolean enabled();

            /** HTTP port for the JobRunr dashboard server. */
            @WithDefault("29091")
            int port();
        }
    }

    interface Harvest {
        @WithDefault("0 0 2 * * ?")
        String cron();
    }

    interface Source {
        /** Human-readable source name (used as YGG target schema after sanitisation). */
        String name();

        /** SQL dialect: oracle | postgresql | clickhouse */
        String dialect();

        /** JDBC URL passed to SKADI adapter (e.g. {@code jdbc:oracle:thin:@host:1521:XE}). */
        @WithName("jdbc-url")
        String jdbcUrl();

        /** JDBC username for the harvest user. */
        String username();

        /** JDBC password for the harvest user. */
        String password();

        /** Schema/owner filter — empty means the adapter's default (all accessible schemas). */
        Optional<String> schema();
    }
}
