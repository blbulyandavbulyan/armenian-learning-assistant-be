package com.blbulyandavbulyan.larm.security;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockDatabaseUserSecurityContextFactory 
        implements WithSecurityContextFactory<WithMockDatabaseUser> {

    @Autowired
    private DatabaseUserJwtConverter jwtConverter;

    @Override
    public @NonNull SecurityContext createSecurityContext(WithMockDatabaseUser mockUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Jwt jwt = Jwt.withTokenValue("mock-test-token")
                .header("alg", "none")
                .claim("iss", mockUser.issuer())
                .subject(mockUser.subject())
                .build();

        Authentication auth = jwtConverter.convert(jwt);

        context.setAuthentication(auth);
        return context;
    }
}
