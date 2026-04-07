package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("KNOT — statement (root or sub) with source/target tables and atom summary")
public record KnotStatement(
    String id,
    String geoid,
    String stmtType,         // SELECT, INSERT, UPDATE, DELETE, MERGE, CURSOR…
    int    lineNumber,
    String routineName,
    String packageName,
    String routineType,      // PROCEDURE, FUNCTION, etc. from DaliRoutine.routine_type

    // Source / Target tables
    List<String> sourceTables,
    List<String> targetTables,

    // Statement aliases (DaliStatement.aliases JSON array)
    List<String> stmtAliases,

    // Atom breakdown
    int atomTotal,
    int atomResolved,
    int atomFailed,
    int atomConstant,

    // Child statements (sub-queries, CTEs)
    List<KnotStatement> children
) {}
