package com.blbulyandavbulyan.larm.ai.chat.advisor;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import tools.jackson.databind.json.JsonMapper;

@Builder
@RequiredArgsConstructor
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
        ChatClientResponse chatClientResponse;
        int repeatCounter = 0;
        boolean isValidationSuccess = false;
        var processedChatClientRequest = chatClientRequest;
        Set<ConstraintViolation<Object>> violations = Collections.emptySet();

        do {
            log.debug("Attempt {} to validate input", repeatCounter + 1);
            repeatCounter++;
            chatClientResponse = callAdvisorChain.copy(this).nextCall(processedChatClientRequest);

            if (chatClientResponse.chatResponse() != null && !chatClientResponse.chatResponse().hasToolCalls()) {
                if (chatClientResponse.chatResponse().getResult() == null
                        || chatClientResponse.chatResponse().getResult().getOutput() == null
                        || chatClientResponse.chatResponse().getResult().getOutput().getText() == null) {
                    log.warn("ChatClientResponse is missing required json output for validation.");
                    continue;
                }

                String json = chatClientResponse.chatResponse().getResult().getOutput().getText();

                Object targetObject = jsonMapper.readValue(json, outputType);

                violations = validator.validate(targetObject);

                if (violations.isEmpty()) {
                    isValidationSuccess = true;
                } else {
                    String violationsMessage = violations.stream()
                            .map(v -> v.getPropertyPath() + " " + v.getMessage())
                            .collect(Collectors.joining(", "));
                    log.debug("Got the following constraint validation errors: {}", violations);

                    processedChatClientRequest = augmentPromptWithErrors(chatClientRequest, violationsMessage);
                }
            }
        } while (!isValidationSuccess && repeatCounter <= this.maxRepeatAttempts);

        if (!violations.isEmpty()) {
            log.debug("All attempts to adjust the output to pass the validation were failed");
            throw new ConstraintViolationException(violations);
        }

        log.debug("The LLM output has been validated successfully");

        return chatClientResponse;
    }

    private ChatClientRequest augmentPromptWithErrors(ChatClientRequest request, String errors) {
        String validationErrorMessage = "%nOutput validation failed because of: %s. Please correct these fields and regenerate.".formatted(errors);
        Prompt augmentedPrompt = request.prompt().augmentUserMessage(userMessage -> userMessage.mutate()
                .text(userMessage.getText() + validationErrorMessage)
                .build());
        return request.mutate().prompt(augmentedPrompt).build();
    }
}
