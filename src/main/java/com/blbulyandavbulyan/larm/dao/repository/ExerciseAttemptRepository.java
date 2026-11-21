package com.blbulyandavbulyan.larm.dao.repository;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, UUID> {

    List<ExerciseAttempt> findBySessionIdOrderByEvaluatedAtAscIdAsc(UUID sessionId);
}
