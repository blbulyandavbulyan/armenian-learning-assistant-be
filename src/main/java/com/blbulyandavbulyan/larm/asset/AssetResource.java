package com.blbulyandavbulyan.larm.asset;

import lombok.Builder;
import org.springframework.core.io.Resource;

@Builder
public record AssetResource(
        String contentType,
        String fileName,
        Resource resource) {
}
