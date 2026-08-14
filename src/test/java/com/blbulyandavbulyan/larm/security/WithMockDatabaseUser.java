package com.blbulyandavbulyan.larm.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.test.context.support.WithSecurityContext;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithMockDatabaseUserSecurityContextFactory.class)
public @interface WithMockDatabaseUser {
    String issuer() default "https://test-issuer.com";
    String subject() default "test-subject-123";
}
