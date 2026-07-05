package com.blbulyandavbulyan.larm.security;

import java.util.UUID;

import com.blbulyandavbulyan.larm.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseUserJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;
    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String issuer = jwt.getIssuer().toString();
        String subject = jwt.getSubject();
        
        UUID userId = userService.aquireUserId(issuer, subject);
        
        return new DatabaseUserJwtAuthenticationToken(jwt, defaultAuthoritiesConverter.convert(jwt), userId);
    }
}
