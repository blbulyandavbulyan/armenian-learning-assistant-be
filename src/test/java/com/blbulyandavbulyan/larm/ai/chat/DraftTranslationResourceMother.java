package com.blbulyandavbulyan.larm.ai.chat;

public interface DraftTranslationResourceMother {

    interface DefaultDraftTranslation {
        String TRANSLATION_TEXT = "Где находится хлеб?";
        String ISO_LANGUAGE_CODE = "ru";

        static Builder builder() {
            return DraftTranslationResourceMother.builder()
                    .withTranslationText(TRANSLATION_TEXT)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE);
        }

        static DraftTranslationResource build() {
            return DefaultDraftTranslation.builder().build();
        }
    }

    interface DefaultDialoguePhraseTranslation {
        String TRANSLATION_TEXT = "Здравствуйте";
        String ISO_LANGUAGE_CODE = "ru";

        static Builder builder() {
            return DraftTranslationResourceMother.builder()
                    .withTranslationText(TRANSLATION_TEXT)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE);
        }

        static DraftTranslationResource build() {
            return DefaultDialoguePhraseTranslation.builder().build();
        }
    }

    interface DefaultDialogueTitleTranslation {
        String TRANSLATION_TEXT = "В магазине";
        String ISO_LANGUAGE_CODE = "ru";

        static Builder builder() {
            return DraftTranslationResourceMother.builder()
                    .withTranslationText(TRANSLATION_TEXT)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE);
        }

        static DraftTranslationResource build() {
            return DefaultDialogueTitleTranslation.builder().build();
        }
    }

    interface DefaultSpeakerTranslation {
        String TRANSLATION_TEXT = "Покупатель";
        String ISO_LANGUAGE_CODE = "ru";

        static Builder builder() {
            return DraftTranslationResourceMother.builder()
                    .withTranslationText(TRANSLATION_TEXT)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE);
        }

        static DraftTranslationResource build() {
            return DefaultSpeakerTranslation.builder().build();
        }
    }

    interface SellerSpeakerTranslation {
        String TRANSLATION_TEXT = "Продавец";
        String ISO_LANGUAGE_CODE = "ru";

        static Builder builder() {
            return DraftTranslationResourceMother.builder()
                    .withTranslationText(TRANSLATION_TEXT)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE);
        }

        static DraftTranslationResource build() {
            return SellerSpeakerTranslation.builder().build();
        }
    }

    interface SellerDialoguePhraseTranslation {
        String TRANSLATION_TEXT = "Здравствуйте, чем я могу помочь?";
        String ISO_LANGUAGE_CODE = "ru";

        static Builder builder() {
            return DraftTranslationResourceMother.builder()
                    .withTranslationText(TRANSLATION_TEXT)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE);
        }

        static DraftTranslationResource build() {
            return SellerDialoguePhraseTranslation.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String translationText;
        private String isoLanguageCode;

        public Builder withTranslationText(String translationText) {
            this.translationText = translationText;
            return this;
        }

        public Builder withIsoLanguageCode(String isoLanguageCode) {
            this.isoLanguageCode = isoLanguageCode;
            return this;
        }

        public DraftTranslationResource build() {
            return new DraftTranslationResource(translationText, isoLanguageCode);
        }
    }
}
