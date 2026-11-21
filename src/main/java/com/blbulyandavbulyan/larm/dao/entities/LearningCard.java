package com.blbulyandavbulyan.larm.dao.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.blbulyandavbulyan.larm.dao.entities.LazyLoadingStringConstants.LAZY_LOADING;

@Entity
@Table(name = "learning_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningCard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_item_id", nullable = false)
    private LearningItem learningItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    private ExerciseType exerciseType;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "next_review_at", nullable = false)
    private Instant nextReviewAt;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    @Column(name = "ease_factor", nullable = false)
    private double easeFactor;

    @Column(name = "repetition_count", nullable = false)
    private int repetitionCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearningCard that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return "LearningCard".hashCode();
    }

    @Override
    public String toString() {
        return ("LearningCard{id=%s, learningItem=%s, exerciseType=%s, variantId=%s, nextReviewAt=%s, intervalDays=%d, "
                + "easeFactor=%s, repetitionCount=%d, createdAt=%s, updatedAt=%s}")
                .formatted(id, LAZY_LOADING, exerciseType, variantId, nextReviewAt, intervalDays,
                        easeFactor, repetitionCount, createdAt, updatedAt);
    }
}
