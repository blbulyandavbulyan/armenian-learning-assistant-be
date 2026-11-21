package com.blbulyandavbulyan.larm.exercise;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.core.exception.ConflictException;
import com.blbulyandavbulyan.larm.core.exception.EntityNotFoundException;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseAttempt;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseSession;
import com.blbulyandavbulyan.larm.dao.entities.LearningCard;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseAttemptRepository;
import com.blbulyandavbulyan.larm.dao.repository.ExerciseSessionRepository;
import com.blbulyandavbulyan.larm.dao.repository.LearningCardRepository;
import com.blbulyandavbulyan.larm.exercise.evaluation.AnswerEvaluators;
import com.blbulyandavbulyan.larm.exercise.evaluation.Evaluation;
import com.blbulyandavbulyan.larm.exercise.srs.Sm2Algorithm;
import com.blbulyandavbulyan.larm.exercise.srs.Sm2Input;
import com.blbulyandavbulyan.larm.exercise.srs.Sm2Result;
import com.blbulyandavbulyan.larm.learning.ContentItemResolver;
import com.blbulyandavbulyan.larm.learning.ContentItemResolvers;
import com.blbulyandavbulyan.larm.learning.ExpectedAnswer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Session lifecycle: start, attempts, recovery, completion (spec section 5).
 */
@Service
@RequiredArgsConstructor
public class ExerciseSessionService {

    static final int HINT_PENALTY = 1;

    private final LearningCardRepository learningCardRepository;

    private final ExerciseSessionRepository exerciseSessionRepository;

    private final ExerciseAttemptRepository exerciseAttemptRepository;

    private final ContentItemResolvers contentItemResolvers;

    private final AnswerEvaluators answerEvaluators;

    private final Sm2Algorithm sm2Algorithm;

    private final Clock clock;

    /**
     * Starts a session for the card, or returns the open one; the card row lock serializes parallel starts.
     */
    @Transactional
    public SessionStartResult startSession(UUID userId, UUID learningCardId) {
        LearningCard card = learningCardRepository.findByIdAndUserIdForUpdate(learningCardId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Learning card %s not found".formatted(learningCardId)));
        if (card.getLearningItem().isExcluded()) {
            throw new ConflictException("Learning item %s is excluded".formatted(card.getLearningItem().getId()));
        }
        Optional<ExerciseSession> openSession = exerciseSessionRepository.findByLearningCardIdAndCompletedAtIsNull(card.getId());
        ExerciseSession session = openSession.orElseGet(() -> exerciseSessionRepository.save(ExerciseSession.builder()
                .learningCard(card)
                .userId(userId)
                .startedAt(Instant.now(clock))
                .build()));
        return new SessionStartResult(toResource(session, card), openSession.isEmpty());
    }

    @Transactional(readOnly = true)
    public SessionStatusResource getSession(UUID userId, UUID sessionId) {
        ExerciseSession session = exerciseSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> sessionNotFound(sessionId));
        SessionResource resource = toResource(session, session.getLearningCard());
        Map<UUID, List<AttemptResource>> attemptsBySubItem = exerciseAttemptRepository
                .findBySessionIdOrderByEvaluatedAtAscIdAsc(sessionId).stream()
                .collect(Collectors.groupingBy(ExerciseAttempt::getContentSubItemId, LinkedHashMap::new,
                        Collectors.mapping(AttemptResource::from, Collectors.toList())));
        List<SubItemAttemptsResource> attempted = resource.subItemIds().stream()
                .filter(attemptsBySubItem::containsKey)
                .map(subItemId -> new SubItemAttemptsResource(subItemId, attemptsBySubItem.get(subItemId)))
                .toList();
        List<UUID> pending = resource.subItemIds().stream()
                .filter(subItemId -> !attemptsBySubItem.containsKey(subItemId))
                .toList();
        return new SessionStatusResource(resource, attempted, pending);
    }

    @Transactional
    public AttemptResultResource submitAttempt(UUID userId, UUID sessionId, AttemptCommand command) {
        ExerciseSession session = lockOpenSession(userId, sessionId);
        LearningCard card = session.getLearningCard();
        LearningItem item = card.getLearningItem();
        ContentItemResolver resolver = contentItemResolvers.forType(item.getContentItemType());
        if (!resolver.resolveSubItemIds(item.getContentItemId(), card.getVariantId()).contains(command.contentSubItemId())) {
            throw subItemNotInSession(command.contentSubItemId(), sessionId);
        }
        ExpectedAnswer expected = resolver.resolveExpectedAnswer(item.getContentItemId(), command.contentSubItemId())
                .orElseThrow(() -> subItemNotInSession(command.contentSubItemId(), sessionId));

        Evaluation evaluation = answerEvaluators.forType(card.getExerciseType()).evaluate(expected, command.answer());
        int quality = command.usedHint() ? Math.max(0, evaluation.rawQuality() - HINT_PENALTY) : evaluation.rawQuality();
        ExerciseAttempt attempt = exerciseAttemptRepository.save(ExerciseAttempt.builder()
                .session(session)
                .contentSubItemId(command.contentSubItemId())
                .answerType(command.answer().type())
                .answerText(answerText(command.answer()))
                .usedHint(command.usedHint())
                .rawQuality(evaluation.rawQuality())
                .quality(quality)
                .correct(evaluation.correct())
                .evaluatedAt(Instant.now(clock))
                .build());
        return new AttemptResultResource(attempt.getId(), command.contentSubItemId(), command.usedHint(), quality,
                evaluation.correct(), evaluation.correct() ? null : expected.text());
    }

    /**
     * Seals the session and applies SM-2 to its card.
     */
    @Transactional
    public CompletionResource completeSession(UUID userId, UUID sessionId) {
        ExerciseSession session = lockOpenSession(userId, sessionId);
        LearningCard card = session.getLearningCard();
        List<UUID> subItemIds = resolveSubItemIds(card);
        if (subItemIds.isEmpty()) {
            throw new ConflictException("Session %s has nothing to answer".formatted(sessionId));
        }
        Map<UUID, List<Integer>> qualitiesBySubItem = exerciseAttemptRepository.findBySessionIdOrderByEvaluatedAtAscIdAsc(sessionId).stream()
                .collect(Collectors.groupingBy(ExerciseAttempt::getContentSubItemId,
                        Collectors.mapping(ExerciseAttempt::getQuality, Collectors.toList())));
        List<UUID> pending = subItemIds.stream().filter(subItemId -> !qualitiesBySubItem.containsKey(subItemId)).toList();
        if (!pending.isEmpty()) {
            throw new ConflictException("Session %s has sub-items without attempts: %s".formatted(sessionId, pending));
        }

        Sm2Result result = sm2Algorithm.apply(new Sm2Input(card.getRepetitionCount(), card.getIntervalDays(), card.getEaseFactor(),
                subItemIds.stream().map(qualitiesBySubItem::get).toList()));
        Instant now = Instant.now(clock);
        Instant nextReviewAt = now.plus(result.intervalDays(), ChronoUnit.DAYS);
        CompletionResource completion = new CompletionResource(session.getId(), now, result.aggregateScore(),
                card.getIntervalDays(), result.intervalDays(), nextReviewAt, result.easeFactor());
        applyResult(session, card, result, now);
        return completion;
    }

    private static void applyResult(ExerciseSession session, LearningCard card, Sm2Result result, Instant now) {
        card.setRepetitionCount(result.repetitionCount());
        card.setIntervalDays(result.intervalDays());
        card.setEaseFactor(result.easeFactor());
        card.setNextReviewAt(now.plus(result.intervalDays(), ChronoUnit.DAYS));
        card.setUpdatedAt(now);
        session.setCompletedAt(now);
        session.setAggregateScore(result.aggregateScore());
    }

    private ExerciseSession lockOpenSession(UUID userId, UUID sessionId) {
        ExerciseSession session = exerciseSessionRepository.findByIdAndUserIdForUpdate(sessionId, userId)
                .orElseThrow(() -> sessionNotFound(sessionId));
        if (session.isCompleted()) {
            throw new ConflictException("Session %s is already completed".formatted(sessionId));
        }
        return session;
    }

    private List<UUID> resolveSubItemIds(LearningCard card) {
        LearningItem item = card.getLearningItem();
        return contentItemResolvers.forType(item.getContentItemType()).resolveSubItemIds(item.getContentItemId(), card.getVariantId());
    }

    private SessionResource toResource(ExerciseSession session, LearningCard card) {
        LearningItem item = card.getLearningItem();
        return new SessionResource(session.getId(), card.getId(), item.getId(), item.getContentItemId(), item.getContentItemType(),
                card.getExerciseType(), card.getVariantId(), resolveSubItemIds(card), session.getStartedAt(), session.getCompletedAt());
    }

    private static @Nullable String answerText(Answer answer) {
        return switch (answer) {
            case TextAnswer textAnswer -> textAnswer.text();
        };
    }

    private static EntityNotFoundException sessionNotFound(UUID sessionId) {
        return new EntityNotFoundException("Exercise session %s not found".formatted(sessionId));
    }

    private static ConflictException subItemNotInSession(UUID subItemId, UUID sessionId) {
        return new ConflictException("Sub-item %s is not part of session %s".formatted(subItemId, sessionId));
    }
}
