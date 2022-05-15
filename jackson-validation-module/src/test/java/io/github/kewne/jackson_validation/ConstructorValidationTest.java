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
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@ExtendWith(ObjectMapperParameterResolver.class)
public class ConstructorValidationTest {

    private final ObjectMapper mapper;

    public ConstructorValidationTest(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Test
    public void validates_json_creator_parameters() {
        var exception = assertThrows(
                JsonMappingException.class,
                () -> mapper.readValue("""
                        {

                        }""", BeanWithConstructor.class));

        var violations = assertInstanceOf(ConstraintViolationException.class, exception.getCause())
                .getConstraintViolations()
                .stream()
                .collect(groupingBy(v -> StreamSupport.stream(v.getPropertyPath().spliterator(), false)
                        .map(n -> n.getName())
                        .collect(Collectors.joining("."))));
        var nameViolations = violations.get("BeanWithConstructor.arg0");
        assertEquals(1, nameViolations.size());
        var v = nameViolations.get(0);
        assertInstanceOf(NotNull.class, v.getConstraintDescriptor().getAnnotation());
    }

    @Test
    public void validates_json_creator_return_value() {
        var exception = assertThrows(
                JsonMappingException.class,
                () -> mapper.readValue("""
                        {
                            "name": ""
                        }""", BeanWithConstructor.class));

        var violations = assertInstanceOf(ConstraintViolationException.class, exception.getCause())
                .getConstraintViolations()
                .stream()
                .collect(groupingBy(v -> StreamSupport.stream(v.getPropertyPath().spliterator(), false)
                        .map(n -> n.getName())
                        .collect(Collectors.joining("."))));
        var nameViolations = violations.get("BeanWithConstructor.<return value>.name");
        assertEquals(1, nameViolations.size());
        var v = nameViolations.get(0);
        assertInstanceOf(Size.class, v.getConstraintDescriptor().getAnnotation());
    }

    public static final class BeanWithConstructor {

        @Size(min = 2)
        private final String name;

        @JsonCreator(mode = Mode.PROPERTIES)
        @Valid
        public BeanWithConstructor(@NotNull @JsonProperty("name") String name) {
            this.name = Objects.requireNonNull(name);
        }
    }

}
