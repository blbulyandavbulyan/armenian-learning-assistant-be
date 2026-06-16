package com.blbulyandavbulyan.larm.api.dialogues;

import com.blbulyandavbulyan.larm.core.DialogueOrchestrator;
import com.blbulyandavbulyan.larm.dialogue.SavedDialogueResource;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class DialogueController implements DialoguesApi {

    private final DialogueOrchestrator dialogueOrchestrator;
    private final DialogueRequestMapper dialogueRequestMapper;

    @Override
    public SaveDialogueResponse saveDialogue(SaveDialogueRequest request) {
        SavedDialogueResource saved = dialogueOrchestrator.saveDialogue(
                dialogueRequestMapper.toParameters(request));
        return new SaveDialogueResponse(saved.id());
    }
}
