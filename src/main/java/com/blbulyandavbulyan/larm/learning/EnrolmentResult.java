package com.blbulyandavbulyan.larm.learning;

/**
 * Enrolment outcome; {@code created} is false when the item already existed (re-enrolment).
 */
public record EnrolmentResult(LearningItemResource item, boolean created) {
}
