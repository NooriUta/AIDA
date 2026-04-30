package studio.seer.heimdall.tenant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TenantContext — request-scoped identity bean.
 *
 * Uses direct instantiation (plain POJO, no CDI).
 *
 * HB-P3C-2
 */
class TenantContextTest {

    private TenantContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new TenantContext();
    }

    // ── Setters / getters ─────────────────────────────────────────────────────

    @Test
    void setUserId_getReturnsValue() {
        ctx.setUserId("user-uuid-123");
        assertEquals("user-uuid-123", ctx.getUserId());
    }

    @Test
    void setTenantId_getReturnsValue() {
        ctx.setTenantId("acme");
        assertEquals("acme", ctx.getTenantId());
    }

    @Test
    void setTenantRole_getReturnsValue() {
        ctx.setTenantRole("admin");
        assertEquals("admin", ctx.getTenantRole());
    }

    @Test
    void setScopes_getReturnsValue() {
        ctx.setScopes(List.of("seer:read", "aida:admin"));
        assertEquals(List.of("seer:read", "aida:admin"), ctx.getScopes());
    }

    @Test
    void setScopes_null_defaultsToEmptyList() {
        ctx.setScopes(null);
        assertNotNull(ctx.getScopes());
        assertTrue(ctx.getScopes().isEmpty());
    }

    @Test
    void defaultScopes_isEmpty() {
        // No setScopes() called — default is List.of()
        assertTrue(ctx.getScopes().isEmpty());
    }

    // ── hasScope / scope checks ───────────────────────────────────────────────

    @Test
    void hasScope_scopePresent_returnsTrue() {
        ctx.setScopes(List.of("aida:admin"));
        assertTrue(ctx.hasScope("aida:admin"));
    }

    @Test
    void hasScope_scopeAbsent_returnsFalse() {
        ctx.setScopes(List.of("seer:read"));
        assertFalse(ctx.hasScope("aida:admin"));
    }

    @Test
    void isSuperAdmin_withScope_returnsTrue() {
        ctx.setScopes(List.of("aida:superadmin"));
        assertTrue(ctx.isSuperAdmin());
    }

    @Test
    void isSuperAdmin_withoutScope_returnsFalse() {
        ctx.setScopes(List.of("aida:admin"));
        assertFalse(ctx.isSuperAdmin());
    }

    @Test
    void isAdmin_withScope_returnsTrue() {
        ctx.setScopes(List.of("aida:admin"));
        assertTrue(ctx.isAdmin());
    }

    @Test
    void isTenantOwner_withScope_returnsTrue() {
        ctx.setScopes(List.of("aida:tenant:owner"));
        assertTrue(ctx.isTenantOwner());
    }

    @Test
    void isLocalAdmin_withScope_returnsTrue() {
        ctx.setScopes(List.of("aida:tenant:admin"));
        assertTrue(ctx.isLocalAdmin());
    }

    @Test
    void isAdminRole_adminRole_returnsTrue() {
        ctx.setTenantRole("admin");
        assertTrue(ctx.isAdminRole());
    }

    @Test
    void isAdminRole_superAdminRole_returnsTrue() {
        ctx.setTenantRole("super-admin");
        assertTrue(ctx.isAdminRole());
    }

    @Test
    void isAdminRole_caseInsensitive() {
        ctx.setTenantRole("ADMIN");
        assertTrue(ctx.isAdminRole());
    }

    @Test
    void isAdminRole_viewerRole_returnsFalse() {
        ctx.setTenantRole("viewer");
        assertFalse(ctx.isAdminRole());
    }

    @Test
    void multipleScopes_allChecksWork() {
        ctx.setScopes(List.of("seer:read", "aida:admin", "aida:tenant:admin"));

        assertTrue(ctx.isAdmin());
        assertTrue(ctx.isLocalAdmin());
        assertFalse(ctx.isSuperAdmin());
    }
}
