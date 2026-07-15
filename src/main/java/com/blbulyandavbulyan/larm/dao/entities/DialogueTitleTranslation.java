package com.blbulyandavbulyan.larm.dao.entities;

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
@Table(name = "dialogue_title_translations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DialogueTitleTranslation implements ContextualTranslation {

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
    @JoinColumn(name = "dialogue_id")
    private Dialogue dialogue;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DialogueTitleTranslation that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return "DialogueTitleTranslation".hashCode();
    }

    @Override
    public String toString() {
        return "DialogueTitleTranslation{id=%s, isoLanguageCode='%s', translationText='%s'}"
                .formatted(id, isoLanguageCode, translationText);
    }
}
