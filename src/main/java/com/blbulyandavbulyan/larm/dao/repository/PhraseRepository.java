package com.blbulyandavbulyan.larm.dao.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PhraseRepository extends CrudRepository<Phrase, UUID> {
    @Query("SELECT p FROM Phrase p WHERE p.phrase IN :phrases")
    List<Phrase> findByPhraseIn(@Param("phrases") Collection<String> phrases);

}
