package studio.seer.heimdall.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GET /team-docs         → JSON array of .md file paths (relative)
 * GET /team-docs/{path}  → raw Markdown content (text/plain)
 *
 * Returns empty list when /team-docs volume is not mounted — frontend auto-hides the tab.
 * Path-traversal is blocked: any request resolving outside TEAM_DOCS_ROOT returns 403.
 */
@Path("/team-docs")
public class TeamDocsResource {

    @ConfigProperty(name = "team-docs.root", defaultValue = "/team-docs")
    String teamDocsRoot;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> list() throws IOException {
        java.nio.file.Path root = Paths.get(teamDocsRoot);
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (var stream = Files.walk(root)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".md"))
                .map(p -> root.relativize(p).toString().replace('\\', '/'))
                .sorted()
                .collect(Collectors.toList());
        }
    }

    @GET
    @Path("{path: .+}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response content(@PathParam("path") String path) throws IOException {
        java.nio.file.Path root = Paths.get(teamDocsRoot).toAbsolutePath().normalize();
        java.nio.file.Path file = root.resolve(path).normalize();

        if (!file.startsWith(root)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
        }

        if (!Files.isRegularFile(file) || !file.toString().endsWith(".md")) {
            return Response.status(Response.Status.NOT_FOUND).entity("Not found").build();
        }

        return Response.ok(Files.readString(file)).build();
    }
}
