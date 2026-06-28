package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.blbulyandavbulyan.larm.api.phrases.PhraseResponseMapper;
import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DialogueResponseMapper {
    private final PhraseResponseMapper phraseResponseMapper;

    public GetDialogueResponse toResponse(Dialogue dialogue) {
        return GetDialogueResponse.builder()
                .id(dialogue.getId())
                .title(phraseResponseMapper.mapToPhraseResponse(dialogue.getTitle(), dialogue.getTitleTranslations()))
                .speakers(mapSpeakers(dialogue))
                .dialoguePhrases(mapDialoguePhrases(dialogue))
                .build();
    }

    private List<GetDialogueResponse.DialogueSpeakerResponse> mapSpeakers(Dialogue dialogue) {
        return Stream.ofNullable(dialogue.getSpeakers())
                .flatMap(Collection::stream)
                .map(dialogueSpeaker -> GetDialogueResponse.DialogueSpeakerResponse.builder()
                        .id(dialogueSpeaker.getId())
                        .name(phraseResponseMapper.mapToPhraseResponse(dialogueSpeaker.getNamePhrase(), dialogueSpeaker.getTranslations()))
                        .build())
                .toList();
    }

    private List<GetDialogueResponse.DialoguePhraseResponse> mapDialoguePhrases(Dialogue dialogue) {
        return Stream.ofNullable(dialogue.getDialoguePhrases())
                .flatMap(Collection::stream)
                .map(dialoguePhrase -> GetDialogueResponse.DialoguePhraseResponse.builder()
                        .speakerId(dialoguePhrase.getSpeaker().getId())
                        .phrase(phraseResponseMapper.mapToPhraseResponse(dialoguePhrase.getPhrase(), dialoguePhrase.getTranslations()))
                        .build())
                .toList();
    }

    public SearchDialoguesResponse toSearchDialoguesResponse(List<Dialogue> results) {
        List<DialogueSummaryResponse> dialogues = results.stream()
                .map(dialogue -> DialogueSummaryResponse.builder()
                        .id(dialogue.getId())
                        .title(phraseResponseMapper.mapToPhraseResponse(dialogue.getTitle(), dialogue.getTitleTranslations()))
                        .build()
                )
                .toList();

        return new SearchDialoguesResponse(dialogues);
    }
}
