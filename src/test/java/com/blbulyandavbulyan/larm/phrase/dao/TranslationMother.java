package com.blbulyandavbulyan.larm.phrase.dao;

import java.time.Instant;
import java.util.UUID;

public interface TranslationMother {
    interface DefaultTranslation {
        UUID ID = UUID.fromString("bc5333d4-c70b-4121-8bdc-4b94547de419");
        String ISO_LANGUAGE_CODE = "en";
        String TRANSLATION_TEXT = "Where is the bread section?";
        Instant CREATED_AT = Instant.parse("2026-06-13T10:00:00Z");

        static Builder builder() {
            return TranslationMother.builder()
                    .withId(ID)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withTranslationText(TRANSLATION_TEXT)
                    .withCreatedAt(CREATED_AT);
        }

        static Translation build() {
            return DefaultTranslation.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private UUID id;
        private String isoLanguageCode;
        private String translationText;
        private Instant createdAt;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withIsoLanguageCode(String isoLanguageCode) {
            this.isoLanguageCode = isoLanguageCode;
            return this;
        }

        public Builder withTranslationText(String translationText) {
            this.translationText = translationText;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Translation build() {
            return new Translation(id, isoLanguageCode, translationText, createdAt);
        }
    }
}
