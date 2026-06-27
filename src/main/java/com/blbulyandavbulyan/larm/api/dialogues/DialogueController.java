package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.UUID;

import com.blbulyandavbulyan.larm.core.DialogueOrchestrator;
import com.blbulyandavbulyan.larm.dialogue.DialogueRetrievalService;
import com.blbulyandavbulyan.larm.dialogue.SavedDialogueResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@Validated
class DialogueController implements DialoguesApi {

    private final DialogueOrchestrator dialogueOrchestrator;
    private final DialogueRequestMapper dialogueRequestMapper;
    private final DialogueRetrievalService dialogueRetrievalService;
    private final DialogueResponseMapper dialogueResponseMapper;

    @Override
    public SaveDialogueResponse saveDialogue(SaveDialogueRequest request) {
        SavedDialogueResource saved = dialogueOrchestrator.saveDialogue(
                dialogueRequestMapper.toParameters(request));
        return new SaveDialogueResponse(saved.id());
    }

    @Override
    public GetDialogueResponse getDialogue(UUID id) {
        return dialogueRetrievalService.getDialogue(id)
                .map(dialogueResponseMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dialogue not found"));
    }
}
