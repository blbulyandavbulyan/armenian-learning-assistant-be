package com.blbulyandavbulyan.larm.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.blbulyandavbulyan.larm.dialogue.DialogueSavingService;
import com.blbulyandavbulyan.larm.dialogue.SaveDialogueParameters;
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
        SavePhraseParameters processedTitle = processDialogueTitle(parameters);
        List<StoreDialogueParameters.StoreSpeakerParameters> storeSpeakers = processSpeakers(parameters.speakers());
        List<StoreDialogueParameters.StoreDialoguePhraseParameters> storeDialoguePhrases = processDialoguePhrases(parameters.dialoguePhrases());

        return dialogueSavingService.saveDialogue(StoreDialogueParameters.builder()
                .titlePhrase(processedTitle)
                .speakers(storeSpeakers)
                .dialoguePhrases(storeDialoguePhrases)
                .build());
    }

    private List<StoreDialogueParameters.StoreDialoguePhraseParameters> processDialoguePhrases(
            List<SaveDialogueParameters.DialoguePhraseParameters> dialoguePhraseParameters) {
        List<StoreDialogueParameters.StoreDialoguePhraseParameters> storeDialoguePhrases = new ArrayList<>();
        for (SaveDialogueParameters.DialoguePhraseParameters dp : dialoguePhraseParameters) {
            SavePhraseParameters processedPhrase = phraseProcessor.process(
                    NewCreatePhraseParameters.builder()
                            .phrase(dp.phrase())
                            .isoLanguageCode(dp.isoLanguageCode())
                            .transcription(dp.transcription())
                            .translations(dp.translations())
                            .build());
            storeDialoguePhrases.add(StoreDialogueParameters.StoreDialoguePhraseParameters.builder()
                    .speakerRefId(dp.speakerRefId())
                    .phrase(processedPhrase)
                    .build());
        }
        return Collections.unmodifiableList(storeDialoguePhrases);
    }

    private List<StoreDialogueParameters.StoreSpeakerParameters> processSpeakers(List<SaveDialogueParameters.SpeakerParameters> speakers) {
        List<StoreDialogueParameters.StoreSpeakerParameters> storeSpeakers = new ArrayList<>();
        for (SaveDialogueParameters.SpeakerParameters sp : speakers) {
            SavePhraseParameters processedSpeaker = phraseProcessor.process(
                    NewCreatePhraseParameters.builder()
                            .phrase(sp.title())
                            .isoLanguageCode("hy")
                            .transcription(sp.transcription())
                            .translations(sp.translations())
                            .build());
            storeSpeakers.add(StoreDialogueParameters.StoreSpeakerParameters.builder()
                    .speakerRefId(sp.speakerRefId())
                    .namePhrase(processedSpeaker)
                    .build());
        }
        return Collections.unmodifiableList(storeSpeakers);
    }

    private SavePhraseParameters processDialogueTitle(SaveDialogueParameters parameters) {
        return phraseProcessor.process(
                NewCreatePhraseParameters.builder()
                        .phrase(parameters.title())
                        .isoLanguageCode("hy") // Assuming Armenian for now
                        .transcription(parameters.transcription())
                        .translations(parameters.titleTranslations())
                        .build());
    }
}
