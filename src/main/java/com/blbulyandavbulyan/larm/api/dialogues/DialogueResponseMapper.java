package com.blbulyandavbulyan.larm.api.dialogues;

import com.blbulyandavbulyan.larm.api.phrases.PhraseResponseMapper;
import com.blbulyandavbulyan.larm.dialogue.FullDialogueResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DialogueResponseMapper {
    private final PhraseResponseMapper phraseResponseMapper;

    public GetDialogueResponse toResponse(FullDialogueResource resource) {
        return GetDialogueResponse.builder()
                .id(resource.id())
                .title(phraseResponseMapper.mapToPhraseResponse(resource.title()))
                .speakers(resource.speakers().stream()
                        .map(s -> GetDialogueResponse.DialogueSpeakerResponse.builder()
                                .speakerRefId(s.speakerRefId())
                                .name(phraseResponseMapper.mapToPhraseResponse(s.name()))
                                .build())
                        .toList())
                .dialoguePhrases(resource.dialoguePhrases().stream()
                        .map(p -> GetDialogueResponse.DialoguePhraseResponse.builder()
                                .speakerRefId(p.speakerRefId())
                                .phrase(phraseResponseMapper.mapToPhraseResponse(p.phrase()))
                                .build())
                        .toList())
                .build();
    }
}
