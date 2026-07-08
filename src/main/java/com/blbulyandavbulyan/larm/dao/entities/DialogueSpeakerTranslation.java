package com.blbulyandavbulyan.larm.dao.entities;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dialogue_speaker_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DialogueSpeakerTranslation implements ContextualTranslation {

    @Id
    @Getter(onMethod_ = {@Override})
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "iso_language_code")
    @Getter(onMethod_ = {@Override})
    private String isoLanguageCode;

    @Column(name = "translation_text")
    @Getter(onMethod_ = {@Override})
    private String translationText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialogue_speaker_id")
    private DialogueSpeaker dialogueSpeaker;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DialogueSpeakerTranslation that)) {
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
        return "DialogueSpeakerTranslation{id=%s, isoLanguageCode='%s', translationText='%s'}"
                .formatted(id, isoLanguageCode, translationText);
    }
}
