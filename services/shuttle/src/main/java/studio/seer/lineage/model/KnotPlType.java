package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

import java.util.List;

@Description("KNOT — PL/SQL user-defined TYPE template (RECORD or COLLECTION)")
public record KnotPlType(
    String            typeGeoid,
    String            typeName,
    String            kind,              // "RECORD" | "COLLECTION"
    String            elementTypeGeoid,  // for COLLECTION: geoid of the element RECORD type
    String            scopeGeoid,        // declaring package or routine geoid
    int               declaredAtLine,
    List<KnotPlTypeField> fields         // non-empty for RECORD kinds
) {}
