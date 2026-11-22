package com.blbulyandavbulyan.larm.api.exercises;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = TextAnswerRequest.class, name = "TEXT"))
@Schema(name = "Answer Request", description = Descriptions.ANSWER,
        oneOf = TextAnswerRequest.class,
        discriminatorProperty = "type",
        discriminatorMapping = @DiscriminatorMapping(value = "TEXT", schema = TextAnswerRequest.class))
public sealed interface AnswerRequest permits TextAnswerRequest {
}
