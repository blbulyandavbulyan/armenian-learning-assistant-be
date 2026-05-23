package com.blbulyandavbulyan.larm.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class ChatClientConfiguration {

    @Bean
    public ChatClient armenianPhrasesGeneratorChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.defaultSystem(new ClassPathResource("prompts/ARMENIAN-PHRASES-GENERATOR.md"))
                .defaultToolNames("")
                .build();
    }
}
