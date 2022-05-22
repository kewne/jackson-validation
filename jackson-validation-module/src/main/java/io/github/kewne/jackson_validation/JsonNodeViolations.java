package io.github.kewne.jackson_validation;

import java.util.Objects;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

public class JsonNodeViolations {

    private final Set<ConstraintViolation<Object>> violations;

    public JsonNodeViolations(Set<ConstraintViolation<Object>> violations) {
        this.violations = Objects.requireNonNullElse(violations, Set.of());
    }

    public Set<ConstraintViolation<Object>> getViolations() {
        return violations;
    }

}
