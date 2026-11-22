package com.blbulyandavbulyan.larm.exercise;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseSession;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import org.jspecify.annotations.Nullable;

public record HistoryItemResource(UUID sessionId, UUID learningCardId, UUID learningItemId, UUID contentItemId,
                                  ContentItemType contentItemType, ExerciseType exerciseType, @Nullable UUID variantId,
                                  Instant startedAt, Instant completedAt, int aggregateScore) {

    static HistoryItemResource from(ExerciseSession session) {
        LearningCard card = session.getLearningCard();
        LearningItem item = card.getLearningItem();
        return new HistoryItemResource(session.getId(), card.getId(), item.getId(), item.getContentItemId(),
                item.getContentItemType(), card.getExerciseType(), card.getVariantId(), session.getStartedAt(),
                session.getCompletedAt(), session.getAggregateScore());
    }
}
