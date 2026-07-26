package com.blbulyandavbulyan.larm.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        CorsProperties cors,
        boolean enabled) {
    public record CorsProperties(List<String> allowedOriginPatterns) {
    }
}
