package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.List;

import com.blbulyandavbulyan.larm.api.dialogues.validation.ValidDialogueSpeakers;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import com.blbulyandavbulyan.larm.validation.ValidIsoLanguageCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@ValidDialogueSpeakers
@Schema(name = "Save Dialogue Request")
public record SaveDialogueRequest(
        @Schema(description = Descriptions.DIALOGUE_TITLE_INFO)
        @NotNull @Valid DialogueTitleRequest info,
        
        @Schema(description = "List of speakers in the dialogue")
        @NotEmpty List<@NotNull @Valid SpeakerRequest> speakers,
        
        @Schema(description = "List of phrases spoken in the dialogue in chronological order")
        @NotEmpty List<@NotNull @Valid DialoguePhraseRequest> dialoguePhrases) {

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_DIALOGUE_TITLE_REQUEST)
    public record DialogueTitleRequest(
            @Schema(description = Descriptions.DIALOGUE_TITLE, example = Examples.DIALOGUE_TITLE)
            @NotBlank String title,
            
            @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.DIALOGUE_TRANSCRIPTION)
            @NotBlank String transcription,
            
            @NotEmpty List<@NotNull @Valid TranslationRequest> translations) {
    }

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_SPEAKER_REQUEST)
    public record SpeakerRequest(
            @Schema(description = Descriptions.SPEAKER_ID, example = Examples.SPEAKER_ID)
            @NotBlank String id,
            
            @Schema(description = Descriptions.SPEAKER_TITLE, example = Examples.SPEAKER_TITLE)
            @NotBlank String title,
            
            @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.SPEAKER_TRANSCRIPTION)
            @NotBlank String transcription,
            
            @NotEmpty List<@NotNull @Valid TranslationRequest> translations) {
    }

    @Schema(name = "Save Dialogue Phrase Request")
    public record DialoguePhraseRequest(
            @Schema(description = Descriptions.SPEAKER_ID, example = Examples.SPEAKER_ID)
            @NotBlank String speakerId,
            
            @NotNull @Valid PhraseRequest phrase) {
    }

    @Schema(name = OpenApiConstants.SchemaNames.SAVE_DIALOGUE_PHRASE_INNER_REQUEST)
    public record PhraseRequest(
            @Schema(description = Descriptions.GENERATED_PHRASE, example = Examples.PHRASE)
            @NotBlank String phrase,
            
            @Schema(description = Descriptions.ISO_LANGUAGE_CODE, example = Examples.PHRASE_ISO_LANGUAGE_CODE)
            @NotBlank @ValidIsoLanguageCode String isoLanguageCode,
            
            @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.TRANSCRIPTION)
            @NotBlank String transcription,
            
            @NotEmpty List<@NotNull @Valid TranslationRequest> translations) {
    }

    @Builder
    @Schema(name = "Save Dialogue Translation Request")
    public record TranslationRequest(
            @Schema(description = Descriptions.GENERATED_TRANSLATION_TEXT)
            @NotBlank String translationText,
            
            @Schema(description = Descriptions.ISO_LANGUAGE_CODE)
            @NotBlank @ValidIsoLanguageCode String isoLanguageCode) {
    }
}
