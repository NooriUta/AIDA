package com.mimir.rest;

import com.mimir.byok.CredentialException;
import com.mimir.byok.CredentialMasker;
import com.mimir.byok.LlmCredentialResolver;
import com.mimir.byok.TenantLlmConfig;
import com.mimir.byok.TenantLlmConfigStore;
import com.mimir.heimdall.MimirEventEmitter;
import com.mimir.quota.DailyUsage;
import com.mimir.quota.TenantQuota;
import com.mimir.quota.TenantQuotaGuard;
import com.mimir.quota.TenantQuotaStore;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Admin endpoints for per-tenant BYOK LLM configuration (TIER2 MT-06.3).
 *
 * <p>Auth model: Chur BFF terminates JWT and forwards admin calls with
 * {@code X-Seer-Role: admin}. MIMIR trusts the Chur-fronted header — direct
 * exposure of these endpoints (without Chur) MUST be blocked at the network
 * level (k8s policy / SG). Strict JWT verification is on the MT-06.3 backlog
 * once Chur scope claims are stable.
 *
 * <p>Plain {@code apiKey} is encrypted before persistence. {@link #get} returns
 * a masked view only (the plaintext is never readable through the API).
 */
@Path("/api/admin/llm-config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminLlmConfigResource {

    private static final Logger LOG = Logger.getLogger(AdminLlmConfigResource.class);

    @Inject TenantLlmConfigStore   store;
    @Inject LlmCredentialResolver  resolver;
    @Inject MimirEventEmitter      emitter;
    @Inject TenantQuotaStore       quotaStore;
    @Inject TenantQuotaGuard       quotaGuard;

    public record UpsertRequest(
            @NotBlank String tenantAlias,
            @NotBlank String provider,
            @NotBlank String apiKey,
            String baseUrl,
            String modelName) {}

    @POST
    public Response upsert(UpsertRequest body, @HeaderParam("X-Seer-Role") String role) {
        requireAdmin(role);
        if (body == null) throw new WebApplicationException("missing body", 400);
        try {
            store.save(body.tenantAlias(), body.provider(), body.apiKey(), body.baseUrl(), body.modelName());
            resolver.invalidate(body.tenantAlias());
            emitter.llmConfigUpdated(body.tenantAlias(), body.provider().toLowerCase(), "set", role);
            LOG.infof("BYOK config upserted: tenant=%s provider=%s key=%s admin=%s",
                    body.tenantAlias(), body.provider(), CredentialMasker.mask(body.apiKey()), role);
            return Response.ok(Map.of(
                    "tenantAlias", body.tenantAlias(),
                    "provider",    body.provider().toLowerCase(),
                    "keyMask",     CredentialMasker.mask(body.apiKey()))).build();
        } catch (CredentialException e) {
            throw new WebApplicationException(e.getMessage(), 400);
        }
    }

    @DELETE
    @Path("/{tenantAlias}")
    public Response delete(@PathParam("tenantAlias") String tenantAlias,
                           @HeaderParam("X-Seer-Role") String role) {
        requireAdmin(role);
        boolean ok = store.delete(tenantAlias);
        resolver.invalidate(tenantAlias);
        emitter.llmConfigUpdated(tenantAlias, null, "delete", role);
        LOG.infof("BYOK config deleted: tenant=%s admin=%s ok=%s", tenantAlias, role, ok);
        return ok ? Response.noContent().build()
                  : Response.status(404).entity(Map.of("error", "not found")).build();
    }

    @GET
    @Path("/{tenantAlias}")
    public Response get(@PathParam("tenantAlias") String tenantAlias,
                        @HeaderParam("X-Seer-Role") String role) {
        requireAdmin(role);
        Optional<TenantLlmConfig> cfg = store.findByTenant(tenantAlias);
        if (cfg.isEmpty()) {
            return Response.status(404).entity(Map.of("error", "not found")).build();
        }
        TenantLlmConfig c = cfg.get();
        Map<String, Object> safe = new java.util.HashMap<>();
        safe.put("tenantAlias", c.tenantAlias());
        safe.put("provider",    c.provider());
        safe.put("baseUrl",     c.baseUrl());
        safe.put("modelName",   c.modelName());
        safe.put("updatedAt",   c.updatedAt());
        safe.put("keyMask",     CredentialMasker.mask(c.encryptedApiKey()));
        return Response.ok(safe).build();
    }

    private static void requireAdmin(String role) {
        if (!"admin".equalsIgnoreCase(role) && !"superadmin".equalsIgnoreCase(role)) {
            throw new WebApplicationException("admin role required", 403);
        }
    }

    // ── TIER2 MT-07 — quota / usage admin ────────────────────────────────

    @PUT
    @Path("/quota/{tenantAlias}")
    public Response putQuota(@PathParam("tenantAlias") String tenantAlias,
                             @HeaderParam("X-Seer-Role") String role,
                             TenantQuota body) {
        requireAdmin(role);
        if (body == null) throw new WebApplicationException("missing body", 400);
        quotaStore.saveQuota(tenantAlias, body);
        quotaGuard.invalidate(tenantAlias);
        LOG.infof("Quota set: tenant=%s daily=%d/$%.2f monthly=%d/$%.2f admin=%s",
                tenantAlias, body.dailyTokenLimit(), body.dailyCostLimitUsd(),
                body.monthlyTokenLimit(), body.monthlyCostLimitUsd(), role);
        return Response.ok(body).build();
    }

    @GET
    @Path("/quota/{tenantAlias}")
    public Response getQuota(@PathParam("tenantAlias") String tenantAlias,
                             @HeaderParam("X-Seer-Role") String role) {
        requireAdmin(role);
        return quotaStore.findQuota(tenantAlias)
                .map(q -> Response.ok(q).build())
                .orElseGet(() -> Response.ok(TenantQuota.unlimited()).build());
    }

    @GET
    @Path("/usage/{tenantAlias}")
    public Response getUsage(@PathParam("tenantAlias") String tenantAlias,
                             @HeaderParam("X-Seer-Role") String role,
                             @QueryParam("days") Integer days) {
        requireAdmin(role);
        int safeDays = days == null ? 30 : Math.max(1, Math.min(days, 365));
        List<DailyUsage> rows = quotaStore.listUsage(tenantAlias, safeDays);
        long totalTokens = rows.stream().mapToLong(DailyUsage::totalTokens).sum();
        double totalCost = rows.stream().mapToDouble(DailyUsage::costEstimateUsd).sum();
        int totalReq    = rows.stream().mapToInt(DailyUsage::requestCount).sum();
        return Response.ok(Map.of(
                "tenantAlias",  tenantAlias,
                "days",         safeDays,
                "totals",       Map.of(
                        "tokens",       totalTokens,
                        "costUsd",      totalCost,
                        "requestCount", totalReq),
                "daily",        rows)).build();
    }
}
