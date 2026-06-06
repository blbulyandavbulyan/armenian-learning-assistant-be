package com.blbulyandavbulyan.larm.api.advice;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;

@Builder
public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String path,
        Map<String, String> errors) {
}
