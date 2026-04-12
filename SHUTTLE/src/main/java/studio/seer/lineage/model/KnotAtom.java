package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — individual atom (column reference, constant, function call, etc.)")
public record KnotAtom(
    String  stmtGeoid,
    String  atomText,
    String  columnName,
    String  tableGeoid,
    String  tableName,
    String  status,
    String  atomContext,
    String  parentContext,
    Integer outputColumnSequence,
    String  outputColName,
    String  refSourceName,        // out('ATOM_REF_OUTPUT_COL') → DaliOutputColumn.output_col_name
    String  refStmtGeoid,        // out('ATOM_REF_STMT')       → DaliStatement.stmt_geoid
    String  refColEdge,          // out('ATOM_REF_COLUMN')     → DaliColumn.column_name
    String  refTblEdge,          // out('ATOM_REF_TABLE')      → DaliTable.table_name
    String  refTblGeoidEdge,     // out('ATOM_REF_TABLE')      → DaliTable.table_geoid (NOT @rid!)
    boolean isColumnReference,
    boolean isFunctionCall,
    boolean isConstant,
    boolean isComplex,
    boolean isRoutineParam,
    boolean isRoutineVar,
    Integer nestedAtomsCount,
    int     atomLine,
    int     atomPos
) {}
