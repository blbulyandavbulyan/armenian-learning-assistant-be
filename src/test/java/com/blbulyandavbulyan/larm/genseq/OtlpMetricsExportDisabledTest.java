package com.blbulyandavbulyan.larm.genseq;

import io.micrometer.core.instrument.Clock;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.otlp.OtlpMetricsExportAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The real application.yaml must switch OTLP metrics export off. Integration tests cannot show this because
 * Spring Boot's test support disables metrics export on its own, so the auto-configuration is run directly.
 */
class OtlpMetricsExportDisabledTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withBean(Clock.class, () -> Clock.SYSTEM)
            .withConfiguration(AutoConfigurations.of(MetricsAutoConfiguration.class,
                    OtlpMetricsExportAutoConfiguration.class));

    @Test
    void applicationYamlDoesNotCreateOtlpMeterRegistry() {
        runner.run(context -> assertThat(context).doesNotHaveBean(OtlpMeterRegistry.class));
    }

    @Test
    void autoConfigurationWouldCreateOtlpMeterRegistryWhenExportIsEnabled() {
        runner.withPropertyValues("management.otlp.metrics.export.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(OtlpMeterRegistry.class));
    }
}
