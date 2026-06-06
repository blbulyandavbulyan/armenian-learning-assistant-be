package com.blbulyandavbulyan.larm.ai.tts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.blbulyandavbulyan.larm.ai.SpeechResource;
import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PiperTextToSpeechService implements TextToSpeechService {

    // Define the local path to your downloaded model
    private static final String CONTENT_TYPE = "audio/wav";
    private static final String FILE_EXTENSION = "wav";

    private final PiperConfigurationProperties piperConfigurationProperties;

    @Override
    public SpeechResource convert(String text, String iso2LanguageCode) {
        // Enforce Armenian constraint for this specific model configuration
        if (!"hy".equalsIgnoreCase(iso2LanguageCode)) {
            throw new IllegalArgumentException("Unsupported language code: %s. This service currently only supports 'hy' (Armenian)."
                    .formatted(iso2LanguageCode));
        }

        // Validate that the model exists where we expect it to
        final String modelPath = piperConfigurationProperties.modelPath();
        Path path = Paths.get(modelPath);
        if (!Files.exists(path)) {
            throw new IllegalStateException("Piper model file not found at: " + path.toAbsolutePath());
        }

        // Construct the process execution command array directly
        // This mirrors our working python subprocess exactly
        String[] command = {
                piperConfigurationProperties.executablePath(),
                "--model", modelPath,
                "--output_file", "-"
        };

        try {
            final Process process = new ProcessBuilder(command).start();

            // 1. Pipe text INTO Piper's standard input using pure UTF-8 bytes
            try (OutputStream os = process.getOutputStream()) {
                os.write(text.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 2. Read the resulting audio bytes OUT of Piper's standard output
            final byte[] audioBytes;
            try (InputStream is = process.getInputStream()) {
                audioBytes = is.readAllBytes();
            }

            // 3. Catch any runtime errors thrown by the Piper binary
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                try (InputStream errorStream = process.getErrorStream()) {
                    String errorLog = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    throw new RuntimeException("Piper TTS generation failed with exit code " + exitCode + ". Error: " + errorLog);
                }
            }

            // 4. Return your beautiful, populated record mapping
            return SpeechResource.builder()
                    .bytes(audioBytes)
                    .contentType(CONTENT_TYPE)
                    .modelName(piperConfigurationProperties.modelName())
                    .voiceIdentifier(piperConfigurationProperties.voiceIdentifier())
                    .fileExtension(FILE_EXTENSION)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to communicate with Piper binary. Is piper installed in the system PATH?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("TTS generation process was interrupted.", e);
        }
    }
}