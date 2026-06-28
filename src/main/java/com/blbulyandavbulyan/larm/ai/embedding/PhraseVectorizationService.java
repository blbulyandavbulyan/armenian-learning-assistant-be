package com.blbulyandavbulyan.larm.ai.embedding;

import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.core.CreateNewPhraseParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhraseVectorizationService {

    private final EmbeddingModel embeddingModel;

    public float[] vectorize(CreateNewPhraseParameters params) {
        String base = params.isoLanguageCode() + ": " + params.phrase();
        String translations = concatenateTranslations(params);
        String comboText = base + translations;
        return embeddingModel.embed(comboText);
    }

    private static String concatenateTranslations(CreateNewPhraseParameters params) {
        if (params.translations() != null && !params.translations().isEmpty()) {
            return ", " + params.translations().stream()
                    .map(t -> t.isoLanguageCode() + ": " + t.translationText())
                    .collect(Collectors.joining(", "));
        }
        return "";
    }
}
