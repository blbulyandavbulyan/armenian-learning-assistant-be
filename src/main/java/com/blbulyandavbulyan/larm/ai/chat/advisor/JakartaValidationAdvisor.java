package com.blbulyandavbulyan.larm.ai.chat.advisor;

import java.util.Set;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.ai.chat.UnfixableValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Builder
@Slf4j
public class JakartaValidationAdvisor implements CallAdvisor {

    @NonNull
    private final Validator validator;

    @NonNull
    private final Class<?> outputType;

    @NonNull
    private final JsonMapper jsonMapper;

    private final int maxRepeatAttempts;
    private final int order;

    @Override
    public String getName() {
        return "Jakarta Validation Advisor";
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        var currentRequest = chatClientRequest;
        ValidationResult.Failure lastFailure = null;

        for (int attempt = 0; attempt <= maxRepeatAttempts; attempt++) {
            log.debug("Attempt {} to validate input", attempt + 1);
            ChatClientResponse response = callAdvisorChain.copy(this).nextCall(currentRequest);
            if (response.chatResponse() == null) {
                throw new UnfixableValidationException(
                        "ChatClientResponse contained a null ChatResponse. Upstream provider or advisor failed to return a payload.");
            }

            if (response.chatResponse().hasToolCalls()) {
                log.debug("Response contains tool calls, skipping validation");
                return response;
            }

            ValidationResult lastResult = inspectAndValidate(response.chatResponse());
            switch (lastResult) {
                case ValidationResult.Success _ -> {
                    log.debug("The LLM output has been validated successfully");
                    return response;
                }
                case ValidationResult.Failure failure -> {
                    log.debug("Validation attempt {} failed: {}", attempt, failure.errorMessage());
                    currentRequest = augmentPromptWithErrors(chatClientRequest, failure.errorMessage());
                    lastFailure = failure;
                }
            }
        }
        String reason = (lastFailure != null)
                ? lastFailure.errorMessage()
                : "Validation failed: no attempts were executed or response was invalid.";
        throw new UnfixableValidationException(reason);
    }

    private ValidationResult inspectAndValidate(ChatResponse chatResponse) {
        String rawText = extractRawText(chatResponse);
        if (rawText == null) {
            return new ValidationResult.Failure("Output was null. You must return a valid JSON object matching the requested schema");
        }

        log.debug("Got the following output from model: {}", rawText);

        Object targetObject;
        try {
            targetObject = jsonMapper.readValue(rawText, outputType);
        } catch (JacksonException e) {
            log.warn("Model returned malformed JSON: {}", e.getMessage());
            return new ValidationResult.Failure(
                    "Invalid JSON syntax (%s). You must return ONLY raw, valid JSON matching the requested schema without commentary."
                            .formatted(e.getMessage())
            );
        }

        if (targetObject == null) {
            return new ValidationResult.Failure("Output evaluated to null. You must return a valid JSON object matching the requested schema");
        }

        Set<ConstraintViolation<Object>> violations = validator.validate(targetObject);
        if (!violations.isEmpty()) {
            String message = violationsToMessage(violations);
            log.debug("Got constraint validation errors: {}", message);
            return new ValidationResult.Failure(message);
        }

        return new ValidationResult.Success();
    }

    @Nullable
    private static String extractRawText(ChatResponse chatResponse) {
        var result = chatResponse.getResult();
        if (result == null) {
            return null;
        }
        return result.getOutput().getText();
    }

    private static String violationsToMessage(Set<ConstraintViolation<Object>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));
    }

    private ChatClientRequest augmentPromptWithErrors(ChatClientRequest request, String errors) {
        String validationErrorMessage = "%nOutput validation failed because of: %s. Please correct these fields and regenerate.".formatted(errors);
        Prompt augmentedPrompt = request.prompt().augmentUserMessage(userMessage -> userMessage.mutate()
                .text(userMessage.getText() + validationErrorMessage)
                .build());
        return request.mutate().prompt(augmentedPrompt).build();
    }

    private sealed interface ValidationResult {

        record Success() implements ValidationResult {
        }

        record Failure(String errorMessage) implements ValidationResult {
        }
    }
}
