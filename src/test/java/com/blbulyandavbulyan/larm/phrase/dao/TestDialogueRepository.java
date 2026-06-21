package com.blbulyandavbulyan.larm.phrase.dao;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

public interface TestDialogueRepository extends Repository<Dialogue, UUID> {
    
    @EntityGraph(attributePaths = {
            "title", 
            "title.translations", 
            "title.mediaSet", 
            "speakers", 
            "speakers.namePhrase",
            "speakers.namePhrase.translations",
            "speakers.namePhrase.mediaSet",
            "dialoguePhrases",
            "dialoguePhrases.phrase",
            "dialoguePhrases.phrase.translations",
            "dialoguePhrases.phrase.mediaSet",
            "dialoguePhrases.speaker",
            "dialoguePhrases.speaker.namePhrase"
    })
    Optional<Dialogue> findById(UUID id);
}
