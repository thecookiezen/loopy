package com.loopy.core.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;

/**
 * Generates JSON schema from Java record classes.
 * Used for LLM structured outputs and tool parameter schemas.
 */
public final class JsonSchemaGenerator {

    private static final SchemaGenerator GENERATOR;

    static {
        var config = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
                .with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT)
                .with(Option.FLATTENED_ENUMS_FROM_TOSTRING)
                .build();
        GENERATOR = new SchemaGenerator(config);
    }

    /**
     * Generate a JSON schema for the given type.
     *
     * @param type the Java class to generate schema for
     * @return the JSON schema as a JsonNode
     */
    public static JsonNode schemaFor(Class<?> type) {
        return GENERATOR.generateSchema(type);
    }

    /**
     * Generate JSON schema as a string.
     */
    public static String schemaStringFor(Class<?> type) {
        return schemaFor(type).toPrettyString();
    }

    private JsonSchemaGenerator() {
    }
}
