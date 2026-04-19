package studio.seer.lineage.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import studio.seer.lineage.model.DaliApplicationDto;
import studio.seer.lineage.service.ApplicationsService;

import java.util.List;

/**
 * GraphQL API — App → DB → Schema hierarchy for LOOM L1.
 *
 * Supplements the flat {@code overview} query with a structured hierarchy
 * once DaliApplication / BELONGS_TO_APP edges are populated by Hound.
 */
@GraphQLApi
public class ApplicationsResource {

    @Inject ApplicationsService service;

    @Query("applications")
    @Description("L1 — full App → DB → Schema hierarchy. Role: viewer+")
    public Uni<List<DaliApplicationDto>> applications() {
        return service.fetchApplicationHierarchy();
    }
}
