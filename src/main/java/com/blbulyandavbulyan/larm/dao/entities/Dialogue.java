package com.blbulyandavbulyan.larm.dao.entities;

import java.time.Instant;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dialogues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dialogue {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_phrase_id")
    private Phrase title;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "dialogue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("speakerRefId ASC")
    private Set<DialogueSpeaker> speakers;

    @OneToMany(mappedBy = "dialogue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private Set<DialoguePhrase> dialoguePhrases;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Dialogue that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(getId());
    }

}
