package com.blbulyandavbulyan.larm.api.phrases;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.blbulyandavbulyan.larm.dao.entities.ContextualTranslation;
import com.blbulyandavbulyan.larm.dao.entities.Media;
import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class PhraseResponseMapper {

    public PhraseResponse mapToPhraseResponse(Phrase phraseResource, Collection<? extends ContextualTranslation> translations) {
        return PhraseResponse.builder()
                .id(phraseResource.getId())
                .phrase(phraseResource.getPhrase())
                .transcription(phraseResource.getTranscription())
                .isoLanguageCode(phraseResource.getIsoLanguageCode())
                .translations(mapToTranslationResponses(translations))
                .assets(mapToAssets(phraseResource.getMediaSet()))
                .build();
    }

    private List<TranslationResponse> mapToTranslationResponses(Collection<? extends ContextualTranslation> translations) {
        return Stream.ofNullable(translations)
                .flatMap(Collection::stream)
                .map(this::mapToTranslationResponse)
                .toList();
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

    private TranslationResponse mapToTranslationResponse(ContextualTranslation translation) {
        return TranslationResponse.builder()
                .id(translation.getId())
                .isoLanguageCode(translation.getIsoLanguageCode())
                .translationText(translation.getTranslationText())
                .build();
    }

}
