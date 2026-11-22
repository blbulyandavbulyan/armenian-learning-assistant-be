package com.blbulyandavbulyan.larm.api.exercises;

import java.util.List;
import java.util.function.Function;

import com.blbulyandavbulyan.larm.exercise.Answer;
import com.blbulyandavbulyan.larm.exercise.AttemptCommand;
import com.blbulyandavbulyan.larm.exercise.AttemptResultResource;
import com.blbulyandavbulyan.larm.exercise.CompletionResource;
import com.blbulyandavbulyan.larm.exercise.DueCardResource;
import com.blbulyandavbulyan.larm.exercise.HistoryItemResource;
import com.blbulyandavbulyan.larm.exercise.SessionResource;
import com.blbulyandavbulyan.larm.exercise.SessionStatusResource;
import com.blbulyandavbulyan.larm.exercise.TextAnswer;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
class ExerciseResponseMapper {

    AttemptCommand toCommand(AttemptRequest request) {
        return new AttemptCommand(request.contentSubItemId(), request.usedHint(), toAnswer(request.answer()));
    }

    SessionResponse toResponse(SessionResource session) {
        return SessionResponse.builder()
                .sessionId(session.sessionId())
                .learningCardId(session.learningCardId())
                .learningItemId(session.learningItemId())
                .contentItemId(session.contentItemId())
                .contentItemType(session.contentItemType())
                .exerciseType(session.exerciseType())
                .variantId(session.variantId())
                .subItemIds(session.subItemIds())
                .startedAt(session.startedAt())
                .completedAt(session.completedAt())
                .build();
    }

    SessionStatusResponse toResponse(SessionStatusResource status) {
        return new SessionStatusResponse(
                toResponse(status.session()),
                status.attemptedSubItems().stream()
                        .map(subItem -> new SessionStatusResponse.AttemptedSubItemResponse(subItem.contentSubItemId(),
                                subItem.attempts().stream()
                                        .map(attempt -> SessionStatusResponse.AttemptHistoryResponse.builder()
                                                .attemptId(attempt.attemptId())
                                                .answerType(attempt.answerType())
                                                .answerText(attempt.answerText())
                                                .usedHint(attempt.usedHint())
                                                .quality(attempt.quality())
                                                .correct(attempt.correct())
                                                .evaluatedAt(attempt.evaluatedAt())
                                                .build())
                                        .toList()))
                        .toList(),
                status.pendingSubItemIds());
    }

    AttemptResponse toResponse(AttemptResultResource result) {
        return AttemptResponse.builder()
                .attemptId(result.attemptId())
                .contentSubItemId(result.contentSubItemId())
                .usedHint(result.usedHint())
                .quality(result.quality())
                .correct(result.correct())
                .expectedText(result.expectedText())
                .build();
    }

    CompleteSessionResponse toResponse(CompletionResource completion) {
        return new CompleteSessionResponse(completion.sessionId(), completion.completedAt(), completion.aggregateScore(),
                new CompleteSessionResponse.CardUpdateResponse(completion.previousIntervalDays(), completion.nextIntervalDays(),
                        completion.nextReviewAt(), completion.easeFactor()));
    }

    QueueResponse toQueueResponse(Page<DueCardResource> page) {
        return new QueueResponse(mapContent(page, card -> QueueResponse.QueueItemResponse.builder()
                .learningCardId(card.learningCardId())
                .learningItemId(card.learningItemId())
                .contentItemId(card.contentItemId())
                .contentItemType(card.contentItemType())
                .exerciseType(card.exerciseType())
                .variantId(card.variantId())
                .nextReviewAt(card.nextReviewAt())
                .intervalDays(card.intervalDays())
                .repetitionCount(card.repetitionCount())
                .build()), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    HistoryResponse toHistoryResponse(Page<HistoryItemResource> page) {
        return new HistoryResponse(mapContent(page, item -> HistoryResponse.HistoryItemResponse.builder()
                .sessionId(item.sessionId())
                .learningCardId(item.learningCardId())
                .learningItemId(item.learningItemId())
                .contentItemId(item.contentItemId())
                .contentItemType(item.contentItemType())
                .exerciseType(item.exerciseType())
                .variantId(item.variantId())
                .startedAt(item.startedAt())
                .completedAt(item.completedAt())
                .aggregateScore(item.aggregateScore())
                .build()), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private static Answer toAnswer(AnswerRequest request) {
        return switch (request) {
            case TextAnswerRequest text -> new TextAnswer(text.text());
        };
    }

    private static <T, R> List<R> mapContent(Page<T> page, Function<T, R> mapper) {
        return page.getContent().stream().map(mapper).toList();
    }
}
