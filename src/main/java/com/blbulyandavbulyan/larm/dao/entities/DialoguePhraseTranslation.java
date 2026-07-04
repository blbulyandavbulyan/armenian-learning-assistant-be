package com.blbulyandavbulyan.larm.dao.entities;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dialogue_phrase_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DialoguePhraseTranslation implements ContextualTranslation {

    @Id
    @Getter(onMethod_ = {@Override})
    private UUID id;

    @Column(name = "iso_language_code")
    @Getter(onMethod_ = {@Override})
    private String isoLanguageCode;

    @Column(name = "translation_text")
    @Getter(onMethod_ = {@Override})
    private String translationText;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DialoguePhraseTranslation that)) {
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
        return "DialoguePhraseTranslation{id=%s, isoLanguageCode='%s', translationText='%s'}"
                .formatted(id, isoLanguageCode, translationText);
    }
}
