package com.blbulyandavbulyan.larm.api.exercises;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.security.WithMockDatabaseUser;
import com.blbulyandavbulyan.larm.srs.SrsTestData.Fixtures;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql({Fixtures.DIALOGUE, Fixtures.TEST_USER, Fixtures.OTHER_USER_LEARNING_ITEM})
@WithMockDatabaseUser
class ExerciseQueueControllerIT extends BaseIT {

    private static final String QUEUE = "/exercises/queue";

    private static final String DUE_CARD = "/expected-responses/srs/exercises/queue-due-card.json";

    private static final String EMPTY_PAGE = "/expected-responses/srs/exercises/empty-page.json";

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void getQueue() throws Exception {
        mockMvc.perform(get(QUEUE))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(DUE_CARD), JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void getQueue_filteredByExerciseType() throws Exception {
        mockMvc.perform(get(QUEUE).param("exerciseType", "TYPING"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(DUE_CARD), JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void getQueue_filteredByContentItemType() throws Exception {
        mockMvc.perform(get(QUEUE).param("contentItemType", "DIALOGUE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(DUE_CARD), JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.EXCLUDED_LEARNING_ITEM)
    void getQueue_whenLearningItemExcluded() throws Exception {
        mockMvc.perform(get(QUEUE))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(EMPTY_PAGE), JsonCompareMode.STRICT));
    }

    @Test
    void getQueue_withInvalidPageSize() throws Exception {
        mockMvc.perform(get(QUEUE).param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
