package com.blbulyandavbulyan.larm.api.assets;

import java.nio.file.Files;
import java.nio.file.Path;

import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import com.google.genai.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")

class AssetsControllerIT {

    interface RequestMapping {
        String GET_ASSET = "/assets/{mediaId}";
    }

    @ServiceConnection
    @SuppressWarnings("unused")
    static PostgreSQLContainer postgres = new PostgreSQLContainer("pgvector/pgvector:pg17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.local.folder-name", () -> tempDir.toString());
    }

    @TempDir
    static Path tempDir;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TextToSpeechService textToSpeechService;

    @MockitoBean
    private Client mockGeminiClient;

    @MockitoBean
    private GoogleGenAiEmbeddingConnectionDetails embeddingConnectionDetails;

    @Test
    @Sql(scripts = "/sql-test-scripts/insert-phrases-asset-controller.sql")
    void getAsset() throws Exception {
        Files.write(tempDir.resolve("some-existing-file.wav"), "dummy audio content".getBytes());

        mockMvc.perform(get(RequestMapping.GET_ASSET, "b352560f-58f9-4c3e-8f37-46be09978511"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "audio/wav"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"some-existing-file.wav\""))
                .andExpect(content().bytes("dummy audio content".getBytes()));
    }

    @Test
    void getAsset_forNotFoundAsset() throws Exception {
        // Create a file manually that is NOT in the database
        Files.write(tempDir.resolve("b352560f-58f9-4c3e-8f37-46be09978511"), "secret".getBytes());

        mockMvc.perform(get(RequestMapping.GET_ASSET, "b352560f-58f9-4c3e-8f37-46be09978511"))
                .andExpect(status().isNotFound());
    }
}
