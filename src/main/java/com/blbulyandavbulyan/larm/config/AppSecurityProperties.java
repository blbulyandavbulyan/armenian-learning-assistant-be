package com.blbulyandavbulyan.larm.config;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        CorsProperties cors,
        boolean enabled) {
    public record CorsProperties(List<String> allowedOriginPatterns) {
    }

    public List<String> allowedOriginPatterns() {
        return Optional.ofNullable(cors())
                .map(CorsProperties::allowedOriginPatterns)
                .orElse(Collections.emptyList());
    }
}
