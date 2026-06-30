package com.blbulyandavbulyan.larm.dialogue.dao;

import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.dao.PhraseMother;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialoguePhraseRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialogueSpeakerRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.PhraseRecord;

public interface DialoguePhraseMother {

    interface DefaultDialoguePhrase1 {
        UUID ID = UUID.fromString("33333333-3333-3333-3333-333333333331");
        int ORDER_INDEX = 0;

        static Builder builder() {
            return DialoguePhraseMother.builder()
                    .withId(ID)
                    .withOrderIndex(ORDER_INDEX)
                    .withPhrase(PhraseMother.DialoguePhrase1.build())
                    .withSpeaker(DialogueSpeakerMother.DefaultDialogueSpeaker1.build());
        }

        static DialoguePhraseRecord build() {
            return DefaultDialoguePhrase1.builder().build();
        }
    }

    interface DefaultDialoguePhrase2 {
        UUID ID = UUID.fromString("33333333-3333-3333-3333-333333333332");
        int ORDER_INDEX = 1;

        static Builder builder() {
            return DialoguePhraseMother.builder()
                    .withId(ID)
                    .withOrderIndex(ORDER_INDEX)
                    .withPhrase(PhraseMother.DialoguePhrase2.build())
                    .withSpeaker(DialogueSpeakerMother.DefaultDialogueSpeaker2.build());
        }

        static DialoguePhraseRecord build() {
            return DefaultDialoguePhrase2.builder().build();
        }
    }

    interface DefaultDialoguePhrase3 {
        UUID ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
        int ORDER_INDEX = 2;

        static Builder builder() {
            return DialoguePhraseMother.builder()
                    .withId(ID)
                    .withOrderIndex(ORDER_INDEX)
                    .withPhrase(PhraseMother.DialoguePhrase3.build())
                    .withSpeaker(DialogueSpeakerMother.DefaultDialogueSpeaker1.build());
        }

        static DialoguePhraseRecord build() {
            return DefaultDialoguePhrase3.builder().build();
        }
    }

    interface RealisticDialoguePhrase1 {
        UUID ID = UUID.fromString("43333333-3333-3333-3333-333333333331");
        int ORDER_INDEX = 0;

        static Builder builder() {
            return DialoguePhraseMother.builder()
                    .withId(ID)
                    .withOrderIndex(ORDER_INDEX)
                    .withPhrase(PhraseMother.RealisticDialoguePhrase1.build())
                    .withSpeaker(DialogueSpeakerMother.RealisticDialogueSpeaker1.build());
        }

        static DialoguePhraseRecord build() {
            return RealisticDialoguePhrase1.builder().build();
        }
    }

    interface RealisticDialoguePhrase2 {
        UUID ID = UUID.fromString("43333333-3333-3333-3333-333333333332");
        int ORDER_INDEX = 1;

        static Builder builder() {
            return DialoguePhraseMother.builder()
                    .withId(ID)
                    .withOrderIndex(ORDER_INDEX)
                    .withPhrase(PhraseMother.RealisticDialoguePhrase1.build())
                    .withSpeaker(DialogueSpeakerMother.RealisticDialogueSpeaker2.build());
        }

        static DialoguePhraseRecord build() {
            return DialoguePhraseMother.RealisticDialoguePhrase2.builder().build();
        }
    }

    interface RealisticDialoguePhrase3 {
        UUID ID = UUID.fromString("43333333-3333-3333-3333-333333333333");
        int ORDER_INDEX = 2;

        static Builder builder() {
            return DialoguePhraseMother.builder()
                    .withId(ID)
                    .withOrderIndex(ORDER_INDEX)
                    .withPhrase(PhraseMother.RealisticDialoguePhrase2.build())
                    .withSpeaker(DialogueSpeakerMother.RealisticDialogueSpeaker1.build());
        }

        static DialoguePhraseRecord build() {
            return RealisticDialoguePhrase3.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private UUID id;
        private PhraseRecord phrase;
        private DialogueSpeakerRecord speaker;
        private Integer orderIndex;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withPhrase(PhraseRecord phrase) {
            this.phrase = phrase;
            return this;
        }

        public Builder withSpeaker(DialogueSpeakerRecord speaker) {
            this.speaker = speaker;
            return this;
        }

        public Builder withOrderIndex(Integer orderIndex) {
            this.orderIndex = orderIndex;
            return this;
        }

        public DialoguePhraseRecord build() {
            return new DialoguePhraseRecord(id, phrase, speaker, orderIndex, null);
        }
    }
}
