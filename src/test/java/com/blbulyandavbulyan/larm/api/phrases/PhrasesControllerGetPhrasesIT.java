package com.blbulyandavbulyan.larm.api.phrases;

import com.blbulyandavbulyan.larm.TestUtils;
import com.blbulyandavbulyan.larm.api.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PhrasesControllerGetPhrasesIT extends BaseIT {

    @Test
    @Sql(scripts = "/sql-test-scripts/insert-phrases.sql")
    void getPhrases_firstPage() throws Exception {
        String expectedResponse = TestUtils.readResourceToString("responses/get-phrases-success.json");

        mockMvc.perform(get(RequestMapping.GET_PHRASES)
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    @Test
    @Sql(scripts = "/sql-test-scripts/insert-phrases.sql")
    void getPhrases_secondPage() throws Exception {
        String expectedResponse = TestUtils.readResourceToString("responses/get-phrases-empty-page.json");

        mockMvc.perform(get(RequestMapping.GET_PHRASES)
                        .param("pageNumber", "2")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    @Test
    void getPhrases_invalidPageNumber() throws Exception {
        mockMvc.perform(get(RequestMapping.GET_PHRASES)
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.pageNumber").exists());
    }

    @Test
    void getPhrases_pageSizeTooSmall() throws Exception {
        mockMvc.perform(get(RequestMapping.GET_PHRASES)
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.pageSize").exists());
    }

    @Test
    void getPhrases_pageSizeTooLarge() throws Exception {
        mockMvc.perform(get(RequestMapping.GET_PHRASES)
                        .param("pageNumber", "1")
                        .param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.pageSize").exists());
    }
}
