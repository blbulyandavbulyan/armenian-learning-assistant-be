package com.blbulyandavbulyan.larm.api.exercises;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = DialogueSpeakerRoleplayingTypingRequest.class, name = "DIALOGUE_SPEAKER_ROLEPLAYING_TYPING")
})
public sealed interface TrackProgressRequest permits DialogueSpeakerRoleplayingTypingRequest {
    UUID learningItemId();
}
