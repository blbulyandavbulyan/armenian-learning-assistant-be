package com.blbulyandavbulyan.larm.ai.tts;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "piper")
public record PiperConfigurationProperties(
        String url,
        String modelName,
        String voiceIdentifier) {
}
