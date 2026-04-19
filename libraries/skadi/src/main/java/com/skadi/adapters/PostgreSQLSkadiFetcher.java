package com.skadi.adapters;

import com.skadi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * SKADI adapter for PostgreSQL.
 * Uses pg_get_functiondef / pg_get_viewdef / pg_get_triggerdef for DDL extraction.
 *
 * Connection: DriverManager.getConnection() per fetchScripts() call — no pool.
 * Hound dialect: "postgresql"
 *
 * Incremental harvest: PG has no last_ddl_time. modifiedSince is ignored in MVP.
 * Deduplication is handled by SourceArchiveService (SHA-256 comparison).
 */
public class PostgreSQLSkadiFetcher implements SkadiFetcher {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLSkadiFetcher.class);

    @Override
    public SkadiFetchResult fetchScripts(SkadiFetchConfig config) throws SkadiFetchException {
        long startMs = System.currentTimeMillis();
        List<SkadiFetchedFile> files = new ArrayList<>();
        int[] errors = {0};

        Properties props = new Properties();
        props.setProperty("user",          config.user());
        props.setProperty("password",      config.password());
        props.setProperty("socketTimeout", "120");
        props.setProperty("loginTimeout",  "10");

        try (Connection conn = DriverManager.getConnection(config.jdbcUrl(), props);
             Statement  stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            String schema = config.schema() != null ? config.schema() : "public";

            var types = config.objectTypes().isEmpty()
                    ? java.util.Set.of(SkadiFetchConfig.ObjectType.values())
                    : config.objectTypes();

            if (types.contains(SkadiFetchConfig.ObjectType.FUNCTION)
                    || types.contains(SkadiFetchConfig.ObjectType.PROCEDURE)) {
                fetchFunctionsAndProcedures(conn, schema, types, files, errors);
            }
            if (types.contains(SkadiFetchConfig.ObjectType.VIEW)) {
                fetchViews(conn, schema, files, errors);
            }
            if (types.contains(SkadiFetchConfig.ObjectType.TRIGGER)) {
                fetchTriggers(conn, schema, files, errors);
            }
            // TABLE: TODO (skadi_pg sprint) — reconstructed from information_schema

        } catch (SQLException e) {
            throw mapSQLException(e, null);
        }

        return new SkadiFetchResult(files,
                new SkadiFetchResult.FetchStats(
                        files.size(), errors[0],
                        System.currentTimeMillis() - startMs,
                        adapterName()));
    }

    @Override
    public boolean ping(SkadiFetchConfig config) {
        Properties props = new Properties();
        props.setProperty("user",         config.user());
        props.setProperty("password",     config.password());
        props.setProperty("loginTimeout", "5");
        try (Connection conn = DriverManager.getConnection(config.jdbcUrl(), props);
             Statement  stmt = conn.createStatement()) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery("SELECT 1")) {
                return rs.next();
            }
        } catch (Exception e) {
            log.debug("PG ping failed for {}: {}", config.jdbcUrl(), e.getMessage());
            return false;
        }
    }

    @Override
    public String adapterName() { return "postgresql"; }

    @Override
    public void close() {
        // DriverManager: нет пула — нечего закрывать
    }

    // ── fetch methods ──────────────────────────────────────────────────────────

    private void fetchFunctionsAndProcedures(
            Connection conn, String schema,
            java.util.Set<SkadiFetchConfig.ObjectType> types,
            List<SkadiFetchedFile> files, int[] errors) throws SQLException {

        String sql = """
            SELECT p.proname                 AS name,
                   n.nspname                 AS schema,
                   pg_get_functiondef(p.oid) AS ddl,
                   p.oid::text               AS oid,
                   CASE p.prokind
                       WHEN 'f' THEN 'FUNCTION'
                       WHEN 'p' THEN 'PROCEDURE'
                   END                       AS object_type
            FROM   pg_proc p
            JOIN   pg_namespace n ON n.oid = p.pronamespace
            WHERE  n.nspname = ?
              AND  p.prokind IN ('f', 'p')
              AND  n.nspname NOT IN ('pg_catalog', 'information_schema')
            ORDER  BY p.proname
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name       = rs.getString("name");
                    String ddl        = rs.getString("ddl");
                    String typeStr    = rs.getString("object_type");
                    String oid        = rs.getString("oid");

                    SkadiFetchConfig.ObjectType objectType = "PROCEDURE".equals(typeStr)
                            ? SkadiFetchConfig.ObjectType.PROCEDURE
                            : SkadiFetchConfig.ObjectType.FUNCTION;

                    if (!types.contains(objectType)) continue;

                    files.add(new SkadiFetchedFile(name, schema, objectType, ddl,
                            null, Map.of("oid", oid, "nspname", schema)));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch functions/procedures from schema {}: {}", schema, e.getMessage());
            errors[0]++;
        }
    }

    private void fetchViews(
            Connection conn, String schema,
            List<SkadiFetchedFile> files, int[] errors) throws SQLException {

        String sql = """
            SELECT c.relname   AS name,
                   n.nspname   AS schema,
                   'CREATE OR REPLACE VIEW ' || n.nspname || '.' || c.relname
                       || E' AS\\n' || pg_get_viewdef(c.oid, true) AS ddl,
                   c.oid::text AS oid
            FROM   pg_class c
            JOIN   pg_namespace n ON n.oid = c.relnamespace
            WHERE  c.relkind = 'v'
              AND  n.nspname = ?
            ORDER  BY c.relname
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    files.add(new SkadiFetchedFile(
                            rs.getString("name"), schema,
                            SkadiFetchConfig.ObjectType.VIEW,
                            rs.getString("ddl"), null,
                            Map.of("oid", rs.getString("oid"))));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch views from schema {}: {}", schema, e.getMessage());
            errors[0]++;
        }
    }

    private void fetchTriggers(
            Connection conn, String schema,
            List<SkadiFetchedFile> files, int[] errors) throws SQLException {

        String sql = """
            SELECT t.tgname                  AS name,
                   n.nspname                 AS schema,
                   pg_get_triggerdef(t.oid)  AS ddl,
                   t.oid::text               AS oid
            FROM   pg_trigger t
            JOIN   pg_class c ON c.oid = t.tgrelid
            JOIN   pg_namespace n ON n.oid = c.relnamespace
            WHERE  n.nspname = ?
              AND  NOT t.tgisinternal
            ORDER  BY t.tgname
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    files.add(new SkadiFetchedFile(
                            rs.getString("name"), schema,
                            SkadiFetchConfig.ObjectType.TRIGGER,
                            rs.getString("ddl"), null,
                            Map.of("oid", rs.getString("oid"))));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch triggers from schema {}: {}", schema, e.getMessage());
            errors[0]++;
        }
    }

    // ── error mapping ──────────────────────────────────────────────────────────

    private SkadiFetchException mapSQLException(SQLException e, String objectName) {
        String state = e.getSQLState();
        if (state != null && (state.startsWith("08") || state.startsWith("28") || "3D000".equals(state)))
            return new SkadiFetchConnectionException(e.getMessage(), adapterName(), e);
        if ("42501".equals(state))
            return new SkadiFetchPermissionException(e.getMessage(), adapterName(), objectName, e);
        if ("57014".equals(state))
            return new SkadiFetchTimeoutException(e.getMessage(), adapterName(), objectName, 120_000, e);
        return new SkadiFetchException(e.getMessage(), adapterName(), objectName, e);
    }
}
