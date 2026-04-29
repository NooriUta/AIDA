package studio.seer.analytics.schema;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.analytics.client.AnalyticsClient;

/**
 * Schema v27 migration — runs at startup, idempotent ({@code IF NOT EXISTS}).
 *
 * <p>Adds analytics columns to DaliTable and DaliRelation.
 * Uses ArcadeDB's {@code ALTER TYPE ... ADD ... IF NOT EXISTS} syntax
 * available in ArcadeDB ≥ 26.x.
 *
 * <h3>Schema additions (v27)</h3>
 * <pre>
 * DaliTable:
 *   pagerank_score         FLOAT    — PageRank centrality (hourly)
 *   analytics_updated_at   DATETIME — last analytics run timestamp
 *   in_degree              INT      — incoming edge count (from WCC job)
 *   out_degree             INT      — outgoing edge count
 *   wcc_component_id       STRING   — weakly connected component id
 *   scc_component_id       STRING   — strongly connected component id
 *   is_articulation_point  BOOLEAN  — true if removal disconnects graph
 *   community_id           STRING   — schema grouping (phase 0: schema_geoid)
 *   hub_score              FLOAT    — HITS hub score
 *   authority_score        FLOAT    — HITS authority score
 *
 * DaliRelation:
 *   is_on_bridge  BOOLEAN  — true if this edge is a bridge
 * </pre>
 */
@ApplicationScoped
public class AnalyticsSchemaInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsSchemaInitializer.class);

    @Inject
    AnalyticsClient analytics;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("[schema-v27] Running analytics schema migration on db={}", analytics.dbName());
        try {
            applyDaliTableColumns();
            applyDaliRelationColumns();
            LOG.info("[schema-v27] Schema migration completed successfully");
        } catch (Exception e) {
            // Non-fatal: analytics jobs will fail gracefully if columns are missing.
            // The error is expected on first boot against an empty DB.
            LOG.warn("[schema-v27] Schema migration failed (may be expected if db is empty): {}", e.getMessage());
        }
    }

    private void applyDaliTableColumns() {
        // PageRank centrality
        analytics.ddl("ALTER TYPE DaliTable ADD pagerank_score FLOAT IF NOT EXISTS");
        // Timestamp of last analytics computation on this node
        analytics.ddl("ALTER TYPE DaliTable ADD analytics_updated_at DATETIME IF NOT EXISTS");
        // Degree centrality
        analytics.ddl("ALTER TYPE DaliTable ADD in_degree INT IF NOT EXISTS");
        analytics.ddl("ALTER TYPE DaliTable ADD out_degree INT IF NOT EXISTS");
        // Graph community identifiers
        analytics.ddl("ALTER TYPE DaliTable ADD wcc_component_id STRING IF NOT EXISTS");
        analytics.ddl("ALTER TYPE DaliTable ADD scc_component_id STRING IF NOT EXISTS");
        analytics.ddl("ALTER TYPE DaliTable ADD community_id STRING IF NOT EXISTS");
        // Cut-vertex flag (ArticulationJob)
        analytics.ddl("ALTER TYPE DaliTable ADD is_articulation_point BOOLEAN IF NOT EXISTS");
        // HITS authority model
        analytics.ddl("ALTER TYPE DaliTable ADD hub_score FLOAT IF NOT EXISTS");
        analytics.ddl("ALTER TYPE DaliTable ADD authority_score FLOAT IF NOT EXISTS");
    }

    private void applyDaliRelationColumns() {
        // Bridge flag (BridgesJob)
        analytics.ddl("ALTER TYPE DaliRelation ADD is_on_bridge BOOLEAN IF NOT EXISTS");
    }
}
