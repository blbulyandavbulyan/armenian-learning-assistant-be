package com.blbulyandavbulyan.larm.api.assets;

import java.util.UUID;

import com.blbulyandavbulyan.larm.asset.AssetResource;
import com.blbulyandavbulyan.larm.asset.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetsController {
    private final AssetService assetService;

    @GetMapping("/{mediaId}")
    public ResponseEntity<Resource> getAsset(@PathVariable UUID mediaId) {
        AssetResource asset = assetService.findAssetByMediaId(mediaId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"%s\"".formatted(asset.fileName()))
                .body(asset.resource());
    }
}
