package com.blbulyandavbulyan.larm.api.assets;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.blbulyandavbulyan.larm.asset.AssetResource;
import com.blbulyandavbulyan.larm.asset.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class AssetsController implements AssetsApi {
    private final AssetService assetService;

    @Override
    public ResponseEntity<Resource> getAsset(UUID mediaId) {
        AssetResource asset = assetService.findAssetByMediaId(mediaId);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .contentType(MediaType.parseMediaType(asset.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s\"".formatted(asset.fileName()))
                .body(asset.resource());
    }
}
