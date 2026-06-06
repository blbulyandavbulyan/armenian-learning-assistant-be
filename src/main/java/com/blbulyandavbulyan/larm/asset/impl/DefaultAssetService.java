package com.blbulyandavbulyan.larm.asset.impl;

import java.util.UUID;

import com.blbulyandavbulyan.larm.asset.AssetNotFoundException;
import com.blbulyandavbulyan.larm.asset.AssetResource;
import com.blbulyandavbulyan.larm.asset.AssetService;
import com.blbulyandavbulyan.larm.phrase.MediaResource;
import com.blbulyandavbulyan.larm.phrase.PhraseMediaService;
import com.blbulyandavbulyan.larm.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class DefaultAssetService implements AssetService {
    private final ObjectStorageService storageService;
    private final PhraseMediaService phraseMediaService;

    @Override
    public AssetResource findAssetByMediaId(UUID mediaId) {
        MediaResource media = phraseMediaService.findById(mediaId)
                .orElseThrow(() -> new AssetNotFoundException("Asset not found in database: " + mediaId));

        Resource resource = storageService.loadAsResource(media.storageKey());

        return AssetResource.builder()
                .contentType(media.contentType())
                .fileName(media.storageKey())
                .resource(resource)
                .build();
    }
}
