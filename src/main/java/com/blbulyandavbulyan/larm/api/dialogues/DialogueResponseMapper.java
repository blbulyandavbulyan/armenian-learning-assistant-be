package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.Collection;
import java.util.stream.Stream;

import com.blbulyandavbulyan.larm.api.phrases.PhraseResponseMapper;
import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DialogueResponseMapper {
    private final PhraseResponseMapper phraseResponseMapper;

    public GetDialogueResponse toResponse(Dialogue resource) {
        return GetDialogueResponse.builder()
                .id(resource.getId())
                .title(phraseResponseMapper.mapToPhraseResponse(resource.getTitle()))
                .speakers(Stream.ofNullable(resource.getSpeakers())
                        .flatMap(Collection::stream)
                        .map(s -> GetDialogueResponse.DialogueSpeakerResponse.builder()
                                .speakerRefId(s.getSpeakerRefId())
                                .name(phraseResponseMapper.mapToPhraseResponse(s.getNamePhrase()))
                                .build())
                        .toList())
                .dialoguePhrases(Stream.ofNullable(resource.getDialoguePhrases())
                        .flatMap(Collection::stream)
                        .map(p -> GetDialogueResponse.DialoguePhraseResponse.builder()
                                .speakerRefId(p.getSpeaker().getSpeakerRefId())
                                .phrase(phraseResponseMapper.mapToPhraseResponse(p.getPhrase()))
                                .build())
                        .toList())
                .build();
    }
}
