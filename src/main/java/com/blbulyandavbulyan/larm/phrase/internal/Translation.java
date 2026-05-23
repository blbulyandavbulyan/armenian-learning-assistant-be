package com.blbulyandavbulyan.larm.phrase.internal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Table("translations")
public record Translation(
        @Id UUID id,
        String language,
        String translationText,
        Instant createdAt
) {
    // Convenience constructor for creating new translations easily
    public static Translation create(String language, String text) {
        return new Translation(UUID.randomUUID(), language, text, Instant.now());
    }
}