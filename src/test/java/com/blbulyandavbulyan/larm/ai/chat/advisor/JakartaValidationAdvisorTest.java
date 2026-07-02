package com.blbulyandavbulyan.larm.ai.chat.advisor;

import java.util.List;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JakartaValidationAdvisorTest {

    // Minimal DTO with a constraint — used as the "outputType" in all tests
    record ValidatedOutput(@NotBlank String value) {}

    private Validator validator;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        jsonMapper = new JsonMapper();
    }

    private JakartaValidationAdvisor buildAdvisor(int maxRetries) {
        return JakartaValidationAdvisor.builder()
                .validator(validator)
                .outputType(ValidatedOutput.class)
                .jsonMapper(jsonMapper)
                .maxRepeatAttempts(maxRetries)
                .order(Ordered.LOWEST_PRECEDENCE - 2000)
                .build();
    }

    private ChatClientResponse responseWithJson(String json) {
        AssistantMessage message = new AssistantMessage(json);
        Generation generation = new Generation(message);
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        ChatClientResponse response = mock(ChatClientResponse.class);
        when(response.chatResponse()).thenReturn(chatResponse);
        return response;
    }

    private ChatClientRequest stubRequestAugmentation(ChatClientRequest request) {
        Prompt mockPrompt = mock(Prompt.class);
        when(request.prompt()).thenReturn(mockPrompt);
        when(mockPrompt.augmentUserMessage(anyString())).thenReturn(mockPrompt);
        ChatClientRequest.Builder requestBuilder = mock(ChatClientRequest.Builder.class);
        when(request.mutate()).thenReturn(requestBuilder);
        when(requestBuilder.prompt(any())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        return request;
    }

    @Test
    void returnsResponse_whenFirstAttemptPassesValidation() {
        // Arrange: inner chain always returns a valid response
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        CallAdvisorChain copiedChain = mock(CallAdvisorChain.class);
        when(chain.copy(any())).thenReturn(copiedChain);
        ChatClientResponse validResponse = responseWithJson("{\"value\":\"hello\"}");
        when(copiedChain.nextCall(any())).thenReturn(validResponse);

        JakartaValidationAdvisor advisor = buildAdvisor(3);

        // Act
        ChatClientResponse result = advisor.adviseCall(mock(ChatClientRequest.class), chain);

        // Assert: chain was called exactly once (no retry needed)
        verify(copiedChain, times(1)).nextCall(any());
        assertThat(result).isNotNull();
    }

    @Test
    void throwsConstraintViolationException_whenAllAttemptsExhausted() {
        // Arrange: inner chain always returns a response that fails validation (blank value)
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        CallAdvisorChain copiedChain = mock(CallAdvisorChain.class);
        ChatClientRequest request = stubRequestAugmentation(mock(ChatClientRequest.class));

        when(chain.copy(any())).thenReturn(copiedChain);
        // Always returns invalid JSON (value is blank → @NotBlank fails)
        ChatClientResponse invalidResponse = responseWithJson("{\"value\":\"\"}");
        when(copiedChain.nextCall(any())).thenReturn(invalidResponse);

        JakartaValidationAdvisor advisor = buildAdvisor(2); // maxRepeatAttempts=2 → 3 total calls

        // Act & Assert
        assertThatThrownBy(() -> advisor.adviseCall(request, chain))
                .isInstanceOf(ConstraintViolationException.class);

        // Verify: called maxRepeatAttempts + 1 = 3 times
        verify(copiedChain, times(3)).nextCall(any());
    }
}
