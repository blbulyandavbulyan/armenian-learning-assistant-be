package com.blbulyandavbulyan.larm.dao.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "learning_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "content_item_id", nullable = false)
    private UUID contentItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_item_type", nullable = false)
    private ContentItemType contentItemType;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Column(name = "excluded_at")
    private Instant excludedAt;

    public boolean isExcluded() {
        return excludedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LearningItem that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return "LearningItem".hashCode();
    }

    @Override
    public String toString() {
        return "LearningItem{id=%s, userId=%s, contentItemId=%s, contentItemType=%s, enrolledAt=%s, excludedAt=%s}"
                .formatted(id, userId, contentItemId, contentItemType, enrolledAt, excludedAt);
    }
}
