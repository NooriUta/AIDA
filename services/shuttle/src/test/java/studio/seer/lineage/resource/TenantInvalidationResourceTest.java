package studio.seer.lineage.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import studio.seer.tenantrouting.YggLineageRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantInvalidationResource — MTN-01 internal invalidation endpoint.
 *
 * Verifies:
 * - Bad / missing X-Internal-Auth → 403
 * - Null body → 400
 * - all=true → invalidateAll() + 200
 * - tenantAlias only → invalidate(alias) + 200
 * - neither tenantAlias nor all=true → 400
 *
 * SHT-P3D-4: TenantInvalidationResource coverage
 */
@QuarkusTest
class TenantInvalidationResourceTest {

    @InjectMock YggLineageRegistry lineageRegistry;

    @Inject TenantInvalidationResource resource;

    private static final String SECRET = "aida-internal-dev-secret"; // matches @ConfigProperty defaultValue

    // ── Auth guard ─────────────────────────────────────────────────────────────

    @Test
    void missingAuth_returns403() {
        Response resp = resource.invalidate(null,
                new TenantInvalidationResource.InvalidateRequest("acme", null));

        assertEquals(403, resp.getStatus());
        verifyNoInteractions(lineageRegistry);
    }

    @Test
    void wrongAuth_returns403() {
        Response resp = resource.invalidate("wrong-secret",
                new TenantInvalidationResource.InvalidateRequest("acme", null));

        assertEquals(403, resp.getStatus());
        verifyNoInteractions(lineageRegistry);
    }

    // ── Body validation ────────────────────────────────────────────────────────

    @Test
    void nullBody_returns400() {
        Response resp = resource.invalidate(SECRET, null);

        assertEquals(400, resp.getStatus());
        verifyNoInteractions(lineageRegistry);
    }

    @Test
    void emptyAlias_andNoAllFlag_returns400() {
        Response resp = resource.invalidate(SECRET,
                new TenantInvalidationResource.InvalidateRequest("", null));

        assertEquals(400, resp.getStatus());
        verifyNoInteractions(lineageRegistry);
    }

    @Test
    void blankAlias_andNoAllFlag_returns400() {
        Response resp = resource.invalidate(SECRET,
                new TenantInvalidationResource.InvalidateRequest("   ", null));

        assertEquals(400, resp.getStatus());
        verifyNoInteractions(lineageRegistry);
    }

    @Test
    void nullAlias_andNoAllFlag_returns400() {
        Response resp = resource.invalidate(SECRET,
                new TenantInvalidationResource.InvalidateRequest(null, null));

        assertEquals(400, resp.getStatus());
        verifyNoInteractions(lineageRegistry);
    }

    // ── Happy paths ────────────────────────────────────────────────────────────

    @Test
    void allTrue_callsInvalidateAll_returns200() {
        Response resp = resource.invalidate(SECRET,
                new TenantInvalidationResource.InvalidateRequest(null, true));

        assertEquals(200, resp.getStatus());
        verify(lineageRegistry).invalidateAll();
        verify(lineageRegistry, never()).invalidate(anyString());
    }

    @Test
    void specificTenant_callsInvalidate_returns200() {
        Response resp = resource.invalidate(SECRET,
                new TenantInvalidationResource.InvalidateRequest("acme", null));

        assertEquals(200, resp.getStatus());
        verify(lineageRegistry).invalidate("acme");
        verify(lineageRegistry, never()).invalidateAll();
    }

    @Test
    void allTrueWithAlias_allFlagTakesPrecedence() {
        // When all=true is set together with tenantAlias, all=true wins (checked first in the source)
        Response resp = resource.invalidate(SECRET,
                new TenantInvalidationResource.InvalidateRequest("acme", true));

        assertEquals(200, resp.getStatus());
        verify(lineageRegistry).invalidateAll();
        verify(lineageRegistry, never()).invalidate(anyString());
    }
}
