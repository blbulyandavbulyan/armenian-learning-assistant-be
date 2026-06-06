package com.blbulyandavbulyan.larm.ai.tts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.blbulyandavbulyan.larm.ai.SpeechResource;
import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PiperTextToSpeechService implements TextToSpeechService {

    private static final String CONTENT_TYPE = "audio/wav";
    private static final String FILE_EXTENSION = "wav";

    private final PiperConfigurationProperties piperConfigurationProperties;

    private Process process;
    private BufferedWriter processStdin;
    private InputStream processStdout;
    private BufferedReader processStderr;

    @PostConstruct
    public void init() {
        final String modelPath = piperConfigurationProperties.modelPath();
        Path path = Paths.get(modelPath);
        if (!Files.exists(path)) {
            throw new IllegalStateException("Piper model file not found at: " + path.toAbsolutePath());
        }

        String[] command = {
                piperConfigurationProperties.executablePath(),
                "--model", modelPath,
                "--debug",
                "--output-raw" // Instant unbuffered chunk flushing
        };

        try {
            this.process = new ProcessBuilder(command).start();
            this.processStdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            this.processStdout = process.getInputStream();
            this.processStderr = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize continuous Piper engine.", e);
        }
    }

    @Override
    public synchronized SpeechResource convert(String text, String iso2LanguageCode) {
        if (!"hy".equalsIgnoreCase(iso2LanguageCode)) {
            throw new IllegalArgumentException("Unsupported language code: " + iso2LanguageCode);
        }

        try {
            String singleLineText = text.replaceAll("\\s+", " ").trim() + "\n";

            // 1. Submit text phrase to the warm daemon
            processStdin.write(singleLineText);
            processStdin.flush();

            // 2. Safely read output data blocks
            ByteArrayOutputStream pcmOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];

            // Instead of racing available(), loop and read blocks blocking-style until
            // Piper signals on stderr that generation for this line is finished.
            while (process.isAlive()) {
                // Read whatever audio blocks are ready right now
                while (processStdout.available() > 0) {
                    int bytesRead = processStdout.read(buffer);
                    if (bytesRead > 0) {
                        pcmOutputStream.write(buffer, 0, bytesRead);
                    }
                }

                // Check stderr for Piper's operational completion signature
                if (processStderr.ready()) {
                    String logLine = processStderr.readLine();
                    // Piper outputs performance metrics text when a line finishes synthesizing
                    if (logLine != null && logLine.contains("DEBUG:piper.voice:text")) {
                        // Capture any final straggling trailing bytes remaining in the stream pipe buffer
                        while (processStdout.available() > 0) {
                            int bytesRead = processStdout.read(buffer);
                            if (bytesRead > 0) {
                                pcmOutputStream.write(buffer, 0, bytesRead);
                            }
                        }
                        break; // Generation complete!
                    }
                }

                Thread.onSpinWait();
            }

            byte[] rawPcmBytes = pcmOutputStream.toByteArray();
            if (rawPcmBytes.length == 0) {
                // TODO, it is FAST, but first request fails here for some reason, the second request seems like it passed
                throw new RuntimeException("Piper returned an empty audio block stream.");
            }

            // 3. Complete container assembly packaging
            byte[] fullyFormedWavBytes = addWavHeader(rawPcmBytes);

            return SpeechResource.builder()
                    .bytes(fullyFormedWavBytes)
                    .contentType(CONTENT_TYPE)
                    .modelName(piperConfigurationProperties.modelName())
                    .voiceIdentifier(piperConfigurationProperties.voiceIdentifier())
                    .fileExtension(FILE_EXTENSION)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to communicate with continuous Piper process.", e);
        }
    }

    private byte[] addWavHeader(byte[] pcmBytes) throws IOException {
        int totalAudioLen = pcmBytes.length;
        int totalDataLen = totalAudioLen + 36;
        int sampleRate = 22050;
        int channels = 1;
        long byteRate = sampleRate * channels * 2;

        byte[] header = new byte[44];
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;
        header[20] = 1; header[21] = 0;
        header[22] = (byte) channels; header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = 2; header[33] = 0;
        header[34] = 16; header[35] = 0;
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        ByteArrayOutputStream wavStream = new ByteArrayOutputStream();
        wavStream.write(header);
        wavStream.write(pcmBytes);
        return wavStream.toByteArray();
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (processStdin != null) processStdin.close();
            if (processStdout != null) processStdout.close();
            if (processStderr != null) processStderr.close();
        } catch (IOException ignored) {}

        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }
}