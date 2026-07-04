package com.blbulyandavbulyan.larm.dao.entities;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.blbulyandavbulyan.larm.dao.entities.LazyLoadingStringConstants.LAZY_LOADING;

@Entity
@Table(name = "dialogue_speakers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DialogueSpeaker {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialogue_id")
    private Dialogue dialogue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_phrase_id")
    private Phrase namePhrase;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "dialogue_speaker_id", nullable = false)
    @Builder.Default
    private Set<DialogueSpeakerTranslation> translations = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DialogueSpeaker that)) {
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
        return "DialogueSpeaker{id=%s, dialogue=%s, namePhrase=%s, createdAt=%s, translations=%s}"
                .formatted(id, LAZY_LOADING, LAZY_LOADING, createdAt, translations);
    }
}
