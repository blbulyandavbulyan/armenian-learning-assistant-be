package com.blbulyandavbulyan.larm.config;

import java.util.List;

import com.blbulyandavbulyan.larm.security.DatabaseUserJwtConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AppSecurityProperties securityProperties;
    private final DatabaseUserJwtConverter jwtConverter;

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // important to have cors configuration, cause otherwise ui does not work properly
        http.cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth -> {
            if (securityProperties.enabled()) {
                log.info("Security is enabled");
                // TODO, this might be dumb, but it is better then no security, will be adjusted later when
                //  real security is going to be implemented
                auth.anyRequest().authenticated();
            } else {
                log.warn("Security is disabled");
                auth.anyRequest().permitAll();
            }
        });
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer ->
                jwtConfigurer.jwtAuthenticationConverter(jwtConverter)
        ));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> allowedOriginPatterns = securityProperties.allowedOriginPatterns();
        log.info("Allowed origin patterns: {}", allowedOriginPatterns);
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);

        // Allow all standard methods, including OPTIONS for the preflight
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        configuration.setAllowedHeaders(List.of(CorsConfiguration.ALL));

        // Required if your frontend sends cookies or authorization headers
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all API routes
        return source;
    }
}
