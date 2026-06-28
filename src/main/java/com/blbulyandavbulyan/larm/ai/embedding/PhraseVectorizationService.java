package com.blbulyandavbulyan.larm.ai.embedding;

import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.core.CreateNewPhraseParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhraseVectorizationService {

    private final EmbeddingModel embeddingModel;

    public float[] vectorize(CreateNewPhraseParameters params) {
        final String base = params.isoLanguageCode() + ": " + params.phrase();
        final String translations = concatenateTranslations(params);
        final String embeddingText = base + ", " + translations;
        log.debug("Converted phrase parameters: {} into embedding text: {}", params, embeddingText);
        return embeddingModel.embed(embeddingText);
    }

    private static String concatenateTranslations(CreateNewPhraseParameters params) {
        return params.translations().stream()
                .map(t -> t.isoLanguageCode() + ": " + t.translationText())
                .collect(Collectors.joining(", "));
    }
}
