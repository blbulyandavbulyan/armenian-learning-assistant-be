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

    interface DialogueTitlePhrase {
        UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111112");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Հացի փռում";
        String TRANSCRIPTION = "Hatsi prrum";
        String EMBEDDING_TEXT = PhraseMother.DialogueTitlePhrase.PHRASE + " ("
                + TranslationMother.DialogueTitleTranslation.TRANSLATION_TEXT + ")";

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.DialogueTitleTranslation.build());
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

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.DialogueSpeaker1NameTranslation.build());
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

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.DialogueSpeaker2NameTranslation.build());
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

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.DialoguePhrase1Translation.build());
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

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.DialoguePhrase2Translation.build());
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

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.DialoguePhrase3Translation.build());
        }

        static PhraseRecord build() {
            return DialoguePhrase3.builder().build();
        }
    }

    interface RealisticDialogueTitlePhrase {
        UUID ID = UUID.fromString("21111111-1111-1111-1111-111111111112");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Հանդիպում ընկերոջ հետ";
        String TRANSCRIPTION = "Handipum @nzeroch het";

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.RealisticDialogueTitleTranslation.build());
        }

        static PhraseRecord build() {
            return RealisticDialogueTitlePhrase.builder().build();
        }
    }

    interface RealisticDialogueSpeaker1NamePhrase {
        UUID ID = UUID.fromString("21111111-1111-1111-1111-111111111113");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Արամ";
        String TRANSCRIPTION = "Aram";

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.RealisticDialogueSpeaker1NameTranslation.build());
        }

        static PhraseRecord build() {
            return RealisticDialogueSpeaker1NamePhrase.builder().build();
        }
    }

    interface RealisticDialogueSpeaker2NamePhrase {
        UUID ID = UUID.fromString("21111111-1111-1111-1111-111111111114");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Աննա";
        String TRANSCRIPTION = "Anna";

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.RealisticDialogueSpeaker2NameTranslation.build());
        }

        static PhraseRecord build() {
            return RealisticDialogueSpeaker2NamePhrase.builder().build();
        }
    }

    interface RealisticDialoguePhrase1 {
        UUID ID = UUID.fromString("21111111-1111-1111-1111-111111111115");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Բարեւ";
        String TRANSCRIPTION = "Barev";

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.RealisticDialoguePhrase1Translation.build());
        }

        static PhraseRecord build() {
            return RealisticDialoguePhrase1.builder().build();
        }
    }

    interface RealisticDialoguePhrase2 {
        UUID ID = UUID.fromString("21111111-1111-1111-1111-111111111117");
        PhraseStatus STATUS = PhraseStatus.DRAFT;
        String ISO_LANGUAGE_CODE = "hy";
        String PHRASE = "Ինչպես ես:";
        String TRANSCRIPTION = "Inchpes es:";

        static Builder builder() {
            return PhraseMother.builder()
                    .withId(ID)
                    .withStatus(STATUS)
                    .withIsoLanguageCode(ISO_LANGUAGE_CODE)
                    .withPhrase(PHRASE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TranslationMother.RealisticDialoguePhrase2Translation.build());
        }

        static PhraseRecord build() {
            return RealisticDialoguePhrase2.builder().build();
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
