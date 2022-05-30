package io.github.kewne.jackson_validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
    public void validates_basic_parameters() {
        var exception = assertThrows(
                FailedValidationException.class,
                () -> mapper.readValue("""
                        {

                        }""", BeanWithConstructor.class));

        var jsonViolations = exception.getJsonViolations();
        var nameViolations = List.copyOf(jsonViolations.getNode("json_name").getViolations());
        assertEquals(1, nameViolations.size());
        var v = nameViolations.get(0);
        assertInstanceOf(NotNull.class, v.getConstraintDescriptor().getAnnotation());
    }

    public static final class BeanWithConstructor {

        private final String name;

        @JsonCreator(mode = Mode.PROPERTIES)
        @Valid
        public BeanWithConstructor(
                @NotNull @Size(min = 2) @JsonProperty("json_name") String name) {
            this.name = Objects.requireNonNull(name);
        }
    }

}
