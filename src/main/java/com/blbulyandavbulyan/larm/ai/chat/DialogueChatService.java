package com.blbulyandavbulyan.larm.ai.chat;

import java.util.UUID;

import com.blbulyandavbulyan.larm.ai.chat.advisor.JakartaValidationAdvisor;
import com.blbulyandavbulyan.larm.ai.chat.advisor.LoggingAdvisor;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class DialogueChatService {
    private final ChatClient chatClient;
    private final JsonMapper jsonMapper;
    private final Validator validator;

    /**
     * Processes user message and generates the dialogue on the given topic.
     *
     * @param message chat message, containing the given topic
     * @param chatId id of the chat
     * @return structured dialogue, ready to be saved
     * @throws UnfixableValidationException in case if LLM continued to generate bad response which failed the jakarta validation
     */
    @Valid
    public StructuredDialogueResource dialogueChat(String message, UUID chatId) {
        // TODO most probably we need here the tool which checks the existing phrases in the database
        //  probably it should check 'exact match' and 'similar' phrases
        // .tools()
        try {
            return chatClient.prompt()
                    .system(new ClassPathResource("prompts/ARMENIAN-DIALOGUE-GENERATOR.md"))
                    .user(message)
                    // the idea of injecting several logging advisors is to see what happens in between them
                    // Response path (inner -> outer):
                    //   LoggingAdvisor(-1997) -> StructuredOutputValidationAdvisor(-1998, fixes JSON/schema)
                    //   -> LoggingAdvisor(-1999) -> JakartaValidationAdvisor(-2000, validates bean constraints)
                    //   -> LoggingAdvisor(HIGHEST_PRECEDENCE)
                    .advisors(LoggingAdvisor.forOrder(Ordered.HIGHEST_PRECEDENCE))
                    .advisors(JakartaValidationAdvisor.builder()
                            .outputType(StructuredDialogueResource.class)
                            .order(Ordered.LOWEST_PRECEDENCE - 2000)
                            .maxRepeatAttempts(5)
                            .validator(validator)
                            .jsonMapper(jsonMapper)
                            .build())
                    .advisors(LoggingAdvisor.forOrder(Ordered.LOWEST_PRECEDENCE - 1999))
                    .advisors(StructuredOutputValidationAdvisor.builder()
                            .outputType(StructuredDialogueResource.class)
                            .jsonMapper(jsonMapper)
                            .maxRepeatAttempts(5)
                            .advisorOrder(Ordered.LOWEST_PRECEDENCE - 1998)
                            .build())
                    .advisors(LoggingAdvisor.forOrder(Ordered.LOWEST_PRECEDENCE - 1997))
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId.toString()))
                    .call()
                    .entity(StructuredDialogueResource.class);

        } catch (ConstraintViolationException e) {
            log.error("Unfixable constraint validation error occurred, LLM generated bad response.", e);
            throw new UnfixableValidationException();
        }
    }

}
