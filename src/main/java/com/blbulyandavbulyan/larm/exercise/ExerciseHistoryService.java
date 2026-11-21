package com.blbulyandavbulyan.larm.exercise;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseSessionRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExerciseHistoryService {

    private final ExerciseSessionRepository exerciseSessionRepository;

    @Transactional(readOnly = true)
    public Page<HistoryItemResource> getHistory(UUID userId, @Nullable Instant from, @Nullable Instant to,
                                                @Nullable ExerciseType exerciseType, @Nullable ContentItemType contentItemType,
                                                Pageable pageable) {
        return exerciseSessionRepository.findCompleted(userId, from, to, exerciseType, contentItemType, pageable)
                .map(HistoryItemResource::from);
    }
}
