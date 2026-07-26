package com.blbulyandavbulyan.larm.config.validation;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setConfigurationInitializer(config -> {
            if (!(config instanceof HibernateValidatorConfiguration hibernateConfig)) {
                throw new IllegalStateException("Expected HibernateValidatorConfiguration");
            }
            hibernateConfig.getterPropertySelectionStrategy(new HybridGetterStrategy());
        });
        return factory;
    }
}