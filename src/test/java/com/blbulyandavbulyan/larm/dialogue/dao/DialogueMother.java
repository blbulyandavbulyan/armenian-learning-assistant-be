package com.blbulyandavbulyan.larm.dialogue.dao;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.dao.PhraseMother;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialoguePhraseRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialogueRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialogueSpeakerRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.PhraseRecord;

public interface DialogueMother {

    interface DefaultDialogue {
        UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

        static Builder builder() {
            return DialogueMother.builder()
                    .withId(ID)
                    .withTitle(PhraseMother.DialogueTitlePhrase.build())
                    .withSpeakers(
                            DialogueSpeakerMother.DefaultDialogueSpeaker1.build(), 
                            DialogueSpeakerMother.DefaultDialogueSpeaker2.build()
                    )
                    .withDialoguePhrases(
                            DialoguePhraseMother.DefaultDialoguePhrase1.build(), 
                            DialoguePhraseMother.DefaultDialoguePhrase2.build(), 
                            DialoguePhraseMother.DefaultDialoguePhrase3.build()
                    );
        }

        static DialogueRecord build() {
            return DefaultDialogue.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private UUID id;
        private PhraseRecord title;
        private Set<DialogueSpeakerRecord> speakers;
        private Set<DialoguePhraseRecord> dialoguePhrases;
        private Instant createdAt;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withTitle(PhraseRecord title) {
            this.title = title;
            return this;
        }

        public Builder withSpeakers(DialogueSpeakerRecord... speakers) {
            this.speakers = java.util.Arrays.stream(speakers).collect(java.util.stream.Collectors.toSet());
            return this;
        }

        public Builder withDialoguePhrases(DialoguePhraseRecord... dialoguePhrases) {
            this.dialoguePhrases = java.util.Arrays.stream(dialoguePhrases).collect(java.util.stream.Collectors.toSet());
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DialogueRecord build() {
            return new DialogueRecord(id, title, speakers, dialoguePhrases, createdAt);
        }
    }
}
