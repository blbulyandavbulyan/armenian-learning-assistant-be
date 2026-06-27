package com.blbulyandavbulyan.larm.core;

import com.blbulyandavbulyan.larm.dialogue.DialogueSavingService;
import com.blbulyandavbulyan.larm.dialogue.SavedDialogueResource;
import com.blbulyandavbulyan.larm.dialogue.StoreDialogueParameters;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DialogueOrchestrator {

    private final PhraseProcessor phraseProcessor;
    private final DialogueSavingService dialogueSavingService;

    public SavedDialogueResource saveDialogue(SaveDialogueParameters parameters) {
        return dialogueSavingService.saveDialogue(
                StoreDialogueParameters.builder()
                        .titlePhrase(processDialogueTitle(parameters))
                        .speakers(parameters.speakers().stream().map(this::processSpeaker).toList())
                        .dialoguePhrases(parameters.dialoguePhrases().stream().map(this::processDialoguePhrase).toList())
                        .build());
    }

    private StoreDialogueParameters.StoreDialoguePhraseParameters processDialoguePhrase(
            SaveDialogueParameters.DialoguePhraseParameters phraseParameters) {

        SavePhraseParameters processedPhrase = phraseProcessor.process(
                CreateNewPhraseParameters.builder()
                        .phrase(phraseParameters.phrase())
                        .isoLanguageCode(phraseParameters.isoLanguageCode())
                        .transcription(phraseParameters.transcription())
                        .translations(phraseParameters.translations())
                        .build());
        return StoreDialogueParameters.StoreDialoguePhraseParameters.builder()
                .speakerRefId(phraseParameters.speakerRefId())
                .phrase(processedPhrase)
                .build();
    }

    private StoreDialogueParameters.StoreSpeakerParameters processSpeaker(SaveDialogueParameters.SpeakerParameters sp) {
        SavePhraseParameters processedSpeaker = phraseProcessor.process(
                CreateNewPhraseParameters.builder()
                        .phrase(sp.title())
                        .isoLanguageCode("hy")
                        .transcription(sp.transcription())
                        .translations(sp.translations())
                        .build());
        return StoreDialogueParameters.StoreSpeakerParameters.builder()
                .speakerRefId(sp.speakerRefId())
                .namePhrase(processedSpeaker)
                .build();
    }

    private SavePhraseParameters processDialogueTitle(SaveDialogueParameters parameters) {
        return phraseProcessor.process(
                CreateNewPhraseParameters.builder()
                        .phrase(parameters.title())
                        .isoLanguageCode("hy") // Assuming Armenian for now
                        .transcription(parameters.transcription())
                        .translations(parameters.titleTranslations())
                        .build());
    }
}
