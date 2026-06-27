package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.List;

import com.blbulyandavbulyan.larm.core.SaveDialogueParameters;
import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import org.springframework.stereotype.Component;

@Component
class DialogueRequestMapper {

    SaveDialogueParameters toParameters(SaveDialogueRequest request) {
        return SaveDialogueParameters.builder()
                .title(request.info().title())
                .transcription(request.info().transcription())
                .titleTranslations(mapTranslations(request.info().translations()))
                .speakers(request.speakers().stream().map(this::mapSpeaker).toList())
                .dialoguePhrases(request.dialoguePhrases().stream().map(this::mapDialoguePhrase).toList())
                .build();
    }

    private SaveDialogueParameters.SpeakerParameters mapSpeaker(SaveDialogueRequest.SpeakerRequest s) {
        return SaveDialogueParameters.SpeakerParameters.builder()
                .speakerRefId(s.id())
                .title(s.title())
                .transcription(s.transcription())
                .translations(mapTranslations(s.translations()))
                .build();
    }

    private SaveDialogueParameters.DialoguePhraseParameters mapDialoguePhrase(
            SaveDialogueRequest.DialoguePhraseRequest request) {
        return SaveDialogueParameters.DialoguePhraseParameters.builder()
                .speakerRefId(request.speakerId())
                .phrase(request.phrase().phrase())
                .isoLanguageCode(request.phrase().isoLanguageCode())
                .transcription(request.phrase().transcription())
                .translations(mapTranslations(request.phrase().translations()))
                .build();
    }

    private List<CreateTranslationParameters> mapTranslations(
            List<SaveDialogueRequest.TranslationRequest> translations) {
        return translations.stream()
                .map(t -> CreateTranslationParameters.builder()
                        .isoLanguageCode(t.isoLanguageCode())
                        .translationText(t.translationText())
                        .build())
                .toList();
    }
}
