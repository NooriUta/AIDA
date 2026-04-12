package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — routine call relationship (CALLS edge between DaliRoutine nodes)")
public record KnotCall(
    String callerName,    // DaliRoutine.routine_name of the caller
    String callerPackage, // derived package geoid prefix
    String calleeName,    // CALLS edge: callee_name property
    int    lineStart      // CALLS edge: line_start property
) {}
