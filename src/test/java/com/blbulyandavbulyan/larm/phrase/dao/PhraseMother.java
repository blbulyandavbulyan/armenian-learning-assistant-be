package com.blbulyandavbulyan.larm.phrase.dao;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.PhraseStatus;
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
        String EMBEDDING_TEXT = PhraseMother.DefaultPhrase.PHRASE + " ("
                + TranslationMother.DefaultTranslation.TRANSLATION_TEXT + ")";

        static float[] embedding() {
            float[] embedding = new float[3072];
            embedding[0] = 0.123f;
            embedding[1] = 0.456f;
            return embedding;
        }

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withEmbedding(embedding())
                    .withTranslations(TranslationMother.DefaultTranslation.build());
        }
    }

    interface DialogueTitlePhrase {
        UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111112");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Հացի փռում";
        String TRANSCRIPTION = "Hatsi prrum";
        String EMBEDDING_TEXT = PhraseMother.DialogueTitlePhrase.PHRASE + " ("
                + TranslationMother.DialogueTitleTranslation.TRANSLATION_TEXT + ")";

        static float[] embedding() {
            float[] embedding = new float[3072];
            Arrays.fill(embedding, 0.1f);
            return embedding;
        }

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withEmbedding(embedding())
                    .withTranslations(TranslationMother.DialogueTitleTranslation.build())
                    .withMedias(MediaMother.DefaultMedia.builder().build());
        }

        static PhraseRecord build() {
            return DialogueTitlePhrase.builder().build();
        }
    }

    interface DialogueSpeaker1NamePhrase {
        UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111113");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Հացթուխ";
        String TRANSCRIPTION = "Hatstukh";
        String EMBEDDING_TEXT = PhraseMother.DialogueSpeaker1NamePhrase.PHRASE + " ("
                + TranslationMother.DialogueSpeaker1NameTranslation.TRANSLATION_TEXT + ")";

        static float[] embedding() {
            float[] embedding = new float[3072];
            embedding[0] = 0.2f;
            return embedding;
        }

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withEmbedding(embedding())
                    .withTranslations(TranslationMother.DialogueSpeaker1NameTranslation.build())
                    .withMedias(MediaMother.DefaultMedia.builder().build());
        }

        static PhraseRecord build() {
            return DialogueSpeaker1NamePhrase.builder().build();
        }
    }

    interface DialogueSpeaker2NamePhrase {
        UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111114");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Գնորդ";
        String TRANSCRIPTION = "Gnord";
        String EMBEDDING_TEXT = PhraseMother.DialogueSpeaker2NamePhrase.PHRASE + " ("
                + TranslationMother.DialogueSpeaker2NameTranslation.TRANSLATION_TEXT + ")";

        static float[] embedding() {
            float[] embedding = new float[3072];
            embedding[0] = 0.3f;
            return embedding;
        }

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withEmbedding(embedding())
                    .withTranslations(TranslationMother.DialogueSpeaker2NameTranslation.build())
                    .withMedias(MediaMother.DefaultMedia.builder().build());
        }

        static PhraseRecord build() {
            return DialogueSpeaker2NamePhrase.builder().build();
        }
    }

    interface DialoguePhrase1 {
        UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111115");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Բարեւ ձեզ";
        String TRANSCRIPTION = "Barev dzez";
        String EMBEDDING_TEXT = PhraseMother.DialoguePhrase1.PHRASE + " ("
                + TranslationMother.DialoguePhrase1Translation.TRANSLATION_TEXT + ")";

        static float[] embedding() {
            float[] embedding = new float[3072];
            embedding[0] = 0.4f;
            return embedding;
        }

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withEmbedding(embedding())
                    .withTranslations(TranslationMother.DialoguePhrase1Translation.build())
                    .withMedias(MediaMother.DefaultMedia.builder().build());
        }

        static PhraseRecord build() {
            return DialoguePhrase1.builder().build();
        }
    }

    interface DialoguePhrase2 {
        UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111116");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Բարեւ ձեզ, խնդրում եմ մեկ հաց:";
        String TRANSCRIPTION = "Barev dzez, khndrum em mek hats.";
        String EMBEDDING_TEXT = PhraseMother.DialoguePhrase2.PHRASE + " ("
                + TranslationMother.DialoguePhrase2Translation.TRANSLATION_TEXT + ")";

        static float[] embedding() {
            float[] embedding = new float[3072];
            embedding[0] = 0.5f;
            return embedding;
        }

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withEmbedding(embedding())
                    .withTranslations(TranslationMother.DialoguePhrase2Translation.build())
                    .withMedias(MediaMother.DefaultMedia.builder().build());
        }

        static PhraseRecord build() {
            return DialoguePhrase2.builder().build();
        }
    }

    interface DialoguePhrase3 {
        UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111117");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Ահա, խնդրեմ:";
        String TRANSCRIPTION = "Aha, khndrem.";
        String EMBEDDING_TEXT = PhraseMother.DialoguePhrase3.PHRASE + " ("
                + TranslationMother.DialoguePhrase3Translation.TRANSLATION_TEXT + ")";

        static float[] embedding() {
            float[] embedding = new float[3072];
            embedding[0] = 0.6f;
            return embedding;
        }

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withEmbedding(embedding())
                    .withTranslations(TranslationMother.DialoguePhrase3Translation.build())
                    .withMedias(MediaMother.DefaultMedia.builder().build());
        }

        static PhraseRecord build() {
            return DialoguePhrase3.builder().build();
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
        private float[] embedding;

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

        public Builder withEmbedding(float[] embedding) {
            this.embedding = embedding;
            return this;
        }

        public PhraseRecord build() {
            return new PhraseRecord(id, status, isoLanguageCode, phrase, transcription, translations, mediaSet, embedding);
        }
    }
}
