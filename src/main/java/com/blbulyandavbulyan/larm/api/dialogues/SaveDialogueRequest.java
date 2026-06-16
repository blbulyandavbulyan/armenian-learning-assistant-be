package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.List;

import com.blbulyandavbulyan.larm.api.dialogues.validation.ValidDialogueSpeakers;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@ValidDialogueSpeakers
@Schema(name = OpenApiConstants.SchemaNames.SAVE_DIALOGUE_REQUEST)
public record SaveDialogueRequest(
        @Schema(description = "Dialogue title information")
        @NotNull @Valid DialogueTitleRequest info,
        
        @Schema(description = "List of speakers in the dialogue")
        @NotEmpty List<@NotNull @Valid SpeakerRequest> speakers,
        
        @Schema(description = "List of phrases spoken in the dialogue in chronological order")
        @NotEmpty List<@NotNull @Valid DialoguePhraseRequest> dialoguePhrases) {

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_DIALOGUE_TITLE_REQUEST)
    public record DialogueTitleRequest(
            @Schema(description = OpenApiConstants.Descriptions.DIALOGUE_TITLE, example = OpenApiConstants.Examples.DIALOGUE_TITLE)
            @NotBlank String title,
            
            @Schema(description = OpenApiConstants.Descriptions.GENERATED_TRANSCRIPTION, example = OpenApiConstants.Examples.DIALOGUE_TRANSCRIPTION)
            @NotBlank String transcription,
            
            @NotEmpty List<@NotNull @Valid TranslationRequest> translations) {
    }

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_SPEAKER_REQUEST)
    public record SpeakerRequest(
            @Schema(description = OpenApiConstants.Descriptions.SPEAKER_ID, example = OpenApiConstants.Examples.SPEAKER_ID)
            @NotBlank String id,
            
            @Schema(description = OpenApiConstants.Descriptions.SPEAKER_TITLE, example = OpenApiConstants.Examples.SPEAKER_TITLE)
            @NotBlank String title,
            
            @Schema(description = OpenApiConstants.Descriptions.GENERATED_TRANSCRIPTION, example = OpenApiConstants.Examples.SPEAKER_TRANSCRIPTION)
            @NotBlank String transcription,
            
            @NotEmpty List<@NotNull @Valid TranslationRequest> translations) {
    }

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_DIALOGUE_PHRASE_REQUEST)
    public record DialoguePhraseRequest(
            @Schema(description = OpenApiConstants.Descriptions.SPEAKER_ID, example = OpenApiConstants.Examples.SPEAKER_ID)
            @NotBlank String speakerId,
            
            @NotNull @Valid PhraseRequest phrase) {
    }

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_DIALOGUE_PHRASE_INNER_REQUEST)
    public record PhraseRequest(
            @Schema(description = OpenApiConstants.Descriptions.GENERATED_PHRASE, example = OpenApiConstants.Examples.PHRASE)
            @NotBlank String phrase,
            
            @Schema(description = OpenApiConstants.Descriptions.ISO_LANGUAGE_CODE, example = OpenApiConstants.Examples.PHRASE_ISO_LANGUAGE_CODE)
            @NotBlank String isoLanguageCode,
            
            @Schema(description = OpenApiConstants.Descriptions.GENERATED_TRANSCRIPTION, example = OpenApiConstants.Examples.TRANSCRIPTION)
            @NotBlank String transcription,
            
            @NotEmpty List<@NotNull @Valid TranslationRequest> translations) {
    }

    @Builder
    @Schema(name = OpenApiConstants.SchemaNames.SAVE_DIALOGUE_TRANSLATION_REQUEST)
    public record TranslationRequest(
            @Schema(description = OpenApiConstants.Descriptions.GENERATED_TRANSLATION_TEXT)
            @NotBlank String translationText,
            
            @Schema(description = OpenApiConstants.Descriptions.ISO_LANGUAGE_CODE)
            @NotBlank String isoLanguageCode) {
    }
}
