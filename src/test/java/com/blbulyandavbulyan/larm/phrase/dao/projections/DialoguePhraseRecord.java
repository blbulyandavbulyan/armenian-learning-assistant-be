package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.DialoguePhrase;

public record DialoguePhraseRecord(
        UUID id,
        PhraseRecord phrase,
        DialogueSpeakerRecord speaker,
        int orderIndex,
        Instant createdAt) {

    public DialoguePhraseRecord(DialoguePhrase phrase) {
        this(phrase.getId(), 
                new PhraseRecord(phrase.getPhrase(), 
                        phrase.getTranslations().stream()
                                .map(TranslationRecord::new)
                                .collect(Collectors.toSet())),
                new DialogueSpeakerRecord(phrase.getSpeaker()), 
                phrase.getOrderIndex(), 
                phrase.getCreatedAt());
    }
}
