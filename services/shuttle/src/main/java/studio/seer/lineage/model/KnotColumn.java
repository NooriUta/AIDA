package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Name;

@Description("KNOT — column within a table")
public record KnotColumn(
    String  id,
    String  name,
    String  dataType,
    int     position,
    int     atomRefCount,   // ATOM_REF_COLUMN edges pointing here (0 in summary)
    String  alias,          // DaliColumn.alias
    @Name("isRequired") boolean isRequired,     // NOT NULL constraint
    @Name("isPk")       boolean isPk,           // participates in PRIMARY KEY
    @Name("isFk")       boolean isFk,           // FOREIGN KEY column
    String  fkRefTable,     // referenced table geoid (FK only)
    String  defaultValue,   // DEFAULT expression text (from DDL)
    String  dataSource      // 'master' | 'reconstructed'
) {}
