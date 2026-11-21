package com.blbulyandavbulyan.larm.dialogue;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.repository.DialoguePhraseRepository;
import com.blbulyandavbulyan.larm.dao.repository.DialogueRepository;
import com.blbulyandavbulyan.larm.dao.repository.DialogueSpeakerRepository;
import com.blbulyandavbulyan.larm.learning.ContentItemResolver;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Dialogues for exercises: variants are speakers, sub-items are lines (dialogue phrases).
 */
@Component
@RequiredArgsConstructor
public class DialogueContentItemResolver implements ContentItemResolver {

    private final DialogueRepository dialogueRepository;

    private final DialogueSpeakerRepository dialogueSpeakerRepository;

    private final DialoguePhraseRepository dialoguePhraseRepository;

    @Override
    public ContentItemType supportedType() {
        return ContentItemType.DIALOGUE;
    }

    @Override
    public Set<ExerciseType> supportedExerciseTypes() {
        return EnumSet.of(ExerciseType.TYPING);
    }

    @Override
    public boolean exists(UUID contentItemId) {
        return dialogueRepository.existsById(contentItemId);
    }

    @Override
    public List<UUID> resolveVariantIds(UUID contentItemId) {
        return dialogueSpeakerRepository.findIdsByDialogueId(contentItemId);
    }

    @Override
    public List<UUID> resolveSubItemIds(UUID contentItemId, @Nullable UUID variantId) {
        if (variantId == null) {
            return dialoguePhraseRepository.findIdsByDialogueId(contentItemId);
        }
        return dialoguePhraseRepository.findIdsByDialogueIdAndSpeakerId(contentItemId, variantId);
    }

    @Override
    public Optional<ExpectedAnswer> resolveExpectedAnswer(UUID contentItemId, UUID contentSubItemId) {
        return dialoguePhraseRepository.findPhraseText(contentItemId, contentSubItemId)
                .map(view -> new ExpectedAnswer(view.getText(), view.getTranscription()));
    }
}
