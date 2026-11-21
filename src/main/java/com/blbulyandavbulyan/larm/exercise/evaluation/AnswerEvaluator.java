package com.blbulyandavbulyan.larm.exercise.evaluation;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.exercise.Answer;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;

/**
 * Evaluates answers for exactly one exercise type.
 */
public interface AnswerEvaluator {

    ExerciseType supportedType();

    /**
     * Evaluates the answer against the expected answer.
     *
     * @throws UnsupportedAnswerTypeException if the answer type cannot be used for this exercise type
     */
    Evaluation evaluate(ExpectedAnswer expected, Answer answer);
}
