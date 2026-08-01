package com.blbulyandavbulyan.larm.ai.chat;

/**
 * Throw in case when LLM couldn't fix the validation error within given attempts.
 */
public class UnfixableValidationException extends RuntimeException {
    public UnfixableValidationException(String message) {
        super(message);
    }
}
