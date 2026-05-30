package com.blbulyandavbulyan.larm.phrase.dao;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface PhraseRepository extends CrudRepository<Phrase, UUID> {
    @Query("SELECT phrase FROM phrases WHERE phrase IN (:phrases)")
    Set<String> findExistingPhrases(@Param("phrases") Collection<String> phrases);
}
