package com.blbulyandavbulyan.larm.genseq;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

/**
 * Marks an integration test whose real OpenTelemetry spans are rendered as a sequence diagram
 * under {@code docs/generated/sequence}.
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Tag("genseq")
public @interface GenerateSequence {
}
