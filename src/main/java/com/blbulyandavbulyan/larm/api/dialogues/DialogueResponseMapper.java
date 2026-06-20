package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.Collection;
import java.util.stream.Stream;

import com.blbulyandavbulyan.larm.api.phrases.PhraseResponseMapper;
import com.blbulyandavbulyan.larm.phrase.dao.Dialogue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DialogueResponseMapper {
    private final PhraseResponseMapper phraseResponseMapper;

    public GetDialogueResponse toResponse(Dialogue resource) {
        return GetDialogueResponse.builder()
                .id(resource.id())
                .title(phraseResponseMapper.mapToPhraseResponse(resource.title()))
                .speakers(Stream.ofNullable(resource.speakers())
                        .flatMap(Collection::stream)
                        .map(s -> GetDialogueResponse.DialogueSpeakerResponse.builder()
                                .speakerRefId(s.speakerRefId())
                                .name(phraseResponseMapper.mapToPhraseResponse(s.namePhrase()))
                                .build())
                        .toList())
                .dialoguePhrases(Stream.ofNullable(resource.dialoguePhrases())
                        .flatMap(Collection::stream)
                        .map(p -> GetDialogueResponse.DialoguePhraseResponse.builder()
                                .speakerRefId(p.speaker().speakerRefId())
                                .phrase(phraseResponseMapper.mapToPhraseResponse(p.phrase()))
                                .build())
                        .toList())
                .build();
    }
}
