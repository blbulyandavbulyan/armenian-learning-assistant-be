package com.blbulyandavbulyan.larm.phrase.dao;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

public interface TestPhraseRepository extends Repository<Phrase, UUID> {
    
    @EntityGraph(attributePaths = {"mediaSet", "translations"})
    Optional<Phrase> findById(UUID id);
}
