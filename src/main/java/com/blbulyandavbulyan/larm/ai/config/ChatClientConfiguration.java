package com.blbulyandavbulyan.larm.ai.config;

import com.blbulyandavbulyan.larm.ai.tools.SavePhrasesTool;
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
            ChatMemory chatMemory,
            SavePhrasesTool savePhrasesTool) {
        return chatClientBuilder
                .defaultSystem(new ClassPathResource("prompts/ARMENIAN-PHRASES-GENERATOR.md"))
                .defaultTools(savePhrasesTool)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
