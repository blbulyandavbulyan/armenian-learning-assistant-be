package com.blbulyandavbulyan.larm.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.blbulyandavbulyan.larm.core.PhraseOrchestrator;
import com.blbulyandavbulyan.larm.phrase.dao.PhraseRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.genai.Client;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@EnableWireMock({
    @ConfigureWireMock(name = "piper-tts-service", baseUrlProperties = "piper.url")
})
public abstract class BaseIT {

    @ServiceConnection
    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("pgvector/pgvector:pg17");

    static {
        POSTGRES.start();
    }

    protected static final Path TEMP_DIR;

    static {
        try {
            TEMP_DIR = Files.createTempDirectory("larm-test-storage");
            // Optional: delete it when the JVM exits
            TEMP_DIR.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory for tests", e);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.local.folder-name", TEMP_DIR::toString);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PhraseRepository phraseRepository;

    @MockitoBean
    protected Client mockGeminiClient;

    @MockitoBean
    protected GoogleGenAiEmbeddingConnectionDetails embeddingConnectionDetails;

    @InjectWireMock("piper-tts-service")
    protected WireMockServer wireMockServer;

    @MockitoSpyBean
    protected PhraseOrchestrator phraseOrchestrator;
}
