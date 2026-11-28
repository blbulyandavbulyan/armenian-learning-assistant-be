package com.blbulyandavbulyan.larm.genseq;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Collects finished OpenTelemetry spans in memory, so integration tests can build sequence diagrams from them.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TracingTestConfiguration {
    private static final long FLUSH_TIMEOUT_SECONDS = 5;

    public static final String MOCK_EMBEDDING_MODEL = "mockEmbeddingModel";

    /** The ITs mock the embedding model, so nothing records the call as Spring AI does in production. */
    @Bean
    @Primary
    EmbeddingModel observedEmbeddingModel(@Qualifier(MOCK_EMBEDDING_MODEL) EmbeddingModel mock,
                                          ObservationRegistry registry) {
        return new ObservedEmbeddingModel(mock, registry);
    }

    private record ObservedEmbeddingModel(EmbeddingModel delegate, ObservationRegistry registry)
            implements EmbeddingModel {
        @Override
        public @NonNull EmbeddingResponse call(@NonNull EmbeddingRequest request) {
            return delegate.call(request);
        }

        @Override
        public float @NonNull [] embed(@NonNull Document document) {
            return delegate.embed(document);
        }

        @Override
        public float @NonNull [] embed(@NonNull String text) {
            return Observation.createNotStarted(SpanNames.EMBEDDING_SPAN, registry)
                    .observe(() -> delegate.embed(text));
        }
    }

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
        var result = ctx.getBean(SdkTracerProvider.class).forceFlush().join(FLUSH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!result.isSuccess()) {
            throw new IllegalStateException("Flushing spans did not finish within " + FLUSH_TIMEOUT_SECONDS
                    + "s; the span list would be incomplete");
        }
    }
}
