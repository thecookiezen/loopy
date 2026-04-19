package com.loopy.core.prompt;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class PromptTemplateTest {

    @Test
    void render_substitutesPlaceholders() {
        var template = PromptTemplate.of("Hello, {{name}}! You are {{age}} years old.");
        var result = template.render(Map.of("name", "Alice", "age", 30));
        assertThat(result).isEqualTo("Hello, Alice! You are 30 years old.");
    }

    @Test
    void render_singleParameter() {
        var template = PromptTemplate.of("Summarize: {{text}}");
        assertThat(template.render("text", "some content"))
                .isEqualTo("Summarize: some content");
    }

    @Test
    void requiredParameters_extractsKeys() {
        var template = PromptTemplate.of("{{a}} and {{b}} but not {{a}} again");
        assertThat(template.requiredParameters()).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void validate_throwsOnMissingParams() {
        var template = PromptTemplate.of("{{x}} and {{y}}");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> template.validate(Map.of("x", "val")))
                .withMessageContaining("y");
    }

    @Test
    void renderer_fluent() {
        var result = PromptTemplate.of("{{greeting}}, {{name}}!")
                .renderer()
                .set("greeting", "Hi")
                .set("name", "Bob")
                .render();
        assertThat(result).isEqualTo("Hi, Bob!");
    }

    @Test
    void noPlaceholders_returnsVerbatim() {
        var template = PromptTemplate.of("Just plain text");
        assertThat(template.render(Map.of())).isEqualTo("Just plain text");
        assertThat(template.requiredParameters()).isEmpty();
    }
}
