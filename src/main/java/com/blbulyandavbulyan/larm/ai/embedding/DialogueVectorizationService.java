package com.blbulyandavbulyan.larm.ai.embedding;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.core.SaveDialogueParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DialogueVectorizationService {

    private final EmbeddingModel embeddingModel;

    public float[] vectorize(SaveDialogueParameters params) {
        final var speakerRefIdToSpeaker = params.speakers()
                .stream()
                .collect(Collectors.toMap(SaveDialogueParameters.SpeakerParameters::speakerRefId, Function.identity()));

        String embeddingText = params.dialoguePhrases().stream()
                .map(dp -> {
                    String speakerName = speakerRefIdToSpeaker.get(dp.speakerRefId()).title();
                    String prefix = speakerName + ": ";
                    String text = prefix + dp.isoLanguageCode() + ": " + dp.phrase();

                    text += ", " + dp.translations().stream()
                            .map(t -> t.isoLanguageCode() + ": " + t.translationText())
                            .collect(Collectors.joining(", "));
                    return text;
                })
                .collect(Collectors.joining("\n"));

        log.debug("Converted dialogue parameters: {} into embedding text: {}", params, embeddingText);
        return embeddingModel.embed(embeddingText);
    }
}
