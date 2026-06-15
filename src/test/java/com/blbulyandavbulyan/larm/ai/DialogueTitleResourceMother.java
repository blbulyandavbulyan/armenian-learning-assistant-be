package com.blbulyandavbulyan.larm.ai;

import java.util.List;

import com.blbulyandavbulyan.larm.ai.chat.DraftTranslationResource;
import com.blbulyandavbulyan.larm.ai.chat.DraftTranslationResourceMother;

public interface DialogueTitleResourceMother {

    interface DefaultDialogueTitle {
        String TITLE = "Խանութում";
        String TRANSCRIPTION = "Khanutum";

        List<DraftTranslationResource> TRANSLATIONS = List.of(
                DraftTranslationResourceMother.DefaultDialogueTitleTranslation.build()
        );

        static Builder builder() {
            return DialogueTitleResourceMother.builder()
                    .withTitle(TITLE)
                    .withTranscription(TRANSCRIPTION)
                    .withTranslations(TRANSLATIONS);
        }

        static StructuredDialogueResource.DialogueTitleResource build() {
            return DefaultDialogueTitle.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String title;
        private String transcription;
        private List<DraftTranslationResource> translations;

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

        public StructuredDialogueResource.DialogueTitleResource build() {
            return new StructuredDialogueResource.DialogueTitleResource(title, transcription, translations);
        }
    }
}
