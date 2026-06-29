package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import org.jspecify.annotations.NonNull;

public record DialogueRecord(
        UUID id,
        PhraseRecord title,
        Set<DialogueSpeakerRecord> speakers,
        Set<DialoguePhraseRecord> dialoguePhrases,
        Instant createdAt,
        float[] embedding) {

    public DialogueRecord(Dialogue dialogue) {
        this(dialogue.getId(), 
                new PhraseRecord(dialogue.getTitle(), 
                        dialogue.getTitleTranslations().stream()
                                .map(TranslationRecord::new)
                                .collect(Collectors.toSet())),
                getSpeakerRecords(dialogue),
                getPhraseRecords(dialogue),
                dialogue.getCreatedAt(),
                dialogue.getEmbedding());
    }

    private static Set<DialogueSpeakerRecord> getSpeakerRecords(Dialogue dialogue) {
        return dialogue.getSpeakers().stream().map(DialogueSpeakerRecord::new).collect(Collectors.toSet());
    }

    private static Set<DialoguePhraseRecord> getPhraseRecords(Dialogue dialogue) {
        return dialogue.getDialoguePhrases().stream().map(DialoguePhraseRecord::new).collect(Collectors.toSet());
    }

    @SuppressWarnings({"DeconstructionCanBeUsed", "java:S6878"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DialogueRecord that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.deepEquals(embedding, that.embedding)
                && Objects.equals(title, that.title)
                && Objects.equals(speakers, that.speakers)
                && Objects.equals(dialoguePhrases, that.dialoguePhrases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, speakers, dialoguePhrases, createdAt, Arrays.hashCode(embedding));
    }

    @Override
    @NonNull
    public String toString() {
        return "DialogueRecord{id=%s, title=%s, speakers=%s, dialoguePhrases=%s, createdAt=%s, embedding=%s}"
                .formatted(id, title, speakers, dialoguePhrases, createdAt, Arrays.toString(embedding));
    }
}
