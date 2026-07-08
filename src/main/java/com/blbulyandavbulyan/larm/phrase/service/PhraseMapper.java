package com.blbulyandavbulyan.larm.phrase.service;

import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhraseMapper {
    private final MediaMapper mediaMapper;

    public Phrase mapToPhrase(SavePhraseParameters resource) {
        Phrase phrase = Phrase.builder()
                .phrase(resource.phrase())
                .transcription(resource.transcription())
                .isoLanguageCode(resource.isoLanguageCode())
                .build();


        var mediaSet = resource.mediaResources().stream()
                .map(mediaMapper::toMedia)
                .map(m -> m.setPhrase(phrase))
                .collect(Collectors.toSet());
        phrase.setMediaSet(mediaSet);

        return phrase;
    }
}
