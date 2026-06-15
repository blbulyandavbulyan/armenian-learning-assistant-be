package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;

public interface DraftPhraseResourceMother {

    interface DefaultDraftPhrase {
        String PHRASE = "Որտե՞ղ է հացը:";
        String ISO_LANGUAGE_CODE = "hy";
        String TRANSCRIPTION = "Vortegh e hatsy?";
        List<DraftTranslationResource> TRANSLATIONS = List.of(DraftTranslationResourceMother.DefaultDraftTranslation.build());

        static Builder builder() {
            return DraftPhraseResourceMother.builder()
                    .withPhrase(PHRASE)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TRANSLATIONS);
        }

        static DraftPhraseResource build() {
            return DefaultDraftPhrase.builder().build();
        }
    }

    interface DefaultDialoguePhrase {
        String PHRASE = "Բարև ձեզ";
        String ISO_LANGUAGE_CODE = "hy";
        String TRANSCRIPTION = "Barev dzez";
        List<DraftTranslationResource> TRANSLATIONS = List.of(DraftTranslationResourceMother.DefaultDialoguePhraseTranslation.build());

        static Builder builder() {
            return DraftPhraseResourceMother.builder()
                    .withPhrase(PHRASE)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TRANSLATIONS);
        }

        static DraftPhraseResource build() {
            return DefaultDialoguePhrase.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String phrase;
        private String isoLanguageCode;
        private String transcription;
        private List<DraftTranslationResource> translations;

        public Builder withPhrase(String phrase) {
            this.phrase = phrase;
            return this;
        }

        public Builder withIsoLanguageCode(String isoLanguageCode) {
            this.isoLanguageCode = isoLanguageCode;
            return this;
        }

        public Builder withTranscription(String transcription) {
            this.transcription = transcription;
            return this;
        }

        public Builder withTranslations(List<DraftTranslationResource> translations) {
            this.translations = translations;
            return this;
        }

        public DraftPhraseResource build() {
            return new DraftPhraseResource(phrase, isoLanguageCode, transcription, translations);
        }
    }
}
