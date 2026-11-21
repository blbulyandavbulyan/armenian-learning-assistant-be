package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.exercise.ExerciseHistoryService;
import com.blbulyandavbulyan.larm.exercise.ExerciseQueueService;
import com.blbulyandavbulyan.larm.exercise.ExerciseSessionService;
import com.blbulyandavbulyan.larm.exercise.SessionStartResult;
import com.blbulyandavbulyan.larm.security.DatabaseUserJwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class ExercisesController implements ExercisesApi {

    private final ExerciseQueueService exerciseQueueService;

    private final ExerciseSessionService exerciseSessionService;

    private final ExerciseHistoryService exerciseHistoryService;

    private final ExerciseResponseMapper mapper;

    @Override
    public QueueResponse getQueue(DatabaseUserJwtAuthenticationToken auth, @Nullable ExerciseType exerciseType,
                                  @Nullable ContentItemType contentItemType, int page, int size) {
        return mapper.toQueueResponse(
                exerciseQueueService.getDueCards(auth.getUserId(), exerciseType, contentItemType, PageRequest.of(page, size)));
    }

    @Override
    public ResponseEntity<SessionResponse> startSession(DatabaseUserJwtAuthenticationToken auth, StartSessionRequest request) {
        SessionStartResult result = exerciseSessionService.startSession(auth.getUserId(), request.learningCardId());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(mapper.toResponse(result.session()));
    }

    @Override
    public SessionStatusResponse getSession(DatabaseUserJwtAuthenticationToken auth, UUID sessionId) {
        return mapper.toResponse(exerciseSessionService.getSession(auth.getUserId(), sessionId));
    }

    @Override
    public AttemptResponse submitAttempt(DatabaseUserJwtAuthenticationToken auth, UUID sessionId, AttemptRequest request) {
        return mapper.toResponse(exerciseSessionService.submitAttempt(auth.getUserId(), sessionId, mapper.toCommand(request)));
    }

    @Override
    public CompleteSessionResponse completeSession(DatabaseUserJwtAuthenticationToken auth, UUID sessionId) {
        return mapper.toResponse(exerciseSessionService.completeSession(auth.getUserId(), sessionId));
    }

    @Override
    public HistoryResponse getHistory(DatabaseUserJwtAuthenticationToken auth, @Nullable Instant from, @Nullable Instant to,
                                      @Nullable ExerciseType exerciseType, @Nullable ContentItemType contentItemType,
                                      int page, int size) {
        return mapper.toHistoryResponse(exerciseHistoryService.getHistory(auth.getUserId(), from, to, exerciseType,
                contentItemType, PageRequest.of(page, size)));
    }
}
