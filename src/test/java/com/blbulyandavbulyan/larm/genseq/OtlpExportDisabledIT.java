package com.blbulyandavbulyan.larm.genseq;

import com.blbulyandavbulyan.larm.BaseIT;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Even with an OTLP endpoint configured (which would otherwise create an OTLP exporter),
 * the default application configuration must not export spans over OTLP.
 */
@TestPropertySource(properties = "management.opentelemetry.tracing.export.otlp.endpoint=http://localhost:4318/v1/traces")
class OtlpExportDisabledIT extends BaseIT {
    @Test
    void otlpSpanExportIsDisabledByDefaultWhileInMemoryExporterIsPresent() {
        assertThat(applicationContext.getBeansOfType(SpanExporter.class).values())
                .hasAtLeastOneElementOfType(InMemorySpanExporter.class)
                .noneMatch(e -> e.getClass().getSimpleName().startsWith("Otlp"));
    }
}
