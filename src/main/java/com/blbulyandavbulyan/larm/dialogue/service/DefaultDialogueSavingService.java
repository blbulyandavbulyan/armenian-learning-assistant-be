package com.blbulyandavbulyan.larm.dialogue.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.blbulyandavbulyan.larm.dialogue.DialogueSavingService;
import com.blbulyandavbulyan.larm.dialogue.SavedDialogueResource;
import com.blbulyandavbulyan.larm.dialogue.StoreDialogueParameters;
import com.blbulyandavbulyan.larm.dialogue.dao.Dialogue;
import com.blbulyandavbulyan.larm.dialogue.dao.DialoguePhrase;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueRepository;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueSpeaker;
import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DefaultDialogueSavingService implements DialogueSavingService {

    private final DialogueRepository dialogueRepository;
    private final PhraseStoringService phraseStoringService;

    @Override
    @Transactional
    public SavedDialogueResource saveDialogue(StoreDialogueParameters parameters) {
        final UUID dialogueId = UUID.randomUUID();
        final Instant dialogueCreatedAt = Instant.now();

        // 1. Save all phrases (title, speakers, dialogue phrases) in this transaction
        List<SavePhraseParameters> allPhrasesToSave = new ArrayList<>();
        allPhrasesToSave.add(parameters.titlePhrase());
        parameters.speakers().forEach(sp -> allPhrasesToSave.add(sp.namePhrase()));
        parameters.dialoguePhrases().forEach(dp -> allPhrasesToSave.add(dp.phrase()));

        List<PhraseResource> savedPhrases = phraseStoringService.batchSavePhrases(
                new BatchSavePhrasesParameters(allPhrasesToSave));

        int savedIndex = 0;
        PhraseResource savedTitle = savedPhrases.get(savedIndex++);

        // 2. Build speakers, mapping speakerRefId -> DB UUID
        Map<String, UUID> speakerRefIdToUuid = new HashMap<>();
        Set<DialogueSpeaker> speakers = new LinkedHashSet<>();
        for (StoreDialogueParameters.StoreSpeakerParameters sp : parameters.speakers()) {
            PhraseResource savedSpeaker = savedPhrases.get(savedIndex++);
            UUID speakerId = UUID.randomUUID();
            speakers.add(DialogueSpeaker.builder()
                    .id(speakerId)
                    .dialogueId(dialogueId)
                    .speakerRefId(sp.speakerRefId())
                    .namePhraseId(savedSpeaker.id())
                    .createdAt(dialogueCreatedAt)
                    .isNewFlag(true)
                    .build());
            speakerRefIdToUuid.put(sp.speakerRefId(), speakerId);
        }

        // 3. Build ordered dialogue_phrases
        List<StoreDialogueParameters.StoreDialoguePhraseParameters> dialoguePhraseParams =
                parameters.dialoguePhrases();
        Set<DialoguePhrase> dialoguePhrases = IntStream.range(0, dialoguePhraseParams.size())
                .mapToObj(i -> {
                    StoreDialogueParameters.StoreDialoguePhraseParameters dp = dialoguePhraseParams.get(i);
                    // We need the phrase ID for this specific dialogue phrase.
                    // However, we cannot access savedIndex from inside the lambda,
                    // so we map by using the fact that dialogue phrases start at a fixed offset.
                    // The offset is 1 (title) + speakers.size()
                    int phraseOffset = 1 + parameters.speakers().size() + i;
                    PhraseResource savedPhrase = savedPhrases.get(phraseOffset);

                    UUID speakerId = speakerRefIdToUuid.get(dp.speakerRefId());
                    return DialoguePhrase.builder()
                            .id(UUID.randomUUID())
                            .dialogueId(dialogueId)
                            .phraseId(savedPhrase.id())
                            .speakerId(speakerId)
                            .orderIndex(i)
                            .createdAt(dialogueCreatedAt)
                            .isNewFlag(true)
                            .build();
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 4. Persist dialogue aggregate
        dialogueRepository.save(Dialogue.builder()
                .id(dialogueId)
                .titlePhraseId(savedTitle.id())
                .createdAt(dialogueCreatedAt)
                // Spring Data JDBC handles the speakers relation if we set dialogueId on speakers,
                // but since DialogueSpeaker is its own aggregate root we don't strictly need to
                // add them to Dialogue's collection before saving. If we save Dialogue aggregate,
                // it cascades.
                .speakers(speakers)
                .dialoguePhrases(dialoguePhrases)
                .isNewFlag(true)
                .build());

        return SavedDialogueResource.builder().id(dialogueId).build();
    }
}
