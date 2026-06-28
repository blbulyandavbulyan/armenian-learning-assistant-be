package com.blbulyandavbulyan.larm.dialogue.dao;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.phrase.dao.PhraseMother;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialoguePhraseRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialogueRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialogueSpeakerRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.PhraseRecord;

public interface DialogueMother {

    interface DefaultDialogue {
        UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        String EMBEDDING_TEXT = PhraseMother.DialoguePhrase1.EMBEDDING_TEXT
                + "\n" + PhraseMother.DialoguePhrase2.EMBEDDING_TEXT
                + "\n" + PhraseMother.DialoguePhrase3.EMBEDDING_TEXT;

        static float[] embedding() {
            float[] embedding = new float[1536];
            embedding[0] = 0.9f;
            return embedding;
        }

        static Builder builder() {
            return DialogueMother.builder()
                    .withId(ID)
                    .withTitle(PhraseMother.DialogueTitlePhrase.build())
                    .withSpeakers(
                            DialogueSpeakerMother.DefaultDialogueSpeaker1.build(), 
                            DialogueSpeakerMother.DefaultDialogueSpeaker2.build()
                    )
                    .withEmbedding(embedding())
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
        private float[] embedding;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withTitle(PhraseRecord title) {
            this.title = title;
            return this;
        }

        public Builder withSpeakers(DialogueSpeakerRecord... speakers) {
            this.speakers = Arrays.stream(speakers).collect(Collectors.toSet());
            return this;
        }

        public Builder withDialoguePhrases(DialoguePhraseRecord... dialoguePhrases) {
            this.dialoguePhrases = Arrays.stream(dialoguePhrases).collect(Collectors.toSet());
            return this;
        }

        public Builder withEmbedding(float[] embedding) {
            this.embedding = embedding;
            return this;
        }

        public DialogueRecord build() {
            return new DialogueRecord(id, title, speakers, dialoguePhrases, null, embedding);
        }
    }
}
