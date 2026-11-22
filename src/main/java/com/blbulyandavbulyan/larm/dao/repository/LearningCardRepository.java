package com.blbulyandavbulyan.larm.dao.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearningCardRepository extends JpaRepository<LearningCard, UUID> {

    List<LearningCard> findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(UUID learningItemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM LearningCard c JOIN FETCH c.learningItem i WHERE c.id = :id AND i.userId = :userId")
    Optional<LearningCard> findByIdAndUserIdForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);

    @EntityGraph(attributePaths = "learningItem")
    @Query(value =
            """
            SELECT c FROM LearningCard c JOIN c.learningItem i
            WHERE i.userId = :userId
              AND i.excludedAt IS NULL
              AND c.nextReviewAt <= :now
              AND (:exerciseType IS NULL OR c.exerciseType = :exerciseType)
              AND (:contentItemType IS NULL OR i.contentItemType = :contentItemType)
            ORDER BY c.nextReviewAt ASC, c.id ASC
            """,
            countQuery =
                    """
                    SELECT COUNT(c) FROM LearningCard c JOIN c.learningItem i
                    WHERE i.userId = :userId
                      AND i.excludedAt IS NULL
                      AND c.nextReviewAt <= :now
                      AND (:exerciseType IS NULL OR c.exerciseType = :exerciseType)
                      AND (:contentItemType IS NULL OR i.contentItemType = :contentItemType)
                    """)
    Page<LearningCard> findDueCards(@Param("userId") UUID userId,
                                    @Param("now") Instant now,
                                    @Param("exerciseType") @Nullable ExerciseType exerciseType,
                                    @Param("contentItemType") @Nullable ContentItemType contentItemType,
                                    Pageable pageable);
}
