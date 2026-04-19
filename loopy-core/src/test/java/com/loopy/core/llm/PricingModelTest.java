package com.loopy.core.llm;

import com.loopy.core.TokenUsage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PricingModelTest {

    @Test
    void costOf_computesCorrectly() {
        var model = new PricingModel("test", 2.0, 8.0);
        var usage = new TokenUsage(500_000, 250_000);
        var cost = model.costOf(usage);
        assertThat(cost).isCloseTo(1.0 + 2.0, within(0.0001));
    }

    @Test
    void costOf_zeroUsage() {
        var model = new PricingModel("test", 2.0, 8.0);
        assertThat(model.costOf(TokenUsage.zero())).isCloseTo(0.0, within(0.0001));
    }
}
