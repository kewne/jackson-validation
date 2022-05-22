package io.github.kewne.jackson_validation;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiator.Delegating;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path.ParameterNode;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstructorDescriptor;

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
                throw new FailedValidationException(
                        ctxt.getParser(),
                        "Constructor delegate parameter failed validation",
                        violations,
                        new JsonObjectViolations(violations, null));
            }
            return super.createUsingDelegate(ctxt, delegate);
        }

        private <T> void validateConstructorParameters(
                ConstructorDescriptor desc,
                Constructor<T> c,
                Object[] args,
                DeserializationContext ctxt,
                SettableBeanProperty[] props)
                throws FailedValidationException {
            var execValidator = validator.forExecutables();
            if (desc == null) {
                return;
            }
            if (desc.hasConstrainedParameters()) {
                var violations = execValidator.<Object>validateConstructorParameters(c, args);
                if (!violations.isEmpty()) {
                    var rootViolations = Set.<ConstraintViolation<Object>>of();
                    var propertyViolations = violations.stream()
                            .filter(v -> {
                                var pathIter = v.getPropertyPath().iterator();
                                pathIter.next();
                                return pathIter.next().getKind() == ElementKind.PARAMETER;
                            }).collect(Collectors.groupingBy(v -> {
                                var pathIter = v.getPropertyPath().iterator();
                                pathIter.next();
                                var param = pathIter.next().as(ParameterNode.class);
                                return props[param.getParameterIndex()].getName();
                            }, Collectors.collectingAndThen(Collectors.toSet(), JsonNodeViolations::new)));
                    throw new FailedValidationException(
                            ctxt.getParser(),
                            "Constructor parameters failed validation",
                            violations,
                            new JsonObjectViolations(rootViolations, propertyViolations));
                }
            }
        }

        @Override
        public Object createFromObjectWith(
                DeserializationContext ctxt,
                SettableBeanProperty[] props,
                PropertyValueBuffer buffer) throws IOException {
            // needs to be overridden to ensure our implementation is called
            Object[] args = buffer.getParameters(props);
            var creator = delegate().getWithArgsCreator();
            var beanDesc = validator.getConstraintsForClass(creator.getDeclaringClass());
            if (creator.getMember() instanceof Constructor<?> c) {
                var descriptor = beanDesc.getConstraintsForConstructor(c.getParameterTypes());
                validateConstructorParameters(descriptor, c, args, ctxt, props);
                var result = createFromObjectWith(ctxt, args);
                return result;
            }
            LOGGER.log(
                    Level.WARNING,
                    "Creator ({0}) does not support validation",
                    creator.getMember());
            return createFromObjectWith(ctxt, args);
        }

    }
}
