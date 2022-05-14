package io.github.kewne.jackson_validation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Set;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.deser.ValueInstantiator.Delegating;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.executable.ValidateOnExecution;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstructorDescriptor;

public class ValidatingValueInstantiators implements ValueInstantiators {

    private final Validator validator;

    public ValidatingValueInstantiators(Validator validator) {
        this.validator = validator;
    }

    public ValueInstantiator findValueInstantiator(
            DeserializationConfig config,
            BeanDescription beanDesc,
            ValueInstantiator defaultInstantiator
    ) {
        return new ValidatingValueInstantiator(defaultInstantiator);
    }

    private class ValidatingValueInstantiator extends Delegating {

        protected ValidatingValueInstantiator(ValueInstantiator delegate) {
            super(delegate);
        }

        @Override
        public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException {
            var creator = delegate().getWithArgsCreator();
            var beanDesc = validator.getConstraintsForClass(creator.getDeclaringClass());
            return switch (creator.getMember()) {
                case Constructor<?> c -> { 
                    var descriptor = beanDesc.getConstraintsForConstructor(c.getParameterTypes());
                    validateConstructorParameters(descriptor, c, args);
                    var result = super.createFromObjectWith(ctxt, args);
                    validateConstructorResult(descriptor, result);
                    return result;
                }
                default -> super.createFromObjectWith(ctxt, args);
            };
        }

        private void validateConstructorParameters(ConstructorDescriptor desc, Constructor<?> c, Object[] args) {
            var execValidator = validator.forExecutables();
            if (desc == null) {
                return;
            }
            if (desc.hasConstrainedParameters()) {
                var violations = execValidator.validateConstructorParameters(c, args);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }
            }
        }

        private void validateConstructorResult(ConstructorDescriptor desc, Constructor<?> c, Object result) {
            var execValidator = validator.forExecutables();
            if (desc == null) { return; }
            if (desc.hasConstrainedReturnValue()) {
                var violations = execValidator.validateConstructorReturnValue(c, result);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }
            }
        }

        @Override
        public Object createFromObjectWith(
            DeserializationContext ctxt,
            SettableBeanProperty[] props,
            PropertyValueBuffer buffer
        ) throws IOException {
            // needs to be overridden to ensure our implementation is called
            return createFromObjectWith(ctxt, buffer.getParameters(props));
        }
        
    }
}
