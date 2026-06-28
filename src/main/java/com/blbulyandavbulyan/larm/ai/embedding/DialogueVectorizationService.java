package com.blbulyandavbulyan.larm.ai.embedding;

import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.core.SaveDialogueParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DialogueVectorizationService {

    private final EmbeddingModel embeddingModel;

    public float[] vectorize(SaveDialogueParameters params) {
        if (params.dialoguePhrases() == null) {
            return new float[0];
        }
        // TODO what about speakers should we include them here?
        //  If we do we have to update already commited plan, because it will stay in git, in order to not confuse anyone
        String concatenatedText = params.dialoguePhrases().stream()
                .map(dp -> {
                    String text = dp.isoLanguageCode() + ": " + dp.phrase();
                    if (dp.translations() != null && !dp.translations().isEmpty()) {
                        text += ", " + dp.translations().stream()
                                .map(t -> t.isoLanguageCode() + ": " + t.translationText())
                                .collect(Collectors.joining(", "));
                    }
                    return text;
                })
                .collect(Collectors.joining("\n"));
                
        return embeddingModel.embed(concatenatedText);
    }
}
