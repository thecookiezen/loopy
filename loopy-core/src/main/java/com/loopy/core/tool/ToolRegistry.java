package com.loopy.core.tool;

import java.util.*;

/**
 * Registry of tools, supports lookup by name.
 */
public record ToolRegistry(Map<String, Tool> toolsByName) {

    public ToolRegistry {
        toolsByName = Collections.unmodifiableMap(new LinkedHashMap<>(toolsByName));
    }

    public static ToolRegistry empty() {
        return new ToolRegistry(Map.of());
    }

    public Optional<Tool> findByName(String name) {
        return Optional.ofNullable(toolsByName.get(name));
    }

    public List<Tool> all() {
        return List.copyOf(toolsByName.values());
    }

    public ToolRegistry with(Tool tool) {
        var updated = new LinkedHashMap<>(toolsByName);
        updated.put(tool.name(), tool);
        return new ToolRegistry(updated);
    }

    public ToolRegistry withAll(Collection<Tool> tools) {
        var updated = new LinkedHashMap<>(toolsByName);
        tools.forEach(t -> updated.put(t.name(), t));
        return new ToolRegistry(updated);
    }
}
