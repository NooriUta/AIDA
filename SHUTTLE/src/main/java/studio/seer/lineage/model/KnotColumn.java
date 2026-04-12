package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — column within a table")
public record KnotColumn(
    String id,
    String name,
    String dataType,
    int    position,
    int    atomRefCount,  // ATOM_REF_COLUMN edges pointing here
    String alias          // DaliColumn.alias
) {}
