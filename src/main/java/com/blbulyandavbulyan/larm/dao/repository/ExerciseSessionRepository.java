package com.blbulyandavbulyan.larm.dao.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseSession;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseSessionRepository extends JpaRepository<ExerciseSession, UUID> {

    @EntityGraph(attributePaths = {"learningCard", "learningCard.learningItem"})
    Optional<ExerciseSession> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            SELECT s FROM ExerciseSession s
            JOIN FETCH s.learningCard c
            JOIN FETCH c.learningItem
            WHERE s.id = :id AND s.userId = :userId
            """)
    Optional<ExerciseSession> findByIdAndUserIdForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<ExerciseSession> findByLearningCardIdAndCompletedAtIsNull(UUID learningCardId);

    @EntityGraph(attributePaths = {"learningCard", "learningCard.learningItem"})
    @Query(value =
            """
            SELECT s FROM ExerciseSession s JOIN s.learningCard c JOIN c.learningItem i
            WHERE s.userId = :userId
              AND s.completedAt IS NOT NULL
              AND (CAST(:from AS java.time.Instant) IS NULL OR s.completedAt >= :from)
              AND (CAST(:to AS java.time.Instant) IS NULL OR s.completedAt <= :to)
              AND (:exerciseType IS NULL OR c.exerciseType = :exerciseType)
              AND (:contentItemType IS NULL OR i.contentItemType = :contentItemType)
            ORDER BY s.completedAt DESC, s.id ASC
            """,
            countQuery =
                    """
                    SELECT COUNT(s) FROM ExerciseSession s JOIN s.learningCard c JOIN c.learningItem i
                    WHERE s.userId = :userId
                      AND s.completedAt IS NOT NULL
                      AND (CAST(:from AS java.time.Instant) IS NULL OR s.completedAt >= :from)
                      AND (CAST(:to AS java.time.Instant) IS NULL OR s.completedAt <= :to)
                      AND (:exerciseType IS NULL OR c.exerciseType = :exerciseType)
                      AND (:contentItemType IS NULL OR i.contentItemType = :contentItemType)
                    """)
    Page<ExerciseSession> findCompleted(@Param("userId") UUID userId,
                                        @Param("from") @Nullable Instant from,
                                        @Param("to") @Nullable Instant to,
                                        @Param("exerciseType") @Nullable ExerciseType exerciseType,
                                        @Param("contentItemType") @Nullable ContentItemType contentItemType,
                                        Pageable pageable);
}
