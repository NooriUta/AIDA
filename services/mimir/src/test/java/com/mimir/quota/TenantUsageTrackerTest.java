package com.mimir.quota;

import com.mimir.heimdall.MimirEventEmitter;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class TenantUsageTrackerTest {

    @Inject TenantUsageTracker tracker;
    @InjectMock TenantQuotaStore store;
    @InjectMock MimirEventEmitter emitter;

    @Test
    void estimateTokensIs4CharsPerToken() {
        assertThat(TenantUsageTracker.estimateTokens("hello")).isEqualTo(1L);
        assertThat(TenantUsageTracker.estimateTokens("a".repeat(40))).isEqualTo(10L);
        assertThat(TenantUsageTracker.estimateTokens(null)).isZero();
        assertThat(TenantUsageTracker.estimateTokens("")).isZero();
    }

    @Test
    void recordPersistsAndEmits() {
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        when(store.findUsage("acme", today)).thenReturn(Optional.empty());

        tracker.record("acme", "deepseek", "deepseek-chat", 1000L, 500L);

        ArgumentCaptor<DailyUsage> cap = ArgumentCaptor.forClass(DailyUsage.class);
        verify(store, times(1)).upsertUsage(cap.capture());
        assertThat(cap.getValue().tenantAlias()).isEqualTo("acme");
        assertThat(cap.getValue().promptTokens()).isEqualTo(1000L);
        assertThat(cap.getValue().completionTokens()).isEqualTo(500L);
        assertThat(cap.getValue().totalTokens()).isEqualTo(1500L);
        assertThat(cap.getValue().costEstimateUsd()).isPositive();
        assertThat(cap.getValue().requestCount()).isEqualTo(1);

        verify(emitter).tokenUsageRecorded(eq("acme"), eq("deepseek"), eq("deepseek-chat"),
                eq(1000L), eq(500L), anyDouble());
    }

    @Test
    void recordAccumulatesIntoExistingDailyRow() {
        String today = LocalDate.now(ZoneOffset.UTC).toString();
        when(store.findUsage("acme", today)).thenReturn(Optional.of(
                new DailyUsage("acme", today, 100L, 200L, 300L, 0.001, 2)));

        tracker.record("acme", "deepseek", "deepseek-chat", 50L, 50L);

        ArgumentCaptor<DailyUsage> cap = ArgumentCaptor.forClass(DailyUsage.class);
        verify(store).upsertUsage(cap.capture());
        assertThat(cap.getValue().promptTokens()).isEqualTo(150L);
        assertThat(cap.getValue().completionTokens()).isEqualTo(250L);
        assertThat(cap.getValue().totalTokens()).isEqualTo(400L);
        assertThat(cap.getValue().requestCount()).isEqualTo(3);
    }

    @Test
    void blankTenantSkipsPersistAndEmit() {
        tracker.record(null, "deepseek", "deepseek-chat", 100L, 100L);
        tracker.record("",   "deepseek", "deepseek-chat", 100L, 100L);
        verify(store, never()).upsertUsage(any());
        verify(emitter, never()).tokenUsageRecorded(anyString(), anyString(), anyString(),
                anyLong(), anyLong(), anyDouble());
    }
}
