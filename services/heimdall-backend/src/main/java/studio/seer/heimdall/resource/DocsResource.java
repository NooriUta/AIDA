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
 * Temporary dev-only endpoint — browse docs/ directory from the browser.
 *
 * Enabled via docker-compose.yml volume mount:
 *   heimdall-backend:
 *     volumes:
 *       - ./docs:/docs:ro
 *
 * GET /docs         → JSON array of .md file paths (relative)
 * GET /docs/{path}  → raw Markdown content (text/plain)
 *
 * Path-traversal is blocked: any request that resolves outside DOCS_ROOT
 * returns 403.
 */
@Path("/docs")
public class DocsResource {

    @ConfigProperty(name = "docs.root", defaultValue = "/docs")
    String docsRoot;

    /**
     * Resolve effective docs root:
     *  1. Use docs.root if it's a valid directory (Docker: /docs mounted as volume).
     *  2. Walk up from user.dir looking for a "docs" sibling that contains .md files
     *     (dev mode: works whether Gradle runs from repo root or subproject dir).
     */
    private java.nio.file.Path effectiveRoot() {
        java.nio.file.Path configured = Paths.get(docsRoot);
        if (Files.isDirectory(configured)) return configured;

        // Walk up from CWD looking for a docs/ directory with .md files
        java.nio.file.Path dir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        for (int i = 0; i < 5; i++) {
            java.nio.file.Path candidate = dir.resolve("docs");
            if (Files.isDirectory(candidate)) {
                // Check for .md files at any depth (post-restructuring: all in subdirs)
                try (var s = Files.walk(candidate, 3)) {
                    if (s.anyMatch(p -> p.toString().endsWith(".md"))) return candidate;
                } catch (IOException ignored) {}
            }
            java.nio.file.Path parent = dir.getParent();
            if (parent == null) break;
            dir = parent;
        }
        return configured;
    }

    /** List all .md files under DOCS_ROOT, sorted, relative paths. */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> list() throws IOException {
        java.nio.file.Path root = effectiveRoot();
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

    /** Return the raw content of a single .md file. */
    @GET
    @Path("{path: .+}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response content(@PathParam("path") String path) throws IOException {
        java.nio.file.Path root = effectiveRoot().toAbsolutePath().normalize();
        java.nio.file.Path file = root.resolve(path).normalize();

        // Block path traversal
        if (!file.startsWith(root)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied").build();
        }

        if (!Files.isRegularFile(file) || !file.toString().endsWith(".md")) {
            return Response.status(Response.Status.NOT_FOUND).entity("Not found").build();
        }

        return Response.ok(Files.readString(file)).build();
    }
}
