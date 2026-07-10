package com.blbulyandavbulyan.larm.dialogue.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import com.blbulyandavbulyan.larm.dao.entities.DialoguePhrase;
import com.blbulyandavbulyan.larm.dao.entities.DialoguePhraseTranslation;
import com.blbulyandavbulyan.larm.dao.entities.DialogueSpeaker;
import com.blbulyandavbulyan.larm.dao.entities.DialogueSpeakerTranslation;
import com.blbulyandavbulyan.larm.dao.entities.DialogueTitleTranslation;
import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import com.blbulyandavbulyan.larm.dao.repository.DialogueRepository;
import com.blbulyandavbulyan.larm.dialogue.DialogueSavingService;
import com.blbulyandavbulyan.larm.dialogue.SavedDialogueResource;
import com.blbulyandavbulyan.larm.dialogue.StoreDialogueParameters;
import com.blbulyandavbulyan.larm.logging.Loggable;
import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import io.micrometer.core.annotation.Timed;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultDialogueSavingService implements DialogueSavingService {

    private final DialogueRepository dialogueRepository;
    private final PhraseStoringService phraseStoringService;

    @Override
    @Transactional
    @Loggable(logLevel = Loggable.LogLevel.DEBUG)
    @Timed(value = "dialogue.save.latency",
            description = "The amount of time it takes to save a dialogue (does not include external API calls which were made before)")
    public SavedDialogueResource saveDialogue(StoreDialogueParameters parameters) {
        final Instant dialogueCreatedAt = Instant.now();

        DialogueSavedPhrases dialogueSavedPhrases = saveAllPhrases(parameters);

        Dialogue dialogue = Dialogue.builder()
                .title(dialogueSavedPhrases.titlePhrase())
                .createdAt(dialogueCreatedAt)
                .embedding(parameters.embedding())
                .build();
        Set<DialogueTitleTranslation> titleTranslations = parameters.titlePhrase().translations().stream()
                .map(t -> DialogueTitleTranslation.builder()
                        .dialogue(dialogue)
                        .isoLanguageCode(t.isoLanguageCode())
                        .translationText(t.translationText())
                        .build())
                .collect(Collectors.toSet());
        dialogue.setTitleTranslations(titleTranslations);

        Map<String, DialogueSpeaker> speakerRefToSpeaker = new HashMap<>();
        Set<DialogueSpeaker> speakers = new LinkedHashSet<>();
        for (int i = 0; i < parameters.speakers().size(); i++) {
            StoreDialogueParameters.StoreSpeakerParameters sp = parameters.speakers().get(i);
            Phrase speakerName = dialogueSavedPhrases.speakerNames().get(i);

            final var speaker = DialogueSpeaker.builder()
                    .dialogue(dialogue)
                    .namePhrase(speakerName)
                    .createdAt(dialogueCreatedAt)
                    .build();
            Set<DialogueSpeakerTranslation> speakerTranslations = sp.namePhrase().translations().stream()
                    .map(t -> DialogueSpeakerTranslation.builder()
                            .dialogueSpeaker(speaker)
                            .isoLanguageCode(t.isoLanguageCode())
                            .translationText(t.translationText())
                            .build())
                    .collect(Collectors.toSet());
            speaker.setTranslations(speakerTranslations);
            speakers.add(speaker);
            speakerRefToSpeaker.put(sp.speakerRefId(), speaker);
        }

        List<StoreDialogueParameters.StoreDialoguePhraseParameters> dialoguePhraseParams = parameters.dialoguePhrases();

        Set<DialoguePhrase> dialoguePhrases = IntStream.range(0, dialoguePhraseParams.size())
                .mapToObj(i -> {
                    StoreDialogueParameters.StoreDialoguePhraseParameters dp = dialoguePhraseParams.get(i);
                    Phrase savedPhrase = dialogueSavedPhrases.dialoguePhrases().get(i);

                    DialogueSpeaker speaker = speakerRefToSpeaker.get(dp.speakerRefId());
                    DialoguePhrase dialoguePhrase = DialoguePhrase.builder()
                            .dialogue(dialogue)
                            .phrase(savedPhrase)
                            .speaker(speaker)
                            .orderIndex(i)
                            .createdAt(dialogueCreatedAt)
                            .build();
                    Set<DialoguePhraseTranslation> phraseTranslations = dp.phrase().translations().stream()
                            .map(t -> DialoguePhraseTranslation.builder()
                                    .dialoguePhrase(dialoguePhrase)
                                    .isoLanguageCode(t.isoLanguageCode())
                                    .translationText(t.translationText())
                                    .build())
                            .collect(Collectors.toSet());
                    dialoguePhrase.setTranslations(phraseTranslations);
                    return dialoguePhrase;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        dialogue.setSpeakers(speakers);
        dialogue.setDialoguePhrases(dialoguePhrases);

        log.trace("Saving dialogue: {}", dialogue);
        dialogueRepository.save(dialogue);

        return SavedDialogueResource.builder().id(dialogue.getId()).build();
    }

    @Builder
    private record DialogueSavedPhrases(Phrase titlePhrase, List<Phrase> speakerNames, List<Phrase> dialoguePhrases) {
    }

    private DialogueSavedPhrases saveAllPhrases(StoreDialogueParameters parameters) {
        List<SavePhraseParameters> allPhrasesToSave = new ArrayList<>();
        allPhrasesToSave.add(parameters.titlePhrase());

        parameters.speakers()
                .stream()
                .map(StoreDialogueParameters.StoreSpeakerParameters::namePhrase)
                .forEach(allPhrasesToSave::add);

        parameters.dialoguePhrases()
                .stream()
                .map(StoreDialogueParameters.StoreDialoguePhraseParameters::phrase)
                .forEach(allPhrasesToSave::add);

        List<Phrase> savedPhrases = phraseStoringService.batchSavePhrases(
                new BatchSavePhrasesParameters(allPhrasesToSave));

        return DialogueSavedPhrases.builder()
                .titlePhrase(savedPhrases.getFirst())
                .speakerNames(savedPhrases.subList(1, parameters.speakers().size() + 1))
                .dialoguePhrases(savedPhrases.subList(parameters.speakers().size() + 1, savedPhrases.size()))
                .build();
    }
}
