package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — routine parameter (HAS_PARAMETER edge)")
public record KnotParameter(
    String routineName,
    String paramName,
    String dataType,
    String direction   // IN, OUT, IN OUT
) {}
