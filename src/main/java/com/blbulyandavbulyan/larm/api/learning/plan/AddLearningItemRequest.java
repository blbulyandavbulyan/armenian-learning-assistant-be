package com.blbulyandavbulyan.larm.api.learning.plan;

import java.util.UUID;

public record AddLearningItemRequest(
        UUID targetId,
        LearningItemType targetType) {
}
