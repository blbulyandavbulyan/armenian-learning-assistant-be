package com.blbulyandavbulyan.larm.api.advice;

import java.time.LocalDateTime;
import java.util.Map;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Validation Error Response")
@Builder
public record ValidationErrorResponse(
        @Schema(description = Descriptions.TIMESTAMP_OF_THE_ERROR, example = Examples.TIMESTAMP_OF_THE_ERROR)
        LocalDateTime timestamp,

        @Schema(description = Descriptions.HTTP_STATUS_CODE, example = Examples.HTTP_STATUS_CODE)
        int status,

        @Schema(description = Descriptions.ERROR_TYPE_SUMMARY, example = Examples.ERROR_TYPE_SUMMARY)
        String error,

        @Schema(description = Descriptions.ERROR_PATH, example = Examples.ERROR_PATH)
        String path,

        @Schema(description = Descriptions.ERROR_MESSAGES_MAP)
        Map<String, String> errors) {
}
