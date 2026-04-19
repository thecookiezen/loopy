package com.loopy.core.prompt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parameterized prompt template using {@code {{key}}} mustache-style
 * placeholders.
 */
public record PromptTemplate(String template) {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");

    /**
     * Render the template with named parameters.
     */
    public String render(Map<String, Object> params) {
        var result = template;
        for (var entry : params.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }

    /**
     * Render with a single parameter.
     */
    public String render(String key, Object value) {
        return render(Map.of(key, value));
    }

    /**
     * Parse and return the set of required parameter names.
     */
    public Set<String> requiredParameters() {
        return PLACEHOLDER.matcher(template).results()
                .map(m -> m.group(1))
                .collect(Collectors.toSet());
    }

    /**
     * Validate that all required parameters are provided.
     */
    public void validate(Map<String, Object> params) {
        var missing = requiredParameters().stream()
                .filter(p -> !params.containsKey(p))
                .collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing template parameters: " + missing);
        }
    }

    /**
     * Create a new builder for incrementally assembling parameters.
     */
    public TemplateRenderer renderer() {
        return new TemplateRenderer(this);
    }

    public static PromptTemplate of(String template) {
        return new PromptTemplate(template);
    }

    /**
     * Incremental renderer - accumulate parameters then render.
     */
    public static final class TemplateRenderer {
        private final PromptTemplate template;
        private final Map<String, Object> params = new HashMap<>();

        private TemplateRenderer(PromptTemplate template) {
            this.template = template;
        }

        public TemplateRenderer set(String key, Object value) {
            params.put(key, value);
            return this;
        }

        public String render() {
            template.validate(params);
            return template.render(params);
        }
    }
}
