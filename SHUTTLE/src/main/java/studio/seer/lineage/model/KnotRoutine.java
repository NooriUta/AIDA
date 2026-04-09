package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — routine (procedure/function) in a session")
public record KnotRoutine(
    String routineName,
    String routineType,    // PROCEDURE, FUNCTION, TRIGGER…
    String packageGeoid    // empty string for standalone routines
) {}
