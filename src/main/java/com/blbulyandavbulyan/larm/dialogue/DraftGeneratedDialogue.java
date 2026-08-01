package com.blbulyandavbulyan.larm.dialogue;

import java.util.List;

import com.blbulyandavbulyan.larm.validation.ValidDialogueSpeakers;
import com.blbulyandavbulyan.larm.validation.ValidIsoLanguageCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@SuppressWarnings("java:S1452") // out of my face sonar, without wildcard
@ValidDialogueSpeakers
public interface DraftGeneratedDialogue {
    @NotNull
    @Valid
    DraftDialogueTitle info();

    @NotEmpty
    List<@NotNull @Valid ? extends DraftSpeaker> speakers();

    @NotEmpty
    List<@NotNull @Valid ? extends DraftDialoguePhrase> dialoguePhrases();

    interface DraftDialogueTitle {
        @NotBlank
        String title();

        @NotBlank
        String transcription();

        @NotEmpty
        List<@NotNull @Valid ? extends DraftTranslation> translations();
    }

    interface DraftSpeaker {
        @NotBlank
        String id();

        @NotBlank
        String title();

        @NotBlank
        String transcription();

        @NotEmpty
        List<@NotNull @Valid ? extends  DraftTranslation> translations();
    }

    interface DraftTranslation {
        @NotBlank
        String translationText();

        @NotBlank
        @ValidIsoLanguageCode
        String isoLanguageCode();
    }

    interface DraftPhrase {
        @NotBlank
        String phrase();

        @NotBlank
        @ValidIsoLanguageCode(supportedLanguages = "hy")
        String isoLanguageCode();

        @NotBlank
        String transcription();

        @NotEmpty
        List<@NotNull @Valid ? extends DraftTranslation> translations();
    }

    interface DraftDialoguePhrase {
        @NotBlank
        String speakerId();

        @NotNull
        @Valid
        DraftPhrase phrase();
    }
}
