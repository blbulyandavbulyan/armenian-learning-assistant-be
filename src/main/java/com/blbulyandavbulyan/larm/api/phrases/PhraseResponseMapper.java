package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import com.blbulyandavbulyan.larm.phrase.PagedPhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.TranslationResource;
import org.springframework.stereotype.Component;

@Component
class PhraseResponseMapper {
    PhrasesPagedResponse mapToResponse(PagedPhraseResource resource) {
        return PhrasesPagedResponse.builder()
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

    public List<PhraseResponse> mapToCreatePhrasesResponse(List<PhraseResource> savedPhrases) {
        return savedPhrases.stream().map(this::mapToPhraseResponse).toList();
    }
}
