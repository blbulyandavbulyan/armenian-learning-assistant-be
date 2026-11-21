package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.AnswerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Session Status Response")
public record SessionStatusResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        SessionResponse session,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<AttemptedSubItemResponse> attemptedSubItems,

        @Schema(description = Descriptions.PENDING_SUB_ITEM_IDS, requiredMode = Schema.RequiredMode.REQUIRED)
        List<UUID> pendingSubItemIds) {

    @Schema(name = "Attempted Sub Item Response")
    public record AttemptedSubItemResponse(
            @Schema(description = Descriptions.SUB_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID contentSubItemId,

            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            List<AttemptHistoryResponse> attempts) {
    }

    @Schema(name = "Attempt History Response")
    @Builder
    public record AttemptHistoryResponse(
            @Schema(description = Descriptions.ATTEMPT_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID attemptId,

            @Schema(description = Descriptions.ANSWER_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
            AnswerType answerType,

            @Schema(description = Descriptions.ANSWER_TEXT)
            @Nullable
            String answerText,

            @Schema(description = Descriptions.USED_HINT, requiredMode = Schema.RequiredMode.REQUIRED)
            boolean usedHint,

            @Schema(description = Descriptions.QUALITY, requiredMode = Schema.RequiredMode.REQUIRED)
            int quality,

            @Schema(description = Descriptions.CORRECT, requiredMode = Schema.RequiredMode.REQUIRED)
            boolean correct,

            @Schema(description = Descriptions.EVALUATED_AT, requiredMode = Schema.RequiredMode.REQUIRED)
            Instant evaluatedAt) {
    }
}
