package com.blbulyandavbulyan.larm.security;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class DatabaseUserJwtAuthenticationToken extends JwtAuthenticationToken {
    private final UUID userId;

    public DatabaseUserJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, UUID userId) {
        // We set the principal name to the string representation of our internal UUID
        super(jwt, authorities, userId.toString());
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DatabaseUserJwtAuthenticationToken that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId);
    }
}
