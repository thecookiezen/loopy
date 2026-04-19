package com.loopy.llm.openai;

import java.time.Duration;

/**
 * Configuration for the OpenAI client.
 * Supports custom base URLs for OpenAI-compatible endpoints (Ollama, LMStudio, etc.).
 */
public record OpenAiConfig(
        String apiKey,
        String baseUrl,
        String completionPath,
        Duration timeout) {
    public static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    public static final String DEFAULT_COMPLETION_PATH = "/chat/completions";

    public static OpenAiConfig fromEnvironment() {
        var apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not set");
        }
        return new OpenAiConfig(apiKey, DEFAULT_BASE_URL, DEFAULT_COMPLETION_PATH, Duration.ofSeconds(60));
    }

    public static OpenAiConfig of(String apiKey) {
        return new OpenAiConfig(apiKey, DEFAULT_BASE_URL, DEFAULT_COMPLETION_PATH, Duration.ofSeconds(60));
    }

    public static OpenAiConfig of(String apiKey, String baseUrl) {
        return new OpenAiConfig(apiKey, baseUrl, DEFAULT_COMPLETION_PATH, Duration.ofSeconds(60));
    }

    public static OpenAiConfig of(String apiKey, String baseUrl, String completionPath) {
        return new OpenAiConfig(apiKey, baseUrl, completionPath, Duration.ofSeconds(60));
    }
}
