package com.loopy.core.llm;

import com.loopy.core.TokenUsage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class LlmResponseTest {

    @Test
    void hasToolCalls_trueWhenPresent() {
        var msg = new ChatMessage.Assistant("", List.of(
                new ToolCallRequest("1", "func", "{}")));
        var response = new LlmResponse(msg, TokenUsage.zero(), "m", "tool_calls");
        assertThat(response.hasToolCalls()).isTrue();
    }

    @Test
    void hasToolCalls_falseWhenEmpty() {
        var msg = new ChatMessage.Assistant("hello");
        var response = new LlmResponse(msg, TokenUsage.zero(), "m", "stop");
        assertThat(response.hasToolCalls()).isFalse();
    }

    @Test
    void hasToolCalls_falseWhenNullToolCalls() {
        var msg = new ChatMessage.Assistant("hello", null);
        var response = new LlmResponse(msg, TokenUsage.zero(), "m", "stop");
        assertThat(response.hasToolCalls()).isFalse();
    }
}
