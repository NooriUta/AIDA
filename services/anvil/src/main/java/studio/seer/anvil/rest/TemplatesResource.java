package studio.seer.anvil.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.anvil.model.QueryTemplate;

import java.util.List;
import java.util.Map;

/**
 * AV-09 — 5 built-in query templates for ANVIL IDE (ANVIL_SPEC §5).
 *
 * <pre>
 * GET /api/templates
 * </pre>
 */
@Path("/api/templates")
@Produces(MediaType.APPLICATION_JSON)
public class TemplatesResource {

    private static final List<QueryTemplate> TEMPLATES = List.of(

        new QueryTemplate(
            "downstream_impact",
            "Downstream Impact",
            "cypher",
            """
            MATCH (start)
            WHERE start.geoid = $nodeId AND start.db_name = $dbName
            CALL {
              WITH start
              MATCH path = (start)-[:ATOM_REF_COLUMN|DATA_FLOW|FILTER_FLOW*1..5]->(affected)
              WHERE affected.db_name = $dbName
              RETURN affected, length(path) AS depth
            }
            RETURN affected.geoid AS id, labels(affected)[0] AS type,
                   affected.qualifiedName AS label, depth
            ORDER BY depth ASC LIMIT 500
            """.strip(),
            "Find all nodes downstream from a given node (what breaks if X changes?)"
        ),

        new QueryTemplate(
            "upstream_lineage",
            "Upstream Lineage",
            "cypher",
            """
            MATCH (start)
            WHERE start.geoid = $nodeId AND start.db_name = $dbName
            CALL {
              WITH start
              MATCH path = (start)<-[:DATA_FLOW*1..10]-(source)
              WHERE source.db_name = $dbName
              RETURN source, length(path) AS depth
            }
            RETURN source.geoid AS id, labels(source)[0] AS type,
                   source.qualifiedName AS label, depth
            ORDER BY depth ASC LIMIT 500
            """.strip(),
            "Trace the data lineage upstream (where does this value come from?)"
        ),

        new QueryTemplate(
            "schema_summary",
            "Schema Summary",
            "sql",
            """
            SELECT schema_geoid, count(*) AS tableCount
            FROM DaliTable
            WHERE db_name = $dbName
            GROUP BY schema_geoid
            ORDER BY tableCount DESC
            LIMIT 50
            """.strip(),
            "Tables grouped by schema with counts"
        ),

        new QueryTemplate(
            "orphan_tables",
            "Orphan Tables",
            "cypher",
            """
            MATCH (t:DaliTable)
            WHERE t.db_name = $dbName
              AND NOT (t)<-[:DATA_FLOW]-()
              AND NOT (t)<-[:READS_FROM]-()
            RETURN t.geoid AS id, t.qualifiedName AS label, 'DaliTable' AS type
            ORDER BY label ASC LIMIT 100
            """.strip(),
            "Tables with no incoming DATA_FLOW or READS_FROM edges (potential orphans)"
        ),

        new QueryTemplate(
            "package_routines",
            "Package Routines",
            "cypher",
            """
            MATCH (r:DaliRoutine)
            WHERE r.db_name = $dbName AND r.package_name IS NOT NULL
            RETURN r.package_name AS package, count(r) AS routineCount,
                   collect(r.name)[0..5] AS sample
            ORDER BY routineCount DESC LIMIT 50
            """.strip(),
            "Routines grouped by package with counts"
        )
    );

    @GET
    public Response templates() {
        return Response.ok(Map.of("templates", TEMPLATES, "total", TEMPLATES.size())).build();
    }
}
