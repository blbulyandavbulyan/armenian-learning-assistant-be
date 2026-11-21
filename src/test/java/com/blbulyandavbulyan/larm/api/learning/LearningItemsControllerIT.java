package com.blbulyandavbulyan.larm.api.learning;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import com.blbulyandavbulyan.larm.dao.repository.LearningItemRepository;
import com.blbulyandavbulyan.larm.security.WithMockDatabaseUser;
import com.blbulyandavbulyan.larm.srs.SrsTestData.Fixtures;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.json.JsonCompareMode;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.CARD_A_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.CARD_B_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.DIALOGUE_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.LEARNING_ITEM_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.OTHER_USER_LEARNING_ITEM_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.SPEAKER_A_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.SPEAKER_B_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.TEST_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql({Fixtures.DIALOGUE, Fixtures.TEST_USER})
@WithMockDatabaseUser
class LearningItemsControllerIT extends BaseIT {

    interface RequestMapping {
        String LEARNING_ITEMS = "/learning-items";
        String LEARNING_ITEM = "/learning-items/{learningItemId}";
        String LEARNING_ITEM_CARDS = "/learning-items/{learningItemId}/cards";
    }

    private static final String ENROL_DIALOGUE_REQUEST =
            """
            {"contentItemId": "%s", "contentItemType": "DIALOGUE"}
            """.formatted(DIALOGUE_ID);

    @Autowired
    private LearningItemRepository learningItemRepository;

    @Autowired
    private LearningCardRepository learningCardRepository;

    @Test
    void enrol() throws Exception {
        String response = mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ENROL_DIALOGUE_REQUEST))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID learningItemId = UUID.fromString(JsonPath.read(response, "$.id"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/learning-items/enrolled-new.json").formatted(learningItemId), response);

        assertThat(learningItemRepository.findById(learningItemId)).hasValueSatisfying(item -> {
            assertThat(item.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(item.getContentItemId()).isEqualTo(DIALOGUE_ID);
            assertThat(item.getEnrolledAt()).isEqualTo(NOW);
            assertThat(item.getExcludedAt()).isNull();
        });
        assertThat(learningCardRepository.findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(learningItemId))
                .extracting(LearningCard::getExerciseType, LearningCard::getVariantId, LearningCard::getNextReviewAt,
                        LearningCard::getIntervalDays, LearningCard::getEaseFactor, LearningCard::getRepetitionCount,
                        LearningCard::getCreatedAt, LearningCard::getUpdatedAt)
                .containsExactly(
                        tuple(ExerciseType.TYPING, SPEAKER_A_ID, NOW, 0, 2.5, 0, NOW, NOW),
                        tuple(ExerciseType.TYPING, SPEAKER_B_ID, NOW, 0, 2.5, 0, NOW, NOW));
    }

    @Test
    @Sql(Fixtures.EXCLUDED_LEARNING_ITEM)
    void enrol_whenExcluded() throws Exception {
        mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ENROL_DIALOGUE_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/learning-items/active-learning-item.json"),
                        JsonCompareMode.STRICT));

        assertThat(learningItemRepository.findById(LEARNING_ITEM_ID)).hasValueSatisfying(item ->
                assertThat(item.getExcludedAt()).isNull());
        assertThatCardProgressIsUnchanged();
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void enrol_whenAlreadyActive() throws Exception {
        mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ENROL_DIALOGUE_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/learning-items/active-learning-item.json"),
                        JsonCompareMode.STRICT));

        assertThat(learningItemRepository.count()).isEqualTo(1);
        assertThatCardProgressIsUnchanged();
    }

    @Test
    void enrol_whenContentItemNotFound() throws Exception {
        mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"contentItemId": "00000000-0000-0000-0000-000000000000", "contentItemType": "DIALOGUE"}
                                """))
                .andExpect(status().isNotFound());

        assertThat(learningItemRepository.count()).isZero();
        assertThat(learningCardRepository.count()).isZero();
    }

    @Test
    void enrol_withMissingFields() throws Exception {
        mockMvc.perform(post(RequestMapping.LEARNING_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        assertThat(learningItemRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void exclude() throws Exception {
        mockMvc.perform(delete(RequestMapping.LEARNING_ITEM, LEARNING_ITEM_ID))
                .andExpect(status().isNoContent());

        assertThat(learningItemRepository.findById(LEARNING_ITEM_ID)).hasValueSatisfying(item ->
                assertThat(item.getExcludedAt()).isEqualTo(NOW));
        assertThatCardProgressIsUnchanged();
    }

    @Test
    @Sql(Fixtures.OTHER_USER_LEARNING_ITEM)
    void exclude_whenOwnedByOtherUser() throws Exception {
        mockMvc.perform(delete(RequestMapping.LEARNING_ITEM, OTHER_USER_LEARNING_ITEM_ID))
                .andExpect(status().isNotFound());

        assertThat(learningItemRepository.findById(OTHER_USER_LEARNING_ITEM_ID)).hasValueSatisfying(item ->
                assertThat(item.getExcludedAt()).isNull());
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void findByContent() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEMS)
                        .param("contentItemId", DIALOGUE_ID.toString())
                        .param("contentItemType", "DIALOGUE"))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/learning-items/active-learning-item.json"),
                        JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.EXCLUDED_LEARNING_ITEM)
    void findByContent_whenExcluded() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEMS)
                        .param("contentItemId", DIALOGUE_ID.toString())
                        .param("contentItemType", "DIALOGUE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(Fixtures.OTHER_USER_LEARNING_ITEM)
    void findByContent_whenNotEnrolled() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEMS)
                        .param("contentItemId", DIALOGUE_ID.toString())
                        .param("contentItemType", "DIALOGUE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(Fixtures.ACTIVE_LEARNING_ITEM)
    void getCards() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEM_CARDS, LEARNING_ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/learning-items/cards.json"),
                        JsonCompareMode.STRICT));
    }

    @Test
    @Sql(Fixtures.OTHER_USER_LEARNING_ITEM)
    void getCards_whenOwnedByOtherUser() throws Exception {
        mockMvc.perform(get(RequestMapping.LEARNING_ITEM_CARDS, OTHER_USER_LEARNING_ITEM_ID))
                .andExpect(status().isNotFound());
    }

    private void assertThatCardProgressIsUnchanged() {
        List<LearningCard> cards = learningCardRepository.findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(LEARNING_ITEM_ID);
        assertThat(cards)
                .extracting(LearningCard::getId, LearningCard::getRepetitionCount, LearningCard::getIntervalDays,
                        LearningCard::getEaseFactor)
                .containsExactly(tuple(CARD_A_ID, 2, 6, 2.5), tuple(CARD_B_ID, 2, 6, 2.6));
    }
}
