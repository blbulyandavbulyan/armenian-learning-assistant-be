package com.blbulyandavbulyan.larm.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUserJwtAuthenticationTokenTest {

    private final Jwt defaultJwt = Jwt.withTokenValue("token1")
            .header("alg", "none")
            .claim("sub", "user1")
            .build();
    private final Collection<? extends GrantedAuthority> defaultAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    private final UUID defaultUserId = UUID.fromString("5f0e99bb-407c-49f6-8683-768adb53903c");
    private final DatabaseUserJwtAuthenticationToken defaultToken = 
            new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, defaultUserId);

    @SuppressWarnings("EqualsWithItself")
    @Test
    void equals_sameInstances() {
        assertThat(defaultToken).isEqualTo(defaultToken);
    }

    @Test
    void equals_differentButEqualInstances() {
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, defaultUserId);
        assertThat(defaultToken).isEqualTo(otherToken);
    }

    @Test
    void equals_differentJwt() {
        Jwt otherJwt = Jwt.withTokenValue("token2")
                .header("alg", "none")
                .claim("sub", "user2")
                .build();
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(otherJwt, defaultAuthorities, defaultUserId);
        assertThat(defaultToken).isNotEqualTo(otherToken);
    }

    @Test
    void equals_differentAuthorities() {
        Collection<? extends GrantedAuthority> otherAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, otherAuthorities, defaultUserId);
        assertThat(defaultToken).isNotEqualTo(otherToken);
    }

    @Test
    void equals_differentUserIds() {
        UUID otherUserId = UUID.fromString("a59203c7-b3d4-4593-8feb-352932fe8de4");
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, otherUserId);
        assertThat(defaultToken).isNotEqualTo(otherToken);
    }

    @Test
    void equals_comparingToNull() {
        assertThat(defaultToken).isNotEqualTo(null);
    }

    @Test
    void equals_comparingToDifferentClass() {
        assertThat(defaultToken).isNotEqualTo(new Object());
    }

    @Test
    void hashCode_forEqualInstances() {
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, defaultUserId);
        assertThat(defaultToken).hasSameHashCodeAs(otherToken);
    }

    @Test
    void hashCode_forDifferentUserIds() {
        UUID otherUserId = UUID.fromString("90a31374-81df-4f31-a908-50b925273d6b");
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, otherUserId);
        assertThat(defaultToken).doesNotHaveSameHashCodeAs(otherToken);
    }
}
