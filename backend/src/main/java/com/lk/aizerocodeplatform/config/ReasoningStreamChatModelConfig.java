package com.lk.aizerocodeplatform.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/29 13:28
 * 配置推理流式聊天模型，可以支持更复杂的任务推理
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamChatModelConfig {
    /**
     * 加载配置文件中的api-key
     */
    private String apiKey;
    /**
     * 加载配置文件中的base-url
     */
    private String baseUrl;


    @Bean
    public StreamingChatModel reasoningStreamChatModel() {
        Integer maxTokens = 32768;
        String modelName = "deepseek-reasoner";
        return OpenAiStreamingChatModel
                .builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
