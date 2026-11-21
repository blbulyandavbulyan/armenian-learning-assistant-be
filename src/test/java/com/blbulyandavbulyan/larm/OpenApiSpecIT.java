package com.blbulyandavbulyan.larm;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.blbulyandavbulyan.larm.security.WithMockDatabaseUser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regenerates the committed {@code openapi.yaml} at the repository root from the running application context.
 *
 * <p>Run the build, commit the file if it changed. CI fails when the committed file differs from what the code
 * produces, so the spec on every branch always matches that branch's code (which is what makes it diffable).
 */
@WithMockDatabaseUser
class OpenApiSpecIT extends BaseIT {

    private static final Path SPEC_FILE = Path.of("openapi.yaml");

    @Test
    void writeOpenApiSpec() throws Exception {
        String spec = mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(spec).startsWith("openapi:").contains("paths:");

        String normalized = spec.endsWith("\n") ? spec : spec + "\n";
        Files.writeString(SPEC_FILE, normalized, StandardCharsets.UTF_8);
    }
}
