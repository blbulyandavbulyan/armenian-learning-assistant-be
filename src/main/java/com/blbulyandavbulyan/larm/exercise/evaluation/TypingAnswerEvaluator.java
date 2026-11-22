package com.blbulyandavbulyan.larm.exercise.evaluation;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.exercise.Answer;
import com.blbulyandavbulyan.larm.exercise.TextAnswer;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;
import org.springframework.stereotype.Component;

/**
 * Compares typed text with the expected phrase using normalized Levenshtein similarity.
 */
@Component
public class TypingAnswerEvaluator implements AnswerEvaluator {

    private static final int MAX_QUALITY = 5;

    private static final Pattern PUNCTUATION = Pattern.compile("\\p{P}+");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    @Override
    public ExerciseType supportedType() {
        return ExerciseType.TYPING;
    }

    @Override
    public Evaluation evaluate(ExpectedAnswer expected, Answer answer) {
        if (!(answer instanceof TextAnswer(String typedText))) {
            throw new UnsupportedAnswerTypeException(answer.type(), supportedType());
        }
        String expectedText = normalize(expected.text());
        String actualText = normalize(typedText);
        if (expectedText.equals(actualText)) {
            return new Evaluation(MAX_QUALITY, true);
        }
        if (actualText.isEmpty()) {
            return new Evaluation(0, false);
        }
        double similarity = 1.0 - (double) levenshtein(expectedText, actualText) / Math.max(expectedText.length(), actualText.length());
        return new Evaluation(qualityFor(similarity), false);
    }

    /**
     * Normalizes text: NFKC (maps the ligature "և" to "եւ"), lower case, no punctuation, single spaces.
     */
    static String normalize(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        normalized = PUNCTUATION.matcher(normalized).replaceAll("");
        return WHITESPACE.matcher(normalized).replaceAll(" ").trim();
    }

    private static int qualityFor(double similarity) {
        if (similarity > 0.8) {
            return 4;
        }
        if (similarity > 0.6) {
            return 3;
        }
        if (similarity > 0.4) {
            return 2;
        }
        if (similarity > 0.2) {
            return 1;
        }
        return 0;
    }

    private static int levenshtein(String first, String second) {
        int[] previous = new int[second.length() + 1];
        int[] current = new int[second.length() + 1];
        for (int j = 0; j <= second.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= first.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= second.length(); j++) {
                int substitution = previous[j - 1] + (first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1);
                current[j] = Math.min(substitution, Math.min(previous[j], current[j - 1]) + 1);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[second.length()];
    }
}
