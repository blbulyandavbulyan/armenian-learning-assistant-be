package com.blbulyandavbulyan.larm.api.phrases;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.blbulyandavbulyan.larm.phrase.PagedPhraseResource;
import com.blbulyandavbulyan.larm.phrase.dao.Media;
import com.blbulyandavbulyan.larm.phrase.dao.Phrase;
import com.blbulyandavbulyan.larm.phrase.dao.Translation;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class PhraseResponseMapper {
    PhrasesPagedResponse mapToResponse(PagedPhraseResource resource) {
        return PhrasesPagedResponse.builder()
                .page(mapToPageResponse(resource.page()))
                .phrases(mapToPhrases(resource.phrases()))
                .build();
    }

    private List<PhraseResponse> mapToPhrases(List<Phrase> phrases) {
        return phrases.stream().map(this::mapToPhraseResponse).toList();
    }

    public PhraseResponse mapToPhraseResponse(Phrase phraseResource) {
        return PhraseResponse.builder()
                .id(phraseResource.getId())
                .phrase(phraseResource.getPhrase())
                .transcription(phraseResource.getTranscription())
                .isoLanguageCode(phraseResource.getIsoLanguageCode())
                .translations(phraseResource.getTranslations().stream().map(this::mapToTranslationResponse).toList())
                .assets(mapToAssets(phraseResource.getMediaSet()))
                .build();
    }

    private List<AssetResponse> mapToAssets(Set<Media> mediaSet) {
        return Stream.ofNullable(mediaSet)
                .flatMap(Collection::stream)
                .map(this::mapMediaToAssetResponse)
                .toList();
    }

    private AssetResponse mapMediaToAssetResponse(Media media) {
        return AssetResponse.builder()
                .contentType(media.getContentType())
                .url(generateUrl(media.getId()))
                .build();
    }

    private String generateUrl(UUID storageKey) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/assets/{mediaId}")
                .buildAndExpand(storageKey)
                .toUriString();
    }

    private TranslationResponse mapToTranslationResponse(Translation translationResource) {
        return TranslationResponse.builder()
                .id(translationResource.getId())
                .translationText(translationResource.getTranslationText())
                .isoLanguageCode(translationResource.getIsoLanguageCode())
                .build();
    }

    private PageResponse mapToPageResponse(PagedPhraseResource.Page page) {
        return PageResponse.builder()
                .totalPages(page.totalPages())
                .pageSize(page.pageSize())
                .pageNumber(page.pageNumber())
                .build();
    }

    public List<PhraseResponse> mapToCreatePhrasesResponse(List<Phrase> savedPhrases) {
        return savedPhrases.stream().map(this::mapToPhraseResponse).toList();
    }
}
