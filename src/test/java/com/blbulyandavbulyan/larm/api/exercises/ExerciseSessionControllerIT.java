package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Duration;
import java.util.UUID;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.dao.entities.AnswerType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseAttempt;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseAttemptRepository;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseSessionRepository;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import com.blbulyandavbulyan.larm.security.WithMockDatabaseUser;
import com.blbulyandavbulyan.larm.srs.SrsTestData.Fixtures;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.ResultActions;

import static com.blbulyandavbulyan.larm.TestUtils.readResourceToString;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.CARD_A_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.COMPLETED_SESSION_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.IN_PROGRESS_SESSION_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.LINE_1_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.LINE_2_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.LINE_3_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.OTHER_USER_CARD_A_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.OTHER_USER_SESSION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql({Fixtures.DIALOGUE, Fixtures.TEST_USER, Fixtures.ACTIVE_LEARNING_ITEM})
@WithMockDatabaseUser
class ExerciseSessionControllerIT extends BaseIT {

    interface RequestMapping {
        String SESSIONS = "/exercises/sessions";
        String SESSION = "/exercises/sessions/{sessionId}";
        String ATTEMPTS = "/exercises/sessions/{sessionId}/attempts";
        String COMPLETE = "/exercises/sessions/{sessionId}/complete";
    }

    @Autowired
    private ExerciseSessionRepository exerciseSessionRepository;

    @Autowired
    private ExerciseAttemptRepository exerciseAttemptRepository;

    @Autowired
    private LearningCardRepository learningCardRepository;

    // ======================== start ========================

    @Test
    void startSession() throws Exception {
        String response = startSessionFor(CARD_A_ID)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID sessionId = UUID.fromString(JsonPath.read(response, "$.sessionId"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/exercises/session-started.json").formatted(sessionId), response);
        assertThat(exerciseSessionRepository.findAll())
                .singleElement()
                .satisfies(session -> {
                    assertThat(session.getId()).isEqualTo(sessionId);
                    assertThat(session.getStartedAt()).isEqualTo(NOW);
                    assertThat(session.getCompletedAt()).isNull();
                });
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void startSession_whenSessionInProgress() throws Exception {
        startSessionFor(CARD_A_ID)
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/exercises/session-in-progress.json"),
                        JsonCompareMode.STRICT));

        assertThat(exerciseSessionRepository.count()).isEqualTo(1);
    }

    @Test
    @Sql(Fixtures.EXCLUDE_ACTIVE_LEARNING_ITEM)
    void startSession_whenLearningItemExcluded() throws Exception {
        startSessionFor(CARD_A_ID).andExpect(status().isConflict());

        assertThat(exerciseSessionRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.OTHER_USER_LEARNING_ITEM)
    void startSession_whenCardOwnedByOtherUser() throws Exception {
        startSessionFor(OTHER_USER_CARD_A_ID).andExpect(status().isNotFound());

        assertThat(exerciseSessionRepository.count()).isZero();
    }

    // ======================== attempts ========================

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt() throws Exception {
        // ligature "և" and lower case are accepted for "Բարեւ ձեզ"
        String response = submitTextAttempt(IN_PROGRESS_SESSION_ID, LINE_1_ID, false, "բարև ձեզ")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID attemptId = UUID.fromString(JsonPath.read(response, "$.attemptId"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/exercises/attempt-correct.json").formatted(attemptId), response);
        assertThatStoredAttempt(attemptId, LINE_1_ID, "բարև ձեզ", false, 5, 5, true);
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_withWrongAnswer() throws Exception {
        String response = submitTextAttempt(IN_PROGRESS_SESSION_ID, LINE_3_ID, false, "Ահա")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID attemptId = UUID.fromString(JsonPath.read(response, "$.attemptId"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/exercises/attempt-wrong.json").formatted(attemptId), response);
        assertThatStoredAttempt(attemptId, LINE_3_ID, "Ահա", false, 1, 1, false);
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_withHint() throws Exception {
        String response = submitTextAttempt(IN_PROGRESS_SESSION_ID, LINE_3_ID, true, "Ահա, խնդրեմ")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID attemptId = UUID.fromString(JsonPath.read(response, "$.attemptId"));
        assertJsonEquals(readResourceToString("/expected-responses/srs/exercises/attempt-with-hint.json").formatted(attemptId), response);
        assertThatStoredAttempt(attemptId, LINE_3_ID, "Ահա, խնդրեմ", true, 5, 4, true);
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_forOtherSpeakersLine() throws Exception {
        submitTextAttempt(IN_PROGRESS_SESSION_ID, LINE_2_ID, false, "Բարեւ ձեզ, խնդրում եմ մեկ հաց:")
                .andExpect(status().isConflict());

        assertThat(exerciseAttemptRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.COMPLETED_SESSION)
    void submitAttempt_whenSessionCompleted() throws Exception {
        submitTextAttempt(COMPLETED_SESSION_ID, LINE_2_ID, false, "Բարեւ ձեզ, խնդրում եմ մեկ հաց:")
                .andExpect(status().isConflict());

        assertThat(exerciseAttemptRepository.count()).isEqualTo(1);
    }

    @Test
    @Sql({Fixtures.OTHER_USER_LEARNING_ITEM, Fixtures.OTHER_USER_SESSION})
    void submitAttempt_whenSessionOwnedByOtherUser() throws Exception {
        submitTextAttempt(OTHER_USER_SESSION_ID, LINE_1_ID, false, "Բարեւ ձեզ")
                .andExpect(status().isNotFound());

        assertThat(exerciseAttemptRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_withoutAnswerText() throws Exception {
        postAttempt(IN_PROGRESS_SESSION_ID,
                """
                {"contentSubItemId": "%s", "usedHint": false, "answer": {"type": "TEXT"}}
                """.formatted(LINE_1_ID))
                .andExpect(status().isBadRequest());

        assertThat(exerciseAttemptRepository.count()).isZero();
    }

    @Test
    @Sql(Fixtures.IN_PROGRESS_SESSION)
    void submitAttempt_withUnknownAnswerType() throws Exception {
        postAttempt(IN_PROGRESS_SESSION_ID,
                """
                {"contentSubItemId": "%s", "usedHint": false, "answer": {"type": "AUDIO", "assetId": "%s"}}
                """.formatted(LINE_1_ID, UUID.randomUUID()))
                .andExpect(status().isBadRequest());

        assertThat(exerciseAttemptRepository.count()).isZero();
    }

    // ======================== recovery ========================

    @Test
    @Sql({Fixtures.IN_PROGRESS_SESSION, Fixtures.FIRST_LINE_ATTEMPTS})
    void getSession() throws Exception {
        mockMvc.perform(get(RequestMapping.SESSION, IN_PROGRESS_SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/exercises/session-status.json"),
                        JsonCompareMode.STRICT));
    }

    @Test
    @Sql({Fixtures.OTHER_USER_LEARNING_ITEM, Fixtures.OTHER_USER_SESSION})
    void getSession_whenSessionOwnedByOtherUser() throws Exception {
        mockMvc.perform(get(RequestMapping.SESSION, OTHER_USER_SESSION_ID))
                .andExpect(status().isNotFound());
    }

    // ======================== complete ========================

    @Test
    @Sql({Fixtures.IN_PROGRESS_SESSION, Fixtures.FIRST_LINE_ATTEMPTS, Fixtures.LAST_LINE_ATTEMPT})
    void completeSession() throws Exception {
        mockMvc.perform(post(RequestMapping.COMPLETE, IN_PROGRESS_SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(readResourceToString("/expected-responses/srs/exercises/session-completed.json"),
                        JsonCompareMode.STRICT));

        assertThat(exerciseSessionRepository.findById(IN_PROGRESS_SESSION_ID)).hasValueSatisfying(session -> {
            assertThat(session.getCompletedAt()).isEqualTo(NOW);
            assertThat(session.getAggregateScore()).isEqualTo(75);
        });
        assertThat(learningCardRepository.findById(CARD_A_ID)).hasValueSatisfying(card -> {
            assertThat(card.getRepetitionCount()).isEqualTo(3);
            assertThat(card.getIntervalDays()).isEqualTo(15);
            assertThat(card.getEaseFactor()).isEqualTo(2.5);
            assertThat(card.getNextReviewAt()).isEqualTo(NOW.plus(Duration.ofDays(15)));
            assertThat(card.getUpdatedAt()).isEqualTo(NOW);
        });
    }

    @Test
    @Sql({Fixtures.IN_PROGRESS_SESSION, Fixtures.FIRST_LINE_ATTEMPTS})
    void completeSession_whenSubItemsPending() throws Exception {
        mockMvc.perform(post(RequestMapping.COMPLETE, IN_PROGRESS_SESSION_ID))
                .andExpect(status().isConflict());

        assertThat(exerciseSessionRepository.findById(IN_PROGRESS_SESSION_ID)).hasValueSatisfying(session ->
                assertThat(session.getCompletedAt()).isNull());
        assertThat(learningCardRepository.findById(CARD_A_ID)).hasValueSatisfying(card ->
                assertThat(card.getRepetitionCount()).isEqualTo(2));
    }

    @Test
    @Sql(Fixtures.COMPLETED_SESSION)
    void completeSession_whenAlreadyCompleted() throws Exception {
        mockMvc.perform(post(RequestMapping.COMPLETE, COMPLETED_SESSION_ID))
                .andExpect(status().isConflict());

        assertThat(exerciseSessionRepository.findById(COMPLETED_SESSION_ID)).hasValueSatisfying(session ->
                assertThat(session.getAggregateScore()).isEqualTo(80));
    }

    // ======================== helpers ========================

    private ResultActions startSessionFor(UUID learningCardId) throws Exception {
        return mockMvc.perform(post(RequestMapping.SESSIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"learningCardId": "%s"}
                        """.formatted(learningCardId)));
    }

    private ResultActions submitTextAttempt(UUID sessionId, UUID subItemId, boolean usedHint, String text) throws Exception {
        return postAttempt(sessionId,
                """
                {"contentSubItemId": "%s", "usedHint": %s, "answer": {"type": "TEXT", "text": "%s"}}
                """.formatted(subItemId, usedHint, text));
    }

    private ResultActions postAttempt(UUID sessionId, String body) throws Exception {
        return mockMvc.perform(post(RequestMapping.ATTEMPTS, sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void assertThatStoredAttempt(UUID attemptId, UUID subItemId, String answerText, boolean usedHint,
                                         int rawQuality, int quality, boolean correct) {
        assertThat(exerciseAttemptRepository.findById(attemptId)).get()
                .extracting(ExerciseAttempt::getContentSubItemId, ExerciseAttempt::getAnswerType, ExerciseAttempt::getAnswerText,
                        ExerciseAttempt::isUsedHint, ExerciseAttempt::getRawQuality, ExerciseAttempt::getQuality,
                        ExerciseAttempt::isCorrect, ExerciseAttempt::getEvaluatedAt)
                .containsExactly(subItemId, AnswerType.TEXT, answerText, usedHint, rawQuality, quality, correct, NOW);
    }
}
