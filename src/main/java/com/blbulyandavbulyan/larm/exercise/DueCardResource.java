package com.blbulyandavbulyan.larm.exercise;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import org.jspecify.annotations.Nullable;

public record DueCardResource(UUID learningCardId, UUID learningItemId, UUID contentItemId, ContentItemType contentItemType,
                              ExerciseType exerciseType, @Nullable UUID variantId, Instant nextReviewAt, int intervalDays,
                              int repetitionCount) {

    static DueCardResource from(LearningCard card) {
        LearningItem item = card.getLearningItem();
        return new DueCardResource(card.getId(), item.getId(), item.getContentItemId(), item.getContentItemType(),
                card.getExerciseType(), card.getVariantId(), card.getNextReviewAt(), card.getIntervalDays(), card.getRepetitionCount());
    }
}
