package com.blbulyandavbulyan.larm.exercise.evaluation;

import com.blbulyandavbulyan.larm.exercise.TextAnswer;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypingAnswerEvaluatorTest {

    private final TypingAnswerEvaluator evaluator = new TypingAnswerEvaluator();

    private Evaluation evaluate(String expected, String typed) {
        return evaluator.evaluate(new ExpectedAnswer(expected, null), new TextAnswer(typed));
    }

    @Test
    void evaluate_exactMatch() {
        assertThat(evaluate("Բարեւ ձեզ", "Բարեւ ձեզ")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_ignoresCase() {
        assertThat(evaluate("Բարեւ ձեզ", "բարեւ ձեզ")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_treatsLigatureEwAsEchYiwn() {
        // U+0587 "և" is the same letters as "եւ"
        assertThat(evaluate("Բարեւ ձեզ", "Բարև ձեզ")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_ignoresPunctuationIncludingArmenianQuestionMark() {
        assertThat(evaluate("Ինչպե՞ս եք:", "Ինչպես եք")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_ignoresExtraWhitespace() {
        assertThat(evaluate("Ահա, խնդրեմ:", "  ահա   խնդրեմ ")).isEqualTo(new Evaluation(5, true));
    }

    @Test
    void evaluate_withOneTypo() {
        // "բարեւ ձեզ" vs "բարեվ ձեզ": 1 edit out of 9 characters -> similarity 0.89
        assertThat(evaluate("Բարեւ ձեզ", "Բարեվ ձեզ")).isEqualTo(new Evaluation(4, false));
    }

    @Test
    void evaluate_withIncompleteAnswer() {
        // "ահա խնդրեմ" vs "ահա": 7 edits out of 10 characters -> similarity 0.3
        assertThat(evaluate("Ահա, խնդրեմ:", "Ահա")).isEqualTo(new Evaluation(1, false));
    }

    @Test
    void evaluate_withUnrelatedAnswer() {
        assertThat(evaluate("Բարեւ ձեզ", "Ահա, խնդրեմ:").rawQuality()).isLessThanOrEqualTo(1);
    }

    @Test
    void evaluate_withEmptyAnswer() {
        assertThat(evaluate("Բարեւ ձեզ", "   ")).isEqualTo(new Evaluation(0, false));
    }
}
