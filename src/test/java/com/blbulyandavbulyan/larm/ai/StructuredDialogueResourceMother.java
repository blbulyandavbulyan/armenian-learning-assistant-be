package com.blbulyandavbulyan.larm.ai;

import java.util.List;

public interface StructuredDialogueResourceMother {

    interface DefaultStructuredDialogueResource {
        String MESSAGE = "Dialogue generated";
        StructuredDialogueResource.DialogueTitleResource INFO = DialogueTitleResourceMother.DefaultDialogueTitle.build();
        List<StructuredDialogueResource.SpeakerResource> SPEAKERS = List.of(SpeakerResourceMother.DefaultSpeaker.build());
        List<StructuredDialogueResource.DialoguePhrase> DIALOGUE_PHRASES = List.of(DialoguePhraseMother.DefaultDialoguePhrase.build());

        static Builder builder() {
            return StructuredDialogueResourceMother.builder()
                    .withInfo(INFO)
                    .withMessage(MESSAGE)
                    .withSpeakers(SPEAKERS)
                    .withDialoguePhrases(DIALOGUE_PHRASES);
        }

        static StructuredDialogueResource build() {
            return DefaultStructuredDialogueResource.builder().build();
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String message;
        private StructuredDialogueResource.DialogueTitleResource info;
        private List<StructuredDialogueResource.SpeakerResource> speakers;
        private List<StructuredDialogueResource.DialoguePhrase> dialoguePhrases;

        public Builder withInfo(StructuredDialogueResource.DialogueTitleResource info) {
            this.info = info;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withSpeakers(List<StructuredDialogueResource.SpeakerResource> speakers) {
            this.speakers = speakers;
            return this;
        }

        public Builder withDialoguePhrases(List<StructuredDialogueResource.DialoguePhrase> dialoguePhrases) {
            this.dialoguePhrases = dialoguePhrases;
            return this;
        }

        public StructuredDialogueResource build() {
            return new StructuredDialogueResource(info, message, speakers, dialoguePhrases);
        }
    }
}
