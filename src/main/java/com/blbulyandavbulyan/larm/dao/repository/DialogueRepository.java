package com.blbulyandavbulyan.larm.dao.repository;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface DialogueRepository extends CrudRepository<Dialogue, UUID> {
    @EntityGraph(attributePaths = {"title", "title.translations", "title.mediaSet",
            "speakers.namePhrase", "speakers.namePhrase.translations", "speakers.namePhrase.mediaSet",
            "dialoguePhrases.phrase", "dialoguePhrases.phrase.translations", "dialoguePhrases.phrase.mediaSet"})
    @Query("SELECT d FROM Dialogue d WHERE d.id = :id")
    Optional<Dialogue> findByIdEagerly(@Param("id") UUID id);
}
