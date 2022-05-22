package io.github.kewne.jackson_validation;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

public class JsonObjectViolations extends JsonNodeViolations {

    private final Map<String, JsonNodeViolations> propertyViolations;

    public JsonObjectViolations(Set<ConstraintViolation<Object>> violations, Map<String, JsonNodeViolations> propertyViolations) {
        super(violations);
        this.propertyViolations = Objects.requireNonNullElse(propertyViolations, Map.of());
    }

    public JsonNodeViolations getNode(String name) {
        return propertyViolations.getOrDefault(name, new JsonNodeViolations(Set.of()));
    }
    
}
