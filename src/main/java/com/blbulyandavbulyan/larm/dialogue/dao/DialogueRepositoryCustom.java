package com.blbulyandavbulyan.larm.dialogue.dao;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dialogue.FullDialogueResource;

public interface DialogueRepositoryCustom {
    Optional<FullDialogueResource> findFullById(UUID id);
}
