package com.blbulyandavbulyan.larm.phrase.util;

import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.dao.TestDialogueRepository;
import com.blbulyandavbulyan.larm.phrase.dao.projections.DialogueRecord;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.OptionalAssert;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class DialogueRecordAssertHelper {

    private final TestDialogueRepository testDialogueRepository;

    public OptionalAssert<DialogueRecord> assertThatDialogueWithId(UUID dialogueId) {
        return Assertions.assertThat(testDialogueRepository.findById(dialogueId).map(DialogueRecord::new));
    }
}
