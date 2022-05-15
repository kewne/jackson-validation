package io.github.kewne.jackson_validation;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ValidationTest {

    private final ObjectMapper mapper;

    public ValidationTest() {
        this.mapper = new ObjectMapper();
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        mapper.registerModule(new ValidationModule(validator));
    }

    @Test
    public void validates_json_creator() {
        var exception = assertThrows(
                JsonMappingException.class,
                () -> mapper.readValue("""
                        {

                        }""", BeanWithName.class));

        var violations = assertInstanceOf(ConstraintViolationException.class, exception.getCause())
                .getConstraintViolations()
                .stream()
                .collect(groupingBy(v ->
                     StreamSupport.stream(v.getPropertyPath().spliterator(), false)
                        .map(n -> n.getName())
                        .collect(Collectors.joining("."))));
        var nameViolations = violations.get("BeanWithName.arg0");
        assertEquals(1, nameViolations.size());
        var v = nameViolations.get(0);
        assertInstanceOf(NotNull.class, v.getConstraintDescriptor().getAnnotation());
    }

    public static final class BeanWithName {

        @Size(min = 2)
        private final String name;

        @JsonCreator(mode = Mode.PROPERTIES)
        public BeanWithName(@NotNull @JsonProperty("name") String name) {
            this.name = Objects.requireNonNull(name);
        }
    }
}
