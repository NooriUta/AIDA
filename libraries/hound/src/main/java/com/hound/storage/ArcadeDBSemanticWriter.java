// File: src/main/java/com/hound/storage/ArcadeDBSemanticWriter.java
package com.hound.storage;

// TODO(arcadedb-embed): EMBEDDED mode removed during 26.x upgrade.
// Restore EmbeddedWriter + Mode.EMBEDDED after engine API stabilises.

import com.arcadedb.remote.RemoteDatabase;
import com.hound.metrics.PipelineTimer;
import com.hound.semantic.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Thin facade over {@link RemoteWriter}.
 *
 * <p>Lifecycle, public API routing, and shared count computation live here.
 * All heavy write logic is delegated to the mode-specific writer.
 *
 * <ul>
 *   <li>{@link RemoteWriter}   — ArcadeDB remote-mode write path (REMOTE + REMOTE_BATCH)</li>
 *   <li>{@link WriteHelpers}   — pure static helpers shared by both writers</li>
 * </ul>
 */
public class ArcadeDBSemanticWriter implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ArcadeDBSemanticWriter.class);

    private final RemoteWriter    remote;
    private final HttpBatchClient batchClient;  // non-null only in REMOTE_BATCH mode
    private final Mode            mode;

    public enum Mode { REMOTE, REMOTE_BATCH }

    // ═══════════════════════════════════════════════════════════════
    // Constructors
    // ═══════════════════════════════════════════════════════════════

    public ArcadeDBSemanticWriter(String host, int port, String dbName,
                                  String user, String password) {
        this.mode = Mode.REMOTE;
        RemoteDatabase rdb = new RemoteDatabase(host, port, dbName, user, password);
        for (String cmd : SchemaInitializer.remoteSchemaCommands()) {
            try { rdb.command("sql", cmd); }
            catch (Exception e) { logger.debug("Schema init cmd skipped ({}): {}", e.getClass().getSimpleName(), e.getMessage()); }
        }
        this.remote      = new RemoteWriter(rdb);
        this.batchClient = null;
        logger.info("ArcadeDB REMOTE: {}:{}/{}", host, port, dbName);
    }

    public ArcadeDBSemanticWriter(String host, int port, String dbName,
                                  String user, String password, boolean useBatch) {
        this.mode = useBatch ? Mode.REMOTE_BATCH : Mode.REMOTE;
        RemoteDatabase rdb = new RemoteDatabase(host, port, dbName, user, password);
        for (String cmd : SchemaInitializer.remoteSchemaCommands()) {
            try { rdb.command("sql", cmd); }
            catch (Exception e) { logger.debug("Schema init cmd skipped ({}): {}", e.getClass().getSimpleName(), e.getMessage()); }
        }
        this.remote      = new RemoteWriter(rdb);
        this.batchClient = useBatch
                ? new HttpBatchClient(host, port, dbName, user, password)
                : null;
        if (useBatch)
            logger.info("ArcadeDB REMOTE_BATCH: {}:{}/{}", host, port, dbName);
        else
            logger.info("ArcadeDB REMOTE: {}:{}/{}", host, port, dbName);
    }

    // ═══════════════════════════════════════════════════════════════
    // Main entry point
    // ═══════════════════════════════════════════════════════════════

    public WriteStats saveResult(SemanticResult result, PipelineTimer timer) {
        return saveResult(result, timer, null, null);
    }

    public WriteStats saveResult(SemanticResult result, PipelineTimer timer,
                                 CanonicalPool pool, String dbName) {
        String sid = result.getSessionId();
        long t0 = System.currentTimeMillis();

        WriteStats ws;
        if (mode == Mode.REMOTE_BATCH) {
            ws = remote.writeBatch(batchClient, sid, result, timer, pool, dbName);
        } else {
            remote.write(sid, result, timer, pool, dbName);
            ws = new WriteStats(); // REMOTE mode: no per-type stats available
        }

        long ms = System.currentTimeMillis() - t0;
        Structure s = result.getStructure();

        int oc = 0;
        if (s != null) {
            for (var e : s.getStatements().entrySet()) oc += e.getValue().getColumnsOutput().size();
        }

        int cntTables     = s != null ? s.getTables().size()     : 0;
        int cntColumns    = s != null ? s.getColumns().size()    : 0;
        int cntStatements = s != null ? s.getStatements().size() : 0;
        int cntRoutines   = s != null ? s.getRoutines().size()   : 0;
        int cntAtoms      = result.getAtoms().size();
        int cntJoins      = s != null ? s.getStatements().values().stream()
                .mapToInt(st -> st.getJoins().size()).sum() : 0;
        int cntLineage    = result.getLineage().size();

        logger.info("ArcadeDB ({}) [db={}]: T:{} C:{} S:{} R:{} A:{} J:{} OC:{} L:{} [{}]",
                mode, dbName != null ? dbName : "ad-hoc",
                cntTables, cntColumns, cntStatements, cntRoutines,
                cntAtoms, cntJoins, oc, cntLineage, WriteHelpers.formatTime(ms));

        writePerfStats(sid, result, timer, dbName,
                cntTables, cntColumns, cntStatements, cntRoutines,
                cntAtoms, cntJoins, oc, cntLineage);
        return ws;
    }

    // ═══════════════════════════════════════════════════════════════
    // Canonical pool
    // ═══════════════════════════════════════════════════════════════

    public CanonicalPool ensureCanonicalPool(String dbName) {
        return ensureCanonicalPool(dbName, null, null);
    }

    public CanonicalPool ensureCanonicalPool(String dbName, String appName, String appGeoid) {
        return remote.ensurePool(dbName, appName, appGeoid);
    }

    // ═══════════════════════════════════════════════════════════════
    // DaliPerfStats
    // ═══════════════════════════════════════════════════════════════

    private void writePerfStats(String sid, SemanticResult result, PipelineTimer timer,
                                String dbName,
                                int cntTables, int cntColumns, int cntStatements, int cntRoutines,
                                int cntAtoms, int cntJoins, int cntOutputCols, int cntLineage) {
        int atomResolvedTmp = 0, atomConstTmp = 0, atomFuncTmp = 0, atomFailedTmp = 0;
        for (var container : result.getAtoms().entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cont = (Map<String, Object>) container.getValue();
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> atoms =
                    (Map<String, Map<String, Object>>) cont.get("atoms");
            if (atoms == null) continue;
            for (var at : atoms.entrySet()) {
                String status = (String) at.getValue().get("status");
                if (AtomInfo.STATUS_RESOLVED.equals(status))         atomResolvedTmp++;
                else if ("constant".equals(status))      atomConstTmp++;
                else if ("function_call".equals(status)) atomFuncTmp++;
                else                                     atomFailedTmp++;
            }
        }
        final int atomResolved = atomResolvedTmp;
        final int atomConst    = atomConstTmp;
        final int atomFunc     = atomFuncTmp;
        final int atomFailed   = atomFailedTmp;

        remote.writePerfStats(sid, result, timer, dbName,
                cntTables, cntColumns, cntStatements, cntRoutines,
                cntAtoms, cntJoins, cntOutputCols, cntLineage,
                atomResolved, atomConst, atomFunc, atomFailed);
    }

    // ═══════════════════════════════════════════════════════════════
    // cleanAll
    // ═══════════════════════════════════════════════════════════════

    public void cleanAll() {
        String[] edgeTypes = {
                "ATOM_REF_TABLE","ATOM_REF_COLUMN","ATOM_REF_STMT","ATOM_REF_OUTPUT_COL","ATOM_PRODUCES",
                "DATA_FLOW","FILTER_FLOW","JOIN_FLOW","UNION_FLOW",
                "JOIN_SOURCE_TABLE","JOIN_TARGET_TABLE",
                "HAS_AFFECTED_COL","AFFECTED_COL_REF_TABLE",
                "HAS_ATOM","HAS_OUTPUT_COL","HAS_JOIN","READS_FROM","WRITES_TO",
                "USES_SUBQUERY","NESTED_IN","CONTAINS_STMT",
                "HAS_PARAMETER","HAS_VARIABLE","CHILD_OF","CONTAINS_ROUTINE",
                "ROUTINE_USES_TABLE","CALLS",
                "BULK_COLLECTS_INTO","RECORD_USED_IN","HAS_RECORD_FIELD","FIELD_MAPS_TO",
                "HAS_COLUMN","CONTAINS_TABLE","CONTAINS_SCHEMA","BELONGS_TO_APP","BELONGS_TO_SESSION",
                // v28: DaliStatement -[HAS_SNIPPET]-> DaliSnippet
                "HAS_SNIPPET"
        };
        String[] vtxTypes = {
                "DaliAffectedColumn","DaliAtom","DaliOutputColumn","DaliJoin",
                "DaliParameter","DaliVariable","DaliStatement",
                "DaliColumn","DaliPackage","DaliRoutine","DaliTable",
                "DaliSchema","DaliSession","DaliDatabase",
                "DaliApplication","DaliRecord","DaliRecordField",
                // v28: DaliSnippet promoted from document to vertex
                "DaliSnippet"
        };
        String[] docTypes = {
                "DaliSnippetScript"
        };

        remote.cleanAll(edgeTypes, vtxTypes, docTypes);
        logger.info("ArcadeDB CLEAN: all Dali records deleted ({})", mode);
    }

    // ═══════════════════════════════════════════════════════════════
    // Lifecycle
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void close() {
        // Remote connections are stateless HTTP — nothing to close
    }
}
