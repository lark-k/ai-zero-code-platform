package com.lk.aizerocodeplatform.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/7 13:49
 * 配置普通的对话模型（多例）
 */
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Configuration
@Data
public class ChatModelConfig {
    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private boolean logRequests;

    private boolean logResponses;

    @Scope("prototype")
    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}
