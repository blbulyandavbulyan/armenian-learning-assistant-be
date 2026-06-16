package com.blbulyandavbulyan.larm.ai;

import com.blbulyandavbulyan.larm.ai.chat.DraftPhraseResource;
import com.blbulyandavbulyan.larm.ai.chat.DraftPhraseResourceMother;

public interface DialoguePhraseMother {

    interface DefaultDialoguePhrase {
        String SPEAKER_ID = "speaker-1";
        DraftPhraseResource PHRASE = DraftPhraseResourceMother.DefaultDialoguePhrase.build();

        static Builder builder() {
            return DialoguePhraseMother.builder()
                    .withSpeakerId(SPEAKER_ID)
                    .withPhrase(PHRASE);
        }

        static StructuredDialogueResource.DialoguePhrase build() {
            return DefaultDialoguePhrase.builder().build();
        }
    }

    interface SellerDialoguePhrase {
        String SPEAKER_ID = "speaker-2";
        DraftPhraseResource PHRASE = DraftPhraseResourceMother.SellerDialoguePhrase.build();

        static Builder builder() {
            return DialoguePhraseMother.builder()
                    .withSpeakerId(SPEAKER_ID)
                    .withPhrase(PHRASE);
        }

        static StructuredDialogueResource.DialoguePhrase build() {
            return SellerDialoguePhrase.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String speakerId;
        private DraftPhraseResource phrase;

        public Builder withSpeakerId(String speakerId) {
            this.speakerId = speakerId;
            return this;
        }

        public Builder withPhrase(DraftPhraseResource phrase) {
            this.phrase = phrase;
            return this;
        }

        public StructuredDialogueResource.DialoguePhrase build() {
            return new StructuredDialogueResource.DialoguePhrase(speakerId, phrase);
        }
    }
}
