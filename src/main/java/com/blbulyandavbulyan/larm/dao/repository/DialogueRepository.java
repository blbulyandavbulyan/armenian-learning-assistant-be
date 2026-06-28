package com.blbulyandavbulyan.larm.dao.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Dialogue;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface DialogueRepository extends CrudRepository<Dialogue, UUID> {
    @EntityGraph(attributePaths = {"title", "title.mediaSet", "titleTranslations",
            "speakers.namePhrase", "speakers.namePhrase.mediaSet", "speakers.translations",
            "dialoguePhrases.phrase", "dialoguePhrases.phrase.mediaSet", "dialoguePhrases.translations"})
    @Query("SELECT d FROM Dialogue d WHERE d.id = :id")
    Optional<Dialogue> findByIdEagerly(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"title", "titleTranslations", "title.mediaSet"})
    @Query(value =
            """
            SELECT d FROM Dialogue d \
            ORDER BY l2_distance(d.embedding, :embedding)\
            """)
    List<Dialogue> searchByEmbedding(@Param("embedding") float[] embedding, Limit limit);
}
