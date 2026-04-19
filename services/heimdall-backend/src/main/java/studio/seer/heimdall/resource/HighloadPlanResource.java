package studio.seer.heimdall.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/highload-plan")
public class HighloadPlanResource {

    @ConfigProperty(name = "team-docs.root", defaultValue = "/team-docs")
    String docsRoot;

    @ConfigProperty(name = "highload.plan.file", defaultValue = "PLAN_APR_OCT_2026.html")
    String planFile;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get() throws IOException {
        java.nio.file.Path file = Paths.get(docsRoot, planFile).normalize();
        if (!Files.isRegularFile(file)) {
            return Response.status(Response.Status.NOT_FOUND).entity("Plan not found").build();
        }
        return Response.ok(Files.readString(file)).build();
    }
}
