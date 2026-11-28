package com.blbulyandavbulyan.larm;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;

import com.blbulyandavbulyan.larm.ai.tts.PiperWireMock;
import com.blbulyandavbulyan.larm.dialogue.util.DialogueRecordAssertHelper;
import com.blbulyandavbulyan.larm.genseq.SequenceDiagramExtension;
import com.blbulyandavbulyan.larm.genseq.TracingTestConfiguration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.genai.Client;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@Slf4j
@SpringBootTest
@ExtendWith(SequenceDiagramExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableWireMock({
    @ConfigureWireMock(name = "piper-tts-service", baseUrlProperties = "app.piper.url")
})
@Import({DialogueRecordAssertHelper.class, TracingTestConfiguration.class})
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
        registry.add("app.storage.local.folder-name", TEMP_DIR::toString);
    }

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected DialogueRecordAssertHelper dialogueRecordAssertHelper;

    @MockitoBean
    protected Client mockGeminiClient;

    @MockitoBean
    protected GoogleGenAiEmbeddingConnectionDetails embeddingConnectionDetails;

    // Named so that TracingTestConfiguration's observing @Primary EmbeddingModel wraps it; real Spring AI embedding
    // calls are observed by Spring AI itself.
    @MockitoBean(name = TracingTestConfiguration.MOCK_EMBEDDING_MODEL)
    protected EmbeddingModel embeddingModel;

    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @InjectWireMock("piper-tts-service")
    protected WireMockServer wireMockServer;

    protected PiperWireMock piperWireMock;

    @BeforeEach
    protected void beforeEach(TestInfo testInfo) {
        log.info("Starting test: {}", testInfo);
        this.piperWireMock = new PiperWireMock(wireMockServer);
        TracingTestConfiguration.reset(applicationContext);
    }

    @Autowired(required = false)
    protected CacheManager cacheManager;

    @AfterEach
    protected void afterEach(TestInfo testInfo) {
        log.info("Finished test: {}", testInfo);
        clearCache();
        clearDb();
        cleanTempDirectory();
    }

    private void clearCache() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
    }

    private void clearDb() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("/sql-test-scripts/drop-all-data-after-test.sql"));
        populator.execute(dataSource);
        cleanTempDirectory();
    }

    private void cleanTempDirectory() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(TEMP_DIR)) {
            for (Path entry : stream) {
                FileSystemUtils.deleteRecursively(entry);
            }
        } catch (IOException e) {
            log.warn("Failed to clean up TEMP_DIR", e);
        }
    }
}
