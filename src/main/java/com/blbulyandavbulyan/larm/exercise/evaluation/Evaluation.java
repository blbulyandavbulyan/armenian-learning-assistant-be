package com.blbulyandavbulyan.larm.exercise.evaluation;

/**
 * Result of evaluating one answer: raw quality 0-5 (before hint penalty) and whether it matched exactly.
 */
public record Evaluation(int rawQuality, boolean correct) {
}
