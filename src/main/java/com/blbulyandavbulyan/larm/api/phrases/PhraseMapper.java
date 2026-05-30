package com.blbulyandavbulyan.larm.api.phrases;

import com.blbulyandavbulyan.larm.phrase.PagedPhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.TranslationResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PhraseMapper {
    PhrasesResponse mapToResponse(PagedPhraseResource resource) {
        return PhrasesResponse.builder()
                .page(mapToPageResponse(resource.page()))
                .phrases(mapToPhrases(resource.phrases()))
                .build();
    }

    private List<PhraseResponse> mapToPhrases(List<PhraseResource> phrases) {
        return phrases.stream().map(this::mapToPhraseResponse).toList();
    }

    private PhraseResponse mapToPhraseResponse(PhraseResource phraseResource) {
        return PhraseResponse.builder()
                .id(phraseResource.id())
                .phrase(phraseResource.phrase())
                .transcription(phraseResource.transcription())
                .iso2LanguageCode(phraseResource.iso2LanguageCode())
                .translations(phraseResource.translations().stream().map(this::mapToTranslationResponse).toList())
                .build();
    }

    private TranslationResponse mapToTranslationResponse(TranslationResource translationResource) {
        return TranslationResponse.builder()
                .id(translationResource.id())
                .translationText(translationResource.translationText())
                .iso2LanguageCode(translationResource.iso2LanguageCode())
                .build();
    }

    private PageResponse mapToPageResponse(PagedPhraseResource.Page page) {
        return PageResponse.builder()
                .totalPages(page.totalPages())
                .pageSize(page.pageSize())
                .pageNumber(page.pageNumber())
                .build();
    }
}
