package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import com.blbulyandavbulyan.larm.dialogue.DraftGeneratedDialogue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Save Dialogue Request")
public record SaveDialogueRequest(
        @Schema(description = Descriptions.DIALOGUE_TITLE_INFO)
        DialogueTitleRequest info,

        @Schema(description = Descriptions.SPEAKERS_LIST)
        List<SpeakerRequest> speakers,

        @Schema(description = Descriptions.DIALOGUE_PHRASES)
        List<DialoguePhraseRequest> dialoguePhrases) implements DraftGeneratedDialogue {

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_DIALOGUE_TITLE_REQUEST)
    public record DialogueTitleRequest(
            @Schema(description = Descriptions.DIALOGUE_TITLE, example = Examples.DIALOGUE_TITLE)
            String title,

            @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.DIALOGUE_TRANSCRIPTION)
            String transcription,

            List<TranslationRequest> translations) implements DraftDialogueTitle {
    }

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_SPEAKER_REQUEST)
    public record SpeakerRequest(
            @Schema(description = Descriptions.SPEAKER_ID, example = Examples.SPEAKER_ID)
            String id,

            @Schema(description = Descriptions.SPEAKER_TITLE, example = Examples.SPEAKER_TITLE)
            String title,

            @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.SPEAKER_TRANSCRIPTION)
            String transcription,

            List<TranslationRequest> translations) implements DraftSpeaker {
    }

    @Schema(name = "Save Dialogue Phrase Request")
    public record DialoguePhraseRequest(
            @Schema(description = Descriptions.SPEAKER_ID, example = Examples.SPEAKER_ID)
            String speakerId,

            PhraseRequest phrase) implements DraftDialoguePhrase {
    }

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_DIALOGUE_PHRASE_INNER_REQUEST)
    public record PhraseRequest(
            @Schema(description = Descriptions.GENERATED_PHRASE, example = Examples.PHRASE)
            String phrase,

            @Schema(description = Descriptions.ISO_LANGUAGE_CODE, example = Examples.PHRASE_ISO_LANGUAGE_CODE)
            String isoLanguageCode,

            @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.TRANSCRIPTION)
            String transcription,

            List<TranslationRequest> translations) implements DraftPhrase {
    }

    @Builder
    @Schema(name = "Save Dialogue Translation Request")
    public record TranslationRequest(
            @Schema(description = Descriptions.GENERATED_TRANSLATION_TEXT)
            String translationText,

            @Schema(description = Descriptions.ISO_LANGUAGE_CODE)
            String isoLanguageCode) implements DraftTranslation {
    }
}
