package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("KNOT — parsed session (one SQL file processed by Hound)")
public record KnotSession(
    String id,
    String sessionId,     // DaliSession.session_id — used as key for knotReport
    String sessionName,
    String dialect,
    String filePath,
    long   processingMs,

    // Summary counts
    int tableCount,
    int columnCount,
    int schemaCount,
    int packageCount,
    int routineCount,
    int parameterCount,
    int variableCount,

    // Statement type breakdown
    int stmtSelect,
    int stmtInsert,
    int stmtUpdate,
    int stmtDelete,
    int stmtMerge,
    int stmtCursor,
    int stmtOther,

    // Atom resolution
    int atomTotal,
    int atomResolved,
    int atomFailed,
    int atomConstant,
    int atomFuncCall,

    // Edges (lineage quality)
    int edgeReadsFrom,
    int edgeWritesTo,
    int edgeAtomRefColumn,
    int edgeDataFlow
) {}
