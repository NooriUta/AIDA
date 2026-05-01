package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("One field of a PL/SQL TYPE IS RECORD template")
public record KnotPlTypeField(
    String fieldGeoid,
    String fieldName,
    String fieldType,   // declared data type, e.g. "NUMBER(19)"
    int    position     // 1-based declaration order
) {}
