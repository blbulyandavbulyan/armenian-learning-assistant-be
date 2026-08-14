package com.blbulyandavbulyan.larm.api.learning.plan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.exercises.ExerciseType;

public record LearningPlanResponse(List<LearningPlanItem> items) {

    public record LearningPlanItem(
            UUID id,
            UUID targetId,
            LearningItemType targetType,
            List<ExerciseType> availableExercisesTypes,
            LocalDateTime addedAt) {
    }
}
