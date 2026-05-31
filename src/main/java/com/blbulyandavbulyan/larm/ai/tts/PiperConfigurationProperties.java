package com.blbulyandavbulyan.larm.ai.tts;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "piper")
public record PiperConfigurationProperties(
        String executablePath,
        String modelPath,
        String modelName,
        String voiceIdentifier) {
}
