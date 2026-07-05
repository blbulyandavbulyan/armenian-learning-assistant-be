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
    private final UUID defaultUserId = UUID.randomUUID();
    private final DatabaseUserJwtAuthenticationToken defaultToken = 
            new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, defaultUserId);

    @SuppressWarnings("EqualsWithItself")
    @Test
    void equalsShouldReturnTrueWhenSameInstance() {
        assertThat(defaultToken).isEqualTo(defaultToken);
    }

    @Test
    void equalsShouldReturnTrueWhenEqualInstances() {
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, defaultUserId);
        assertThat(defaultToken).isEqualTo(otherToken);
    }

    @Test
    void equalsShouldReturnFalseWhenJwtDiffers() {
        Jwt otherJwt = Jwt.withTokenValue("token2")
                .header("alg", "none")
                .claim("sub", "user2")
                .build();
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(otherJwt, defaultAuthorities, defaultUserId);
        assertThat(defaultToken).isNotEqualTo(otherToken);
    }

    @Test
    void equalsShouldReturnFalseWhenAuthoritiesDiffer() {
        Collection<? extends GrantedAuthority> otherAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, otherAuthorities, defaultUserId);
        assertThat(defaultToken).isNotEqualTo(otherToken);
    }

    @Test
    void equalsShouldReturnFalseWhenUserIdDiffers() {
        UUID otherUserId = UUID.randomUUID();
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, otherUserId);
        assertThat(defaultToken).isNotEqualTo(otherToken);
    }

    @Test
    void equalsShouldReturnFalseWhenComparedToNull() {
        assertThat(defaultToken).isNotEqualTo(null);
    }

    @Test
    void equalsShouldReturnFalseWhenComparedToDifferentClass() {
        assertThat(defaultToken).isNotEqualTo(new Object());
    }

    @Test
    void hashCodeShouldBeEqualWhenInstancesAreEqual() {
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, defaultUserId);
        assertThat(defaultToken).hasSameHashCodeAs(otherToken);
    }

    @Test
    void hashCodeShouldDifferWhenUserIdDiffers() {
        UUID otherUserId = UUID.randomUUID();
        DatabaseUserJwtAuthenticationToken otherToken = new DatabaseUserJwtAuthenticationToken(defaultJwt, defaultAuthorities, otherUserId);
        assertThat(defaultToken).doesNotHaveSameHashCodeAs(otherToken);
    }
}


