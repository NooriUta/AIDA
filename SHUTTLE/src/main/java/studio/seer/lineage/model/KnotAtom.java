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
