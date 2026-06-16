package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;

public interface StructuredPhrasesResourceMother {

    interface DefaultStructuredPhrasesResource {
        String MESSAGE = "Here is the phrase";
        List<DraftPhraseResource> PHRASES = List.of(DraftPhraseResourceMother.DefaultDraftPhrase.build());

        static Builder builder() {
            return StructuredPhrasesResourceMother.builder()
                    .withMessage(MESSAGE)
                    .withPhrases(PHRASES);
        }

        static StructuredPhrasesResource build() {
            return DefaultStructuredPhrasesResource.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String message;
        private List<DraftPhraseResource> phrases;

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withPhrases(List<DraftPhraseResource> phrases) {
            this.phrases = phrases;
            return this;
        }

        public StructuredPhrasesResource build() {
            return new StructuredPhrasesResource(message, phrases);
        }
    }
}
