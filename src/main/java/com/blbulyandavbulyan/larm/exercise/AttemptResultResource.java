package com.blbulyandavbulyan.larm.exercise;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Immediate feedback for one attempt; {@code expectedText} is only set when the answer was not correct.
 */
public record AttemptResultResource(UUID attemptId, UUID contentSubItemId, boolean usedHint, int quality, boolean correct,
                                    @Nullable String expectedText) {
}
