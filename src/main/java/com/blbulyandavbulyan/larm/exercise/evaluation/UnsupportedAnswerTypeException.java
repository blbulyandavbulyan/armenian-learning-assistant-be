package com.blbulyandavbulyan.larm.exercise.evaluation;

import com.blbulyandavbulyan.larm.dao.entities.AnswerType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;

/**
 * Thrown when an answer type is not suitable for the exercise type of the card (HTTP 400).
 */
public class UnsupportedAnswerTypeException extends RuntimeException {
    public UnsupportedAnswerTypeException(AnswerType answerType, ExerciseType exerciseType) {
        super("Answer type %s is not supported for exercise type %s".formatted(answerType, exerciseType));
    }
}
