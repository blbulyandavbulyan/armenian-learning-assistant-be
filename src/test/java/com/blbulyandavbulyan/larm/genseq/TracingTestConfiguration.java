package com.blbulyandavbulyan.larm.genseq;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Collects finished OpenTelemetry spans in memory, so integration tests can build sequence diagrams from them.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TracingTestConfiguration {
    private static final long FLUSH_TIMEOUT_SECONDS = 5;

    @Bean
    InMemorySpanExporter spanExporter() {
        return InMemorySpanExporter.create();
    }

    public static List<SpanData> finishedSpans(ApplicationContext ctx) {
        flush(ctx);
        return ctx.getBean(InMemorySpanExporter.class).getFinishedSpanItems();
    }

    public static void reset(ApplicationContext ctx) {
        flush(ctx);
        ctx.getBean(InMemorySpanExporter.class).reset();
    }

    private static void flush(ApplicationContext ctx) {
        ctx.getBean(SdkTracerProvider.class).forceFlush().join(FLUSH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
