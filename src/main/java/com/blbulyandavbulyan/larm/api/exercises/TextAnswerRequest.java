package com.blbulyandavbulyan.larm.api.exercises;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "Text Answer Request")
public record TextAnswerRequest(
        @Schema(description = Descriptions.ANSWER_TEXT, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Size(max = MAX_TEXT_LENGTH)
        String text) implements AnswerRequest {

    public static final int MAX_TEXT_LENGTH = 1000;
}
