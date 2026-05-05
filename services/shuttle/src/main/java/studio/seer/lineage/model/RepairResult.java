package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

import java.util.List;

@Description("Result of a hierarchy edge repair operation")
public record RepairResult(
        boolean dryRun,
        int     containsTableCreated,
        int     hasColumnCreated,
        int     totalCreated,
        List<String> errors
) {}
