package studio.seer.heimdall.control;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.heimdall.snapshot.FriggGateway;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MTN-51 — Detect tenant status transitions in FRIGG and emit control events.
 *
 * <p>Runs every 15 seconds. Reads current {@code (tenantAlias, status, configVersion)}
 * snapshot from {@code DaliTenantConfig} and compares against the last-seen
 * snapshot in memory. Any status change causes a corresponding
 * {@code seer.control.tenant_*} event via {@link ControlEventEmitter}.
 *
 * <p>Why not triggered straight from Chur's admin endpoints? Because:
 * <ol>
 *   <li>Chur might bypass (emergency direct SQL, data migration, etc.).</li>
 *   <li>Multiple Chur replicas can race on the same update.</li>
 *   <li>Tight coupling chur→heimdall is avoided — heimdall owns event authorship.</li>
 * </ol>
 *
 * <p>First tick after startup only seeds the baseline — no events are emitted
 * for pre-existing state. Subsequent ticks detect deltas.
 *
 * <p>Restart amnesia: the in-memory snapshot is empty on boot, so first tick
 * captures current state as baseline. Events missed during heimdall downtime
 * are picked up by consumer pull-reconcile (ControlEventPoller) which has its
 * own persisted offset.
 */
@ApplicationScoped
public class TenantStatusReconciler {

    private static final Logger LOG = Logger.getLogger(TenantStatusReconciler.class);

    @Inject FriggGateway         frigg;
    @Inject ControlEventEmitter  emitter;

    /** alias → last-seen status. {@code null}/missing means never observed. */
    private final Map<String, String> lastStatus = new ConcurrentHashMap<>();
    private volatile boolean baseline = false;

    @Scheduled(every = "15s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void tick() {
        try {
            Uni<List<Map<String, Object>>> q = frigg.sqlTenants(
                    "SELECT tenantAlias, status FROM DaliTenantConfig",
                    Map.of());
            List<Map<String, Object>> rows = q.await().atMost(Duration.ofSeconds(10));
            if (rows == null || rows.isEmpty()) return;

            Map<String, String> current = new HashMap<>();
            for (Map<String, Object> row : rows) {
                String alias  = String.valueOf(row.getOrDefault("tenantAlias", ""));
                String status = String.valueOf(row.getOrDefault("status", "UNKNOWN"));
                if (!alias.isEmpty()) current.put(alias, status);
            }

            if (!baseline) {
                lastStatus.putAll(current);
                baseline = true;
                LOG.infof("[MTN-51] TenantStatusReconciler baseline seeded with %d tenants", current.size());
                return;
            }

            // Detect transitions
            for (Map.Entry<String, String> e : current.entrySet()) {
                String alias     = e.getKey();
                String newStatus = e.getValue();
                String oldStatus = lastStatus.get(alias);
                if (oldStatus == null) {
                    // New tenant appeared — emit invalidated so consumer caches refresh
                    emitter.emitTenantInvalidated(alias, "new_tenant");
                } else if (!oldStatus.equals(newStatus)) {
                    emitForTransition(alias, oldStatus, newStatus);
                }
                lastStatus.put(alias, newStatus);
            }

            // Detect deletions (present before, gone now)
            for (String alias : lastStatus.keySet().toArray(String[]::new)) {
                if (!current.containsKey(alias)) {
                    emitter.emitTenantPurged(alias);
                    lastStatus.remove(alias);
                }
            }
        } catch (Exception e) {
            LOG.warnf("[MTN-51] TenantStatusReconciler tick failed: %s", e.getMessage());
        }
    }

    private void emitForTransition(String alias, String oldStatus, String newStatus) {
        LOG.infof("[MTN-51] transition tenant=%s %s -> %s", alias, oldStatus, newStatus);
        switch (newStatus) {
            case "SUSPENDED" -> emitter.emitTenantSuspended(alias);
            case "ARCHIVED"  -> emitter.emitTenantArchived(alias);
            case "PURGED"    -> emitter.emitTenantPurged(alias);
            case "ACTIVE" -> {
                if ("SUSPENDED".equals(oldStatus) || "ARCHIVED".equals(oldStatus)) {
                    emitter.emitTenantRestored(alias);
                } else {
                    emitter.emitTenantInvalidated(alias, "activated");
                }
            }
            default -> emitter.emitTenantInvalidated(alias, "status_" + newStatus);
        }
    }

    /** @internal test hook. */
    void resetBaselineForTests() {
        baseline = false;
        lastStatus.clear();
    }
}
