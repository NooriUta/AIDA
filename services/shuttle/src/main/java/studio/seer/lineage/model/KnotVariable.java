package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — routine variable (HAS_VARIABLE edge)")
public record KnotVariable(
    String routineName,
    String varName,
    String dataType
) {}
