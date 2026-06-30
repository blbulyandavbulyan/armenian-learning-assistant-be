package com.blbulyandavbulyan.larm.api.phrases;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.blbulyandavbulyan.larm.dao.entities.Media;
import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import com.blbulyandavbulyan.larm.dao.entities.Translation;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class PhraseResponseMapper {

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

}
