package com.blbulyandavbulyan.larm.exercise;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import org.jspecify.annotations.Nullable;

public record SessionResource(UUID sessionId, UUID learningCardId, UUID learningItemId, UUID contentItemId,
                              ContentItemType contentItemType, ExerciseType exerciseType, @Nullable UUID variantId,
                              List<UUID> subItemIds, Instant startedAt, @Nullable Instant completedAt) {
}
