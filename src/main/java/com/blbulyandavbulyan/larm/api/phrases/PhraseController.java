package com.blbulyandavbulyan.larm.api.phrases;

import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesResult;
import com.blbulyandavbulyan.larm.phrase.IPhraseStoringService;
import com.blbulyandavbulyan.larm.phrase.PageParameters;
import com.blbulyandavbulyan.larm.phrase.PagedPhraseResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/phrases")
@RequiredArgsConstructor
@Validated
class PhraseController {
    private final IPhraseStoringService phraseService;
    private final PhraseResponseMapper phraseResponseMapper;
    private final PhraseRequestMapper phraseRequestMapper;

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
    public CreatePhrasesResponse savePhrases(@RequestBody CreatePhrasesRequest request) {
        BatchSavePhrasesResult batchSavePhrasesResult = phraseService.batchSavePhrases(phraseRequestMapper.mapToBatchPhrasesParameters(request));

        return new CreatePhrasesResponse(phraseResponseMapper.mapToCreatePhrasesResponse(batchSavePhrasesResult));
    }
}
