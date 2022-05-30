package io.github.kewne.jackson_validation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.kewne.jackson_validation.Tags.Wip;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@ExtendWith(ObjectMapperParameterResolver.class)
public class NestedValidationTest {

    private final ObjectMapper mapper;

    public NestedValidationTest(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Test
    @Wip
    public void validates_nested_objects() {
        var exception = assertThrows(
            FailedValidationException.class,
            () -> mapper.readValue("""
                {
                    "nested": {

                    }
                }
            """, RootObject.class));
            fail();
    }

    private static class RootObject {


        private final NestedObject nested;

        @JsonCreator
        public RootObject(@JsonProperty("nested") @Valid NestedObject nested) {
            this.nested = nested;
        }

    }

    private static class NestedObject {

        private final String name;

        @JsonCreator
        public NestedObject(@NotNull @JsonProperty("name") String name) {
            this.name = name;
        }

    }
}
