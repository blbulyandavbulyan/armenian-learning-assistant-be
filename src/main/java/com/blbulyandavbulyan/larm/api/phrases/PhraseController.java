package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import com.blbulyandavbulyan.larm.core.PhraseOrchestrator;
import com.blbulyandavbulyan.larm.phrase.PageParameters;
import com.blbulyandavbulyan.larm.phrase.PagedPhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/phrases")
@RequiredArgsConstructor
@Validated
class PhraseController {
    private final PhraseStoringService phraseService;
    private final PhraseResponseMapper phraseResponseMapper;
    private final PhraseRequestMapper phraseRequestMapper;
    private final PhraseOrchestrator phraseOrchestrator;

    @GetMapping
    public PhrasesPagedResponse getPhrases(@Validated PageRequest pageRequest) {
        PagedPhraseResource phraseResource = phraseService.findAll(PageParameters.builder()
                .pageNumber(pageRequest.pageNumber())
                .pageSize(pageRequest.pageSize())
                .build());

        return phraseResponseMapper.mapToResponse(phraseResource);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePhrasesResponse savePhrases(@Validated @RequestBody CreatePhrasesRequest request) {
        // TODO most probably you have to handle validation errors here (like invalid iso2Language codes and so on)
        // TODO most probably you have to check if at least one phrase is a duplicate -> return error and don't save anything
        List<PhraseResource> savedPhrases = phraseOrchestrator.savePhrases(phraseRequestMapper.mapToBatchPhrasesParameters(request));
        return new CreatePhrasesResponse(phraseResponseMapper.mapToCreatePhrasesResponse(savedPhrases));
    }
}
