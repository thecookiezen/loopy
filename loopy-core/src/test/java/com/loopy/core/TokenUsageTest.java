package com.loopy.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TokenUsageTest {

    @Test
    void totalTokens_sumsPromptAndCompletion() {
        var usage = new TokenUsage(100, 50);
        assertThat(usage.totalTokens()).isEqualTo(150);
    }

    @Test
    void zero_returnsZeroUsage() {
        var zero = TokenUsage.zero();
        assertThat(zero.promptTokens()).isEqualTo(0);
        assertThat(zero.completionTokens()).isEqualTo(0);
        assertThat(zero.totalTokens()).isEqualTo(0);
    }

    @Test
    void plus_addsBothFields() {
        var a = new TokenUsage(10, 20);
        var b = new TokenUsage(30, 40);
        var sum = a.plus(b);
        assertThat(sum.promptTokens()).isEqualTo(40);
        assertThat(sum.completionTokens()).isEqualTo(60);
        assertThat(sum.totalTokens()).isEqualTo(100);
    }

    @Test
    void plus_isCommutative() {
        var a = new TokenUsage(1, 2);
        var b = new TokenUsage(3, 4);
        assertThat(a.plus(b)).isEqualTo(b.plus(a));
    }
}
