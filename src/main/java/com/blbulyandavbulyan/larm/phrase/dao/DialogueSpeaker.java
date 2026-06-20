package com.blbulyandavbulyan.larm.phrase.dao;

import java.time.Instant;
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
@Table(name = "dialogue_speakers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DialogueSpeaker {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialogue_id")
    private Dialogue dialogue;

    @Column(name = "speaker_ref_id")
    // TODO most probably we don't need this field here at all, cause it is referenced by 'id' field, if it exist in the api response most probably it is not needed there as well, the 'id' should be returned there
    private String speakerRefId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_phrase_id")
    private Phrase namePhrase;

    @Column(name = "created_at")
    private Instant createdAt;

}
