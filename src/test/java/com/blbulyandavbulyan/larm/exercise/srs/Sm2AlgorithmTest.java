package com.blbulyandavbulyan.larm.exercise.srs;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class Sm2AlgorithmTest {

    private final Sm2Algorithm sm2Algorithm = new Sm2Algorithm();

    private Sm2Result applySingle(int repetitionCount, int intervalDays, double easeFactor, int quality) {
        return sm2Algorithm.apply(new Sm2Input(repetitionCount, intervalDays, easeFactor, List.of(List.of(quality))));
    }

    @Test
    void apply_firstSuccessfulRepetition() {
        Sm2Result result = applySingle(0, 0, 2.5, 4);

        assertThat(result.repetitionCount()).isEqualTo(1);
        assertThat(result.intervalDays()).isEqualTo(1);
    }

    @Test
    void apply_secondSuccessfulRepetition() {
        Sm2Result result = applySingle(1, 1, 2.5, 4);

        assertThat(result.repetitionCount()).isEqualTo(2);
        assertThat(result.intervalDays()).isEqualTo(6);
    }

    @Test
    void apply_thirdSuccessfulRepetitionScalesByPreviousEaseFactor() {
        Sm2Result result = applySingle(2, 6, 2.5, 5);

        assertThat(result.repetitionCount()).isEqualTo(3);
        assertThat(result.intervalDays()).isEqualTo(15);
        assertThat(result.easeFactor()).isCloseTo(2.6, within(1e-9));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void apply_failedReviewResetsScheduleAndKeepsEaseFactor(int quality) {
        Sm2Result result = applySingle(3, 15, 2.5, quality);

        assertThat(result.repetitionCount()).isZero();
        assertThat(result.intervalDays()).isEqualTo(1);
        assertThat(result.easeFactor()).isEqualTo(2.5);
    }

    @Test
    void apply_easeFactorNeverDropsBelowMinimum() {
        // 1.3 + 0.1 - 2 * (0.08 + 2 * 0.02) = 1.16 -> clamped
        Sm2Result result = applySingle(2, 6, 1.3, 3);

        assertThat(result.easeFactor()).isEqualTo(1.3);
    }

    @Test
    void apply_averagesRetriesPerSubItemBeforeAveragingSubItems() {
        // sub-item means 3.5 and 5 -> session mean 4.25 -> quality 4, score 85
        Sm2Result result = sm2Algorithm.apply(new Sm2Input(2, 6, 2.5, List.of(List.of(2, 5), List.of(5))));

        assertThat(result.aggregateQuality()).isEqualTo(4);
        assertThat(result.aggregateScore()).isEqualTo(85);
    }

    @Test
    void apply_perfectSessionScoresHundred() {
        assertThat(applySingle(0, 0, 2.5, 5).aggregateScore()).isEqualTo(100);
    }

    @Test
    void apply_withoutSubItems() {
        Sm2Input input = new Sm2Input(0, 0, 2.5, List.of());

        assertThatThrownBy(() -> sm2Algorithm.apply(input)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apply_withSubItemWithoutAttempts() {
        Sm2Input input = new Sm2Input(0, 0, 2.5, List.of(List.of(5), List.of()));

        assertThatThrownBy(() -> sm2Algorithm.apply(input)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void apply_capsIntervalSoRepeatedEarlyReviewsCannotOverflow() {
        Sm2Result result = applySingle(5, 2_000_000_000, 2.5, 5);

        assertThat(result.intervalDays()).isEqualTo(Sm2Algorithm.MAX_INTERVAL_DAYS);
    }
}
