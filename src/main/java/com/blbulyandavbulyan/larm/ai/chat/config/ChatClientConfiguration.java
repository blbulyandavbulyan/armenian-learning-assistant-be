package com.blbulyandavbulyan.larm.ai.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class ChatClientConfiguration {

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(10)
                .build();
    }

    @Bean
    public ChatClient armenianPhrasesGeneratorChatClient(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory) {
        return chatClientBuilder
                .defaultSystem(new ClassPathResource("prompts/ARMENIAN-PHRASES-GENERATOR.md"))
                // TODO most probably we need here the tool which checks the existing phrases in the database
                //  probably it should check 'exact match' and 'similar' phrases
//                .defaultTools()
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
