package com.blbulyandavbulyan.larm.learning;

import org.jspecify.annotations.Nullable;

/**
 * What the user is expected to answer for one sub-item; grows when new evaluators need more.
 */
public record ExpectedAnswer(String text, @Nullable String transcription) {
}
