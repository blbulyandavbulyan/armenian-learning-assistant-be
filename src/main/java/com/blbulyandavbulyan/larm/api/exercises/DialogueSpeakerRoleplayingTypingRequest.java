package com.blbulyandavbulyan.larm.api.exercises;

import java.util.List;
import java.util.UUID;

public record DialogueSpeakerRoleplayingTypingRequest(
    UUID learningItemId,
    List<PhraseTypingAttempt> typingAttempts) implements TrackProgressRequest {
}
