package studio.seer.lineage.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import studio.seer.lineage.heimdall.HeimdallEmitter;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.model.*;
import studio.seer.lineage.service.KnotService;

import java.util.List;
import java.util.Map;

/**
 * GraphQL API for KNOT — Hound session analytics report.
 *
 * knotSessions  → sidebar list of all parsed sessions
 * knotReport    → full report for one session (summary + tables + statements)
 */
@GraphQLApi
public class KnotResource {

    @Inject KnotService     knotService;
    @Inject HeimdallEmitter heimdall;

    @Query("knotSessions")
    @Description("KNOT — list of all parsed Hound sessions (for sidebar). Role: viewer+")
    public Uni<List<KnotSession>> knotSessions() {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0, Map.of("op", "knotSessions", "call", "knotSessions()"));
        return knotService.knotSessions()
                .invoke(sessions -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotSessions",
                               "count", sessions != null ? sessions.size() : 0,
                               "call", "knotSessions() → " + (sessions != null ? sessions.size() : 0) + " sessions")));
    }

    @Query("knotReport")
    @Description("KNOT — full report for one session: summary + tables + statements. Role: viewer+")
    public Uni<KnotReport> knotReport(
        @Name("sessionId")
        @Description("session_id property of the DaliSession vertex")
        String sessionId,
        @Name("sourceFile") @DefaultValue("")
        @Description("Optional source_file filter for per-file view in batch sessions (G4). Empty = all files.")
        String sourceFile
    ) {
        long start = System.currentTimeMillis();
        String effectiveFile = (sourceFile != null && !sourceFile.isBlank()) ? sourceFile : null;
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0,
                Map.of("op", "knotReport", "sessionId", sessionId != null ? sessionId : "",
                       "sourceFile", effectiveFile != null ? effectiveFile : "",
                       "call", "knotReport(sessionId=" + (sessionId != null ? sessionId : "") + ")"));
        return knotService.knotReport(sessionId, effectiveFile)
                .invoke(__ -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotReport",
                               "sessionId", sessionId != null ? sessionId : "",
                               "call", "knotReport(sessionId=" + (sessionId != null ? sessionId : "") + ")")));
    }

    @Query("knotSnippet")
    @Description("KNOT — lazy SQL snippet fetch for one statement by stmt_geoid. Role: viewer+")
    public Uni<String> knotSnippet(
        @Name("stmtGeoid")
        @Description("stmt_geoid of the DaliStatement")
        String stmtGeoid
    ) {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0,
                Map.of("op", "knotSnippet",
                       "call", "knotSnippet(stmtGeoid=" + (stmtGeoid != null ? stmtGeoid : "") + ")"));
        return knotService.knotSnippet(stmtGeoid)
                .invoke(s -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotSnippet",
                               "stmtGeoid", stmtGeoid != null ? stmtGeoid : "",
                               "call", "knotSnippet(stmtGeoid=" + (stmtGeoid != null ? stmtGeoid : "")
                                       + ") → " + (s != null ? s.length() + " chars" : "null"))));
    }

    @Query("knotStatementExtras")
    @Description("KNOT — recursive subquery tree + atom stats for one statement by @rid or stmt_geoid. Role: viewer+")
    public Uni<StatementExtras> knotStatementExtras(
        @Name("stmtGeoid")
        @Description("stmt_geoid or @rid of the DaliStatement")
        String stmtGeoid
    ) {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0,
                Map.of("op", "knotStatementExtras",
                       "call", "knotStatementExtras(stmtGeoid=" + (stmtGeoid != null ? stmtGeoid : "") + ")"));
        return knotService.knotStatementExtras(stmtGeoid)
                .invoke(extras -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotStatementExtras",
                               "stmtGeoid", stmtGeoid != null ? stmtGeoid : "",
                               "call", "knotStatementExtras(stmtGeoid=" + (stmtGeoid != null ? stmtGeoid : "")
                                       + ") → " + (extras != null
                                                    ? extras.descendants().size() + " desc, "
                                                      + extras.totalAtomCount() + " atoms"
                                                    : "null"))));
    }

    @Query("knotTableDetail")
    @Description("KNOT — lazy column detail for one table: PK/FK/type/default + SQL snippet. Role: viewer+")
    public Uni<KnotTableDetail> knotTableDetail(
        @Name("sessionId")
        @Description("session_id of the DaliSession — used to scope snippet lookup")
        String sessionId,
        @Name("tableGeoid")
        @Description("table_geoid of the DaliTable vertex")
        String tableGeoid
    ) {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0,
                Map.of("op", "knotTableDetail",
                       "call", "knotTableDetail(sessionId=" + (sessionId != null ? sessionId : "")
                               + ", tableGeoid=" + (tableGeoid != null ? tableGeoid : "") + ")"));
        return knotService.knotTableDetail(sessionId, tableGeoid)
                .invoke(detail -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotTableDetail",
                               "tableGeoid", tableGeoid != null ? tableGeoid : "",
                               "call", "knotTableDetail(tableGeoid=" + (tableGeoid != null ? tableGeoid : "")
                                       + ") → " + (detail != null ? detail.columns().size() : 0) + " cols")));
    }

    @Query("knotTableRoutines")
    @Description("KNOT — routines and statements that read from or write to a table (by @rid). Lazy — fired only when the Routines section is opened. Role: viewer+")
    public Uni<List<KnotTableUsage>> knotTableRoutines(
        @Name("tableRid")
        @Description("ArcadeDB @rid of the DaliTable vertex, e.g. #16:4404")
        String tableRid
    ) {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0,
                Map.of("op", "knotTableRoutines",
                       "call", "knotTableRoutines(tableRid=" + (tableRid != null ? tableRid : "") + ")"));
        return knotService.knotTableRoutines(tableRid)
                .invoke(list -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotTableRoutines",
                               "tableRid", tableRid != null ? tableRid : "",
                               "call", "knotTableRoutines(tableRid=" + (tableRid != null ? tableRid : "")
                                       + ") → " + (list != null ? list.size() : 0) + " rows")));
    }

    @Query("knotColumnStatements")
    @Description("KNOT — statements that reference a given column (by column_geoid). Lazy expand — fires only on user request. Role: viewer+")
    public Uni<List<KnotColumnUsage>> knotColumnStatements(
        @Name("columnGeoid")
        @Description("column_geoid of the DaliColumn, e.g. DWH.STAGE_ADDRESSES.ID")
        String columnGeoid
    ) {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0,
                Map.of("op", "knotColumnStatements",
                       "call", "knotColumnStatements(columnGeoid=" + (columnGeoid != null ? columnGeoid : "") + ")"));
        return knotService.knotColumnStatements(columnGeoid)
                .invoke(list -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotColumnStatements",
                               "columnGeoid", columnGeoid != null ? columnGeoid : "",
                               "call", "knotColumnStatements(columnGeoid=" + (columnGeoid != null ? columnGeoid : "")
                                       + ") → " + (list != null ? list.size() : 0) + " rows")));
    }

    @Query("knotScript")
    @Description("KNOT — full source file text for a session (from DaliSnippetScript). Lazy — fires only when the Source tab is open. Role: viewer+")
    public Uni<KnotScript> knotScript(
        @Name("sessionId")
        @Description("session_id of the DaliSession whose source file to retrieve")
        String sessionId
    ) {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0,
                Map.of("op", "knotScript",
                       "call", "knotScript(sessionId=" + (sessionId != null ? sessionId : "") + ")"));
        return knotService.knotScript(sessionId)
                .invoke(s -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotScript",
                               "sessionId", sessionId != null ? sessionId : "",
                               "call", "knotScript(sessionId=" + (sessionId != null ? sessionId : "")
                                       + ") → " + (s != null ? s.lineCount() + " lines, "
                                               + s.charCount() + " chars" : "null"))));
    }

    @Query("plTypes")
    @Description("HND-07 — list PL/SQL TYPE templates (RECORD / COLLECTION) for a lineage session. Role: viewer+")
    public Uni<List<KnotPlType>> plTypes(
        @Name("sessionId")
        @Description("session_id of the DaliSession to query")
        String sessionId
    ) {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0,
                Map.of("op", "plTypes",
                       "call", "plTypes(sessionId=" + (sessionId != null ? sessionId : "") + ")"));
        return knotService.plTypes(sessionId)
                .invoke(list -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "plTypes",
                               "sessionId", sessionId != null ? sessionId : "",
                               "call", "plTypes(sessionId=" + (sessionId != null ? sessionId : "")
                                       + ") → " + (list != null ? list.size() : 0) + " types")));
    }

    @Query("knotSourceFile")
    @Description("KNOT — full source file from the source archive (hound_src_{tenant}). Two-step lookup: session_id → DaliSession.file_path (lineage DB) → DaliSourceFile (archive DB). Lazy — fires only when the Source tab is open. Role: viewer+")
    public Uni<KnotSourceFile> knotSourceFile(
        @Name("sessionId")
        @Description("session_id of the DaliSession (Hound format: session-{timestamp})")
        String sessionId
    ) {
        long start = System.currentTimeMillis();
        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO,
                null, null, 0,
                Map.of("op", "knotSourceFile",
                       "call", "knotSourceFile(sessionId=" + (sessionId != null ? sessionId : "") + ")"));
        return knotService.knotSourceFile(sessionId)
                .invoke(f -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
                        null, null, System.currentTimeMillis() - start,
                        Map.of("op", "knotSourceFile",
                               "sessionId", sessionId != null ? sessionId : "",
                               "call", "knotSourceFile(sessionId=" + (sessionId != null ? sessionId : "")
                                       + ") → " + (f != null ? f.sizeBytes() + " bytes" : "null"))));
    }
}
