package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("KNOT — source or target reference (table or subquery/CTE) with aliases")
public record KnotSourceRef(
    String       name,       // table_name OR short stmt name parsed from geoid
    String       geoid,      // table_geoid (NOT @rid!) OR stmt_geoid
    List<String> aliases,    // aliases from READS_FROM / WRITES_TO edge
    String       nodeType    // "TABLE" | "STMT"
) {}
