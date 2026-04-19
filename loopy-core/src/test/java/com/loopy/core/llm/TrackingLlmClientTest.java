package com.loopy.core.llm;

import com.loopy.core.TokenUsage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TrackingLlmClientTest {

    private final StubLlmClient stub = new StubLlmClient();
    private final TrackingLlmClient tracking = new TrackingLlmClient(stub);

    @Test
    void accumulatesUsageAcrossChatCalls() {
        stub.setUsage(new TokenUsage(10, 20));
        tracking.chat(LlmRequest.simple("m", "hi"));

        stub.setUsage(new TokenUsage(5, 15));
        tracking.chat(LlmRequest.simple("m", "hi again"));

        assertThat(tracking.accumulated()).isEqualTo(new TokenUsage(15, 35));
    }

    @Test
    void accumulatesUsageAcrossCreateObjectCalls() {
        stub.setUsage(new TokenUsage(100, 200));
        tracking.createObject(LlmRequest.simple("m", "analyze"), String.class);

        assertThat(tracking.accumulated()).isEqualTo(new TokenUsage(100, 200));
    }

    @Test
    void reset_returnsPreviousAndClears() {
        stub.setUsage(new TokenUsage(10, 20));
        tracking.chat(LlmRequest.simple("m", "hi"));

        var previous = tracking.reset();
        assertThat(previous).isEqualTo(new TokenUsage(10, 20));
        assertThat(tracking.accumulated()).isEqualTo(TokenUsage.zero());
    }

    @Test
    void delegatesChatResponse() {
        var response = tracking.chat(LlmRequest.simple("m", "hi"));
        assertThat(response.message().content()).isEqualTo("stub-response");
    }

    @Test
    void delegatesCreateObjectResponse() {
        var result = tracking.createObject(LlmRequest.simple("m", "hi"), String.class);
        assertThat(result.value()).isEqualTo("stub-object");
    }

    private static class StubLlmClient implements LlmClient {
        private TokenUsage usage = TokenUsage.zero();

        void setUsage(TokenUsage usage) {
            this.usage = usage;
        }

        @Override
        public LlmResponse chat(LlmRequest request) {
            return new LlmResponse(
                    new ChatMessage.Assistant("stub-response"),
                    usage, "stub-model", "stop");
        }

        @Override
        public <T> LlmObjectResult<T> createObject(LlmRequest request, Class<T> outputType) {
            @SuppressWarnings("unchecked")
            T value = (T) "stub-object";
            return new LlmObjectResult<>(value, usage);
        }
    }
}
