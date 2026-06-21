package com.blbulyandavbulyan.larm.phrase.dao;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

@Entity
@Table(name = "dialogues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dialogue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_phrase_id")
    private Phrase title;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "dialogue", cascade = CascadeType.ALL, orphanRemoval = true)
    // TODO, set relies on equals and hashcode, so proper equals and hashcode must be implemented
    private Set<DialogueSpeaker> speakers;

    @OneToMany(mappedBy = "dialogue", cascade = CascadeType.ALL, orphanRemoval = true)
    // TODO, where TF IS ORDER BY USING THE 'orderIndex' FIELD? The tests arent failing too, they should fail if this order field is not used,
    // meaning that in the database it should be inserted in such way, that without using this field the order will be WRONG,
    // which should fail the tests
    private Set<DialoguePhrase> dialoguePhrases; // TODO, set relies on equals and hashcode, so proper equals and hashcode must be implemented

}
