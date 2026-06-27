package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import com.blbulyandavbulyan.larm.core.PhraseOrchestrator;
import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import com.blbulyandavbulyan.larm.phrase.PageParameters;
import com.blbulyandavbulyan.larm.phrase.PagedPhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class PhraseController implements PhrasesApi {
    private final PhraseStoringService phraseService;
    private final PhraseResponseMapper phraseResponseMapper;
    private final PhraseRequestMapper phraseRequestMapper;
    private final PhraseOrchestrator phraseOrchestrator;

    @Override
    public PhrasesPagedResponse getPhrases(PageRequest pageRequest) {
        PagedPhraseResource phraseResource = phraseService.findAll(PageParameters.builder()
                .pageNumber(pageRequest.pageNumber())
                .pageSize(pageRequest.pageSize())
                .build());

        return phraseResponseMapper.mapToResponse(phraseResource);
    }

    @Override
    public CreatePhrasesResponse savePhrases(CreatePhrasesRequest request) {
        // TODO most probably you have to check if at least one phrase is a duplicate -> return error and don't save anything
        List<Phrase> savedPhrases = phraseOrchestrator.savePhrases(phraseRequestMapper.mapToBatchPhrasesParameters(request));
        return new CreatePhrasesResponse(phraseResponseMapper.mapToCreatePhrasesResponse(savedPhrases));
    }
}
