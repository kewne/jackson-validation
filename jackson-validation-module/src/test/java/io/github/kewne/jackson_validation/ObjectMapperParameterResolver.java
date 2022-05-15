package io.github.kewne.jackson_validation;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import jakarta.validation.Validation;

public class ObjectMapperParameterResolver extends TypeBasedParameterResolver<ObjectMapper> {

    @Override
    public ObjectMapper resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var mapper = new ObjectMapper();
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        mapper.registerModule(new ValidationModule(validator));
        return mapper;
    }

}
