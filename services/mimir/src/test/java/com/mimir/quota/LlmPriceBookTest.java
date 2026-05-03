package com.mimir.quota;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class LlmPriceBookTest {

    @Inject LlmPriceBook book;

    @Test
    void deepseekPriceMatchesShippedRate() {
        // 1000 prompt + 1000 completion → 0.14 + 0.28 = 0.42 cents = $0.00042
        double cost = book.estimate("deepseek", "deepseek-chat", 1000, 1000);
        assertThat(cost).isEqualTo(0.00014 + 0.00028);
    }

    @Test
    void anthropicSonnetPriceMatchesShippedRate() {
        double cost = book.estimate("anthropic", "claude-sonnet-4-6", 1000, 1000);
        assertThat(cost).isEqualTo(0.003 + 0.015);
    }

    @Test
    void unknownProviderReturnsZero() {
        assertThat(book.estimate("acme-llm", "unicorn-chat", 100_000, 100_000)).isZero();
    }

    @Test
    void caseInsensitiveProviderLookup() {
        assertThat(book.estimate("DeepSeek", "deepseek-chat", 1000, 1000)).isPositive();
    }

    @Test
    void zeroTokensIsZeroCost() {
        assertThat(book.estimate("anthropic", "claude-sonnet-4-6", 0, 0)).isZero();
    }
}
