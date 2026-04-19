package com.loopy.core.action;

import com.loopy.core.TokenUsage;

public sealed interface ActionResult {

    record Success(Object output, TokenUsage usage) implements ActionResult {
        public Success(Object output) {
            this(output, TokenUsage.zero());
        }

        public static Success of(Object output) {
            return new Success(output);
        }

        public static Success of(Object output, TokenUsage usage) {
            return new Success(output, usage);
        }
    }

    record Failure(String reason, Exception cause) implements ActionResult {
        public Failure(String reason) {
            this(reason, null);
        }
    }
}
