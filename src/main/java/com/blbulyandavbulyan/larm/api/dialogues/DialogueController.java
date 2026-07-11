package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.blbulyandavbulyan.larm.core.DialogueOrchestrator;
import com.blbulyandavbulyan.larm.dialogue.DialogueRetrievalService;
import com.blbulyandavbulyan.larm.dialogue.DialogueSearchService;
import com.blbulyandavbulyan.larm.dialogue.SavedDialogueResource;
import com.blbulyandavbulyan.larm.logging.Loggable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final DialogueSearchService dialogueSearchService;

    @Override
    @Loggable(logLevel = Loggable.LogLevel.DEBUG)
    public SaveDialogueResponse saveDialogue(SaveDialogueRequest request) {
        SavedDialogueResource saved = dialogueOrchestrator.saveDialogue(
                dialogueRequestMapper.toParameters(request));
        return new SaveDialogueResponse(saved.id());
    }

    @Override
    public ResponseEntity<GetDialogueResponse> getDialogue(UUID id) {
        GetDialogueResponse response = dialogueRetrievalService.getDialogue(id)
                .map(dialogueResponseMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dialogue not found"));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(response);
    }

    @Override
    public SearchDialoguesResponse searchDialogues(String query) {
        var results = dialogueSearchService.searchDialogues(query);
        return dialogueResponseMapper.toSearchDialoguesResponse(results);
    }
}
