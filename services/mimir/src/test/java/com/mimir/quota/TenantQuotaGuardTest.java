package com.mimir.quota;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@QuarkusTest
class TenantQuotaGuardTest {

    @Inject TenantQuotaGuard guard;
    @InjectMock TenantQuotaStore store;

    @BeforeEach
    void clear() {
        guard.invalidate("acme");
        reset(store);
    }

    @Test
    void noQuotaConfiguredAllows() {
        when(store.findQuota("acme")).thenReturn(Optional.empty());
        assertThat(guard.check("acme").allowed()).isTrue();
    }

    @Test
    void unlimitedQuotaAllows() {
        when(store.findQuota("acme")).thenReturn(Optional.of(TenantQuota.unlimited()));
        when(store.findUsage(anyString(), anyString())).thenReturn(Optional.empty());
        assertThat(guard.check("acme").allowed()).isTrue();
    }

    @Test
    void dailyTokenLimitHitBlocks() {
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        when(store.findQuota("acme")).thenReturn(Optional.of(
                new TenantQuota(1000L, 0L, 0.0, 0.0, "UTC")));
        when(store.findUsage("acme", today)).thenReturn(Optional.of(
                new DailyUsage("acme", today, 600L, 500L, 1100L, 0.5, 5)));

        QuotaCheckResult r = guard.check("acme");
        assertThat(r.allowed()).isFalse();
        assertThat(r.reason()).isEqualTo("daily_tokens");
        assertThat(r.current()).isEqualTo(1100L);
        assertThat(r.limit()).isEqualTo(1000L);
    }

    @Test
    void dailyCostLimitHitBlocks() {
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        when(store.findQuota("acme")).thenReturn(Optional.of(
                new TenantQuota(0L, 0L, 0.10, 0.0, "UTC")));
        when(store.findUsage("acme", today)).thenReturn(Optional.of(
                new DailyUsage("acme", today, 0L, 0L, 0L, 0.15, 1)));

        QuotaCheckResult r = guard.check("acme");
        assertThat(r.allowed()).isFalse();
        assertThat(r.reason()).isEqualTo("daily_cost");
        assertThat(r.currentValue()).isEqualTo(0.15);
    }

    @Test
    void emptyAliasAllows() {
        assertThat(guard.check(null).allowed()).isTrue();
        assertThat(guard.check("").allowed()).isTrue();
    }

    @Test
    void monthlyTokenLimitAggregatesAcrossDays() {
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        String monthPrefix = today.substring(0, 7);
        when(store.findQuota("acme")).thenReturn(Optional.of(
                new TenantQuota(0L, 5000L, 0.0, 0.0, "UTC")));
        when(store.findUsage("acme", today)).thenReturn(Optional.of(
                new DailyUsage("acme", today, 0L, 0L, 1000L, 0.0, 1)));
        when(store.listUsage("acme", 31)).thenReturn(List.of(
                new DailyUsage("acme", today, 0L, 0L, 1000L, 0.0, 1),
                new DailyUsage("acme", monthPrefix + "-01", 0L, 0L, 4500L, 0.0, 5)
        ));

        QuotaCheckResult r = guard.check("acme");
        assertThat(r.allowed()).isFalse();
        assertThat(r.reason()).isEqualTo("monthly_tokens");
    }
}
