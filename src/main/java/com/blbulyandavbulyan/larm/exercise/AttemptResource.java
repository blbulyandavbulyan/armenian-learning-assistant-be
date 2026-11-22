package com.blbulyandavbulyan.larm.exercise;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.AnswerType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseAttempt;
import org.jspecify.annotations.Nullable;

public record AttemptResource(UUID attemptId, AnswerType answerType, @Nullable String answerText, boolean usedHint,
                              int quality, boolean correct, Instant evaluatedAt) {

    static AttemptResource from(ExerciseAttempt attempt) {
        return new AttemptResource(attempt.getId(), attempt.getAnswerType(), attempt.getAnswerText(), attempt.isUsedHint(),
                attempt.getQuality(), attempt.isCorrect(), attempt.getEvaluatedAt());
    }
}
