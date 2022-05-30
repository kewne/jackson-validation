package io.github.kewne.jackson_validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

public final class Tags {
    
    private Tags() {}

    /**
     * Marks tests that are Work In Progress.
     * 
     * These should be skipped by CI.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Tag("WIP")
    @interface Wip {}
}
