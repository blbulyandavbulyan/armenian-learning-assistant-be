package com.blbulyandavbulyan.larm.learning;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.core.exception.EntityNotFoundException;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import com.blbulyandavbulyan.larm.dao.repository.LearningItemRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningItemService {

    private static final double INITIAL_EASE_FACTOR = 2.5;

    private final LearningItemRepository learningItemRepository;

    private final LearningCardRepository learningCardRepository;

    private final ContentItemResolvers contentItemResolvers;

    private final Clock clock;

    /**
     * Enrols the content item, or re-enrols it, and makes sure every card it should have exists.
     */
    @Transactional
    public EnrolmentResult enrol(UUID userId, UUID contentItemId, ContentItemType contentItemType) {
        ContentItemResolver resolver = contentItemResolvers.forType(contentItemType);
        if (!resolver.exists(contentItemId)) {
            throw new EntityNotFoundException("Content item %s of type %s not found".formatted(contentItemId, contentItemType));
        }
        Instant now = Instant.now(clock);
        Optional<LearningItem> existing = learningItemRepository
                .findByUserIdAndContentItemIdAndContentItemType(userId, contentItemId, contentItemType);
        LearningItem item = existing.orElseGet(() -> learningItemRepository.save(LearningItem.builder()
                .userId(userId)
                .contentItemId(contentItemId)
                .contentItemType(contentItemType)
                .enrolledAt(now)
                .build()));
        item.setExcludedAt(null);
        createMissingCards(item, resolver, now);
        return new EnrolmentResult(LearningItemResource.from(item), existing.isEmpty());
    }

    @Transactional
    public void exclude(UUID userId, UUID learningItemId) {
        LearningItem item = findOwned(userId, learningItemId);
        if (!item.isExcluded()) {
            item.setExcludedAt(Instant.now(clock));
        }
    }

    @Transactional(readOnly = true)
    public LearningItemResource findActiveByContent(UUID userId, UUID contentItemId, ContentItemType contentItemType) {
        return learningItemRepository.findByUserIdAndContentItemIdAndContentItemType(userId, contentItemId, contentItemType)
                .filter(item -> !item.isExcluded())
                .map(LearningItemResource::from)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active learning item for content item %s of type %s".formatted(contentItemId, contentItemType)));
    }

    @Transactional(readOnly = true)
    public List<LearningCardResource> getCards(UUID userId, UUID learningItemId) {
        LearningItem item = findOwned(userId, learningItemId);
        return learningCardRepository.findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(item.getId()).stream()
                .map(LearningCardResource::from)
                .toList();
    }

    private LearningItem findOwned(UUID userId, UUID learningItemId) {
        return learningItemRepository.findByIdAndUserId(learningItemId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Learning item %s not found".formatted(learningItemId)));
    }

    private void createMissingCards(LearningItem item, ContentItemResolver resolver, Instant now) {
        Set<CardKey> existingKeys = learningCardRepository.findByLearningItemIdOrderByExerciseTypeAscVariantIdAsc(item.getId()).stream()
                .map(card -> new CardKey(card.getExerciseType(), card.getVariantId()))
                .collect(Collectors.toSet());
        List<UUID> variantIds = resolver.resolveVariantIds(item.getContentItemId());
        List<@Nullable UUID> variants = variantIds.isEmpty() ? Collections.singletonList(null) : variantIds;

        List<LearningCard> missingCards = new ArrayList<>();
        for (ExerciseType exerciseType : resolver.supportedExerciseTypes()) {
            for (UUID variantId : variants) {
                if (!existingKeys.contains(new CardKey(exerciseType, variantId))) {
                    missingCards.add(LearningCard.builder()
                            .learningItem(item)
                            .exerciseType(exerciseType)
                            .variantId(variantId)
                            .nextReviewAt(now)
                            .intervalDays(0)
                            .easeFactor(INITIAL_EASE_FACTOR)
                            .repetitionCount(0)
                            .createdAt(now)
                            .updatedAt(now)
                            .build());
                }
            }
        }
        learningCardRepository.saveAll(missingCards);
    }

    private record CardKey(ExerciseType exerciseType, @Nullable UUID variantId) {
    }
}
