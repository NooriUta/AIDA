package studio.seer.heimdall.resource;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Parallel health-ping for all platform services — both IDE (dev) and Docker instances.
 *
 * GET  /services/health  → [{name, port, mode, status, latencyMs}, ...]
 *      mode = "dev"    — process running directly in IDE (standard ports)
 *      mode = "docker" — container exposed via host port (docker-compose port offset)
 *
 * POST /services/{name}/restart?mode=docker  → restarts Docker container only
 *      Requires X-Seer-Role: admin
 *
 * Services list is driven by application.properties:
 *   seer.services.dev    — comma-separated "name:port:type[:healthPath]"
 *   seer.services.docker — same format for Docker instances
 *
 * Overridable at runtime via env vars SEER_SERVICES_DEV / SEER_SERVICES_DOCKER.
 */
@Path("/services")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ServicesResource {

    public record ServiceStatus(String name, int port, String mode, String status, long latencyMs, String version) {}

    private record ServiceDef(String name, int port, String pingHost, int pingPort,
                              String mode, String healthPath, boolean self, boolean tcp) {
        /** Actual URL used for health-check (Docker: uses internal DNS + port, dev: localhost). */
        String url() { return "http://" + pingHost + ":" + pingPort + healthPath; }
    }

    @ConfigProperty(name = "seer.services.dev")
    Optional<List<String>> devEntries;

    @ConfigProperty(name = "seer.services.docker")
    Optional<List<String>> dockerEntries;

    /** Self version — other services' versions come from their own /q/info/build (best-effort). */
    @ConfigProperty(name = "quarkus.application.version", defaultValue = "unknown")
    String selfVersion;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    // Heimdall must not restart itself — it would kill the connection.
    private static final Set<String> NO_RESTART = Set.of("heimdall-backend");

    /**
     * Parses service entries.  Format (all fields after type are optional):
     *   name:displayPort:type[:healthPath[:internalPort[:pingName]]]
     *
     * type         = http | tcp | self
     * healthPath   — appended to ping URL for http checks (default "/")
     * internalPort — container-internal port used for the actual health-check
     *                (defaults to displayPort; ignored for "self")
     * pingName     — Docker DNS hostname to ping (defaults to name;
     *                use for external containers whose DNS name differs, e.g. HoundArcade)
     *
     * For docker mode the ping is sent to http://pingName:internalPort,
     * so the backend container reaches peers via Docker's internal network.
     * For dev mode the ping always goes to localhost:displayPort.
     */
    private List<ServiceDef> parseEntries(List<String> entries, String mode) {
        boolean isDocker = "docker".equals(mode);
        List<ServiceDef> result = new ArrayList<>();
        for (String entry : entries) {
            String trimmed = entry.strip();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split(":", -1);
            if (parts.length < 3) {
                continue; // malformed, skip
            }
            String name        = parts[0];
            int    displayPort = Integer.parseInt(parts[1]);
            String type        = parts[2];
            String healthPath  = parts.length >= 4 && !parts[3].isEmpty() ? parts[3] : "/";
            int    pingPort    = parts.length >= 5 && !parts[4].isEmpty()
                                     ? Integer.parseInt(parts[4]) : displayPort;
            // Dev mode: use 127.0.0.1 explicitly — on Windows, "localhost" resolves to
            // ::1 (IPv6) first, but Node.js/Vite/Quarkus dev servers bind IPv4 only.
            String pingHost    = isDocker
                                     ? (parts.length >= 6 && !parts[5].isEmpty() ? parts[5] : name)
                                     : "127.0.0.1";

            result.add(switch (type) {
                case "tcp"  -> new ServiceDef(name, displayPort, pingHost, pingPort, mode, "/",        false, true);
                case "self" -> new ServiceDef(name, displayPort, pingHost, pingPort, mode, "/",        true,  false);
                default     -> new ServiceDef(name, displayPort, pingHost, pingPort, mode, healthPath, false, false);
            });
        }
        return result;
    }

    private List<ServiceDef> services() {
        List<ServiceDef> list = new ArrayList<>();
        devEntries   .ifPresent(e -> list.addAll(parseEntries(e, "dev")));
        dockerEntries.ifPresent(e -> list.addAll(parseEntries(e, "docker")));
        return list;
    }

    // ── Health ─────────────────────────────────────────────────────────────────

    @GET
    @Path("/health")
    public Uni<List<ServiceStatus>> health() {
        return Multi.createFrom().iterable(services())
                .onItem().transformToUniAndMerge(this::ping)
                .collect().asList();
    }

    // ── Restart (Docker only) ──────────────────────────────────────────────────

    @POST
    @Path("/{name}/restart")
    public Response restart(
            @PathParam("name")         String name,
            @QueryParam("mode")        String mode,
            @HeaderParam("X-Seer-Role") String role) {

        if (!"admin".equals(role)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        if (!"docker".equals(mode)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"restart only supported for Docker instances\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        if (NO_RESTART.contains(name)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"protected service\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        try {
            // Derive container name from compose project name + service name.
            // Convention: {COMPOSE_PROJECT_NAME}-{service}-1  (single replica).
            String project = System.getenv().getOrDefault("COMPOSE_PROJECT_NAME", "aida-root");
            String containerName = project + "-" + name + "-1";
            int exit = new ProcessBuilder("docker", "restart", containerName)
                    .redirectErrorStream(true)
                    .start()
                    .waitFor();
            return exit == 0
                    ? Response.ok().build()
                    : Response.serverError()
                              .entity("{\"error\":\"docker restart " + containerName + " exited " + exit + "\"}")
                              .type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return Response.status(503)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    // ── Internal ───────────────────────────────────────────────────────────────

    private Uni<ServiceStatus> ping(ServiceDef svc) {
        // Self — always up, no network round-trip needed.
        if (svc.self()) {
            return Uni.createFrom().item(
                new ServiceStatus(svc.name(), svc.port(), svc.mode(), "self", 0, versionFor(svc)));
        }
        // TCP — just check the port is open (Vite dev servers, etc.)
        // InetSocketAddress(hostname, port) does a blocking DNS lookup; Socket.connect() is
        // blocking I/O. Both must run on a worker thread, not the Vert.x event loop.
        if (svc.tcp()) {
            return Uni.createFrom().<ServiceStatus>item(() -> {
                long start = System.currentTimeMillis();
                try (Socket s = new Socket()) {
                    s.connect(new InetSocketAddress(svc.pingHost(), svc.pingPort()), 1500);
                    return new ServiceStatus(svc.name(), svc.port(), svc.mode(), "up", System.currentTimeMillis() - start, versionFor(svc));
                } catch (Exception e) {
                    return new ServiceStatus(svc.name(), svc.port(), svc.mode(), "down", System.currentTimeMillis() - start, null);
                }
            }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
        }
        return Uni.createFrom().item(System::currentTimeMillis)
                .onItem().transformToUni(start -> {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(svc.url()))
                            .timeout(Duration.ofSeconds(2))
                            .GET()
                            .build();
                    return Uni.createFrom()
                            .completionStage(() -> HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding()))
                            .onItem().transform(resp -> {
                                long latency = System.currentTimeMillis() - start;
                                String status = resp.statusCode() < 500 ? "up" : "degraded";
                                return new ServiceStatus(svc.name(), svc.port(), svc.mode(), status, latency, versionFor(svc));
                            })
                            .onFailure().recoverWithItem(e -> {
                                long latency = System.currentTimeMillis() - start;
                                return new ServiceStatus(svc.name(), svc.port(), svc.mode(), "down", latency, null);
                            });
                });
    }

    // Self version — known at startup; peer services' versions would require
    // per-service /q/info/build (needs quarkus-info extension on every peer).
    // Left as TODO: wire peer fetch once extension is enabled cluster-wide.
    private String versionFor(ServiceDef svc) {
        return "heimdall-backend".equals(svc.name()) ? selfVersion : null;
    }
}
