package studio.seer.tenantrouting;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TenantContextFilterTest {

    private TenantContextFilter filter;
    private TenantContextHolder holder;

    @BeforeEach
    void setUp() {
        holder = new TenantContextHolder();
        filter = new TenantContextFilter();
        filter.holder = holder;
    }

    @Test
    void validAlias_populatesHolder() throws IOException {
        var ctx = mockRequest("acme-corp", "uid-1", "user@acme.com",
                "aida:admin seer:read", "admin,viewer", "corr-123");
        filter.filter(ctx);

        verify(ctx, never()).abortWith(any());
        assertThat(holder.tenantAlias()).isEqualTo("acme-corp");
        assertThat(holder.userId()).isEqualTo("uid-1");
        assertThat(holder.scopes()).containsExactly("aida:admin", "seer:read");
        assertThat(holder.roles()).containsExactly("admin", "viewer");
        assertThat(holder.correlationId()).isEqualTo("corr-123");
    }

    @Test
    void missingAlias_returns400() {
        assertThat(TenantContextFilter.validateAlias(null)).isNotNull();
        assertThat(TenantContextFilter.validateAlias("")).isNotNull();
        assertThat(TenantContextFilter.validateAlias("   ")).isNotNull();
    }

    @Test
    void invalidAliasFormat_returns400() {
        assertThat(TenantContextFilter.validateAlias("UPPERCASE")).isNotNull();
        assertThat(TenantContextFilter.validateAlias("has space")).isNotNull();
        assertThat(TenantContextFilter.validateAlias("has_underscore")).isNotNull();
    }

    @Test
    void shortAlias_returns400() {
        // regex requires [a-z][a-z0-9-]{2,30}[a-z0-9] — minimum 4 chars total
        assertThat(TenantContextFilter.validateAlias("ab")).isNotNull();
        assertThat(TenantContextFilter.validateAlias("abc")).isNotNull();
    }

    @Test
    void validMinLengthAlias_passes() throws IOException {
        var ctx = mockRequest("abcd", null, null, null, null, null); // 4 chars — valid
        filter.filter(ctx);
        verify(ctx, never()).abortWith(any());
    }

    @Test
    void emptyScopes_resultsInEmptyList() throws IOException {
        var ctx = mockRequest("test-corp", null, null, "", null, null);
        filter.filter(ctx);
        assertThat(holder.scopes()).isEmpty();
    }

    private ContainerRequestContext mockRequest(String alias, String userId, String email,
                                                String scopes, String roles, String correlationId) {
        var ctx = mock(ContainerRequestContext.class);
        when(ctx.getHeaderString("X-Seer-Tenant-Alias")).thenReturn(alias);
        when(ctx.getHeaderString("X-Seer-User-Id")).thenReturn(userId);
        when(ctx.getHeaderString("X-Seer-User-Email")).thenReturn(email);
        when(ctx.getHeaderString("X-Seer-Scopes")).thenReturn(scopes);
        when(ctx.getHeaderString("X-Seer-Roles")).thenReturn(roles);
        when(ctx.getHeaderString("X-Correlation-ID")).thenReturn(correlationId);
        when(ctx.getHeaders()).thenReturn(new MultivaluedHashMap<>());
        return ctx;
    }
}
