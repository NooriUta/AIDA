package studio.seer.heimdall.scheduler;

/**
 * HTA-11: S3-compatible export interface for tenant database archives.
 *
 * The default implementation {@link NoopS3ExportClient} logs operations and
 * returns synthetic S3 keys without performing any network I/O — sufficient
 * for dev and smoke tests. Production deployments provide a real @Alternative
 * (e.g. AWS SDK or MinIO) that uploads the exported database zip to the
 * configured bucket.
 */
public interface S3ExportClient {

    /** Upload a database export and return its S3 key (opaque identifier). */
    String exportDatabase(String tenantAlias, String dbName) throws ExportException;

    /** Download a database export back from S3 into a local temp file. */
    String downloadExport(String s3Key) throws ExportException;

    /** Remove all exports under {@code tenants/{alias}/} prefix. */
    void purgeTenantPrefix(String tenantAlias) throws ExportException;

    class ExportException extends RuntimeException {
        public ExportException(String message, Throwable cause) { super(message, cause); }
        public ExportException(String message)                  { super(message); }
    }
}
