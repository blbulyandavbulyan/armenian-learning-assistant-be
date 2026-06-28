package com.blbulyandavbulyan.larm.ai.embedding;

import com.blbulyandavbulyan.larm.core.CreateNewPhraseParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import static com.blbulyandavbulyan.larm.ai.embedding.util.TranslationUtils.extractTranslations;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhraseVectorizationService {

    private final EmbeddingModel embeddingModel;

    public float[] vectorize(CreateNewPhraseParameters params) {
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(params.phrase());
        
        String translations = extractTranslations(params.translations());
        textBuilder.append(" (").append(translations).append(")");
        
        String embeddingText = textBuilder.toString();
        log.debug("Converted phrase parameters: {} into embedding text: {}", params, embeddingText);
        return embeddingModel.embed(embeddingText);
    }
}
