package com.blbulyandavbulyan.larm.dialogue.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.blbulyandavbulyan.larm.core.NewCreatePhraseParameters;
import com.blbulyandavbulyan.larm.core.PhraseProcessor;
import com.blbulyandavbulyan.larm.dialogue.DialogueSavingService;
import com.blbulyandavbulyan.larm.dialogue.SaveDialogueParameters;
import com.blbulyandavbulyan.larm.dialogue.SavedDialogueResource;
import com.blbulyandavbulyan.larm.dialogue.dao.Dialogue;
import com.blbulyandavbulyan.larm.dialogue.dao.DialoguePhrase;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueRepository;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueSpeaker;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueSpeakerRepository;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueSpeakerTranslation;
import com.blbulyandavbulyan.larm.dialogue.dao.DialogueTitleTranslation;
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

    private final PhraseProcessor phraseProcessor;
    private final PhraseStoringService phraseStoringService;
    private final DialogueSpeakerRepository dialogueSpeakerRepository;
    private final DialogueRepository dialogueRepository;

    @Override
    @Transactional
    public SavedDialogueResource saveDialogue(SaveDialogueParameters parameters) {
        // 1. Process TTS for each phrase and persist them
        List<SavePhraseParameters> processedPhrases = parameters.dialoguePhrases().stream()
                .map(dp -> phraseProcessor.process(
                        NewCreatePhraseParameters.builder()
                                .phrase(dp.phrase())
                                .isoLanguageCode(dp.isoLanguageCode())
                                .transcription(dp.transcription())
                                .translations(dp.translations())
                                .build()))
                .toList();

        List<PhraseResource> savedPhrases = phraseStoringService.batchSavePhrases(
                new BatchSavePhrasesParameters(processedPhrases));

        // 2. Persist speakers, mapping speakerRefId -> DB UUID
        Map<String, UUID> speakerRefIdToUuid = new HashMap<>();
        for (SaveDialogueParameters.SpeakerParameters sp : parameters.speakers()) {
            UUID speakerId = UUID.randomUUID();
            Instant now = Instant.now();
            Set<DialogueSpeakerTranslation> translations = sp.translations().stream()
                    .map(t -> DialogueSpeakerTranslation.builder()
                            .id(UUID.randomUUID())
                            .speakerId(speakerId)
                            .isoLanguageCode(t.isoLanguageCode())
                            .translationText(t.translationText())
                            .createdAt(now)
                            .isNewFlag(true)
                            .build())
                    .collect(Collectors.toSet());

            dialogueSpeakerRepository.save(DialogueSpeaker.builder()
                    .id(speakerId)
                    .speakerRefId(sp.speakerRefId())
                    .title(sp.title())
                    .transcription(sp.transcription())
                    .createdAt(now)
                    .translations(translations)
                    .isNewFlag(true)
                    .build());

            speakerRefIdToUuid.put(sp.speakerRefId(), speakerId);
        }

        // 3. Build ordered dialogue_phrases and title translations
        UUID dialogueId = UUID.randomUUID();
        Instant dialogueCreatedAt = Instant.now();

        List<SaveDialogueParameters.DialoguePhraseParameters> dialoguePhraseParams =
                parameters.dialoguePhrases();
        Set<DialoguePhrase> dialoguePhrases = IntStream.range(0, dialoguePhraseParams.size())
                .mapToObj(i -> {
                    SaveDialogueParameters.DialoguePhraseParameters dp = dialoguePhraseParams.get(i);
                    UUID phraseId = savedPhrases.get(i).id();
                    UUID speakerId = speakerRefIdToUuid.get(dp.speakerRefId());
                    return DialoguePhrase.builder()
                            .id(UUID.randomUUID())
                            .dialogueId(dialogueId)
                            .phraseId(phraseId)
                            .speakerId(speakerId)
                            .orderIndex(i)
                            .createdAt(dialogueCreatedAt)
                            .isNewFlag(true)
                            .build();
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<DialogueTitleTranslation> titleTranslations = parameters.titleTranslations().stream()
                .map(t -> DialogueTitleTranslation.builder()
                        .id(UUID.randomUUID())
                        .dialogueId(dialogueId)
                        .isoLanguageCode(t.isoLanguageCode())
                        .translationText(t.translationText())
                        .createdAt(dialogueCreatedAt)
                        .isNewFlag(true)
                        .build())
                .collect(Collectors.toSet());

        // 4. Persist dialogue aggregate
        dialogueRepository.save(Dialogue.builder()
                .id(dialogueId)
                .title(parameters.title())
                .transcription(parameters.transcription())
                .createdAt(dialogueCreatedAt)
                .translations(titleTranslations)
                .dialoguePhrases(dialoguePhrases)
                .isNewFlag(true)
                .build());

        return SavedDialogueResource.builder().id(dialogueId).build();
    }
}
