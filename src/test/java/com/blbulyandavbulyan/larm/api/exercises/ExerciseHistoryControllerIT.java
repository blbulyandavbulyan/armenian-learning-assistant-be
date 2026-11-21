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
@Sql({Fixtures.DIALOGUE, Fixtures.TEST_USER, Fixtures.ACTIVE_LEARNING_ITEM, Fixtures.IN_PROGRESS_SESSION,
        Fixtures.COMPLETED_SESSION})
@WithMockDatabaseUser
class ExerciseHistoryControllerIT extends BaseIT {

    private static final String HISTORY = "/exercises/history";

    private static final String COMPLETED = "/expected-responses/srs/exercises/history-completed-session.json";

    private static final String EMPTY_PAGE = "/expected-responses/srs/exercises/empty-page.json";

    @Test
    void getHistory() throws Exception {
        // the in-progress session is not part of the history
        mockMvc.perform(get(HISTORY))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(COMPLETED), JsonCompareMode.STRICT));
    }

    @Test
    void getHistory_filteredByCompletionTime() throws Exception {
        mockMvc.perform(get(HISTORY).param("from", "2026-08-14T09:10:01Z"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(EMPTY_PAGE), JsonCompareMode.STRICT));
    }

    @Test
    void getHistory_filteredByExerciseTypeAndContentItemType() throws Exception {
        mockMvc.perform(get(HISTORY).param("exerciseType", "TYPING").param("contentItemType", "DIALOGUE")
                        .param("to", "2026-08-14T09:10:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString(COMPLETED), JsonCompareMode.STRICT));
    }
}
