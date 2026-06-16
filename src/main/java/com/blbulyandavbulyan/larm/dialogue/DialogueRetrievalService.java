package com.blbulyandavbulyan.larm.dialogue;

import java.util.Optional;
import java.util.UUID;

public interface DialogueRetrievalService {
    /**
     * Gets the dialogue by ID and fully constructs it with phrases.
     *
     * @param id the dialogue ID
     * @return the dialogue resource if found
     */
    Optional<FullDialogueResource> getDialogue(UUID id);
}
