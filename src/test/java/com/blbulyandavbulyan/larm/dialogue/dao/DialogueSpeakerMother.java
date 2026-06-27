package com.blbulyandavbulyan.larm.dialogue.dao;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.dao.PhraseMother;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialogueSpeakerRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.PhraseRecord;

public interface DialogueSpeakerMother {

    interface DefaultDialogueSpeaker1 {
        UUID ID = UUID.fromString("22222222-2222-2222-2222-222222222221");
        String SPEAKER_REF_ID = "speaker1";

        static Builder builder() {
            return DialogueSpeakerMother.builder()
                    .withId(ID)
                    .withSpeakerRefId(SPEAKER_REF_ID)
                    .withNamePhrase(PhraseMother.DialogueSpeaker1NamePhrase.build());
        }

        static DialogueSpeakerRecord build() {
            return DefaultDialogueSpeaker1.builder().build();
        }
    }

    interface DefaultDialogueSpeaker2 {
        UUID ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
        String SPEAKER_REF_ID = "speaker2";

        static Builder builder() {
            return DialogueSpeakerMother.builder()
                    .withId(ID)
                    .withSpeakerRefId(SPEAKER_REF_ID)
                    .withNamePhrase(PhraseMother.DialogueSpeaker2NamePhrase.build());
        }

        static DialogueSpeakerRecord build() {
            return DefaultDialogueSpeaker2.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private UUID id;
        private String speakerRefId;
        private PhraseRecord namePhrase;
        private Instant createdAt;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withSpeakerRefId(String speakerRefId) {
            this.speakerRefId = speakerRefId;
            return this;
        }

        public Builder withNamePhrase(PhraseRecord namePhrase) {
            this.namePhrase = namePhrase;
            return this;
        }

        public DialogueSpeakerRecord build() {
            return new DialogueSpeakerRecord(id, speakerRefId, namePhrase, createdAt);
        }
    }
}
