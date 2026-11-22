package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.advice.ValidationErrorResponse;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import com.blbulyandavbulyan.larm.security.DatabaseUserJwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/exercises")
@Tag(name = "Exercises", description = "Spaced repetition queue, exercise sessions and history")
@ApiResponse(responseCode = "400", description = "Validation failed",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class)))
interface ExercisesApi {

    @GetMapping(value = "/queue", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cards due for review, oldest due first")
    QueueResponse getQueue(DatabaseUserJwtAuthenticationToken auth,
                           @RequestParam(name = "exerciseType", required = false) @Nullable ExerciseType exerciseType,
                           @RequestParam(name = "contentItemType", required = false) @Nullable ContentItemType contentItemType,
                           @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
                           @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size);

    @PostMapping(value = "/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start a session for a card, or resume its open session", responses = {
            @ApiResponse(responseCode = "201", description = "Session created",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "200", description = "Open session resumed",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "409", description = "Learning item is excluded")
    })
    ResponseEntity<SessionResponse> startSession(DatabaseUserJwtAuthenticationToken auth, @Valid @RequestBody StartSessionRequest request);

    @GetMapping(value = "/sessions/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Session state with all attempts, for resuming after a restart", responses = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    SessionStatusResponse getSession(DatabaseUserJwtAuthenticationToken auth, @PathVariable("sessionId") UUID sessionId);

    @PostMapping(value = "/sessions/{sessionId}/attempts", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Evaluate one answer; retries of the same sub-item are allowed", responses = {
            @ApiResponse(responseCode = "200", description = "Evaluated"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "409", description = "Session completed, or sub-item not part of the session")
    })
    AttemptResponse submitAttempt(DatabaseUserJwtAuthenticationToken auth, @PathVariable("sessionId") UUID sessionId,
                                  @Valid @RequestBody AttemptRequest request);

    @PostMapping(value = "/sessions/{sessionId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Complete the session and schedule the next review", responses = {
            @ApiResponse(responseCode = "200", description = "Completed"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "409", description = "Sub-items without attempts, or already completed")
    })
    CompleteSessionResponse completeSession(DatabaseUserJwtAuthenticationToken auth, @PathVariable("sessionId") UUID sessionId);

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Completed sessions, newest first")
    HistoryResponse getHistory(DatabaseUserJwtAuthenticationToken auth,
                               @RequestParam(name = "from", required = false) @Nullable Instant from,
                               @RequestParam(name = "to", required = false) @Nullable Instant to,
                               @RequestParam(name = "exerciseType", required = false) @Nullable ExerciseType exerciseType,
                               @RequestParam(name = "contentItemType", required = false) @Nullable ContentItemType contentItemType,
                               @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
                               @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size);
}
