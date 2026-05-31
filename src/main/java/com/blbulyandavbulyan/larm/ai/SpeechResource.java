package com.blbulyandavbulyan.larm.ai;

import lombok.Builder;

@Builder
public record SpeechResource(
        byte[] bytes,
        String contentType,
        String modelName,
        String voiceIdentifier,
        String fileExtension) {

    public int sizeInBytes() {
        return bytes.length;
    }
}
