package com.blbulyandavbulyan.larm.exercise;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseQueueService {

    private final LearningCardRepository learningCardRepository;

    private final Clock clock;

    @Transactional(readOnly = true)
    public Page<DueCardResource> getDueCards(UUID userId, @Nullable ExerciseType exerciseType,
                                             @Nullable ContentItemType contentItemType, Pageable pageable) {
        return learningCardRepository.findDueCards(userId, Instant.now(clock), exerciseType, contentItemType, pageable)
                .map(DueCardResource::from);
    }
}
