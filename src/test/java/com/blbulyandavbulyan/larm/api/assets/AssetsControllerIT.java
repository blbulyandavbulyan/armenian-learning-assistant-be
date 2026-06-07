package com.blbulyandavbulyan.larm.api.assets;

import java.nio.file.Files;

import com.blbulyandavbulyan.larm.api.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssetsControllerIT extends BaseIT {

    interface RequestMapping {
        String GET_ASSET = "/assets/{mediaId}";
    }

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
