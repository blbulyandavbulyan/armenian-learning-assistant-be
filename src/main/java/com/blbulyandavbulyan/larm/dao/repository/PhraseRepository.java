package com.blbulyandavbulyan.larm.dao.repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PhraseRepository extends CrudRepository<Phrase, UUID> {
    @Query("SELECT p.phrase FROM Phrase p WHERE p.phrase IN :phrases")
    Set<String> findExistingPhrases(@Param("phrases") Collection<String> phrases);

    @EntityGraph(attributePaths = {"mediaSet", "translations"})
    Page<Phrase> findAll(Pageable pageable);
}
