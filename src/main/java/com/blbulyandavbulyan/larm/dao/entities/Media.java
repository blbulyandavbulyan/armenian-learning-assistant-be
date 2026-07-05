package com.blbulyandavbulyan.larm.dao.entities;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.blbulyandavbulyan.larm.dao.entities.LazyLoadingStringConstants.LAZY_LOADING;

@Entity
@Table(name = "medias")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phrase_id")
    private Phrase phrase;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider")
    private StorageProvider storageProvider;

    @Column(name = "storage_bucket")
    private String storageBucket;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size_bytes")
    private int sizeInBytes;

    @Column(name = "ai_model_used")
    private String aiModelUsed;

    @Column(name = "voice_identifier")
    private String voiceIdentifier;

    @Column(name = "created_at")
    private Instant createdAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Media that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return
                """
                Media{id=%s, phrase=%s, storageProvider=%s, storageBucket='%s', storageKey='%s',\
                 contentType='%s', sizeInBytes=%d, aiModelUsed='%s', voiceIdentifier='%s', createdAt=%s}
                """.formatted(id, LAZY_LOADING, storageProvider, storageBucket, storageKey,
                        contentType, sizeInBytes, aiModelUsed, voiceIdentifier, createdAt);
    }
}
