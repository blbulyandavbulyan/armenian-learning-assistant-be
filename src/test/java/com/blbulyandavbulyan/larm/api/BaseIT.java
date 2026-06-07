package com.blbulyandavbulyan.larm.api;

import java.nio.file.Path;

import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import com.google.genai.Client;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
public abstract class BaseIT {

    @ServiceConnection
    @SuppressWarnings("unused")
    @Container
    protected static PostgreSQLContainer postgres = new PostgreSQLContainer("pgvector/pgvector:pg17");

    @TempDir
    protected static Path tempDir;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.local.folder-name", () -> tempDir.toString());
    }

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected TextToSpeechService textToSpeechService;

    @MockitoBean
    protected Client mockGeminiClient;

    @MockitoBean
    protected GoogleGenAiEmbeddingConnectionDetails embeddingConnectionDetails;
}
