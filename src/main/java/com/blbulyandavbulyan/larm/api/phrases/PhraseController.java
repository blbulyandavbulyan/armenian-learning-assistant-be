package com.blbulyandavbulyan.larm.api.phrases;

import com.blbulyandavbulyan.larm.phrase.IPhraseService;
import com.blbulyandavbulyan.larm.phrase.PageParameters;
import com.blbulyandavbulyan.larm.phrase.PagedPhraseResource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/phrases")
@RequiredArgsConstructor
public class PhraseController {
    private final IPhraseService phraseService;
    private final PhraseMapper phraseMapper;


    @GetMapping
    public PhrasesResponse getPhrases(PageRequest pageRequest) {
        PagedPhraseResource phraseResource = phraseService.findAll(PageParameters.builder()
                .pageNumber(pageRequest.pageNumber())
                .pageSize(pageRequest.pageSize())
                .build());

        return phraseMapper.mapToResponse(phraseResource);
    }
}
