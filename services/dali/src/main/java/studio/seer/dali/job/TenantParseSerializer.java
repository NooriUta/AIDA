package studio.seer.dali.job;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Per-tenant parse serializer: ensures at most one non-preview ParseJob runs per tenant
 * at a time, while allowing different tenants to parse in parallel.
 *
 * With workerThreads=4, up to 4 tenants can parse simultaneously. A second job for the
 * same tenant blocks on acquire() until the first completes, then proceeds — no rejection.
 */
@Unremovable
@ApplicationScoped
public class TenantParseSerializer {

    private static final Logger log = LoggerFactory.getLogger(TenantParseSerializer.class);

    private final ConcurrentHashMap<String, Semaphore> locks = new ConcurrentHashMap<>();

    public void acquire(String tenantAlias) throws InterruptedException {
        Semaphore sem = locks.computeIfAbsent(tenantAlias, k -> new Semaphore(1));
        boolean immediate = sem.tryAcquire();
        if (!immediate) {
            log.info("[TenantParseSerializer] tenant={} — another parse is running, waiting for slot", tenantAlias);
            sem.acquire();
            log.info("[TenantParseSerializer] tenant={} — slot acquired, starting parse", tenantAlias);
        }
    }

    public void release(String tenantAlias) {
        Semaphore sem = locks.get(tenantAlias);
        if (sem != null) sem.release();
    }
}
