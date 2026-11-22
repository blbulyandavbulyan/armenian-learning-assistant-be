package com.blbulyandavbulyan.larm.api.learning;

import java.util.List;

import com.blbulyandavbulyan.larm.learning.LearningCardResource;
import com.blbulyandavbulyan.larm.learning.LearningItemResource;
import org.springframework.stereotype.Component;

@Component
class LearningResponseMapper {

    LearningItemResponse toResponse(LearningItemResource item) {
        return LearningItemResponse.builder()
                .id(item.id())
                .contentItemId(item.contentItemId())
                .contentItemType(item.contentItemType())
                .enrolledAt(item.enrolledAt())
                .excludedAt(item.excludedAt())
                .build();
    }

    LearningCardsResponse toResponse(List<LearningCardResource> cards) {
        return new LearningCardsResponse(cards.stream()
                .map(card -> LearningCardsResponse.LearningCardResponse.builder()
                        .learningCardId(card.id())
                        .learningItemId(card.learningItemId())
                        .exerciseType(card.exerciseType())
                        .variantId(card.variantId())
                        .nextReviewAt(card.nextReviewAt())
                        .intervalDays(card.intervalDays())
                        .repetitionCount(card.repetitionCount())
                        .easeFactor(card.easeFactor())
                        .build())
                .toList());
    }
}
