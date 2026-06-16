package com.blbulyandavbulyan.larm.dialogue.service;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dialogue.DialogueRetrievalService;
import com.blbulyandavbulyan.larm.dialogue.FullDialogueResource;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultDialogueRetrievalService implements DialogueRetrievalService {
    private final DialogueRepository dialogueRepository;

    @Override
    public Optional<FullDialogueResource> getDialogue(UUID id) {
        return dialogueRepository.findFullById(id);
    }
}
