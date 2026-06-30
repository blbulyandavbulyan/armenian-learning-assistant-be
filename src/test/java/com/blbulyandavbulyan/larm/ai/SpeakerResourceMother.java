package com.blbulyandavbulyan.larm.ai;

import java.util.List;

import com.blbulyandavbulyan.larm.ai.chat.DraftTranslationResource;
import com.blbulyandavbulyan.larm.ai.chat.DraftTranslationResourceMother;
import com.blbulyandavbulyan.larm.ai.chat.StructuredDialogueResource;

public interface SpeakerResourceMother {

    interface DefaultSpeaker {
        String ID = "speaker-1";
        String TITLE = "Գնորդ";
        String TRANSCRIPTION = "Gnord";
        List<DraftTranslationResource> TRANSLATIONS = List.of(
                DraftTranslationResourceMother.DefaultSpeakerTranslation.build()
        );

        static Builder builder() {
            return SpeakerResourceMother.builder()
                    .withId(ID)
                    .withTitle(TITLE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TRANSLATIONS);
        }

        static StructuredDialogueResource.SpeakerResource build() {
            return DefaultSpeaker.builder().build();
        }
    }

    interface SellerSpeaker {
        String ID = "speaker-2";
        String TITLE = "Վաճառող";
        String TRANSCRIPTION = "Vacharogh";
        List<DraftTranslationResource> TRANSLATIONS = List.of(
                DraftTranslationResourceMother.SellerSpeakerTranslation.build()
        );

        static Builder builder() {
            return SpeakerResourceMother.builder()
                    .withId(ID)
                    .withTitle(TITLE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TRANSLATIONS);
        }

        static StructuredDialogueResource.SpeakerResource build() {
            return SellerSpeaker.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String id;
        private String title;
        private String transcription;
        private List<DraftTranslationResource> translations;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
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

        public StructuredDialogueResource.SpeakerResource build() {
            return new StructuredDialogueResource.SpeakerResource(id, title, transcription, translations);
        }
    }
}
