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
@Table(name = "exercise_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ExerciseSession session;

    @Column(name = "content_sub_item_id", nullable = false)
    private UUID contentSubItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false)
    private AnswerType answerType;

    @Column(name = "answer_text")
    private String answerText;

    @Column(name = "used_hint", nullable = false)
    private boolean usedHint;

    @Column(name = "raw_quality", nullable = false)
    private int rawQuality;

    @Column(name = "quality", nullable = false)
    private int quality;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExerciseAttempt that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return "ExerciseAttempt".hashCode();
    }

    @Override
    public String toString() {
        return ("ExerciseAttempt{id=%s, session=%s, contentSubItemId=%s, answerType=%s, answerText=%s, usedHint=%s, "
                + "rawQuality=%d, quality=%d, correct=%s, evaluatedAt=%s}")
                .formatted(id, LAZY_LOADING, contentSubItemId, answerType, answerText, usedHint,
                        rawQuality, quality, correct, evaluatedAt);
    }
}
