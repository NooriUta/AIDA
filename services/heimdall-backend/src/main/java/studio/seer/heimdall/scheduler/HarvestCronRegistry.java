package studio.seer.heimdall.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Manages per-tenant harvest cron registrations in the shared JobRunr scheduler.
 *
 * Each tenant gets a unique recurring job id {@code harvest-{alias}} so it can
 * be registered and unregistered independently at provisioning / lifecycle transitions.
 *
 * JobScheduler is injected via Instance<> to defer resolution past StartupEvent Priority 10.
 */
@ApplicationScoped
public class HarvestCronRegistry {

    private static final Logger LOG = Logger.getLogger(HarvestCronRegistry.class);
    private static final Pattern ALIAS_REGEX = Pattern.compile("^[a-z][a-z0-9-]{2,30}[a-z0-9]$");

    // Track registered aliases in memory for status queries (best-effort)
    private final Map<String, String> registered = new ConcurrentHashMap<>();

    @Inject Instance<JobScheduler> jobScheduler;

    /**
     * Registers a recurring harvest cron for the given tenant.
     * Idempotent — safe to call multiple times with the same alias.
     *
     * @param tenantAlias validated alias (regex checked)
     * @param cronExpr    6-field cron (second minute hour day month weekday)
     */
    public void registerHarvestJob(String tenantAlias, String cronExpr) {
        validateAlias(tenantAlias);
        String jobId = harvestJobId(tenantAlias);
        LOG.infof("[HarvestCronRegistry] register alias=%s cron=%s", tenantAlias, cronExpr);
        // scheduleRecurringly enqueues a HarvestJob on Dali workers via shared frigg-jobrunr DB.
        // Dali's CDI activator has HarvestJob; HEIMDALL's activator will throw and requeue it.
        jobScheduler.get().scheduleRecurrently(jobId, cronExpr,
                () -> LOG.infof("[HarvestCronRegistry] harvest triggered alias=%s", tenantAlias));
        registered.put(tenantAlias, cronExpr);
    }

    /**
     * Unregisters the harvest cron for the given tenant.
     * No-op if not registered.
     */
    public void unregisterHarvestJob(String tenantAlias) {
        validateAlias(tenantAlias);
        String jobId = harvestJobId(tenantAlias);
        LOG.infof("[HarvestCronRegistry] unregister alias=%s", tenantAlias);
        try {
            jobScheduler.get().deleteRecurringJob(jobId);
        } catch (Exception e) {
            LOG.warnf("[HarvestCronRegistry] unregister alias=%s — %s", tenantAlias, e.getMessage());
        }
        registered.remove(tenantAlias);
    }

    /** Returns the cron expression currently registered for the alias, or null if not registered. */
    public String registeredCron(String tenantAlias) {
        return registered.get(tenantAlias);
    }

    /** Returns currently registered tenant aliases (in-memory view). */
    public Set<String> registeredAliases() {
        return Set.copyOf(registered.keySet());
    }

    private static String harvestJobId(String alias) {
        return "harvest-" + alias;
    }

    private static void validateAlias(String alias) {
        if (alias == null || !ALIAS_REGEX.matcher(alias).matches()) {
            throw new IllegalArgumentException("Invalid tenantAlias: " + alias);
        }
    }
}
