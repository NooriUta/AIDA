package studio.seer.anvil.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.anvil.model.PhantomColumn;
import studio.seer.anvil.model.YggCommand;
import studio.seer.anvil.util.YggUtil;

import java.util.*;

/**
 * AV-04 — Phantom Column Detector (INSIGHTS §1).
 *
 * Phantom columns: DaliColumn с data_source="reconstructed" без соответствия в DDL.
 * UC2: аналитик видит «призрачные» колонки — потенциальные проблемы схемы.
 *
 * <pre>
 * GET /api/phantom-columns?dbName=hound_default
 * </pre>
 */
@Path("/api/phantom-columns")
@Produces(MediaType.APPLICATION_JSON)
public class PhantomColumnResource {

    private static final String PHANTOM_CYPHER = """
            MATCH (c:DaliColumn {data_source:'reconstructed'})
            WHERE NOT (c)<-[:HAS_COLUMN]-(:DaliTable {data_source:'ddl'})
            RETURN c.geoid AS geoid,
                   c.qualifiedName AS qualifiedName,
                   c.db_name AS dbName
            LIMIT 500
            """;

    @Inject
    @RestClient
    YggQueryClient ygg;

    @ConfigProperty(name = "ygg.db", defaultValue = "hound_default")
    String defaultDb;

    @ConfigProperty(name = "ygg.user", defaultValue = "root")
    String yggUser;

    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata")
    String yggPassword;

    @GET
    public Response phantomColumns(@QueryParam("dbName") String dbName) {
        String db   = dbName != null && !dbName.isBlank() ? dbName : defaultDb;
        String auth = YggUtil.basicAuth(yggUser, yggPassword);

        List<Map<String, Object>> rows;
        try {
            rows = ygg.query(db, auth, new YggCommand("cypher", PHANTOM_CYPHER, Map.of()))
                      .await().indefinitely()
                      .result();
        } catch (Exception e) {
            rows = List.of();
        }
        if (rows == null) rows = List.of();

        List<PhantomColumn> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(new PhantomColumn(
                    str(row, "geoid"),
                    str(row, "qualifiedName"),
                    str(row, "dbName"),
                    "reconstructed"
            ));
        }
        return Response.ok(Map.of("columns", result, "total", result.size())).build();
    }

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : null;
    }
}
