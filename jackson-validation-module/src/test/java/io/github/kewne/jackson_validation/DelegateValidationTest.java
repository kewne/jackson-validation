package io.github.kewne.jackson_validation;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@ExtendWith(ObjectMapperParameterResolver.class)
public class DelegateValidationTest {

    private final ObjectMapper mapper;

    public DelegateValidationTest(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Test
    public void validates_delegate() {
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> mapper.readValue("""
                        {

                        }""", DelegateCreator.class));
        var violations = exception.getConstraintViolations()
                .stream()
                .collect(groupingBy(v -> StreamSupport.stream(v.getPropertyPath().spliterator(), false)
                        .map(n -> n.getName())
                        .collect(Collectors.joining("."))));
        var nameViolations = violations.get("name");
        assertEquals(1, nameViolations.size());
        var v = nameViolations.get(0);
        assertInstanceOf(NotBlank.class, v.getConstraintDescriptor().getAnnotation());

    }

    public static final class DelegateCreator {

        private final String name;
        private final Integer age;

        @JsonCreator
        public static DelegateCreator of(Delegate delegate) {
            return new DelegateCreator(delegate.name, delegate.age);
        }

        public DelegateCreator(String name, Integer age) {
            this.name = Objects.requireNonNull(name);
            this.age = Objects.requireNonNull(age);
        }
    }

    public static final class Delegate {

        @NotBlank
        @Size(min = 2)
        private String name;

        @NotNull
        @PositiveOrZero
        private Integer age;
    }
}
