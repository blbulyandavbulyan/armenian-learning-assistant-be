package com.blbulyandavbulyan.larm.security;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Getter
public class DatabaseUserJwtAuthenticationToken extends JwtAuthenticationToken {
    private final UUID userId;

    public DatabaseUserJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, UUID userId) {
        super(jwt, authorities, userId.toString());
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DatabaseUserJwtAuthenticationToken that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId);
    }
}
