package com.blbulyandavbulyan.larm.dialogue;

import java.util.List;

import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import com.blbulyandavbulyan.larm.dao.repository.DialogueRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DialogueSearchService {

    private final DialogueRepository dialogueRepository;
    private final EmbeddingModel embeddingModel;
    private final ObservationRegistry observationRegistry;

    @Cacheable(value = "dialougesSemanticSearch", key = "#query.trim()")
    public List<Dialogue> searchDialogues(String query) {
        String normalizedQuery = query.trim();
        float[] embedding = Observation.createNotStarted("gen_ai.embedding", observationRegistry)
                .observe(() -> embeddingModel.embed(normalizedQuery));
        return dialogueRepository.searchByEmbedding(embedding, Limit.of(50));
    }
}
