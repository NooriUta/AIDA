package studio.seer.heimdall.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Default {@link S3ExportClient} for dev/CI: logs each operation and returns
 * a synthetic S3 key without performing any network I/O. Production deployments
 * should provide a real CDI alternative (bean that @Alternative-overrides this one).
 *
 * See SPRINT_MT_NEXT_BACKLOG for the production S3 integration task.
 */
@ApplicationScoped
public class NoopS3ExportClient implements S3ExportClient {

    private static final Logger LOG = Logger.getLogger(NoopS3ExportClient.class);

    @Override
    public String exportDatabase(String tenantAlias, String dbName) {
        String key = "tenants/" + tenantAlias + "/" + dbName + ".zip";
        LOG.infof("[NoopS3] would export db=%s for tenant=%s → s3Key=%s", dbName, tenantAlias, key);
        return key;
    }

    @Override
    public String downloadExport(String s3Key) {
        LOG.infof("[NoopS3] would download s3Key=%s", s3Key);
        return "/tmp/noop-export.zip";
    }

    @Override
    public void purgeTenantPrefix(String tenantAlias) {
        LOG.infof("[NoopS3] would purge prefix tenants/%s/", tenantAlias);
    }
}
