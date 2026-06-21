package com.blbulyandavbulyan.larm.phrase.dao;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.phrase.dao.projections.MediaRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.PhraseRecord;
import com.blbulyandavbulyan.larm.phrase.dao.projections.TranslationRecord;

public interface PhraseMother {
    interface DefaultPhrase {
        UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Որտե՞ղ է հացի բաժինը:";
        String TRANSCRIPTION = "Vortegh e hatsi bazhiny?";

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.DefaultTranslation.build());
        }

    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private UUID id;
        private PhraseStatus status;
        private String isoLanguageCode;
        private String phrase;
        private String transcription;
        private Set<TranslationRecord> translations;
        private Set<MediaRecord> mediaSet;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withStatus(PhraseStatus status) {
            this.status = status;
            return this;
        }

        public Builder withIsoLanguageCode(String isoLanguageCode) {
            this.isoLanguageCode = isoLanguageCode;
            return this;
        }

        public Builder withPhrase(String phrase) {
            this.phrase = phrase;
            return this;
        }

        public Builder withTranscription(String transcription) {
            this.transcription = transcription;
            return this;
        }

        public Builder withTranslations(TranslationRecord... translations) {
            this.translations = Arrays.stream(translations).collect(Collectors.toSet());
            return this;
        }

        public Builder withMedias(MediaRecord... medias) {
            this.mediaSet = Arrays.stream(medias).collect(Collectors.toSet());
            return this;
        }

        public PhraseRecord build() {
            return new PhraseRecord(id, status, isoLanguageCode, phrase, transcription, translations, mediaSet);
        }
    }
}
