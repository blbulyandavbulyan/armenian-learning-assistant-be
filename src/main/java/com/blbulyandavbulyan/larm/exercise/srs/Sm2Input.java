package com.blbulyandavbulyan.larm.exercise.srs;

import java.util.List;

/**
 * Card state before the review plus the attempt qualities of the session, one list per sub-item.
 */
public record Sm2Input(int repetitionCount, int intervalDays, double easeFactor, List<List<Integer>> qualitiesPerSubItem) {
}
