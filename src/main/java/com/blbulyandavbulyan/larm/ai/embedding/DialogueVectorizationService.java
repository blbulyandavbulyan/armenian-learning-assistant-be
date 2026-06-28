package com.blbulyandavbulyan.larm.ai.embedding;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.core.SaveDialogueParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import static com.blbulyandavbulyan.larm.ai.embedding.util.TranslationUtils.extractTranslations;

@Slf4j
@Service
@RequiredArgsConstructor
public class DialogueVectorizationService {

    private final EmbeddingModel embeddingModel;

    public float[] vectorize(SaveDialogueParameters params) {
        final var speakerRefIdToSpeaker = params.speakers()
                .stream()
                .collect(Collectors.toMap(SaveDialogueParameters.SpeakerParameters::speakerRefId, Function.identity()));

        final var textBuilder = new StringBuilder();

        textBuilder.append("Topic: ").append(params.title());
        textBuilder.append(" (").append(extractTranslations(params.titleTranslations())).append(")\n\n");

        final String dialogueLines = params.dialoguePhrases().stream()
                .map(dp -> {
                    final var speaker = speakerRefIdToSpeaker.get(dp.speakerRefId());

                    return speaker.title()
                            + " (" + extractTranslations(speaker.translations()) + ")\n"
                            + dp.phrase()
                            + " (" + extractTranslations(dp.translations()) + ")";
                })
                .collect(Collectors.joining("\n\n"));

        textBuilder.append(dialogueLines);
        final var embeddingText = textBuilder.toString();
        
        log.debug("Converted dialogue parameters: {} into embedding text: {}", params, embeddingText);
        return embeddingModel.embed(embeddingText);
    }
}
