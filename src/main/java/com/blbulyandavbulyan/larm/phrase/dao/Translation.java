package com.blbulyandavbulyan.larm.phrase.dao;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Builder
@Table("translations")
public record Translation(
        @Id UUID id,

        @Column("iso_language_code")
        String isoLanguageCode,

        @Column("translation_text")
        String translationText,

        @Column("created_at")
        Instant createdAt) {
}