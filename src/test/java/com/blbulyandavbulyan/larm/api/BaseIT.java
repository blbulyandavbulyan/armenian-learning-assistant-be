package com.blbulyandavbulyan.larm.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.blbulyandavbulyan.larm.ai.tts.PiperWireMock;
import com.blbulyandavbulyan.larm.dialogue.util.DialogueRecordAssertHelper;
import com.blbulyandavbulyan.larm.phrase.util.PhraseRecordAssertHelper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.genai.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableWireMock({
    @ConfigureWireMock(name = "piper-tts-service", baseUrlProperties = "piper.url")
})
@Import({PhraseRecordAssertHelper.class, DialogueRecordAssertHelper.class})
@Sql(scripts = "/sql-test-scripts/drop-all-data-after-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
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
    protected PhraseRecordAssertHelper phraseRecordAssertHelper;

    @Autowired
    protected DialogueRecordAssertHelper dialogueRecordAssertHelper;

    @MockitoBean
    protected Client mockGeminiClient;

    @MockitoBean
    protected GoogleGenAiEmbeddingConnectionDetails embeddingConnectionDetails;

    @MockitoBean
    protected EmbeddingModel embeddingModel;

    @InjectWireMock("piper-tts-service")
    protected WireMockServer wireMockServer;

    protected PiperWireMock piperWireMock;
    private static final Logger LOG = LoggerFactory.getLogger(BaseIT.class);

    @BeforeEach
    protected void beforeEach(TestInfo testInfo) {
        LOG.info("Starting test: {}", testInfo);
        this.piperWireMock = new PiperWireMock(wireMockServer);
    }

    @AfterEach
    protected void afterEach(TestInfo testInfo) {
        LOG.info("Finished test: {}", testInfo);
    }
}
