package com.blbulyandavbulyan.larm.config.validation;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.boot.validation.autoconfigure.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {

    @Bean
    public ValidationConfigurationCustomizer validationCustomizer() {
        return config -> {
            if (!(config instanceof HibernateValidatorConfiguration hibernateConfig)) {
                throw new IllegalStateException("Expected HibernateValidatorConfiguration");
            }
            hibernateConfig.getterPropertySelectionStrategy(new HybridGetterStrategy());
        };
    }
}