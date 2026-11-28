package com.blbulyandavbulyan.larm.dialogue;

import java.util.List;

import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import com.blbulyandavbulyan.larm.dao.repository.DialogueRepository;
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

    @Cacheable(value = "dialougesSemanticSearch", key = "#query.trim()")
    public List<Dialogue> searchDialogues(String query) {
        String normalizedQuery = query.trim();
        float[] embedding = embeddingModel.embed(normalizedQuery);
        return dialogueRepository.searchByEmbedding(embedding, Limit.of(50));
    }
}
