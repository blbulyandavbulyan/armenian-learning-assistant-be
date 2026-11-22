package com.blbulyandavbulyan.larm.exercise.srs;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * SM-2 spaced repetition scheduling, see spec section 7.2.
 */
@Component
public class Sm2Algorithm {

    public static final double INITIAL_EASE_FACTOR = 2.5;

    public static final int MAX_INTERVAL_DAYS = 3650;

    private static final double MIN_EASE_FACTOR = 1.3;

    private static final int MAX_QUALITY = 5;

    private static final int PASSING_QUALITY = 3;

    public Sm2Result apply(Sm2Input input) {
        List<List<Integer>> qualitiesPerSubItem = input.qualitiesPerSubItem();
        if (qualitiesPerSubItem.isEmpty() || qualitiesPerSubItem.stream().anyMatch(List::isEmpty)) {
            throw new IllegalArgumentException("SM-2 needs at least one attempt for every sub-item");
        }
        double sessionMean = qualitiesPerSubItem.stream()
                .mapToDouble(qualities -> qualities.stream().mapToInt(Integer::intValue).average().orElseThrow())
                .average()
                .orElseThrow();
        int quality = (int) Math.round(sessionMean);
        int score = (int) Math.round(sessionMean / MAX_QUALITY * 100);

        if (quality < PASSING_QUALITY) {
            return new Sm2Result(0, 1, input.easeFactor(), quality, score);
        }
        int repetitionCount = input.repetitionCount() + 1;
        int intervalDays = switch (repetitionCount) {
            case 1 -> 1;
            case 2 -> 6;
            default -> (int) Math.min(MAX_INTERVAL_DAYS, Math.round(input.intervalDays() * input.easeFactor()));
        };
        int lapse = MAX_QUALITY - quality;
        double easeFactor = Math.max(MIN_EASE_FACTOR, input.easeFactor() + 0.1 - lapse * (0.08 + lapse * 0.02));
        return new Sm2Result(repetitionCount, intervalDays, easeFactor, quality, score);
    }
}
