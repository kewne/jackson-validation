package io.github.kewne.jackson_validation;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/**
 * Modifies bean deserializers to handle validation.
 */
public class ValidationAwareBeanDeserializerModifier extends BeanDeserializerModifier {

    @Override
    public JsonDeserializer<?> modifyDeserializer(
            DeserializationConfig config,
            BeanDescription beanDesc,
            JsonDeserializer<?> deserializer) {
        if (deserializer instanceof BeanDeserializer src) {
            return new ValidationAwareDeserializer(src);
        }
        var delegate = deserializer.getDelegatee();
        if (delegate == null) {
            return deserializer;
        }
        if (deserializer.getDelegatee() instanceof BeanDeserializer src) {
            deserializer.replaceDelegatee(new ValidationAwareDeserializer(src));
            return deserializer;
        }
       var newDelegate = modifyDeserializer(config, beanDesc, delegate);
       if (newDelegate != delegate) {
           deserializer.replaceDelegatee(newDelegate);
       }
       return deserializer;
    }

    private static final class ValidationAwareDeserializer extends BeanDeserializer {

        protected ValidationAwareDeserializer(BeanDeserializerBase src) {
            super(src);
        }

        

        @Override
        public void wrapAndThrow(
                Throwable t,
                Object bean,
                String fieldName,
                DeserializationContext ctxt)
                throws IOException {
            if (t instanceof FailedValidationException) {
                return;
            }
            super.wrapAndThrow(t, bean, fieldName, ctxt);
        }



        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // TODO Auto-generated method stub
            return super.deserialize(p, ctxt);
        }



        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt, Object bean) throws IOException {
            // TODO Auto-generated method stub
            return super.deserialize(p, ctxt, bean);
        }



        @Override
        public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
                throws IOException {
            // TODO Auto-generated method stub
            return super.deserializeWithType(p, ctxt, typeDeserializer);
        }



        @Override
        public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer,
                Object intoValue) throws IOException, JacksonException {
            // TODO Auto-generated method stub
            return super.deserializeWithType(p, ctxt, typeDeserializer, intoValue);
        }

    }

}
