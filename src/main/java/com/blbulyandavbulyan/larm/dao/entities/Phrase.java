package com.blbulyandavbulyan.larm.dao.entities;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.blbulyandavbulyan.larm.dao.entities.LazyLoadingStringConstants.LAZY_LOADING;

@Entity
@Table(name = "phrases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phrase {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private PhraseStatus status;

    @Column(name = "iso_language_code")
    private String isoLanguageCode;

    private String phrase;
    private String transcription;

    @OneToMany(mappedBy = "phrase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private Set<Media> mediaSet;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Phrase that)) {
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
        return "Phrase{id=%s, status=%s, isoLanguageCode='%s', phrase='%s', transcription='%s', mediaSet=%s}"
                .formatted(id, status, isoLanguageCode, phrase, transcription, LAZY_LOADING);
    }
}
