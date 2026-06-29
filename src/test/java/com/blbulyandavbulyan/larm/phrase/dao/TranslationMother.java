package com.blbulyandavbulyan.larm.phrase.dao;

import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.dao.projections.TranslationRecord;

public interface TranslationMother {

    interface DialogueTitleTranslation {
        UUID ID = UUID.fromString("bc5333d4-c70b-4121-8bdc-4b94547de420");
        String ISO_LANGUAGE_CODE = "ru";
        String TRANSLATION_TEXT = "В пекарне";

        static Builder builder() {
            return TranslationMother.builder()
                    .withId(ID)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withTranslationText(TRANSLATION_TEXT);
        }

        static TranslationRecord build() {
            return DialogueTitleTranslation.builder().build();
        }
    }

    interface DialogueSpeaker1NameTranslation {
        UUID ID = UUID.fromString("bc5333d4-c70b-4121-8bdc-4b94547de421");
        String ISO_LANGUAGE_CODE = "ru";
        String TRANSLATION_TEXT = "Пекарь";

        static Builder builder() {
            return TranslationMother.builder()
                    .withId(ID)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withTranslationText(TRANSLATION_TEXT);
        }

        static TranslationRecord build() {
            return DialogueSpeaker1NameTranslation.builder().build();
        }
    }

    interface DialogueSpeaker2NameTranslation {
        UUID ID = UUID.fromString("bc5333d4-c70b-4121-8bdc-4b94547de422");
        String ISO_LANGUAGE_CODE = "ru";
        String TRANSLATION_TEXT = "Покупатель";

        static Builder builder() {
            return TranslationMother.builder()
                    .withId(ID)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withTranslationText(TRANSLATION_TEXT);
        }

        static TranslationRecord build() {
            return DialogueSpeaker2NameTranslation.builder().build();
        }
    }

    interface DialoguePhrase1Translation {
        UUID ID = UUID.fromString("bc5333d4-c70b-4121-8bdc-4b94547de423");
        String ISO_LANGUAGE_CODE = "ru";
        String TRANSLATION_TEXT = "Здравствуйте";

        static Builder builder() {
            return TranslationMother.builder()
                    .withId(ID)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withTranslationText(TRANSLATION_TEXT);
        }

        static TranslationRecord build() {
            return DialoguePhrase1Translation.builder().build();
        }
    }

    interface DialoguePhrase2Translation {
        UUID ID = UUID.fromString("bc5333d4-c70b-4121-8bdc-4b94547de424");
        String ISO_LANGUAGE_CODE = "ru";
        String TRANSLATION_TEXT = "Здравствуйте, пожалуйста, один хлеб.";

        static Builder builder() {
            return TranslationMother.builder()
                    .withId(ID)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withTranslationText(TRANSLATION_TEXT);
        }

        static TranslationRecord build() {
            return DialoguePhrase2Translation.builder().build();
        }
    }

    interface DialoguePhrase3Translation {
        UUID ID = UUID.fromString("bc5333d4-c70b-4121-8bdc-4b94547de425");
        String ISO_LANGUAGE_CODE = "ru";
        String TRANSLATION_TEXT = "Вот, пожалуйста.";

        static Builder builder() {
            return TranslationMother.builder()
                    .withId(ID)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withTranslationText(TRANSLATION_TEXT);
        }

        static TranslationRecord build() {
            return DialoguePhrase3Translation.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private UUID id;
        private String isoLanguageCode;
        private String translationText;

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

        public TranslationRecord build() {
            return new TranslationRecord(id, isoLanguageCode, translationText);
        }
    }
}
