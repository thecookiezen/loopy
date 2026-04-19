package com.loopy.core.llm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StructuredOutputParserTest {

    record Sample(String name, int age) {}

    @Test
    void parse_validJson() {
        var json = """
                {"name":"Alice","age":30}""";
        var result = StructuredOutputParser.parse(json, Sample.class);
        assertThat(result.name()).isEqualTo("Alice");
        assertThat(result.age()).isEqualTo(30);
    }

    @Test
    void parse_stripsMarkdownJsonBlock() {
        var wrapped = """
                ```json
                {"name":"Bob","age":25}
                ```""";
        var result = StructuredOutputParser.parse(wrapped, Sample.class);
        assertThat(result.name()).isEqualTo("Bob");
    }

    @Test
    void parse_stripsMarkdownCodeBlockWithoutLanguage() {
        var wrapped = """
                ```
                {"name":"Eve","age":22}
                ```""";
        var result = StructuredOutputParser.parse(wrapped, Sample.class);
        assertThat(result.name()).isEqualTo("Eve");
    }

    @Test
    void parse_invalidJson_throws() {
        assertThatThrownBy(() -> StructuredOutputParser.parse("not json", Sample.class))
                .isInstanceOf(StructuredOutputParser.LlmParsingException.class)
                .hasMessageContaining("Sample");
    }

    @Test
    void parse_preservesRawContentOnError() {
        var bad = "not json";
        try {
            StructuredOutputParser.parse(bad, Sample.class);
            fail("Expected exception");
        } catch (StructuredOutputParser.LlmParsingException e) {
            assertThat(e.rawContent()).isEqualTo(bad);
        }
    }
}
