package io.github.kewne.jackson_validation;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiator.Delegating;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstructorDescriptor;
import jakarta.validation.metadata.MethodDescriptor;

public class ValidatingValueInstantiators implements ValueInstantiators {

    private static final Logger LOGGER = System.getLogger(ValidatingValueInstantiators.class.getName());

    private final Validator validator;

    public ValidatingValueInstantiators(Validator validator) {
        this.validator = validator;
    }

    public ValueInstantiator findValueInstantiator(
            DeserializationConfig config,
            BeanDescription beanDesc,
            ValueInstantiator defaultInstantiator) {
        return new ValidatingValueInstantiator(defaultInstantiator);
    }

    private class ValidatingValueInstantiator extends Delegating {

        protected ValidatingValueInstantiator(ValueInstantiator delegate) {
            super(delegate);
        }

        @Override
        public Object createUsingDelegate(DeserializationContext ctxt, Object delegate) throws IOException {
            var violations = validator.validate(delegate);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return super.createUsingDelegate(ctxt, delegate);
        }




        @Override
        public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException {
            var creator = delegate().getWithArgsCreator();
            var beanDesc = validator.getConstraintsForClass(creator.getDeclaringClass());
            if (creator.getMember() instanceof Constructor<?> c) {
                var descriptor = beanDesc.getConstraintsForConstructor(c.getParameterTypes());
                validateConstructorParameters(descriptor, c, args);
                var result = super.createFromObjectWith(ctxt, args);
                validateConstructorResult(descriptor, c, result);
                return result;
            }
            LOGGER.log(
                    Level.WARNING,
                    "Creator ({0}) does not support validation",
                    creator.getMember());
            return super.createFromObjectWith(ctxt, args);
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
            if (desc == null) {
                return;
            }
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
                PropertyValueBuffer buffer) throws IOException {
            // needs to be overridden to ensure our implementation is called
            return createFromObjectWith(ctxt, buffer.getParameters(props));
        }

    }
}
