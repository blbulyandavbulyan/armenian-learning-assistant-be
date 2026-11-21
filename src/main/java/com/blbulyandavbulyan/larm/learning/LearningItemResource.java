package com.blbulyandavbulyan.larm.learning;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import org.jspecify.annotations.Nullable;

public record LearningItemResource(UUID id, UUID contentItemId, ContentItemType contentItemType, Instant enrolledAt,
                                   @Nullable Instant excludedAt) {

    static LearningItemResource from(LearningItem item) {
        return new LearningItemResource(item.getId(), item.getContentItemId(), item.getContentItemType(),
                item.getEnrolledAt(), item.getExcludedAt());
    }
}
