package com.blbulyandavbulyan.larm.api.assets;

import java.nio.file.Files;

import com.blbulyandavbulyan.larm.BaseIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssetsControllerIT extends BaseIT {

    interface RequestMapping {
        String GET_ASSET = "/assets/{mediaId}";
    }

    @Test
    @Transactional
    @Sql(scripts = "/sql-test-scripts/insert-phrases-asset-controller.sql")
    void getAsset() throws Exception {
        Files.write(TEMP_DIR.resolve("some-existing-file.wav"), "dummy audio content".getBytes());

        mockMvc.perform(get(RequestMapping.GET_ASSET, "b352560f-58f9-4c3e-8f37-46be09978511"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "audio/wav"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"some-existing-file.wav\""))
                .andExpect(header().string("Cache-Control", "max-age=604800, public"))
                .andExpect(content().bytes("dummy audio content".getBytes()));
    }

    @Test
    void getAsset_forNotFoundAsset() throws Exception {
        // Create a file manually that is NOT in the database
        Files.write(TEMP_DIR.resolve("b352560f-58f9-4c3e-8f37-46be09978511"), "secret".getBytes());

        mockMvc.perform(get(RequestMapping.GET_ASSET, "b352560f-58f9-4c3e-8f37-46be09978511"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = "/sql-test-scripts/insert-phrases-asset-controller.sql")
    void getAsset_forExistingRecordInDbAndNotExistingFileOnDisk() throws Exception {
        assertThat(TEMP_DIR.resolve("some-existing-file.wav")).doesNotExist();
        mockMvc.perform(get(RequestMapping.GET_ASSET, "b352560f-58f9-4c3e-8f37-46be09978511"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("File not found: some-existing-file.wav"));
    }

    @DisplayName(
            """
            getAsset_forExistingRecordPathTraversalAttempt() ->
                Tests 'path traversal', it is not possible currently to trigger it from API,
                because there is no way to write such data in the database through API,
                such test is here purely for 'coverage' purposes.
            """)
    @Test
    @Sql(scripts = "/sql-test-scripts/insert-phrases-asset-controller-path-traversal.sql")
    void getAsset_forExistingRecordPathTraversalAttempt() throws Exception {
        assertThat(TEMP_DIR.resolve("some-existing-file.wav")).doesNotExist();
        mockMvc.perform(get(RequestMapping.GET_ASSET, "b352560f-58f9-4c3e-8f37-46be09978511"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Invalid storage key (path traversal attempt): ../some-existing-file.wav"));
    }

}
