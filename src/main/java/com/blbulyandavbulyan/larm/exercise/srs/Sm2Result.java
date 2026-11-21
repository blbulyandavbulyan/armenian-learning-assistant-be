package com.blbulyandavbulyan.larm.exercise.srs;

/**
 * Card state after the review, the SM-2 quality used (0-5) and the session score (0-100).
 */
public record Sm2Result(int repetitionCount, int intervalDays, double easeFactor, int aggregateQuality, int aggregateScore) {
}
