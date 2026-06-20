package com.blbulyandavbulyan.larm.phrase.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

public interface DialogueRepository extends CrudRepository<Dialogue, UUID> {
    @Override
    @EntityGraph(attributePaths = {"title", "speakers", "dialoguePhrases"})
    Optional<Dialogue> findById(UUID id);
}
