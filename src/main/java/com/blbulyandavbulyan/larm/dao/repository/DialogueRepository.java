package com.blbulyandavbulyan.larm.dao.repository;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

public interface DialogueRepository extends CrudRepository<Dialogue, UUID> {
    @Override
    @EntityGraph(attributePaths = {"title", "title.translations", "title.mediaSet",
            "speakers.namePhrase", "speakers.namePhrase.translations", "speakers.namePhrase.mediaSet",
            "dialoguePhrases.phrase", "dialoguePhrases.phrase.translations", "dialoguePhrases.phrase.mediaSet"})
    Optional<Dialogue> findById(UUID id);
}
