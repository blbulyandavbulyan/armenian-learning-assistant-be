package com.blbulyandavbulyan.larm.ai.chat.advisor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingAdvisor implements CallAdvisor {
    private final int order;

    public static LoggingAdvisor forOrder(int order) {
        return new LoggingAdvisor(order);
    }

    @Override
    public String getName() {
        return "Raw Text Logging Advisor";
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        
        log.debug("➡️ Sending prompt to LLM: {}", request);

        ChatClientResponse response = chain.nextCall(request);

        if (response.chatResponse() != null && response.chatResponse().getResult() != null) {
            String rawText = response.chatResponse().getResult().getOutput().getText();
            log.debug("⬅️ Raw LLM Response Text:\n{}", rawText);
        } else {
            log.debug("⬅️ LLM returned an empty or null response.");
        }

        return response;
    }
}