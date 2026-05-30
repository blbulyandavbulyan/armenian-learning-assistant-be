package com.blbulyandavbulyan.larm.api.advice;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String path,
        Map<String, String> errors) {
}
