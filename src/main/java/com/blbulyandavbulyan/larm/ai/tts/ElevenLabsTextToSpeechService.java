package com.blbulyandavbulyan.larm.ai.tts;

import com.blbulyandavbulyan.larm.ai.SpeechResource;
import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.tts.*;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.ai.elevenlabs.api.ElevenLabsApi;
import org.springframework.ai.model.elevenlabs.autoconfigure.ElevenLabsSpeechProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ElevenLabsTextToSpeechService implements TextToSpeechService {
    private final TextToSpeechModel textToSpeechModel;
    private final ElevenLabsSpeechProperties elevenLabsProperties;

    @Override
    public SpeechResource convert(String text, String iso2LanguageCode) {
        TextToSpeechResponse textToSpeechResponse = textToSpeechModel.call(new TextToSpeechPrompt(text,
                ElevenLabsTextToSpeechOptions.builder()
                        .outputFormat(ElevenLabsApi.OutputFormat.MP3_44100_128.getValue())
                        .languageCode(iso2LanguageCode)
                        .build()));
        byte[] output = textToSpeechResponse.getResult().getOutput();

        return SpeechResource.builder()
                .contentType("audio/mpeg")
                .fileExtension("mp3")
                .modelName(elevenLabsProperties.getModelId())
                .voiceIdentifier(elevenLabsProperties.getVoiceId())
                .bytes(output)
                .build();
    }
}
