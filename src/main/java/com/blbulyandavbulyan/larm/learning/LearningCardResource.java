package com.blbulyandavbulyan.larm.learning;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import org.jspecify.annotations.Nullable;

public record LearningCardResource(UUID id, UUID learningItemId, ExerciseType exerciseType, @Nullable UUID variantId,
                                   Instant nextReviewAt, int intervalDays, int repetitionCount, double easeFactor) {

    static LearningCardResource from(LearningCard card) {
        return new LearningCardResource(card.getId(), card.getLearningItem().getId(), card.getExerciseType(), card.getVariantId(),
                card.getNextReviewAt(), card.getIntervalDays(), card.getRepetitionCount(), card.getEaseFactor());
    }
}
