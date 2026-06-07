package com.blbulyandavbulyan.larm.ai.tts;

import com.blbulyandavbulyan.larm.ai.SpeechResource;
import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PiperTextToSpeechRestService implements TextToSpeechService {
    private static final String CONTENT_TYPE = "audio/wav";
    private static final String FILE_EXTENSION = "wav";

    private final PiperConfigurationProperties piperConfigurationProperties;
    private final RestClient restClient;

    public PiperTextToSpeechRestService(PiperConfigurationProperties piperConfigurationProperties) {
        this.piperConfigurationProperties = piperConfigurationProperties;
        this.restClient = RestClient.builder()
                .baseUrl(piperConfigurationProperties.url())
                .build();
    }

    @Override
    @Timed(value = "tts.convert.piper.latency", description = "Time taken to synthesize text via Piper REST API")
    public SpeechResource convert(String text, String isoLanguageCode) {
        if (!"hy".equalsIgnoreCase(isoLanguageCode)) {
            throw new IllegalArgumentException("Unsupported language code: " + isoLanguageCode);
        }

        String singleLineText = text.replaceAll("\\s+", " ").trim();

        record PiperRequest(String text) {}

        final var requestBody = new PiperRequest(singleLineText);
        // 2. Perform the POST request to fetch raw WAV bytes natively
        byte[] wavBytes = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(byte[].class); // Tells Spring to cleanly map the stream straight into a memory buffer

        if (wavBytes == null || wavBytes.length == 0) {
            throw new RuntimeException("Piper web server returned an empty response body.");
        }

        // 3. Construct and return your immutable entity payload
        return SpeechResource.builder()
                .bytes(wavBytes)
                .contentType(CONTENT_TYPE)
                .modelName(piperConfigurationProperties.modelName())
                .voiceIdentifier(piperConfigurationProperties.voiceIdentifier())
                .fileExtension(FILE_EXTENSION)
                .build();
    }
}