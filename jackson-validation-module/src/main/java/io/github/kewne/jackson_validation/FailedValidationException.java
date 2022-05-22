package io.github.kewne.jackson_validation;

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import jakarta.validation.ConstraintViolation;

/**
 * Signals a failed validation when deserializing a value.
 */
public class FailedValidationException extends MismatchedInputException {

    private final Set<ConstraintViolation<Object>> violations;

    protected FailedValidationException(JsonParser p, String msg, Set<ConstraintViolation<Object>> violations) {
        super(p, msg);
        this.violations = Objects.requireNonNull(violations);
    }

    public Set<ConstraintViolation<Object>> getViolations() {
        return violations;
    }

}
