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
 * SKADI adapter for Oracle Database.
 * Uses DBMS_METADATA.GET_DDL for DDL extraction.
 *
 * Connection: DriverManager.getConnection() per fetchScripts() call — no pool.
 * Hound dialect: "plsql"
 */
public class OracleSkadiFetcher implements SkadiFetcher {

    private static final Logger log = LoggerFactory.getLogger(OracleSkadiFetcher.class);

    @Override
    public SkadiFetchResult fetchScripts(SkadiFetchConfig config) throws SkadiFetchException {
        long startMs = System.currentTimeMillis();
        List<SkadiFetchedFile> files = new ArrayList<>();
        int[] errors = {0};

        Properties props = new Properties();
        props.setProperty("user",     config.user());
        props.setProperty("password", config.password());
        props.setProperty("oracle.jdbc.ReadTimeout", "120000");

        try (Connection conn = DriverManager.getConnection(config.jdbcUrl(), props);
             Statement  stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            setupDbmsMetadata(conn);

            String schema = config.schema() != null ? config.schema().toUpperCase() : null;
            String inventorySql = buildInventoryQuery(schema, config.modifiedSince());

            try (ResultSet rs = stmt.executeQuery(inventorySql)) {
                while (rs.next()) {
                    String objectName = rs.getString("object_name");
                    String oracleType = rs.getString("object_type");
                    Timestamp lastDdl = rs.getTimestamp("last_ddl_time");

                    SkadiFetchConfig.ObjectType objectType = mapOracleType(oracleType);
                    if (objectType == null) continue;
                    if (!config.objectTypes().isEmpty() && !config.objectTypes().contains(objectType)) continue;

                    try {
                        String ddl = getDDL(conn, oracleType, objectName, schema);
                        SkadiFetchedFile file = new SkadiFetchedFile(
                                objectName.toLowerCase(),
                                schema != null ? schema.toLowerCase() : null,
                                objectType, ddl,
                                lastDdl != null ? lastDdl.toInstant() : null,
                                Map.of("oracle_type", oracleType));
                        files.add(file);
                    } catch (Exception e) {
                        log.warn("Failed to get DDL for {}.{}: {}", schema, objectName, e.getMessage());
                        errors[0]++;
                    }
                }
            }

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
        try (Connection conn = DriverManager.getConnection(
                     config.jdbcUrl(), config.user(), config.password());
             Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {
                return rs.next();
            }
        } catch (Exception e) {
            log.debug("Oracle ping failed for {}: {}", config.jdbcUrl(), e.getMessage());
            return false;
        }
    }

    @Override
    public String adapterName() { return "oracle"; }

    @Override
    public void close() {
        // DriverManager: нет пула — нечего закрывать
    }

    // ── private helpers ────────────────────────────────────────────────────────

    private void setupDbmsMetadata(Connection conn) throws SQLException {
        try (CallableStatement cs = conn.prepareCall(
                "BEGIN " +
                "  DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'STORAGE',false); " +
                "  DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'SQLTERMINATOR',true); " +
                "END;")) {
            cs.execute();
        }
    }

    private String buildInventoryQuery(String schema, java.time.Instant modifiedSince) {
        StringBuilder sql = new StringBuilder(
                "SELECT object_name, object_type, last_ddl_time, status " +
                "FROM ALL_OBJECTS " +
                "WHERE object_type IN ('PROCEDURE','FUNCTION','PACKAGE','PACKAGE BODY','VIEW','TRIGGER','TABLE')");
        if (schema != null) {
            sql.append(" AND owner = '").append(schema.replace("'", "''")).append("'");
        }
        if (modifiedSince != null) {
            sql.append(" AND last_ddl_time > TIMESTAMP '")
               .append(modifiedSince.toString().replace("T", " ").replace("Z", ""))
               .append("'");
        }
        sql.append(" ORDER BY object_type, object_name");
        return sql.toString();
    }

    private String getDDL(Connection conn, String objectType, String name, String owner)
            throws SQLException, SkadiFetchException {
        // Oracle DBMS_METADATA uses "PACKAGE BODY" not "PACKAGE_BODY"
        String metaType = objectType.replace("_", " ");

        String callSql = "SELECT DBMS_METADATA.GET_DDL(?,?,?) FROM DUAL";
        try (PreparedStatement ps = conn.prepareStatement(callSql)) {
            ps.setString(1, metaType);
            ps.setString(2, name);
            ps.setString(3, owner);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SkadiFetchException("No DDL returned", adapterName(), name);
                Clob clob = rs.getClob(1);
                if (clob == null) throw new SkadiFetchException("NULL DDL clob", adapterName(), name);
                long length = clob.length();
                if (length > 5_000_000) {
                    log.warn("Large DDL ({} chars) for {}.{}", length, owner, name);
                }
                try {
                    return clob.getSubString(1, (int) Math.min(length, Integer.MAX_VALUE));
                } finally {
                    clob.free();
                }
            }
        }
    }

    private SkadiFetchConfig.ObjectType mapOracleType(String oracleType) {
        if (oracleType == null) return null;
        return switch (oracleType.trim().toUpperCase()) {
            case "PROCEDURE"    -> SkadiFetchConfig.ObjectType.PROCEDURE;
            case "FUNCTION"     -> SkadiFetchConfig.ObjectType.FUNCTION;
            case "PACKAGE"      -> SkadiFetchConfig.ObjectType.PACKAGE;
            case "PACKAGE BODY" -> SkadiFetchConfig.ObjectType.PACKAGE_BODY;
            case "VIEW"         -> SkadiFetchConfig.ObjectType.VIEW;
            case "TRIGGER"      -> SkadiFetchConfig.ObjectType.TRIGGER;
            case "TABLE"        -> SkadiFetchConfig.ObjectType.TABLE;
            default             -> null;
        };
    }

    private SkadiFetchException mapSQLException(SQLException e, String objectName) {
        int code = e.getErrorCode();
        if (code == 1017 || code == 12541 || code == 12154 || code == 17002)
            return new SkadiFetchConnectionException(e.getMessage(), adapterName(), e);
        if (code == 31603 || code == 1031)
            return new SkadiFetchPermissionException(e.getMessage(), adapterName(), objectName, e);
        if (e instanceof SQLTimeoutException)
            return new SkadiFetchTimeoutException(e.getMessage(), adapterName(), objectName, 120_000, e);
        return new SkadiFetchException(e.getMessage(), adapterName(), objectName, e);
    }
}
