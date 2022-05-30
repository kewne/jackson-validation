package io.github.kewne.jackson_validation;

import java.util.Objects;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jakarta.validation.Validator;

public class ValidationModule extends SimpleModule {

    private final Validator validator;

    public ValidationModule(Validator validator) {
        super("validation", new Version(0, 1, 0, "SNAPSHOT", "io.github.kewne", "jackson-validation"));
        this.validator = Objects.requireNonNull(validator);
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addValueInstantiators(new ValidatingValueInstantiators(validator));
        //context.addBeanDeserializerModifier(new ValidationAwareBeanDeserializerModifier());
    }

}
