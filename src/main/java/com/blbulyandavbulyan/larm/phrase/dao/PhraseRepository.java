package com.blbulyandavbulyan.larm.phrase.dao;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PhraseRepository extends CrudRepository<Phrase, UUID> {
    @Query("SELECT phrase FROM phrases WHERE phrase IN (:phrases)")
    Set<String> findExistingPhrases(@Param("phrases") Collection<String> phrases);

    Page<Phrase> findAll(Pageable pageable);
}
