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
                .title(phraseResponseMapper.mapToPhraseResponse(dialogue.getTitle()))
                .speakers(mapSpeakers(dialogue))
                .dialoguePhrases(mapDialoguePhrases(dialogue))
                .build();
    }

    private List<GetDialogueResponse.DialogueSpeakerResponse> mapSpeakers(Dialogue dialogue) {
        return Stream.ofNullable(dialogue.getSpeakers())
                .flatMap(Collection::stream)
                .map(dialogueSpeaker -> GetDialogueResponse.DialogueSpeakerResponse.builder()
                        .speakerRefId(dialogueSpeaker.getSpeakerRefId())
                        .name(phraseResponseMapper.mapToPhraseResponse(dialogueSpeaker.getNamePhrase()))
                        .build())
                .toList();
    }

    private List<GetDialogueResponse.DialoguePhraseResponse> mapDialoguePhrases(Dialogue dialogue) {
        return Stream.ofNullable(dialogue.getDialoguePhrases())
                .flatMap(Collection::stream)
                .map(dialoguePhrase -> GetDialogueResponse.DialoguePhraseResponse.builder()
                        .speakerRefId(dialoguePhrase.getSpeaker().getSpeakerRefId())
                        .phrase(phraseResponseMapper.mapToPhraseResponse(dialoguePhrase.getPhrase()))
                        .build())
                .toList();
    }
}
