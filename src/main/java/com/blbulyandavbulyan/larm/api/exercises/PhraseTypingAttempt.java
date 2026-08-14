package com.blbulyandavbulyan.larm.api.exercises;

import java.util.UUID;

public record PhraseTypingAttempt(
    UUID dialoguePhraseId,
    String typedText,
    boolean tipUsed) {
}
