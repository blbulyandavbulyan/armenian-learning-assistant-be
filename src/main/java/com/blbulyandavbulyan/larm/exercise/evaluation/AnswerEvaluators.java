package com.blbulyandavbulyan.larm.exercise.evaluation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import org.springframework.stereotype.Component;

/**
 * Looks up the {@link AnswerEvaluator} bean for an exercise type.
 */
@Component
public class AnswerEvaluators {

    private final Map<ExerciseType, AnswerEvaluator> evaluatorsByType;

    public AnswerEvaluators(List<AnswerEvaluator> evaluators) {
        // toMap fails on startup if two evaluators claim the same type
        this.evaluatorsByType = evaluators.stream()
                .collect(Collectors.toMap(AnswerEvaluator::supportedType, Function.identity()));
    }

    public AnswerEvaluator forType(ExerciseType exerciseType) {
        AnswerEvaluator evaluator = evaluatorsByType.get(exerciseType);
        if (evaluator == null) {
            throw new IllegalStateException("No answer evaluator for exercise type " + exerciseType);
        }
        return evaluator;
    }
}
